/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.data;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

import org.jboss.logging.Logger;

import de.intevation.lada.importer.laf.LafImporter;
import de.intevation.lada.model.master.ImportConf;
import de.intevation.lada.model.master.ImportConf_;
import de.intevation.lada.model.master.MeasFacil;
import de.intevation.lada.model.master.Tag;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.auth.UserInfo;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.TagUtil;
import de.intevation.lada.rest.LadaService;

/**
 * This class produces a RESTful service to interact with probe objects.
 *
 * @author <a href = "mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_DATA + "import")
public class LafImportService extends LadaService {

    /**
     * The importer implementation.
     */
    @Inject
    private LafImporter importer;

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
     * Import a LAF formatted file.
     *
     * @return Response object.
     * @deprecated This endpoint will be removed. Use multiUpload() instead.
     */
    @Deprecated(since = "2021-02-10", forRemoval = true)
    @POST
    @Path("laf")
    @Consumes(MediaType.TEXT_PLAIN)
    public Map<String, Object> upload(
        String content,
        @Context HttpServletRequest request
    ) {
        UserInfo userInfo = authorization.getInfo();
        String mstId = request.getHeader("X-LADA-MST");
        MeasFacil mst = repository.getById(MeasFacil.class, mstId);

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
        List<ImportConf> config = new ArrayList<ImportConf>();
        if (!"".equals(mstId)) {
            QueryBuilder<ImportConf> builder =
                repository.queryBuilder(ImportConf.class);
            builder.and(ImportConf_.measFacilId, mstId);
            config = repository.filter(builder.getQuery());
        }
        importer.doImport(content, userInfo, mstId, config);
        Map<String, Object> respData = new HashMap<String, Object>();
        Boolean success = true;
        if (!importer.getErrors().isEmpty()) {
            respData.put("errors", importer.getErrors());
            if (importer.getErrors().values().stream().anyMatch(
                    elem -> elem.stream().anyMatch(
                        ele -> ele.getKey().equals("validation#probe")))) {
                success = false;
            }
        }
        if (!importer.getWarnings().isEmpty()) {
            respData.put("warnings", importer.getWarnings());
        }
        if (!importer.getNotifications().isEmpty()) {
          respData.put("notifications", importer.getNotifications());
        }
        List<Integer> importedProbeids = importer.getImportedIds();
        respData.put("probeIds", importedProbeids);

        // If import created at least a new record
        if (importedProbeids.size() > 0 && !mstId.equals("null") && success) {
            //Generate a tag for the imported probe records
            Tag newTag = tagUtil.generateTag("IMP", mst.getNetworkId());
            tagUtil.setTagsByProbeIds(importedProbeids, newTag.getId());

            respData.put("tag", newTag.getName());
        }

        return respData;
    }

    /**
     * Log the imported file for debugging purposes.
     *
     * @param mstId Id from Header
     * @param content The laf file content
     * @param enc Charset used for writing LAF file
     */
    public static void logLAFFile(String mstId, String content, Charset enc) {
        // Get logger for import logger
        Logger lafLogger = Logger.getLogger("import");

        // Write laf file if debug enabled
        if (lafLogger.isDebugEnabled()) {
            DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssS");
            Date now = new Date();
            // Create filename for imported laf
            String fileName = df.format(now) + "-" + mstId + ".laf";

            // Retrive path set for import logger
            String logDir = System.getProperty("jboss.server.log.dir");
            // Set default log path as fallback
            String filePath = logDir != null ? logDir : "/var/log/wildfly/";

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
