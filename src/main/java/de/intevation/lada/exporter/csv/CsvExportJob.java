/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.exporter.csv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.inject.Inject;

import de.intevation.lada.exporter.QueryExportJob;
import de.intevation.lada.model.lada.MeasVal;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.exporter.ExportConfig;
import de.intevation.lada.exporter.Exporter;
import de.intevation.lada.exporter.ExportFormat;


/**
 * Job class for exporting records to a CSV file.
 *
 * @author <a href="mailto:awoestmann@intevation.de">Alexander Woestmann</a>
 */
public class CsvExportJob extends QueryExportJob {

    /**
     * The csv exporter.
     */
    @Inject
    @ExportConfig(format = ExportFormat.CSV)
    private Exporter exporter;

    public CsvExportJob() {
        super();
        this.format = "csv";
        this.downloadFileName = "export.csv";
    }

    /**
     * Merge records without sub data.
     * @param objects Record list
     * @param ids list of ids to merge
     * @param subDataColumns Subdata columns
     * @param primaryColumns primary data columns
     * @return All records with and without sub-data
     */
    private List<Map<String, Object>> mergeDataWithEmptySubdata(
        Map<Integer, Map<String, Object>> objects, List<Integer> ids,
        List<String> subDataColumns, List<String> primaryColumns) {

        List<Map<String, Object>> merged = new ArrayList<>();
        ids.forEach(id -> {
            Map<String, Object> mergedRow = new HashMap<>();
            subDataColumns.forEach(column -> {
                mergedRow.put(column, null);
            });
            Map<String, Object> primaryRecord = objects.get(id);
            primaryColumns.forEach(column -> {
                mergedRow.put(column, primaryRecord.get(column));
            });
            merged.add(mergedRow);
        });
        return merged;
    }

    @Override
    protected List<Map<String, Object>> mergeMessungData(
        Map<Integer, Map<String, Object>> idMap,
        List<Measm> messungData
    ) {
        // Ids left for merging
        List<Integer> idsLeft = new ArrayList<>();
        idMap.keySet().forEach(key -> idsLeft.add(key));

        List<Map<String, Object>> merged = new ArrayList<>();
        messungData.forEach(messung -> {
            Map<String, Object> mergedRow = transformFieldValues(messung);
            // Add primary record
            Integer primaryId = messung.getSampleId();
            Map<String, Object> primaryRecord = idMap.get(primaryId);
            primaryRecord.forEach((key, value) -> {
                mergedRow.put(key, value);
            });
            // Remove finished record from list
            idsLeft.remove(primaryId);
            merged.add(mergedRow);
        });

        //Merge any skipped records without sub data
        merged.addAll(mergeDataWithEmptySubdata(
            idMap, idsLeft, subDataColumns, columnsToExport));
        return merged;
    }

    @Override
    protected List<Map<String, Object>> mergeMesswertData(
        Map<Integer, Map<String, Object>> idMap,
        List<MeasVal> messwertData
    ) {
        // Ids left for merging
        List<Integer> idsLeft = new ArrayList<>();
        idMap.keySet().forEach(key -> idsLeft.add(key));

        AtomicBoolean success = new AtomicBoolean(true);
        List<Map<String, Object>> merged = new ArrayList<>();
        messwertData.forEach(messwert -> {
            Map<String, Object> mergedRow = transformFieldValues(messwert);
            // Add primary record
            Integer primaryId = messwert.getMeasmId();
            Map<String, Object> primaryRecord = idMap.get(primaryId);
            if (primaryRecord == null) {
                logger.error("Can not get primary record for merging");
                success.set(false);
                return;
            }
            primaryRecord.forEach((key, value) -> {
                mergedRow.put(key, value);
            });
            // Remove finished record from list
            idsLeft.remove(primaryId);
            merged.add(mergedRow);
        });
        // Merge any skipped records without sub data
        merged.addAll(mergeDataWithEmptySubdata(
            idMap, idsLeft, subDataColumns, columnsToExport));

        if (!success.get()) {
            return null;
        }
        return merged;
    }

    @Override
    protected void parseExportParameters() {
        super.parseExportParameters();
        if (this.exportSubdata) {
            // "subData" are appended as further columns in CSV output
            this.columnsToExport.addAll(this.subDataColumns);
        }
    }

    /**
     * Start the CSV export.
     */
    @Override
    public void runWithTx() {
        //Export data to csv
        writeResultToFile(exporter.export(
            getExportData(),
            this.encoding,
            this.exportParameters,
            this.columnsToExport,
            "",
            this.qId,
            this.dateFormat,
            this.locale));

        logger.debug(String.format("Finished CSV export"));
    }
}
