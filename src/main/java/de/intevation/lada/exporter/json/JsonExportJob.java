/* Copyright (C) 2020 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.exporter.json;

import java.util.Map;
import java.util.stream.Stream;

import jakarta.inject.Inject;

import de.intevation.lada.exporter.QueryExportJob;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.data.requests.QueryExportParameters;
import de.intevation.lada.exporter.Exporter;


/**
 * Job class for exporting records to a JSON file.
 *
 * @author <a href="mailto:awoestmann@intevation.de">Alexander Woestmann</a>
 */
public class JsonExportJob extends QueryExportJob<QueryExportParameters> {

    /**
     * The JSON exporter.
     */
    @Inject
    private Exporter<QueryExportParameters> exporter;

    public JsonExportJob() {
        super();
        this.format = "json";
        this.downloadFileName = "export.json";
    }

    @Override
    protected Stream<Map<String, Object>> mergeMessungData(
        Stream<Map<String, Object>> primaryData
    ) {
        final String sDataJsonKey = ID_TYPE_TO_SUBDATA_KEY.get(this.idType);

        return primaryData.map(row -> {
                row.put(sDataJsonKey,
                    repository.getById(Sample.class, row.get(this.idColumn))
                    .getMeasms().stream()
                    .map(this::transformFieldValues).toList());
                return row;
            });
    }

    @Override
    protected Stream<Map<String, Object>> mergeMesswertData(
        Stream<Map<String, Object>> primaryData
    ) {
        // Create a map of id->record
        final String sDataJsonKey = ID_TYPE_TO_SUBDATA_KEY.get(this.idType);

        return primaryData.map(row -> {
                row.put(sDataJsonKey,
                    repository.getById(Measm.class, row.get(this.idColumn))
                    .getMeasVals().stream()
                    .map(this::transformFieldValues).toList());
                return row;
            });
    }

    @Override
    protected Exporter<QueryExportParameters> getExporter() {
        return exporter;
    }
}
