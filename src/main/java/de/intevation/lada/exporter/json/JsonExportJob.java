/* Copyright (C) 2020 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.exporter.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

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

    /**
     * Map of data types and the according sub data key.
     */
    private static final Map<String, String> ID_TYPE_TO_SUBDATA_KEY = Map.of(
        "probeId", "Messungen",
        "messungId", "messwerte");

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
        List<Map<String, Object>> primaryData,
        List<Measm> messungData
    ) {
        // Create a map of id->record
        Map<Integer, Map<String, Object>> idMap = new HashMap<>();
        final String sDataJsonKey = ID_TYPE_TO_SUBDATA_KEY.get(this.idType);
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
        return merged;
    }

    @Override
    protected List<Map<String, Object>> mergeMesswertData(
        List<Map<String, Object>> primaryData,
        List<MeasVal> messwertData
    ) {
        // Create a map of id->record
        Map<Integer, Map<String, Object>> idMap = new HashMap<>();
        final String sDataJsonKey = ID_TYPE_TO_SUBDATA_KEY.get(this.idType);
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
        return merged;
    }


    @Override
    public void runWithTx() {
        //Export data to json
        writeResultToFile(exporter.export(
            getExportData(),
            this.encoding,
            this.exportParameters,
            this.columnsToExport,
            ID_TYPE_TO_SUBDATA_KEY.get(this.idType),
            this.qId,
            this.dateFormat,
            null));

        logger.debug(String.format("Finished JSON export"));
    }
}
