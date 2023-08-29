/* Copyright (C) 2020 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.exporter.json;

import java.util.ArrayList;
import java.util.Collection;
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
    protected Collection<Map<String, Object>> mergeMessungData(
        Map<Integer, Map<String, Object>> idMap,
        List<Measm> messungData
    ) {
        final String sDataJsonKey = ID_TYPE_TO_SUBDATA_KEY.get(this.idType);

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
        return idMap.values();
    }

    @Override
    protected Collection<Map<String, Object>> mergeMesswertData(
        Map<Integer, Map<String, Object>> idMap,
        List<MeasVal> messwertData
    ) {
        // Create a map of id->record
        final String sDataJsonKey = ID_TYPE_TO_SUBDATA_KEY.get(this.idType);

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
        return idMap.values();
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
