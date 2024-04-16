/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */

package de.intevation.lada.exporter.laf;

import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.json.JsonNumber;
import jakarta.json.JsonValue;

import de.intevation.lada.exporter.ExportConfig;
import de.intevation.lada.exporter.Exporter;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.exporter.ExportFormat;
import de.intevation.lada.exporter.ExportJob;
import de.intevation.lada.util.data.QueryBuilder;

/**
 * Job class for exporting records to a laf file.
 *
 * @author <a href="mailto:awoestmann@intevation.de">Alexander Woestmann</a>
 */
public class LafExportJob extends ExportJob {

    /**
     * The laf exporter.
     */
    @Inject
    @ExportConfig(format = ExportFormat.LAF)
    private Exporter exporter;

    public LafExportJob() {
        this.format = "laf";
        this.downloadFileName = "export.laf";
    }

    /**
     * Start the export.
     */
    @Override
    public void runWithTx() {
        logger.debug("Starting LAF export");

        //Load records
        List<Integer> probeIds = new ArrayList<Integer>();
        List<Integer> messungIds = new ArrayList<Integer>();
        if (exportParameters.getJsonArray("proben") != null) {
            for (JsonValue id : exportParameters.getJsonArray("proben")) {
                if (id instanceof JsonNumber) {
                    probeIds.add(((JsonNumber) id).intValue());
                }
            }
        }
        if (exportParameters.getJsonArray("messungen") != null) {
            for (JsonValue id : exportParameters.getJsonArray("messungen")) {
                if (id instanceof JsonNumber) {
                    messungIds.add(((JsonNumber) id).intValue());
                }
            }
        }
        if (probeIds.isEmpty() && messungIds.isEmpty()) {
            throw new IllegalArgumentException("No data to export");
        }

        //Get probe and messung records
        List<Integer> pIds = new ArrayList<Integer>();
        if (!probeIds.isEmpty()) {
            QueryBuilder<Sample> pBuilder = repository.queryBuilder(
                Sample.class);
            pBuilder.andIn("id", probeIds);
            List<Sample> pObjects = repository.filter(
                pBuilder.getQuery());
            for (Sample p : pObjects) {
                pIds.add(p.getId());
            }
        }

        List<Integer> mIds = new ArrayList<Integer>();
        if (!messungIds.isEmpty()) {
            QueryBuilder<Measm> mBuilder = repository.queryBuilder(
                Measm.class);
            mBuilder.andIn("id", messungIds);
            List<Measm> mObjects = repository.filter(
                mBuilder.getQuery());
            for (Measm m : mObjects) {
                mIds.add(m.getId());
            }
        }

        //Export and write to file
        writeResultToFile(
            exporter.exportProben(pIds, mIds, encoding, userInfo));

        logger.debug(String.format("Finished LAF export"));
    }
}
