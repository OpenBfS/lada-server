/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */

package de.intevation.lada.importer.laf;

import static de.intevation.lada.data.LafImportService.logLAFFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import de.intevation.lada.data.requests.LafImportParameters;
import de.intevation.lada.model.master.ImportConf;
import de.intevation.lada.model.master.ImportConf_;
import de.intevation.lada.model.master.MeasFacil;
import de.intevation.lada.model.master.Tag;
import de.intevation.lada.util.data.Job;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.TagUtil;


/**
 * Asynchronous import job.
 *
 * @author Alexander Woestmann <awoestmann@intevation.de>
 */
public class LafImportJob extends Job {

    @Inject
    private LafImporter importer;

    private Map<String, Map<String, Object>> importData = new HashMap<>();

    private LafImportParameters importParams;

    private MeasFacil mst;

    private Map<String, String> files;

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

        //Ids of alle imported probe records
        List<Integer> importedProbeids = new ArrayList<Integer>();

        //Import each file
        String mstId = this.mst.getId();
        this.files.forEach((fileName, content) -> {
            logLAFFile(mstId, content, importParams.getEncoding());
            List<ImportConf> config = new ArrayList<ImportConf>();
            if (!"".equals(mstId)) {
                QueryBuilder<ImportConf> builder =
                    repository.queryBuilder(ImportConf.class);
                builder.and(ImportConf_.measFacilId, mstId);
                config = repository.filter(builder.getQuery());
            }
            importer.doImport(content, userInfo, mstId, config);

            Map<String, Object> fileResponseData = new HashMap<>();
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
            fileResponseData.put("success", !currentStatus.getErrors());
            fileResponseData.put("probeIds", importer.getImportedIds());
            importData.put(fileName, fileResponseData);
            importedProbeids.addAll(importer.getImportedIds());
            logger.debug(
                String.format("Finished import of file \"%s\"", fileName));
        });

        // If import created at least a new record
        if (importedProbeids.size() > 0) {
            //Generate a tag for the imported probe records
            Tag newTag = tagUtil.generateTag("IMP", mst.getNetworkId());
            tagUtil.setTagsByProbeIds(
                importedProbeids, newTag.getId());

            //Put new tag in import response
            importData.forEach((file, responseData) -> {
                responseData.put("tag", newTag.getName());
            });
        }
        logger.debug("Finished LAF import");
    }

    public void setImportParameters(LafImportParameters importParameters) {
         this.importParams = importParameters;
    }

    public void setMst(MeasFacil mst) {
        this.mst = mst;
    }

    public void setFiles(Map<String, String> files) {
        this.files = files;
    }
}
