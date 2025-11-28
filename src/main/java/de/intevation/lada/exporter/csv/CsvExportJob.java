/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.exporter.csv;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import jakarta.inject.Inject;
import jakarta.persistence.TypedQuery;
import de.intevation.lada.exporter.QueryExportJob;
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
    protected Stream<Map<String, Object>> mergeSubData(
        Stream<Map<String, Object>> primaryData,
        TypedQuery<Object[]> subDataQuery
    ) {

        final int mergedRowSize =
            this.columns.size() + this.subDataColumns.size();

        return primaryData.mapMulti((row, c) -> {
                List<Object[]> subData = subDataQuery
                    .setParameter(
                        PRIMARY_DATA_ID_PARAM, row.get(this.idColumn))
                    .getResultList();
                if (subData.isEmpty()) {
                    c.accept(row);
                } else {
                    for (Object[] v : subData) {
                        Map<String, Object> mergedRow
                            = HashMap.newHashMap(mergedRowSize);
                        for (int i = 0; i < this.subDataColumns.size(); i++) {
                            mergedRow.put(this.subDataColumns.get(i), v[i]);
                        }
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
