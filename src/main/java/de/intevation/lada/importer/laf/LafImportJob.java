/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */

package de.intevation.lada.importer.laf;

import static de.intevation.lada.rest.importer.LafImportService.logLAFFile;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;

import de.intevation.lada.importer.ImportConfig;
import de.intevation.lada.importer.ImportFormat;
import de.intevation.lada.importer.Importer;
import de.intevation.lada.model.stammdaten.ImporterConfig;
import de.intevation.lada.model.stammdaten.MessStelle;
import de.intevation.lada.model.stammdaten.Tag;
import de.intevation.lada.util.data.Job;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.data.TagUtil;
import de.intevation.lada.util.rest.Response;

/**
 * Asynchronous import job.
 *
 * @author Alexander Woestmann <awoestmann@intevation.de>
 */
public class LafImportJob extends Job {

    @Inject
    @ImportConfig(format = ImportFormat.LAF)
    private Importer importer;

    private Map<String, Map<String, Object>> importData;

    private JsonObject jsonInput;

    private MessStelle mst;

    private JsonObject result;

    @Inject
    private TagUtil tagUtil;

    public void cleanup() {
        //Intentionally left blank
    }

    /**
     * Create a result json using a status code and message.
     * @param success True if import was successful
     * @param status Status code
     * @param data Message String
     * @return Result as JsonObject
     */
    private JsonObject createResult(boolean success, int status, String data) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("success", success)
        .add("status", status)
        .add("data", data);
        return builder.build();
    }

    public JsonObject getResult() {
        return result;
    }

    public Map<String, Map<String, Object>> getImportData() {
        return importData;
    }

    /**
     * Run the import job.
     */
    @Override
    public void runWithTx() {
        logger.debug("Starting LAF import");

        //Get file content strings from input object
        JsonObject filesObject = jsonInput.getJsonObject("files");

        // TODO: handle this upfront in the service
        Charset charset;
        try {
            charset = Charset.forName(jsonInput.getString("encoding"));
        } catch (IllegalArgumentException e) {
            result = createResult(
                false,
                StatusCodes.IMP_INVALID_VALUE,
                "No valid encoding name given");
            return;
        }

        //Contains: fileName: fileContent as String
        Map<String, String> files = new HashMap<String, String>();
        //Ids of alle imported probe records
        List<Integer> importedProbeids = new ArrayList<Integer>();
        //Contains a response data object for every import
        Map<String, Map<String, Object>> importResponseData =
            new HashMap<String, Map<String, Object>>();

        // TODO: Handle this upfront in the service
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
            String msg = "File content not in valid Base64 scheme";
            result = createResult(
                false,
                StatusCodes.IMP_INVALID_VALUE,
                msg);
            return;
        } catch (CharacterCodingException cce) {
            String msg = "File content not in valid " + charset.name();
            result = createResult(
                false,
                StatusCodes.IMP_INVALID_VALUE,
                msg);
            return;
        }

        if (files.size() == 0) {
            throw new IllegalArgumentException("No valid file given");
        }

        logger.debug(
            String.format("Starting import of %d files", files.size()));

        //Import each file
        String mstId = this.mst.getId();
        files.forEach((fileName, content) -> {
            logLAFFile(mstId, content, charset);
            List<ImporterConfig> config = new ArrayList<ImporterConfig>();
            if (!"".equals(mstId)) {
                QueryBuilder<ImporterConfig> builder =
                    repository.queryBuilder(ImporterConfig.class);
                builder.and("measFacilId", mstId);
                config =
                    (List<ImporterConfig>) repository.filterPlain(
                        builder.getQuery());
            }
            importer.doImport(content, userInfo, config);
            Map<String, Object> fileResponseData =
                new HashMap<String, Object>();
            if (!importer.getErrors().isEmpty()) {
                fileResponseData.put("errors", importer.getErrors());
                this.currentStatus.setErrors(true);
            }
            if (!importer.getWarnings().isEmpty()) {
                fileResponseData.put("warnings", importer.getWarnings());
                this.currentStatus.setWarnings(true);
            }
            if (!importer.getNotifications().isEmpty()) {
                fileResponseData.put(
                    "notifications", importer.getNotifications());
                this.currentStatus.setNotifications(true);
            }
            fileResponseData.put("success", true);
            fileResponseData.put(
                "probeIds", ((LafImporter) importer).getImportedIds());
            importResponseData.put(fileName, fileResponseData);
            importedProbeids.addAll(((LafImporter) importer).getImportedIds());
            logger.debug(
                String.format("Finished import of file \"%s\"", fileName));
        });

        // If import created at least a new record
        if (importedProbeids.size() > 0) {
            //Generate a tag for the imported probe records
            Response tagCreation =
                tagUtil.generateTag("IMP", mst.getNetzbetreiberId());
            if (!tagCreation.getSuccess()) {
                // TODO Tag creation failed -> import success?
                importData = importResponseData;
                return;
            }
            Tag newTag = (Tag) tagCreation.getData();
            tagUtil.setTagsByProbeIds(
                importedProbeids, newTag.getId());

            //Put new tag in import response
            importResponseData.forEach((file, responseData) -> {
                responseData.put("tag", newTag.getTag());
            });
        }
        importData = importResponseData;
        logger.debug("Finished LAF import");
    }

    public void setJsonInput(JsonObject jsonInput) {
         this.jsonInput = jsonInput;
    }

    public void setMst(MessStelle mst) {
        this.mst = mst;
    }
}
