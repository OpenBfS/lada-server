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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

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
     * Date format to convert timestamps to (time zone defaults to UTC).
     */
    protected DateFormat dateFormat =
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * Query id.
     */
    protected Integer qId;

    /**
     * Constructor.
     */
    public QueryExportJob() {
        columns = new ArrayList <GridColConf>();
        columnsToExport = new ArrayList<String>();

        this.dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
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
     * Execute query to fetch export data and merge sub-data, if requested.
     * @return Query result, including sub-data, if requested.
     */
    protected Collection<Map<String, Object>> getExportData() {
        parseExportParameters();

        QueryTools queryTools = new QueryTools(repository, columns);
        List<Map<String, Object>> primaryData = queryTools.getResultForQuery();
        logger.debug(String.format(
                "Fetched %d primary records",
                primaryData == null ? 0 : primaryData.size()));

        if (exportSubdata) {
            return mergeSubData(primaryData);
        }
        return primaryData;
    }

    /**
     * Transform Measm object into map with keys according to subDataColumns.
     * @param measm Measm for which field values should be transformed
     * @return Map with field names and transformed values of original measm
     */
    protected Map<String, Object> transformFieldValues(Measm measm) {
        Map<String, Object> transformed = new HashMap<>();
        subDataColumns.forEach(subDataColumn -> {
                Object fieldValue = null;
                switch (subDataColumn) {
                case "statusKombi":
                    StatusProt prot =
                        repository.getByIdPlain(
                            StatusProt.class, measm.getStatus());
                    StatusMp mp =
                        repository.getByIdPlain(
                            StatusMp.class, prot.getStatusMpId());
                    StatusLev lev = mp.getStatusLev();
                    StatusVal val = mp.getStatusVal();
                    fieldValue = String.format(
                        "%s - %s", lev.getLev(), val.getVal());
                    break;
                case "messwerteCount":
                    QueryBuilder<MeasVal> builder = repository
                        .queryBuilder(MeasVal.class)
                        .and("measmId", measm.getId());
                    // TODO: A nice example of ORM-induced database misuse:
                    fieldValue = repository.filterPlain(builder.getQuery())
                        .size();
                    break;
                default:
                    fieldValue = getFieldByName(subDataColumn, measm);
                }
                transformed.put(subDataColumn, fieldValue);
            });
        return transformed;
    }

    /**
     * Transform MeasVal object into map with keys according to subDataColumns.
     * @param measVal MeasVal for which field values should be transformed
     * @return Map with field names and transformed values of original measVal
     */
    protected Map<String, Object> transformFieldValues(MeasVal measVal) {
        Map<String, Object> transformed = new HashMap<>();
        subDataColumns.forEach(subDataColumn -> {
                Object fieldValue = null;
                switch (subDataColumn) {
                case "measUnitId":
                    fieldValue = repository.getByIdPlain(
                        MeasUnit.class, measVal.getMeasUnitId())
                        .getUnitSymbol();
                    break;
                case "measdId":
                    fieldValue = repository.getByIdPlain(
                        Measd.class, measVal.getMeasdId()).getName();
                    break;
                default:
                    fieldValue = getFieldByName(subDataColumn, measVal);
                }
                transformed.put(subDataColumn, fieldValue);
            });
        return transformed;
    }

    /**
     * Merge sub data into the primary query result.
     *
     * @param primaryData The primary query result as list
     * @return Merged data
     * @throws IllegalArgumentException in case of unknown sub-data type
     */
    protected Collection<Map<String, Object>> mergeSubData(
        List<Map<String, Object>> primaryData
    ) {
        if (primaryData == null) {
            return null;
        }

        // Create a map of id->record
        Map<Integer, Map<String, Object>> idMap = new HashMap<>();
        primaryData.forEach(record -> {
            idMap.put((Integer) record.get(idColumn), record);
        });

        //Get subdata
        switch (this.idType) {
            case "probeId":
                QueryBuilder<Measm> messungBuilder = repository
                    .queryBuilder(Measm.class)
                    .andIn("sampleId", idMap.keySet());
                return mergeMessungData(
                    idMap,
                    repository.filterPlain(messungBuilder.getQuery()));
            case "messungId":
                QueryBuilder<MeasVal> messwertBuilder = repository
                    .queryBuilder(MeasVal.class)
                    .andIn("measmId", idMap.keySet());
                return mergeMesswertData(
                    idMap,
                    repository.filterPlain(messwertBuilder.getQuery()));
            default:
                throw new IllegalArgumentException(
                    String.format("Unknown idType: %s", this.idType));
        }
    }

    /**
     * Merge primary result and measm data.
     *
     * @param primaryData The primary query result as map of IDs with records
     * @param messungData Data to merge
     * @return Merged data
     */
    protected abstract Collection<Map<String, Object>> mergeMessungData(
        Map<Integer, Map<String, Object>> primaryData,
        List<Measm> messungData
    );

    /**
     * Merge primary result and measVal data.
     *
     * @param primaryData The primary query result as map of IDs with records
     * @param messwertData Data to merge
     * @return Merged data
     */
    protected abstract Collection<Map<String, Object>> mergeMesswertData(
        Map<Integer, Map<String, Object>> primaryData,
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
        this.exportSubdata = exportParameters.getBoolean(
            "exportSubData", false);
        //Get identifier type
        this.idColumn = exportParameters.isNull("idField")
            ? null : exportParameters.getString("idField");
        //Get target timezone
        final String timezoneKey = "timezone";
        if (exportParameters.containsKey(timezoneKey)) {
            this.dateFormat.setTimeZone(TimeZone.getTimeZone(
                    exportParameters.getString(timezoneKey)));
        }

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
                this.idType = gridColumn.getDisp().getName();

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
