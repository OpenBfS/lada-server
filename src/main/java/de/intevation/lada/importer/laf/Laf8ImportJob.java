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
import java.util.List;

import jakarta.inject.Inject;
import de.intevation.lada.data.requests.Laf8ImportParameters;
import de.intevation.lada.importer.Report;
import de.intevation.lada.model.master.ImportConf;
import de.intevation.lada.model.master.ImportConf_;
import de.intevation.lada.util.data.QueryBuilder;


/**
 * Asynchronous import job.
 *
 * @author Alexander Woestmann <awoestmann@intevation.de>
 */
public class Laf8ImportJob extends ImportJob<String> {

    @Inject
    private LafImporter importer;

    private Laf8ImportParameters importParams;

    /**
     * Run the import job.
     */
    @Override
    public void runWithTx() {
        logger.debug("Starting LAF import");

        // IDs of all imported probe records
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

            Report fileResponseData = new Report();
            if (!importer.getErrors().isEmpty()) {
                fileResponseData.setErrors(importer.getErrors());
                this.currentStatus.setErrors(true);
            }
            if (!importer.getWarnings().isEmpty()) {
                fileResponseData.setWarnings(importer.getWarnings());
                this.currentStatus.setWarnings(true);
            }
            if (!importer.getNotifications().isEmpty()) {
                fileResponseData.setNotifications(importer.getNotifications());
                this.currentStatus.setNotifications(true);
            }
            fileResponseData.setSuccess(!currentStatus.getErrors());
            fileResponseData.setSampleIds(importer.getImportedIds());
            importData.put(fileName, fileResponseData);
            importedProbeids.addAll(importer.getImportedIds());
            logger.debug(
                String.format("Finished import of file \"%s\"", fileName));
        });

        tagImportedData(importedProbeids, mst);

        logger.debug("Finished LAF import");
    }

    public void setImportParameters(Laf8ImportParameters importParameters) {
         this.importParams = importParameters;
    }
}
