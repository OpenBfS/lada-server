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
import java.util.stream.Stream;

import jakarta.inject.Inject;
import jakarta.persistence.TypedQuery;
import de.intevation.lada.exporter.QueryExportJob;
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
    protected Stream<Map<String, Object>> mergeSubData(
        Stream<Map<String, Object>> primaryData,
        TypedQuery<Object[]> subDataQuery
    ) {
        final String sDataJsonKey = ID_TYPE_TO_SUBDATA_KEY.get(this.idType);

        return primaryData.map(row -> {
                List<Object[]> subData = subDataQuery
                    .setParameter(
                        PRIMARY_DATA_ID_PARAM, row.get(this.idColumn))
                    .getResultList();
                List<Map<String, Object>> subList =
                    new ArrayList<>(subData.size());
                for (Object[] v : subData) {
                    Map<String, Object> subDataMap
                        = HashMap.newHashMap(this.subDataColumns.size());
                    for (int i = 0; i < this.subDataColumns.size(); i++) {
                        subDataMap.put(this.subDataColumns.get(i), v[i]);
                    }
                    subList.add(subDataMap);
                }
                Map<String, Object> rowWithSubData
                    = HashMap.newHashMap(row.size() + 1);
                rowWithSubData.putAll(row);
                rowWithSubData.put(sDataJsonKey, subList);
                return rowWithSubData;
            });
    }

    @Override
    protected Exporter<QueryExportParameters> getExporter() {
        return exporter;
    }
}
