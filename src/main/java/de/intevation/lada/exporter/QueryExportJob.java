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

import de.intevation.lada.model.land.Messung;
import de.intevation.lada.model.land.Messwert;
import de.intevation.lada.model.land.StatusProtokoll;
import de.intevation.lada.model.stammdaten.Filter;
import de.intevation.lada.model.stammdaten.FilterType;
import de.intevation.lada.model.stammdaten.GridColumn;
import de.intevation.lada.model.stammdaten.GridColumnValue;
import de.intevation.lada.model.stammdaten.StatusKombi;
import de.intevation.lada.model.stammdaten.StatusStufe;
import de.intevation.lada.model.stammdaten.StatusWert;
import de.intevation.lada.model.stammdaten.MessEinheit;
import de.intevation.lada.model.stammdaten.Messgroesse;
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
    protected List<GridColumnValue> columns;

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
        columns = new ArrayList <GridColumnValue>();
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
     * Get the sub data for the query.
     * @return Query result as list
     */
    protected List<?> getSubData() {
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
        if (subDataType == null) {
            throw new IllegalArgumentException(
                String.format("Unknown id type: %s", idType));
        }
        switch (subDataType) {
            case "messung": return getMessungSubData(primaryDataIds);
            case "messwert": return getMesswertSubData(primaryDataIds);
            default: return null;
        }
    }

    /**
     * Load messung data filtered by the given ids.
     * @param primaryDataIds Ids to filter for
     * @return Messwert records as list
     */
    private List<Messung> getMessungSubData(List<Integer> primaryDataIds) {
        QueryBuilder<Messung> messungBuilder = repository.queryBuilder(
            Messung.class);
        messungBuilder.andIn("probeId", primaryDataIds);
        return repository.filterPlain(messungBuilder.getQuery());
    }

    /**
     * Load messwert data filtered by the given ids.
     * @param primaryDataIds Ids to filter for
     * @return Messwert records as list
     */
    private List<Messwert> getMesswertSubData(List<Integer> primaryDataIds) {
        QueryBuilder<Messwert> messwertBuilder = repository.queryBuilder(
            Messwert.class);
        messwertBuilder.andIn("messungsId", primaryDataIds);
        return repository.filterPlain(messwertBuilder.getQuery());
    }

    /**
     * Get the status of the given messung as String.
     * Format: [statusStufe - statusWert]
     * @param messung Messung to get status for
     * @return Status as string
     */
    protected String getStatusString(Messung messung) {
        StatusProtokoll protokoll =
            repository.getByIdPlain(
                StatusProtokoll.class, messung.getStatus());
        StatusKombi kombi =
            repository.getByIdPlain(
                StatusKombi.class, protokoll.getStatusKombi());
        StatusStufe stufe = kombi.getStatusStufe();
        StatusWert wert = kombi.getStatusWert();
        return String.format("%s - %s", stufe.getStufe(), wert.getWert());
    }

    /**
     * Get the number of messwerte records referencing the given messung.
     * @param messung Messung to get messwert count for
     * @return Number of messwert records
     */
    protected int getMesswertCount(Messung messung) {
        QueryBuilder<Messwert> builder = repository.queryBuilder(
            Messwert.class);
        builder.and("messungsId", messung.getId());
        // TODO: This is a nice example of ORM-induced database misuse:
        return repository.filterPlain(builder.getQuery()).size();
    }

    /**
    * Get the messeinheit for messwert values using given messwert
    * @param messwert messwertId sungId to get messeinheit for
    * @return messeinheit
     */
    protected String getMesseinheit(Messwert messwert) {
        QueryBuilder<MessEinheit> builder = repository.queryBuilder(
            MessEinheit.class);
        builder.and("id", messwert.getMehId());
        List<MessEinheit> messeinheit = repository.filterPlain(builder.getQuery());
        return messeinheit.get(0).getEinheit();
    }

    /**
    * Get the messgroesse for messwert values using given messwert
    * @param messwert messwertId sungId to get messgroesse for
    * @return messgroesse
     */
    protected String getMessgroesse(Messwert messwert) {
        QueryBuilder<Messgroesse> builder = repository.queryBuilder(
            Messgroesse.class);
        builder.and("id", messwert.getMessgroesseId());
        List<Messgroesse> messgroesse = repository.filterPlain(builder.getQuery());
        return messgroesse.get(0).getMessgroesse();
    }

    /**
     * Get the sub data type to the given primary data type.
     * @param primaryDataType Primary data type
     * @return Sub data type as String
     */
    protected String getSubDataType(String primaryDataType) {
        return mapPrimaryToSubDataTypes.get(primaryDataType);
    }

    /**
     * Merge sub data into the primary query result.
     * @param subData Data to merge into result
     * @return Merged data as list
     */
    protected abstract List<Map<String, Object>> mergeSubData(
        List<?> subData
    );

    /**
     * Parse export parameters.
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
            && exportParameters.get("subDataColumns") != null) {
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
            GridColumnValue columnValue = new GridColumnValue();
            columnValue.setgridColumnId(columnObj.getInt("gridColumnId"));
            String sort = columnObj.get("sort") != null
                && columnObj.get("sort").getValueType() == ValueType.STRING
                ? columnObj.getString("sort") : null;
            columnValue.setSort(sort);
            Integer sortIndex = columnObj.get("sortIndex") != null
                && columnObj.get("sortIndex").getValueType() == ValueType.NUMBER
                ? columnObj.getInt("sortIndex") : null;
            columnValue.setSortIndex(sortIndex);
            columnValue.setFilterValue(columnObj.getString("filterValue"));
            columnValue.setFilterActive(columnObj.getBoolean("filterActive"));
            columnValue.setFilterIsNull(columnObj.getBoolean("filterIsNull"));
            columnValue.setFilterNegate(columnObj.getBoolean("filterNegate"));
            columnValue.setFilterRegex(columnObj.getBoolean("filterRegex"));
            GridColumn gridColumn = repository.getByIdPlain(
                GridColumn.class, columnValue.getGridColumnId());

            columnValue.setGridColumn(gridColumn);

            //Check if the column contains the id
            if (columnValue.getGridColumn().getDataIndex().equals(idColumn)) {
                // Get the column type
                idType = gridColumn.getDataType().getName();

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
                    columnValue.setFilterValue(filterValue.toString());
                    columnValue.setFilterActive(true);
                    columnValue.setFilterIsNull(false);
                    columnValue.setFilterNegate(false);
                    columnValue.setFilterRegex(false);
                }

            }
            columns.add(columnValue);
            if (columnObj.getBoolean("export")) {
                columnsToExport.add(columnValue.getGridColumn().getDataIndex());
            }
        });

        if (columns.size() == 0 || columnsToExport.size() == 0) {
            throw new IllegalArgumentException("No columns to export given");
        }

        //Get query id
        GridColumn gridColumn = repository.getByIdPlain(
            GridColumn.class,
            Integer.valueOf(columns.get(0).getGridColumnId())
        );
        qId = gridColumn.getBaseQuery();
    }
}
