/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */

package de.intevation.lada.exporter.laf;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import jakarta.inject.Inject;
import de.intevation.lada.data.requests.LafExportParameters;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.lada.Sample_;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.JSONBConfig;
import de.intevation.lada.exporter.ExportJob;

/**
 * Job class for exporting sample records to JSON as defined by LAF 9.
 */
public class Laf9ExportJob extends ExportJob<LafExportParameters> {

    @Inject
    private Repository repository;

    public Laf9ExportJob() {
        this.format = "json";
        this.downloadFileName = "export." + this.format;
    }

    /**
     * Start the export.
     */
    @Override
    public void runWithTx() {
        List<Sample> samples = repository.filter(repository
            .queryBuilder(Sample.class)
            .andIn(Sample_.id, exportParameters.getProben())
            .getQuery());

        createTmpFile();
        try (FileWriter writer = new FileWriter(
                this.outputFile, StandardCharsets.UTF_8)) {
            JSONBConfig.JSONB.toJson(samples, writer);
        } catch(IOException e) {
            throw new RuntimeException("Failed writing JSON to file", e);
        }
    }
}
