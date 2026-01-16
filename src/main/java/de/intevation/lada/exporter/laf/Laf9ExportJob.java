/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */

package de.intevation.lada.exporter.laf;

import static de.intevation.lada.util.rest.JSONBConfig.JSONB;
import static de.intevation.lada.util.rest.RequestMethod.GET;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.stream.JsonGenerator;
import de.intevation.lada.data.requests.LafExportParameters;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Measm_;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.lada.Sample_;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.exporter.ExportJob;

/**
 * Job class for exporting sample records to JSON as defined by LAF 9.
 */
public class Laf9ExportJob extends ExportJob<LafExportParameters> {

    @Inject
    private Repository repository;

    @Inject
    private Authorization authorization;

    public Laf9ExportJob() {
        this.format = "json";
        this.downloadFileName = "export." + this.format;
    }

    /**
     * Start the export.
     */
    @Override
    public void runWithTx() {
        // TODO: stream to file
        List<Sample> samples = repository.filter(repository
            .queryBuilder(Sample.class)
            .andIn(Sample_.id, exportParameters.getProben())
            .getQuery());

        try (JsonGenerator generator = Json.createGenerator(
                createTmpFileWriter())) {
            generator.writeStartArray();
            for (Sample sample : samples) {
                // Remove unauthorized measVals from result
                JsonArrayBuilder measmArrayBuilder = Json.createArrayBuilder();
                for (Measm measm : sample.getMeasms()) {
                    JsonObjectBuilder measmBuilder = toJson(measm);
                    if (!authorization.isAuthorized(measm, GET)) {
                        measmBuilder.remove(Measm_.MEAS_VALS);
                    }
                    measmArrayBuilder.add(measmBuilder);
                }
                JsonObjectBuilder sampleBuilder = toJson(sample)
                    .add(Sample_.MEASMS, measmArrayBuilder);

                generator.write(sampleBuilder.build());
            }
            generator.writeEnd();
        }
    }

    private JsonObjectBuilder toJson(Object object) {
        return Json.createObjectBuilder(
            JSONB.fromJson(JSONB.toJson(object), JsonObject.class));
    }
}
