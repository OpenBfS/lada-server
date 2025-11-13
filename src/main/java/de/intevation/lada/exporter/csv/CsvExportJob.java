/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.exporter.csv;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import jakarta.inject.Inject;
import de.intevation.lada.exporter.QueryExportJob;
import de.intevation.lada.model.lada.MeasVal;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.data.requests.CsvExportParameters;
import de.intevation.lada.exporter.Exporter;


/**
 * Job class for exporting records to a CSV file.
 *
 * @author <a href="mailto:awoestmann@intevation.de">Alexander Woestmann</a>
 */
public class CsvExportJob extends QueryExportJob<CsvExportParameters> {

    /**
     * The csv exporter.
     */
    @Inject
    private Exporter<CsvExportParameters> exporter;

    public CsvExportJob() {
        super();
        this.format = "csv";
        this.downloadFileName = "export.csv";
    }

    @Override
    protected Stream<Map<String, Object>> mergeMessungData(
        Stream<Map<String, Object>> primaryData
    ) {
        return primaryData.mapMulti((row, c) -> {
                List<Measm> measms = repository
                    .getById(Sample.class, row.get(this.idColumn))
                    .getMeasms();
                if (measms == null || measms.isEmpty()) {
                    c.accept(row);
                } else {
                    for (Measm m : measms) {
                        Map<String, Object> mergedRow
                            = transformFieldValues(m);
                        mergedRow.putAll(row);
                        c.accept(mergedRow);
                    }
                }
            });
    }

    @Override
    protected Stream<Map<String, Object>> mergeMesswertData(
        Stream<Map<String, Object>> primaryData
    ) {
        return primaryData.mapMulti((row, c) -> {
                List<MeasVal> measVals = repository
                    .getById(Measm.class, row.get(this.idColumn))
                    .getMeasVals();
                if (measVals == null || measVals.isEmpty()) {
                    c.accept(row);
                } else {
                    for (MeasVal v : measVals) {
                        Map<String, Object> mergedRow
                            = transformFieldValues(v);
                        mergedRow.putAll(row);
                        c.accept(mergedRow);
                    }
                }
            });
    }

    @Override
    protected void parseExportParameters() {
        super.parseExportParameters();
        if (this.exportSubdata) {
            // "subData" are appended as further columns in CSV output
            this.columnsToExport.addAll(this.subDataColumns);
        }
    }

    @Override
    protected Exporter<CsvExportParameters> getExporter() {
        return exporter;
    }
}
