/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.exporter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import de.intevation.lada.model.lada.MeasVal;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.StatusProt;
import de.intevation.lada.model.master.Filter;
import de.intevation.lada.model.master.FilterType;
import de.intevation.lada.model.master.GridColConf;
import de.intevation.lada.model.master.GridColMp;
import de.intevation.lada.model.master.MeasUnit;
import de.intevation.lada.model.master.Measd;
import de.intevation.lada.model.master.StatusLev;
import de.intevation.lada.model.master.StatusMp;
import de.intevation.lada.model.master.StatusVal;
import de.intevation.lada.query.QueryTools;
import de.intevation.lada.util.data.QueryBuilder;


/**
 * Abstract class for an export of query results.
 */
public abstract class QueryExportJob extends ExportJob {

    /**
     * True if subdata shall be fetched from the database and exported.
     */
    protected boolean exportSubdata;

    /**
     * Sub data column names to export.
     */
    protected List<String> subDataColumns;

    /**
     * Column containing the id.
     */
    protected String idColumn;

    /**
     * Identifier type.
     */
    protected String idType;

    /**
     * Query result.
     */
    protected List<GridColConf> columns;

    /**
     * Columns to use for export.
     */
    protected List<String> columnsToExport;

    /**
     * Map of data types and the according sub data types.
     */
    private Map<String, String> mapPrimaryToSubDataTypes;

    /**
     * Timezone to convert timestamps to.
     */
    protected String timezone;

    /**
     * Query id.
     */
    protected Integer qId;

    /**
     * Primary data query result.
     */
    protected List<Map<String, Object>> primaryData;

    /**
     * Constructor.
     */
    public QueryExportJob() {
        columns = new ArrayList <GridColConf>();
        columnsToExport = new ArrayList<String>();

        mapPrimaryToSubDataTypes = new HashMap<String, String>();
        mapPrimaryToSubDataTypes.put("probeId", "messung");
        mapPrimaryToSubDataTypes.put("messungId", "messwert");

        this.timezone = "UTC";
    }

    /**
     * Creates an ID list filter for the given dataIndex.
     * @param dataIndex ID column name
     * @return Filter object
     */
    private Filter createIdListFilter(String dataIndex) {
        //Get Filter type from db
        QueryBuilder<FilterType> builder =
            repository.queryBuilder(FilterType.class);
        builder.and("type", "genericid");
        FilterType filterType =
            repository.filterPlain(builder.getQuery()).get(0);

        //Create filter object
        Filter filter = new Filter();
        filter.setFilterType(filterType);
        filter.setParam(dataIndex);
        filter.setSql(String.format(
                "CAST(%1$s AS text) IN ( :%1$s )", dataIndex));
        return filter;
    }

    /**
     * Get the value of an object's field by calling its getter.
     * @param fieldName field name
     * @param object object
     * @return Field value
     */
    protected Object getFieldByName(String fieldName, Object object) {

        String capitalizedName;
        String methodName = "";
        Method method;
        try {
            capitalizedName =
                fieldName.substring(0, 1).toUpperCase()
                + fieldName.substring(1);
            methodName = "get" + capitalizedName;
            method = object.getClass().getMethod(methodName);
            return method.invoke(object);
        } catch (NoSuchMethodException nsme) {
            logger.error(String.format(
                "Can not get field %s(%s) for class %s",
                fieldName, methodName, object.getClass().toString()));
            return null;
        } catch (IllegalAccessException | InvocationTargetException exc) {
            logger.error(String.format(
                "Can not call %s for class %s",
                methodName, object.getClass().toString()));
            return null;
        }
    }

    /**
     * Execute the query.
     * @return Query result as list
     */
    protected List<Map<String, Object>> getQueryResult() {
        QueryTools queryTools = new QueryTools(repository, columns);
        List<Map<String, Object>> result = queryTools.getResultForQuery();
        logger.debug(String.format(
                "Fetched %d primary records",
                result == null ? 0 : result.size()));
        return result;
    }

