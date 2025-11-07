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

import jakarta.inject.Inject;

import de.intevation.lada.exporter.QueryExportJob;
import de.intevation.lada.model.lada.MeasVal;
import de.intevation.lada.model.lada.Measm;
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
    protected Collection<Map<String, Object>> mergeMessungData(
        Map<Integer, Map<String, Object>> idMap,
        List<Measm> messungData
    ) {
        final String sDataJsonKey = ID_TYPE_TO_SUBDATA_KEY.get(this.idType);

        messungData.forEach(messung -> {
            Map<String, Object> mergedMessung = transformFieldValues(messung);
            //Append messung to probe
            Map<String, Object> primaryRecord = idMap.get(
                messung.getSample().getId());
            if (primaryRecord.get(sDataJsonKey) == null) {
                primaryRecord.put(sDataJsonKey, new ArrayList<Object>());
            }
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> messungenList =
                (List<Map<String, Object>>) primaryRecord.get(sDataJsonKey);
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
                messwert.getMeasm().getId());
            if (primaryRecord.get(sDataJsonKey) == null) {
                primaryRecord.put(sDataJsonKey, new ArrayList<Object>());
            }
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> messwertList =
                (List<Map<String, Object>>) primaryRecord.get(sDataJsonKey);
            messwertList.add(mergedMesswert);
        });
        return idMap.values();
    }

    @Override
    protected Exporter<QueryExportParameters> getExporter() {
        return exporter;
    }
}
