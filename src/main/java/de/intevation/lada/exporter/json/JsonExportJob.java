/* Copyright (C) 2020 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.exporter.json;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import de.intevation.lada.exporter.QueryExportJob;
import de.intevation.lada.model.lada.MeasVal;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.exporter.ExportConfig;
import de.intevation.lada.exporter.Exporter;
import de.intevation.lada.exporter.ExportFormat;


/**
 * Job class for exporting records to a JSON file.
 *
 * @author <a href="mailto:awoestmann@intevation.de">Alexander Woestmann</a>
 */
public class JsonExportJob extends QueryExportJob {

    private static final int LENGTH = 1024;
    private String subDataJsonKey;

    /**
     * The JSON exporter.
     */
    @Inject
    @ExportConfig(format = ExportFormat.JSON)
    private Exporter exporter;

    public JsonExportJob() {
        super();
        this.format = "json";
        this.downloadFileName = "export.json";
    }

    @Override
    protected List<Map<String, Object>> mergeMessungData(
        List<Measm> messungData
    ) {
        // Create a map of id->record
        Map<Integer, Map<String, Object>> idMap = new HashMap<>();
        final String sDataJsonKey = "Messungen";
        primaryData.forEach(record -> {
            idMap.put((Integer) record.get(idColumn), record);
        });

        List<Map<String, Object>> merged = primaryData;
        messungData.forEach(messung -> {
            Map<String, Object> mergedMessung = transformFieldValues(messung);
            //Append messung to probe
            Map<String, Object> primaryRecord = idMap.get(
                messung.getSampleId());
            if (primaryRecord.get(sDataJsonKey) == null) {
                primaryRecord.put(sDataJsonKey, new ArrayList<Object>());
            }
            ArrayList<Map<String, Object>> messungenList =
                (ArrayList<Map<String, Object>>) primaryRecord.get(
                    sDataJsonKey);
            messungenList.add(mergedMessung);
        });
        this.subDataJsonKey = sDataJsonKey;
        return merged;
    }

    @Override
    protected List<Map<String, Object>> mergeMesswertData(
        List<MeasVal> messwertData
    ) {
        // Create a map of id->record
        Map<Integer, Map<String, Object>> idMap = new HashMap<>();
        final String sDataJsonKey = "messwerte";
        primaryData.forEach(record -> {
            idMap.put((Integer) record.get(idColumn), record);
        });

        List<Map<String, Object>> merged = primaryData;
        messwertData.forEach(messwert -> {
            Map<String, Object> mergedMesswert = transformFieldValues(messwert);
            //Append messung to probe
            Map<String, Object> primaryRecord = idMap.get(
                messwert.getMeasmId());
            if (primaryRecord.get(sDataJsonKey) == null) {
                primaryRecord.put(sDataJsonKey, new ArrayList<Object>());
            }
            ArrayList<Map<String, Object>> messwertList =
                (ArrayList<Map<String, Object>>) primaryRecord.get(
                    sDataJsonKey);
            messwertList.add(mergedMesswert);
        });
        this.subDataJsonKey = sDataJsonKey;
        return merged;
    }


    @Override
    public void runWithTx() {
        parseExportParameters();

        // Fetch primary records
        primaryData = getQueryResult();

        List<Map<String, Object>> exportData = primaryData;
        // If needed, fetch and merge sub data
        if (exportSubdata) {
            exportData = mergeSubData();
        }

        //Export data to json
        JsonObjectBuilder optionBuilder = Json.createObjectBuilder()
            .add("subData", exportSubdata ? subDataJsonKey : "");
        if (idColumn != null) {
            optionBuilder.add("id", idColumn);
        }
        JsonObject exportOptions = optionBuilder.build();
        InputStream exported = exporter.export(
            exportData,
            encoding,
            exportOptions,
            this.columnsToExport,
            qId,
            this.dateFormat,
            null);

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[LENGTH];
        int length;
        try {
            while ((length = exported.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            writeResultToFile(result.toString(encoding));
        } catch (IOException ioe) {
            throw new RuntimeException(ioe.getMessage());
        }

        logger.debug(String.format("Finished JSON export"));
    }
}
