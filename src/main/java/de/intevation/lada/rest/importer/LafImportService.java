/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest.importer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;

import de.intevation.lada.importer.ImportConfig;
import de.intevation.lada.importer.ImportFormat;
import de.intevation.lada.importer.ImportJobManager;
import de.intevation.lada.importer.Importer;
import de.intevation.lada.importer.laf.LafImporter;
import de.intevation.lada.model.stammdaten.ImporterConfig;
import de.intevation.lada.model.stammdaten.Tag;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.annotation.RepositoryConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.auth.UserInfo;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.RepositoryType;
import de.intevation.lada.util.data.Strings;
import de.intevation.lada.util.data.TagUtil;
import de.intevation.lada.util.data.JobManager.JobNotFoundException;
import de.intevation.lada.util.data.JobManager.JobStatus;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.util.data.StatusCodes;

/**
 * This class produces a RESTful service to interact with probe objects.
 *
 * @author <a href = "mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("data/import")
@RequestScoped
public class LafImportService {

    /**
     * The importer
     */
    @Inject
    @ImportConfig(format = ImportFormat.LAF)
    private Importer importer;

    @Inject
    @RepositoryConfig(type = RepositoryType.RW)
    private Repository repository;

    /**
     * The authorization module.
     */
    @Inject
    @AuthorizationConfig(type = AuthorizationType.HEADER)
    private Authorization authorization;

    @Inject
    private Logger logger;

    @Inject
    ImportJobManager importJobManager;

    @POST
    @Path("/async/laf")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public javax.ws.rs.core.Response createAsyncImport(
        JsonObject jsonInput,
        @Context HttpServletRequest request
    ) {
        String mstId = request.getHeader("X-LADA-MST");
        if (mstId == null) {
            JsonObjectBuilder builder = Json.createObjectBuilder();
            builder.add("success", false)
            .add("status", StatusCodes.NOT_ALLOWED)
            .add("data", "Missing header for messtelle.");
            return javax.ws.rs.core.Response.ok(builder.build().toString()).build();
        }
        UserInfo userInfo = authorization.getInfo(request);
        String newJobId = importJobManager.createImportJob(userInfo, jsonInput, mstId);
        JsonObject responseJson = Json.createObjectBuilder()
            .add("refId", newJobId)
            .build();
        return javax.ws.rs.core.Response.ok(responseJson.toString()).build();
    }

    /**
     * Get the status of an export job.
     *
     * Output format:
     *
     * <pre>
     * {
     *    done: boolean
     *    status: 'waiting' | 'running' | 'finished' | 'error'
     *    message: string (optional)
     *  }
     * </pre>
     *
     * @param id Job id to check
     * @return Json object containing the status information, status
     *         403 if the requesting user has not created the request
     *         or status 404 if job was not found
     */
    @GET
    @Path("/async/status/{id}")
    @Produces("application/json")
    public javax.ws.rs.core.Response getStatus(
        @PathParam("id") String id,
        @Context HttpServletRequest request) {

        JobStatus status;
        UserInfo originalCreator;
        UserInfo requestingUser = authorization.getInfo(request);

        try {
            originalCreator = importJobManager.getJobUserInfo(id);
            if (!originalCreator.getUserId().equals(
                    requestingUser.getUserId())
            ) {
                logger.warn(String.format(
                    "Rejected status request by user "
                    + "#%s for job %s created by user #%s",
                    requestingUser.getUserId(),
                    id,
                    originalCreator.getUserId()));
                return javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.FORBIDDEN).build();
            }

            status = importJobManager.getJobStatus(id);
        } catch (JobNotFoundException jnfe) {
            logger.info(String.format("Could not find status for job %s", id));
            return javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND).build();
        }
        JsonObject responseJson = Json.createObjectBuilder()
            .add("status", status.getStatus())
            .add("message", status.getMessage())
            .add("done", status.isDone())
            .build();

        return javax.ws.rs.core.Response.ok(responseJson.toString()).build();
    }

    /**
     * Import a given list of files, generate a tag and set it to all
     * imported records.
     * Expected input format:
     * <pre>
     * <code>
     * {
     *   "encoding": "UTF-8",
     *   "files": {
     *     "firstFileName.laf": "base64 encoded content",
     *     "secondFilename.laf": "base64 encoded content",
     *     //...
     *   }
     * }
     * </code>
     * </pre>
     */
    @POST
    @Path("/laf/list")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response multiUpload(
        JsonObject jsonInput,
        @Context HttpServletRequest request
    ) {
        UserInfo userInfo = authorization.getInfo(request);
        //Get file content strings from input object
        JsonObject filesObject = jsonInput.getJsonObject("files");

        Charset charset;
        try {
            charset = Charset.forName(jsonInput.getString("encoding"));
        } catch (IllegalArgumentException e) {
            return new Response(
                false,
                StatusCodes.IMP_INVALID_VALUE,
                "No valid encoding name given");
        }

        //Contains: fileName: fileContent as String
        Map<String, String> files = new HashMap<String, String>();
        //Ids of alle imported probe records
        List<Integer> importedProbeids = new ArrayList<Integer>();
        //Contains a response data object for every import
        Map<String, Map<String, Object>> importResponseData =
            new HashMap<String, Map<String, Object>>();

        String mstId = request.getHeader("X-LADA-MST");
        if (mstId == null) {
            return new Response(
                false,
                StatusCodes.NOT_ALLOWED,
                "Missing header for messtelle.");
        }

        try {
            for (Map.Entry<String, JsonValue> e : filesObject.entrySet()) {
                String base64String = ((JsonString) e.getValue()).getString();
                ByteBuffer decodedBytes = ByteBuffer.wrap(
                    Base64.getDecoder().decode(base64String));
                String decodedContent = new String(
                    new StringBuffer(charset.newDecoder()
                        .decode(decodedBytes)));
                files.put(e.getKey(), decodedContent);
            }
        } catch (IllegalArgumentException iae) {
            return new Response(
                false,
                StatusCodes.IMP_INVALID_VALUE,
                "File content not in valid Base64 scheme");
        } catch (CharacterCodingException cce) {
            return new Response(
                false,
                StatusCodes.IMP_INVALID_VALUE,
                "File content not in valid " + charset.name());
        }

        //Import each file
        files.forEach((fileName, content) -> {
            logLAFFile(mstId, content, charset);
            List<ImporterConfig> config = new ArrayList<ImporterConfig>();
            if (!"".equals(mstId)) {
                QueryBuilder<ImporterConfig> builder =
                    new QueryBuilder<ImporterConfig>(
                        repository.entityManager(Strings.STAMM),
                        ImporterConfig.class);
                builder.and("mstId", mstId);
                config =
                    (List<ImporterConfig>) repository.filterPlain(
                        builder.getQuery(), Strings.STAMM);
            }
            importer.doImport(content, userInfo, config);
            Map<String, Object> fileResponseData =
                new HashMap<String, Object>();
            if (!importer.getErrors().isEmpty()) {
                fileResponseData.put("errors", importer.getErrors());
            }
            if (!importer.getWarnings().isEmpty()) {
                fileResponseData.put("warnings", importer.getWarnings());
            }
            if (!importer.getNotifications().isEmpty()) {
                fileResponseData.put(
                    "notifications", importer.getNotifications());
            }
            fileResponseData.put("success", true);
            fileResponseData.put(
                "probeIds", ((LafImporter) importer).getImportedIds());
            importResponseData.put(fileName, fileResponseData);
            importedProbeids.addAll(((LafImporter) importer).getImportedIds());
        });

        boolean success = false;
        // If import created at least a new record
        if (importedProbeids.size() > 0) {
            success = true;
            //Generate a tag for the imported probe records
            Response tagCreation =
                TagUtil.generateTag("IMP", mstId, repository);
            if (!tagCreation.getSuccess()) {
                // TODO Tag creation failed -> import success?
                return new Response(
                    success,
                    StatusCodes.OK,
                    importResponseData);
            }
            Tag newTag = (Tag) tagCreation.getData();
            TagUtil.setTagsByProbeIds(
                importedProbeids, newTag.getId(), repository);

            //Put new tag in import response
            importResponseData.forEach((file, responseData) -> {
                responseData.put("tag", newTag.getTag());
            });
        }
        return new Response(success, StatusCodes.OK, importResponseData);
    }

    /**
     * Import a LAF formatted file.
     *
     * @param input     String containing file content.
     * @param header    The HTTP header containing authorization information.
     * @return Response object.
     * @deprecated This endpoint will be removed. Use multiUpload() instead.
     */
    @Deprecated(since = "2021-02-10", forRemoval = true)
    @POST
    @Path("/laf")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.TEXT_PLAIN)
    public Response upload(
        String content,
        @Context HttpServletRequest request
    ) {
        UserInfo userInfo = authorization.getInfo(request);
        String mstId = request.getHeader("X-LADA-MST");

        /** Preparation for Client-Update: "Vorbelegung Messstelle" will
         * become mandatory!
         *if (mstId.equals("null")) {
         *    return new Response(false, 699, "Missing header for messtelle.");
         *}
         */

        String encoding = request.getCharacterEncoding();
        if (encoding == null) {
            encoding = "iso-8859-15";
        }
        logLAFFile(mstId, content,
            // Validation of encoding name is already done by the framework
            Charset.forName(encoding));
        List<ImporterConfig> config = new ArrayList<ImporterConfig>();
        if (!"".equals(mstId)) {
            QueryBuilder<ImporterConfig> builder =
                new QueryBuilder<ImporterConfig>(
                    repository.entityManager(Strings.STAMM),
                    ImporterConfig.class);
            builder.and("mstId", mstId);
            config = (List<ImporterConfig>) repository.filterPlain(
                builder.getQuery(), Strings.STAMM);
        }
        importer.doImport(content, userInfo, config);
        Map<String, Object> respData = new HashMap<String, Object>();
        if (!importer.getErrors().isEmpty()) {
            respData.put("errors", importer.getErrors());
        }
        if (!importer.getWarnings().isEmpty()) {
            respData.put("warnings", importer.getWarnings());
        }
        if (!importer.getNotifications().isEmpty()) {
          respData.put("notifications", importer.getNotifications());
        }
        List<Integer> importedProbeids =
            ((LafImporter) importer).getImportedIds();
        respData.put("probeIds", importedProbeids);

        // If import created at least a new record
        if (importedProbeids.size() > 0 && !mstId.equals("null")) {
            //Generate a tag for the imported probe records
            Response tagCreation =
                TagUtil.generateTag("IMP", mstId, repository);
            if (!tagCreation.getSuccess()) {
                // TODO Tag creation failed -> import success?
                return new Response(true, StatusCodes.OK, respData);
            }
            Tag newTag = (Tag) tagCreation.getData();
            TagUtil.setTagsByProbeIds(
                importedProbeids, newTag.getId(), repository);

            respData.put("tag", newTag.getTag());
        }

        return new Response(true, StatusCodes.OK, respData);
    }

    /**
     * Log the imported file for debugging purposes.
     *
     * @param mstId Id from Header
     * @param content The laf file content
     */
    private void logLAFFile(String mstId, String content, Charset enc) {
        DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssS");
        Date now = new Date();
        // Create filename for imported laf
        String fileName = df.format(now) + "-" + mstId + ".laf";
        // Set default log path as fallback
        String filePath = "/var/log/wildfly/";
        // Get logger and appender for import logger
        Logger lafLogger = Logger.getLogger("import");
        Appender lafAppender = Logger.getRootLogger().getAppender("laf");
        // Retrive path set for import logger
        if (lafAppender instanceof FileAppender) {
            File appenderFile =
                new File(((FileAppender) lafAppender).getFile());
            filePath = appenderFile.getParent();
        }
        // Write laf file if debug enabled
        if (lafLogger.isDebugEnabled()) {
            lafLogger.debug("X-LADA-MST: " + mstId);
            lafLogger.debug(
                "Imported file logged to: " + filePath + "/" + fileName);
            try (FileWriter f = new FileWriter(
                    filePath + "/" + fileName, enc)) {
                f.write(content);
            } catch (IOException e) {
                lafLogger.debug("Could not write import file to " + filePath);
            }
        }
    }
}
