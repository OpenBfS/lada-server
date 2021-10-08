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

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;

import de.intevation.lada.importer.ImportConfig;
import de.intevation.lada.importer.ImportFormat;
import de.intevation.lada.importer.Importer;
import de.intevation.lada.importer.laf.LafImporter;
import de.intevation.lada.model.stammdaten.ImporterConfig;
import de.intevation.lada.model.stammdaten.Tag;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.auth.UserInfo;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.TagUtil;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.rest.LadaService;

/**
 * This class produces a RESTful service to interact with probe objects.
 *
 * @author <a href = "mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("data/import")
public class LafImportService extends LadaService {

    /**
     * The importer implementation.
     */
    @Inject
    @ImportConfig(format = ImportFormat.LAF)
    private Importer importer;

    @Inject
    private Repository repository;

    /**
     * The authorization module.
     */
    @Inject
    @AuthorizationConfig(type = AuthorizationType.HEADER)
    private Authorization authorization;

    @Inject
    private TagUtil tagUtil;

    /**
     * Represents the expected JSON input for file upload.
     */
    private static class UploadJson {
        private String encoding;
        private Map<String, String> files;

        String getEncoding() {
            return this.encoding;
        }

        void setEncoding(String encoding) {
            this.encoding = encoding;
        }

        Map<String, String> getFiles() {
            return this.files;
        }

        void setFiles(Map<String, String> files) {
            this.files = files;
        }
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
    public Response multiUpload(
        UploadJson jsonInput,
        @Context HttpServletRequest request
    ) {
        UserInfo userInfo = authorization.getInfo(request);

        Charset charset;
        try {
            charset = Charset.forName(jsonInput.getEncoding());
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

        if (jsonInput.getFiles() == null) {
            return new Response(
                false,
                StatusCodes.IMP_INVALID_VALUE,
                "No import files given");
        }
        try {
            for (Map.Entry<String, String> e: jsonInput.getFiles().entrySet()) {
                ByteBuffer decodedBytes = ByteBuffer.wrap(
                    Base64.getDecoder().decode(e.getValue()));
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
                    repository.queryBuilder(ImporterConfig.class);
                builder.and("mstId", mstId);
                config =
                    (List<ImporterConfig>) repository.filterPlain(
                        builder.getQuery());
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
            if(importer.getErrors().values().stream().anyMatch( elem -> elem.stream().anyMatch(
                            ele -> (ele.getKey().equals("validation#probe") || ele.getKey().equals("Parser")) == true))){
                fileResponseData.put("success", false);
            } else {
            fileResponseData.put("success", true);
            }
            fileResponseData.put(
                "probeIds", ((LafImporter) importer).getImportedIds());
            importResponseData.put(fileName, fileResponseData);
            importedProbeids.addAll(((LafImporter) importer).getImportedIds());
        });

        boolean success = true;
        // If import created at least a new record
        if (importedProbeids.size() > 0) {
            success = true;
            //Generate a tag for the imported probe records
            Response tagCreation = tagUtil.generateTag("IMP", mstId);
            if (!tagCreation.getSuccess()) {
                // TODO Tag creation failed -> import success?
                success = false;
                return new Response(
                    success,
                    StatusCodes.OK,
                    importResponseData);
            }
            Tag newTag = (Tag) tagCreation.getData();
            tagUtil.setTagsByProbeIds(importedProbeids, newTag.getId());

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
                repository.queryBuilder(ImporterConfig.class);
            builder.and("mstId", mstId);
            config = (List<ImporterConfig>) repository.filterPlain(
                builder.getQuery());
        }
        importer.doImport(content, userInfo, config);
        Map<String, Object> respData = new HashMap<String, Object>();
        Boolean success = true;
        if (!importer.getErrors().isEmpty()) {
            respData.put("errors", importer.getErrors());
            if (importer.getErrors().values().stream().anyMatch( elem -> elem.stream().anyMatch(ele -> ele.getKey().equals("validation#probe") == true))){
                success = false;
            }
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
        if (importedProbeids.size() > 0 && !mstId.equals("null") && success == true) {
            //Generate a tag for the imported probe records
            Response tagCreation = tagUtil.generateTag("IMP", mstId);
            if (!tagCreation.getSuccess()) {
                // TODO Tag creation failed -> import success?
                success = false;
                return new Response(success, StatusCodes.OK, respData);
            }
            Tag newTag = (Tag) tagCreation.getData();
            tagUtil.setTagsByProbeIds(importedProbeids, newTag.getId());

            respData.put("tag", newTag.getTag());
        }

        return new Response(success, StatusCodes.OK, respData);
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