    /**
     * Get the status of the given messung as String.
     * Format: [statusStufe - statusWert]
     * @param messung Messung to get status for
     * @return Status as string
     */
    protected String getStatusString(Measm messung) {
        StatusProt protokoll =
            repository.getByIdPlain(
                StatusProt.class, messung.getStatus());
        StatusMp kombi =
            repository.getByIdPlain(
                StatusMp.class, protokoll.getStatusMpId());
        StatusLev stufe = kombi.getStatusLev();
        StatusVal wert = kombi.getStatusVal();
        return String.format("%s - %s", stufe.getLev(), wert.getVal());
    }

    /**
     * Get the number of messwerte records referencing the given messung.
     * @param messung Messung to get messwert count for
     * @return Number of messwert records
     */
    protected int getMesswertCount(Measm messung) {
        QueryBuilder<MeasVal> builder = repository.queryBuilder(
            MeasVal.class);
        builder.and("measmId", messung.getId());
        // TODO: This is a nice example of ORM-induced database misuse:
        return repository.filterPlain(builder.getQuery()).size();
    }

    /**
    * Get the messeinheit for messwert values using given messwert.
    * @param messwert messwertId sungId to get messeinheit for
    * @return messeinheit
     */
    protected String getMesseinheit(MeasVal messwert) {
        QueryBuilder<MeasUnit> builder = repository.queryBuilder(
            MeasUnit.class);
        builder.and("id", messwert.getMeasUnitId());
        List<MeasUnit> messeinheit = repository.filterPlain(builder.getQuery());
        return messeinheit.get(0).getUnitSymbol();
    }

    /**
    * Get the messgroesse for messwert values using given messwert.
    * @param messwert messwertId sungId to get messgroesse for
    * @return messgroesse
     */
    protected String getMessgroesse(MeasVal messwert) {
        QueryBuilder<Measd> builder = repository.queryBuilder(
            Measd.class);
        builder.and("id", messwert.getMeasdId());
        List<Measd> messgroesse = repository.filterPlain(builder.getQuery());
        return messgroesse.get(0).getName();
    }

    /**
     * Merge sub data into the primary query result.
     *
     * @return Merged data as list
     * @throws IllegalArgumentException in case of unknown sub-data type
     */
    protected List<Map<String, Object>> mergeSubData() {
        if (primaryData == null) {
            return null;
        }

        //Get ids of primary records
        List<Integer> primaryDataIds = new ArrayList<Integer>();
        primaryData.forEach(item -> {
            primaryDataIds.add((Integer) item.get(idColumn));
        });

        //Get subdata
        String subDataType = mapPrimaryToSubDataTypes.get(idType);
        switch (subDataType) {
            case "messung":
                QueryBuilder<Measm> messungBuilder = repository
                    .queryBuilder(Measm.class)
                    .andIn("sampleId", primaryDataIds);
                return mergeMessungData(
                    repository.filterPlain(messungBuilder.getQuery()));
            case "messwert":
                QueryBuilder<MeasVal> messwertBuilder = repository
                    .queryBuilder(MeasVal.class)
                    .andIn("measmId", primaryDataIds);
                return mergeMesswertData(
                    repository.filterPlain(messwertBuilder.getQuery()));
            default:
                throw new IllegalArgumentException(
                    String.format("Unknown subDataType: %s", subDataType));
        }
    }

    /**
     * Merge primary result and measm data.
     *
     * @param messungData Data to merge
     * @return Merged data as list
     */
    protected abstract List<Map<String, Object>> mergeMessungData(
        List<Measm> messungData
    );

    /**
     * Merge primary result and measVal data.
     *
     * @param messwertData Data to merge
     * @return Merged data as list
     */
    protected abstract List<Map<String, Object>> mergeMesswertData(
        List<MeasVal> messwertData
    );

