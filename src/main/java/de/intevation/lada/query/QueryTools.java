/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.query;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.persistence.Query;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

import de.intevation.lada.model.master.BaseQuery;
import de.intevation.lada.model.master.Filter;
import de.intevation.lada.model.master.GridColConf;
import de.intevation.lada.model.master.GridColMp;
import de.intevation.lada.model.master.Tag;
import de.intevation.lada.util.data.Repository;

/**
 * Utility class to handle the SQL query configuration.
 *
 * @author <a href = "mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class QueryTools {

    static final String GENERICID_FILTER_TYPE = "genericid";
    static final String GENERICTEXT_FILTER_TYPE = "generictext";
    static final String TAG_FILTER_TYPE = "tag";
    static final String TEXT_FILTER_TYPE = "text";

    private Repository repository;

    // Base query and WHERE clause
    private String sql;

    // ORDER BY clause
    private String sortSql;

    private List<GridColConf> customColumns;

    private MultivaluedMap<String, Object> filterValues;

    /**
     * @param repository Repository for database access.
     * @param customColumns Customized column configs, containing
     *      filter, sorting and references to the respective column.
     */
    public QueryTools(
        Repository repository,
        List<GridColConf> customColumns
    )  throws IllegalArgumentException {
        this.repository = repository;

        for (GridColConf columnValue : customColumns) {
            if (columnValue.getGridColMp() == null) {
                GridColMp gridColumn = repository.getByIdPlain(
                    GridColMp.class, columnValue.getGridColMpId());
                columnValue.setGridColMp(gridColumn);
            }
        }
        this.customColumns = customColumns;

        this.sql = prepareSql(
            customColumns,
            repository.getByIdPlain(
                BaseQuery.class,
                customColumns.get(0).getGridColMp().getBaseQueryId()
            ).getSql());

        this.sortSql = prepareSortSql(customColumns);

        // Initialize this.filterValues
        prepareFilters();
    };

    public String getSql() {
        return this.sql + this.sortSql;
    }

    public MultivaluedMap<String, Object> getFilterValues() {
        return this.filterValues;
    }

    /**
     * Execute query and return the filtered and sorted results.
     *
     * @return List of result maps.
     */
    public List<Map<String, Object>> getResultForQuery() {
        return getResultForQuery(0, null);
    }

    /**
     * Execute query and return a subset defined by offset and limit
     * of the filtered and sorted results.
     *
     * @param offset The position of the first result to retrieve,
     * numbered from 0.
     * @param limit The maximum number of results to retrieve,
     * or null for no limit.
     * @return List of result maps.
     */
    public List<Map<String, Object>> getResultForQuery(
        int offset,
        Integer limit
    ) {
        Query query = prepareQuery(getSql()).setFirstResult(offset);
        if (limit != null) {
            query.setMaxResults(limit);
        }

        List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
        for (Object row: query.getResultList()) {
            Map<String, Object> set = new HashMap<String, Object>();
            /* If row contains or is a java.sql.Timestamp:
             * Convert to date to allow serialization
             */
            if (row instanceof Object[]) {
                Object[] rowArr = (Object[]) row;
                for (int i = 0; i < rowArr.length; i++) {
                    Object col = rowArr[i];
                    if (col instanceof Timestamp) {
                        Timestamp ts = (Timestamp) col;
                        rowArr[i] = new Date(ts.getTime());
                    }
                }
            } else if (row instanceof Timestamp) {
                Timestamp ts = (Timestamp) row;
                row = new Date(ts.getTime());
            }
            for (GridColConf column: this.customColumns) {
                set.put(
                    column.getGridColMp().getDataIndex(),
                    row instanceof Object[]
                        ? ((Object[]) row)[
                            column.getGridColMp().getPosition() - 1]
                        : row);
            }
            ret.add(set);
        }
        return ret;
    }

    /**
     * Get total count of entries a filtered query would return.
     *
     * @return Number of entries the given query would return.
     */
    public int getTotalCountForQuery() {
        Query q = prepareQuery(
            "SELECT count(*) FROM (" + this.sql + ") as query");
        return Math.toIntExact((Long) q.getSingleResult());
    }

    /**
     * Complement SQL statement from base query with filter settings.
     *
     * @param customColumns List<GridColumnValue> with filter and sort settings.
     * @param sql The base query without WHERE and ORDER BY clause.
     * @return The query including WHERE clause.
     */
    static String prepareSql(List<GridColConf> customColumns, String sql) {
        String filterSql = "";
        String genericFilterSql = "";
        boolean subquery = false;

        for (GridColConf customColumn : customColumns) {
            boolean generic = false;
            if (customColumn.getIsFilterActive()
                && customColumn.getFilterVal() != null
                && !customColumn.getFilterVal().isEmpty()
                && !customColumn.getIsFilterIsNull()
            ) {
                Filter filter = customColumn.getGridColMp().getFilter();
                String filterValue = customColumn.getFilterVal();
                String currentFilterString = filter.getSql();
                String currentFilterParam = filter.getParam();
                String filterType = filter.getFilterType().getType();
                if (GENERICTEXT_FILTER_TYPE.equals(filterType)
                    || GENERICID_FILTER_TYPE.equals(filterType)
                ) {
                    subquery = true;
                    generic = true;
                }
                if (GENERICTEXT_FILTER_TYPE.equals(filterType)) {
                    String genTextParam = ":" + filter.getParam() + "Param";
                    String genTextValue = filter.getParam() + "Value";
                    currentFilterString =
                        currentFilterString.replace(
                            genTextParam,
                            customColumn.getGridColMp().getDataIndex());
                    currentFilterParam =
                        genTextValue + customColumn.getGridColMpId();
                    currentFilterString =
                        currentFilterString.replace(
                            ":" + genTextValue, ":" + currentFilterParam);
                } else if (TAG_FILTER_TYPE.equals(filterType)) {
                    String[] tagIds = filterValue.split(",");
                    int tagNumber = tagIds.length;
                    String paramlist = "";
                    String param = filter.getParam();
                    String tagFilterSql = filter.getSql();
                    for (int i = 0; i < tagNumber; i++) {
                        if (i != tagNumber - 1) {
                            paramlist += " :" + param + i + " , ";
                        } else {
                            paramlist += " :" + param + i;
                        }
                    }
                    tagFilterSql =
                        tagFilterSql.replace(
                            ":" + filter.getParam(), paramlist);
                    if (filterSql.isEmpty()) {
                        filterSql += " WHERE " + tagFilterSql;
                    } else {
                        filterSql += " AND " + tagFilterSql;
                    }
                    continue;
                }
                if (customColumn.getIsFilterNegate()) {
                    currentFilterString = "NOT(" + currentFilterString + ")";
                }
                if (generic) {
                    if (genericFilterSql.isEmpty()) {
                        genericFilterSql += " WHERE " + currentFilterString;
                    } else {
                        genericFilterSql += " AND " + currentFilterString;
                    }
                } else {
                    //Build WHERE clause
                    if (filterSql.isEmpty()) {
                        filterSql += " WHERE " + currentFilterString;
                    } else {
                        filterSql += " AND " + currentFilterString;
                    }
                }
            } else if (customColumn.getIsFilterActive()
                       && customColumn.getIsFilterIsNull()
            ) {
                Filter filter = customColumn.getGridColMp().getFilter();
                String currentFilterString = filter.getSql();
                String filterType = filter.getFilterType().getType();
                if (GENERICTEXT_FILTER_TYPE.equals(filterType)) {
                    currentFilterString =
                        customColumn.getGridColMp().getDataIndex()
                        + " IS NULL";
                    if (customColumn.getIsFilterNegate()) {
                        currentFilterString =
                            "NOT(" + currentFilterString + ")";
                    }
                    if (genericFilterSql.isEmpty()) {
                        genericFilterSql += " WHERE " + currentFilterString;
                    } else {
                        genericFilterSql += " AND " + currentFilterString;
                    }
                    subquery = true;
                    generic = true;
                 } else {
                    currentFilterString =
                        currentFilterString.replaceAll(" .*", " IS NULL ");
                    currentFilterString =
                        currentFilterString.replaceAll(".*\\(", "");
                    if (customColumn.getIsFilterNegate()) {
                        currentFilterString =
                            "NOT(" + currentFilterString + ")";
                    }
                    if (filterSql.isEmpty()) {
                        filterSql += " WHERE " + currentFilterString;
                    } else {
                        filterSql += " AND " + currentFilterString;
                    }
                }
            }
        }

        // Append (possibly empty) WHERE clause
        sql += filterSql;

        //Append generic and/or tag filter sql seperated from other filters
        if (subquery) {
            sql = "SELECT * FROM (" + sql + ") AS inner_query";
            sql += genericFilterSql;
        }
        return sql;
    }

    /**
     * Generate "ORDER BY" clause from query configuration.
     *
     * @param customColumns List<GridColumnValue> with filter and sort settings.
     * @return The "ORDER BY" clause
     */
    static String prepareSortSql(List<GridColConf> customColumns) {
        TreeMap<Integer, String> sortIndMap = new TreeMap<Integer, String>();
        String sortSql = "";

        for (GridColConf customColumn : customColumns) {
            if (customColumn.getSort() != null
                && !customColumn.getSort().isEmpty()) {
                    String sortValue =
                        customColumn.getGridColMp().getDataIndex() + " "
                        + customColumn.getSort() + " ";
                Integer key =
                    customColumn.getSortIndex() != null
                    ? customColumn.getSortIndex() : -1;
                String value = sortIndMap.get(key);
                value = value != null ? value + ", "  + sortValue : sortValue;
                sortIndMap.put(key, value);
            }
        }

        if (sortIndMap.size() > 0) {
            NavigableMap <Integer, String> orderedSorts =
                sortIndMap.tailMap(0, true);
            String unorderedSorts = sortIndMap.get(-1);

            sortSql = String.join(", ", orderedSorts.values());
            if (unorderedSorts != null && !unorderedSorts.isEmpty()) {
                if (!sortSql.isEmpty()) {
                    sortSql += ", ";
                }
                sortSql += unorderedSorts;
            }
            sortSql = " ORDER BY " + sortSql;
        }

        return sortSql;
    }

    /**
     * Generate map of parameter names and values to be interpolated into
     * the queries WHERE clause.
     *
     * The result is stored as this.filterValues.
     */
    private void prepareFilters() throws IllegalArgumentException {
        //A pattern for finding multiselect date filter values
        Pattern multiselectPattern = Pattern.compile("[0-9]*,[0-9]*");

        //Map containing all filters and filter values
        this.filterValues = new MultivaluedHashMap<String, Object>();

        for (GridColConf customColumn : this.customColumns) {
            if (customColumn.getIsFilterActive()
                && customColumn.getFilterVal() != null
                && !customColumn.getFilterVal().isEmpty()
                && !customColumn.getIsFilterIsNull()
            ) {

                Filter filter = customColumn.getGridColMp().getFilter();
                String filterValue = customColumn.getFilterVal();
                String currentFilterParam = filter.getParam();
                String filterType = filter.getFilterType().getType();

                //Check if filter is generic and replace param and value param
                if (GENERICTEXT_FILTER_TYPE.equals(filterType)) {
                    String genTextValue = filter.getParam() + "Value";
                    currentFilterParam =
                        genTextValue + customColumn.getGridColMpId();
                }

                // If a tag filter is applied, split param into n
                // numbered params for n tags to filter
                if (TAG_FILTER_TYPE.equals(filterType)) {
                    String[] tagIds = filterValue.split(",");
                    int tagNumber = tagIds.length;
                    String param = filter.getParam();
                    for (int i = 0; i < tagNumber; i++) {
                        String tag =
                            repository.getByIdPlain(
                                Tag.class,
                                Integer.parseInt(tagIds[i])
                            ).getName();
                        this.filterValues.add(param + i, tag);
                    }
                    continue;
                }

                //Check if Filter is an in filter
                if (GENERICTEXT_FILTER_TYPE.equals(filterType)
                    || TEXT_FILTER_TYPE.equals(filterType)
                ) {
                    if (!customColumn.getIsFilterRegex()) {
                        filterValue += "%";
                        filterValue = translateToRegex(filterValue);
                    }
                    try {
                        Pattern.compile(filterValue);
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException(
                            String.format("illegal regular expression: '%s'", filterValue));
                    }
                }

                if (!filter.getFilterType().getIsMultiselect()) {
                    if (filter.getFilterType().getType().equals("number")) {
                        String[] params = filter.getParam().split(",");
                        String[] values = filterValue.split(",", -1);
                        if (values.length != 2) {
                            throw new IllegalArgumentException(
                                String.format("illegal number filter parameter: %s", filterValue));
                        } else{
                            double from =
                                values[0].equals("")
                                ? 0 : Double.valueOf(values[0]);
                            if (Double.isNaN(from)) {
                                throw new IllegalArgumentException("\"von\" Parameter kein Zahlenwert");
                            }
                            double to =
                                values[1].equals("")
                                ? Double.MAX_VALUE : Double.valueOf(values[1]);
                            if (Double.isNaN(to)) {
                                throw new IllegalArgumentException("\"bis\" Parameter kein Zahlenwert");
                            }
                            //Add parameters and values to filter map
                            this.filterValues.add(params[0], from);
                            this.filterValues.add(params[1], to);
                        }
                    } else {
                        this.filterValues.add(currentFilterParam, filterValue);
                    }
                } else {
                    //If filter is a multiselect date filter
                    if (filter.getFilterType().getType()
                            .equals("listdatetime")
                    ) {
                        // Get parameters as comma separated values,
                        // expected to be in milliseconds
                        String[] params = filter.getParam().split(",");
                        Matcher matcher =
                            multiselectPattern.matcher(filterValue);
                        if (matcher.find()) {
                            String[] values = matcher.group(0).split(",", -1);
                            //Get filter values and convert to seconds
                            long from = values[0].equals("")
                                ? 0 : Long.valueOf(values[0]) / 1000;
                            long to = values[1].equals("")
                                ? Integer.MAX_VALUE
                                : Long.valueOf(values[1]) / 1000;
                            //Add parameters and values to filter map
                            this.filterValues.add(
                                params[0], String.valueOf(from));
                            this.filterValues.add(
                                params[1], String.valueOf(to));
                        }
                    } else {
                        //else add all filtervalues to the same parameter name
                        String[] multiselect = filterValue.split(",");
                        if (filter.getFilterType().getType()
                                .equals("listnumber")
                        ) {
                            for (Object value : multiselect) {
                                Integer vNumber =
                                    Integer.valueOf(value.toString());
                                this.filterValues.add(
                                    filter.getParam(), vNumber);
                            }
                        } else {
                            for (String value : multiselect) {
                                this.filterValues.add(
                                    filter.getParam(), value);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Create query from given SQL and set parameters from this.filterValues.
     */
    private Query prepareQuery(String queryString) {
        Query query = repository.queryFromString(queryString);
        Set<String> keys = this.filterValues.keySet();
        for (String key : keys) {
            List<Object> values = new ArrayList<>();
            for (Object value: this.filterValues.get(key)) {
                values.add(value);
            }
            query.setParameter(key, values);
        }
        return query;
    }

    private String translateToRegex(String value) {
        value = value.replaceAll("/\\*", ".*");
        value = value.replaceAll("/\\?", ".");
        value = value.replaceAll("%", ".*");
        value = value.replaceAll("_", ".");
        value = "^" + value + "$";
        return value;
    }
}