    /**
     * Parse export parameters.
     *
     * @throws IllegalArgumentException if exportSubData is true but no
     * subDataColumns arge given.
     */
    protected void parseExportParameters() {
        if (exportParameters == null) {
            return;
        }
        //Check if subdata shall be exported
        exportSubdata = exportParameters.getBoolean("exportSubData");
        //Get identifier type
        idColumn = exportParameters.isNull("idField")
            ? null : exportParameters.getString("idField");
        //Get target timezone
        timezone = exportParameters.getString("timezone");

        //Check if sub data columns are present if subdata is exported
        if (exportSubdata
            && !exportParameters.containsKey("subDataColumns")
        ) {
            throw new IllegalArgumentException(
                "Subdata is exported but no subdata columns are present");
        }

        //Get sub data columns
        if (exportSubdata && exportParameters.containsKey("subDataColumns")) {
            subDataColumns = new ArrayList<String>();
            JsonArray columnJson =
                exportParameters.getJsonArray("subDataColumns");
            int columnCount = columnJson.size();
            for (int i = 0; i < columnCount; i++) {
                subDataColumns.add(columnJson.getString(i));
            }
        }

        exportParameters.getJsonArray("columns").forEach(jsonValue -> {
            JsonObject columnObj = (JsonObject) jsonValue;
            GridColConf columnValue = new GridColConf();
            columnValue.setGridColMpId(columnObj.getInt("gridColMpId"));
            String sort = columnObj.get("sort") != null
                && columnObj.get("sort").getValueType() == ValueType.STRING
                ? columnObj.getString("sort") : null;
            columnValue.setSort(sort);
            Integer sortIndex = columnObj.get("sortIndex") != null
                && columnObj.get("sortIndex").getValueType() == ValueType.NUMBER
                ? columnObj.getInt("sortIndex") : null;
            columnValue.setSortIndex(sortIndex);
            columnValue.setFilterVal(
                columnObj.getString("filterVal"));
            columnValue.setIsFilterActive(
                columnObj.getBoolean("isFilterActive"));
            columnValue.setIsFilterNull(
                columnObj.getBoolean("isFilterNull"));
            columnValue.setIsFilterNegate(
                columnObj.getBoolean("isFilterNegate"));
            columnValue.setIsFilterRegex(
                columnObj.getBoolean("isFilterRegex"));
            GridColMp gridColumn = repository.getByIdPlain(
                GridColMp.class, columnValue.getGridColMpId());

            columnValue.setGridColMp(gridColumn);

            //Check if the column contains the id
            if (columnValue.getGridColMp().getDataIndex().equals(idColumn)) {
                // Get the column type
                idType = gridColumn.getDisp().getName();

                // Get IDs to filter result
                JsonArray idsToExport = exportParameters
                    .getJsonArray("idFilter");

                if (idsToExport != null && idsToExport.size() > 0) {
                    // Prepare filtering by IDs
                    Filter filter = createIdListFilter(
                        gridColumn.getDataIndex());
                    gridColumn.setFilter(filter);
                    // TODO: This is a hack to avoid in transactional context:
                    //java.lang.IllegalStateException:
                    //org.hibernate.TransientPropertyValueException:
                    //object references an unsaved transient instance -
                    //save the transient instance before flushing :
                    //de.intevation.lada.model.stammdaten.GridColumn.filter
                    //-> de.intevation.lada.model.stammdaten.Filter
                    repository.entityManager().detach(gridColumn);

                    StringBuilder filterValue = new StringBuilder();
                    for (
                        Iterator<JsonValue> ids = idsToExport.iterator();
                        ids.hasNext();
                    ) {
                        JsonValue id = ids.next();
                        switch (id.getValueType()) {
                        case NUMBER:
                            filterValue.append(
                                ((JsonNumber) id).toString());
                            break;
                        case STRING:
                            filterValue.append(
                                ((JsonString) id).getString());
                            break;
                        default:
                            throw new IllegalArgumentException(
                                "IDs must be number or string");
                        }
                        if (ids.hasNext()) {
                            filterValue.append(",");
                        }
                    }
                    columnValue.setFilterVal(filterValue.toString());
                    columnValue.setIsFilterActive(true);
                    columnValue.setIsFilterNull(false);
                    columnValue.setIsFilterNegate(false);
                    columnValue.setIsFilterRegex(false);
                }

            }
            columns.add(columnValue);
            if (columnObj.getBoolean("export")) {
                columnsToExport.add(columnValue.getGridColMp().getDataIndex());
            }
        });

        if (columns.size() == 0 || columnsToExport.size() == 0) {
            throw new IllegalArgumentException("No columns to export given");
        }

        //Get query id
        GridColMp gridColumn = repository.getByIdPlain(
            GridColMp.class,
            Integer.valueOf(columns.get(0).getGridColMpId())
        );
        qId = gridColumn.getBaseQueryId();
    }
}
