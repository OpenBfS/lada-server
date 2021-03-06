/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.log4j.Logger;

import de.intevation.lada.model.stammdaten.Filter;
import de.intevation.lada.model.stammdaten.GridColumn;
import de.intevation.lada.model.stammdaten.GridColumnValue;
import de.intevation.lada.model.stammdaten.BaseQuery;
import de.intevation.lada.model.stammdaten.Tag;
import de.intevation.lada.util.data.Repository;


/**
 * Utility class to handle the SQL query configuration.
 *
 * @author <a href = "mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class QueryTools {

    @Inject
    private Repository repository;

    @Inject
    private Logger logger;

    /**
     * Execute query and return the filtered and sorted results.
     * @param customColumns Customized column configs, containing
     *      filter, sorting and references to the respective column.
     * @param qId Query id.
     * @return List of result maps.
     */
    public List<Map<String, Object>> getResultForQuery(
        List<GridColumnValue> customColumns,
        Integer qId
    ) {
        String sql = prepareSql(customColumns, qId);
        MultivaluedMap<String, Object> filterValues =
            prepareFilters(customColumns, qId);
        List<GridColumn> columns = new ArrayList<GridColumn>();
        for (GridColumnValue customColumn : customColumns) {
            columns.add(customColumn.getGridColumn());
        }
        return execQuery(sql, filterValues, columns);

    }

    public String prepareSql(List<GridColumnValue> customColumns, Integer qId) {
        BaseQuery query = repository.getByIdPlain(BaseQuery.class, qId);

        String sql = query.getSql();
        String filterSql = "";
        String genericFilterSql = "";
        String sortSql = "";
        boolean subquery = false;
        TreeMap<Integer, String> sortIndMap = new TreeMap<Integer, String>();

        for (GridColumnValue customColumn : customColumns) {
            if (customColumn.getSort() != null
                && !customColumn.getSort().isEmpty()) {
                    String sortValue =
                        customColumn.getGridColumn().getDataIndex() + " "
                        + customColumn.getSort() + " ";
                Integer key =
                    customColumn.getSortIndex() != null
                    ? customColumn.getSortIndex() : -1;
                String value = sortIndMap.get(key);
                value = value != null ? value + ", "  + sortValue : sortValue;
                sortIndMap.put(key, value);
            }

            boolean generic = false;
            if (customColumn.getFilterActive() != null
                && customColumn.getFilterActive()
                && customColumn.getFilterValue() != null
                && !customColumn.getFilterValue().isEmpty()
                && customColumn.getFilterIsNull() != null
                && !customColumn.getFilterIsNull()
            ) {
                Filter filter = customColumn.getGridColumn().getFilter();
                String filterValue = customColumn.getFilterValue();
                String currentFilterString = filter.getSql();
                String currentFilterParam = filter.getParameter();
                String filterType = filter.getFilterType().getType();
                if (filterType.equals("generictext")
                    || filterType.equals("genericid")
                ) {
                    subquery = true;
                    generic = true;
                }
                if (filterType.equals("generictext")) {
                    String genTextParam = ":" + filter.getParameter() + "Param";
                    String genTextValue = filter.getParameter() + "Value";
                    currentFilterString =
                        currentFilterString.replace(
                            genTextParam,
                            customColumn.getGridColumn().getDataIndex());
                    currentFilterParam =
                        genTextValue + customColumn.getGridColumnId();
                    currentFilterString =
                        currentFilterString.replace(
                            ":" + genTextValue, ":" + currentFilterParam);
                } else if (filterType.equals("tag")) {
                    String[] tagIds = filterValue.split(",");
                    int tagNumber = tagIds.length;
                    String paramlist = "";
                    String param = filter.getParameter();
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
                            ":" + filter.getParameter(), paramlist);
                    if (filterSql.isEmpty()) {
                        filterSql += " WHERE " + tagFilterSql;
                    } else {
                        filterSql += " AND " + tagFilterSql;
                    }
                    continue;
                }
                if (customColumn.getFilterNegate() != null
                     && customColumn.getFilterNegate()) {
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
            } else if (customColumn.getFilterActive() != null
                       && customColumn.getFilterActive() == true
                       && customColumn.getFilterIsNull() != null
                       && customColumn.getFilterIsNull() == true
            ) {
                Filter filter = customColumn.getGridColumn().getFilter();
                String currentFilterString = filter.getSql();
                String filterType = filter.getFilterType().getType();
                if (filterType.equals("generictext")) {
                    currentFilterString =
                            customColumn.getGridColumn().getDataIndex() + " IS NULL";
                    if (customColumn.getFilterNegate() != null
                        && customColumn.getFilterNegate()) {
                        currentFilterString = "NOT(" + currentFilterString + ")";
                    }
                    if (genericFilterSql.isEmpty()) {
                        genericFilterSql += " WHERE " + currentFilterString;
                    } else {
                        genericFilterSql += " AND " + currentFilterString;
                    }
                    subquery = true;
                    generic = true;
                 } else {
                    currentFilterString = currentFilterString.replaceAll(" .*", " IS NULL ");
                    currentFilterString = currentFilterString.replaceAll(".*\\(", "");
                    if (customColumn.getFilterNegate() != null
                        && customColumn.getFilterNegate()) {
                        currentFilterString = "NOT(" + currentFilterString + ")";
                    }
                    if (filterSql.isEmpty()) {
                        filterSql += " WHERE " + currentFilterString;
                    } else {
                        filterSql += " AND " + currentFilterString;
                    }
                }
            }
        }

        if (sortIndMap.size() > 0) {
            NavigableMap <Integer, String> orderedSorts =
                sortIndMap.tailMap(0, true);
            String unorderedSorts = sortIndMap.get(-1);
            sortSql += "";
            for (String sortString : orderedSorts.values()) {
                if (sortSql.isEmpty()) {
                    sortSql += " ORDER BY " + sortString;
                } else {
                    sortSql += ", " + sortString;
                }
            }
            if (unorderedSorts != null && !unorderedSorts.isEmpty()) {
                if (sortSql.isEmpty()) {
                    sortSql += " ORDER BY " + unorderedSorts;
                } else {
                    sortSql += ", " + unorderedSorts;
                }
            }

        }


        if (!filterSql.isEmpty()) {
            sql += filterSql + " ";
        }
        sql += sortSql;
        //TODO Avoid using subqueries to use aliases in the where clause
        //Append generic and/or tag filter sql seperated from other filters
        if (subquery) {
            sql = "SELECT * FROM ( " + sql + " ) AS inner_query ";
            sql += genericFilterSql;
        }
        sql += " ;";
        return sql;
    }

    public MultivaluedMap<String, Object> prepareFilters(
        List<GridColumnValue> customColumns,
        Integer qId
    ) {
        //A pattern for finding multiselect date filter values
        Pattern multiselectPattern = Pattern.compile("[0-9]*,[0-9]*");
        Pattern multiselectNumberPattern = Pattern.compile("[0-9.]*,[0-9.]*");

        //Map containing all filters and filter values
        MultivaluedMap<String, Object> filterValues =
            new MultivaluedHashMap<String, Object>();

        for (GridColumnValue customColumn : customColumns) {
            if (customColumn.getFilterActive() != null
                && customColumn.getFilterActive()
                && customColumn.getFilterValue() != null
                && !customColumn.getFilterValue().isEmpty()
                && customColumn.getFilterIsNull() != null
                && !customColumn.getFilterIsNull()
            ) {

                Filter filter = customColumn.getGridColumn().getFilter();
                String filterValue = customColumn.getFilterValue();
                String currentFilterParam = filter.getParameter();
                String filterType = filter.getFilterType().getType();

                //Check if filter is generic and replace param and value param
                if (filterType.equals("generictext")) {
                    String genTextValue = filter.getParameter() + "Value";
                    currentFilterParam =
                        genTextValue + customColumn.getGridColumnId();
                }

                // If a tag filter is applied, split param into n
                // numbered params for n tags to filter
                if (filterType.equals("tag")) {
                    String[] tagIds = filterValue.split(",");
                    int tagNumber = tagIds.length;
                    String param = filter.getParameter();
                    for (int i = 0; i < tagNumber; i++) {
                        String tag =
                            repository.getByIdPlain(
                                Tag.class,
                                Integer.parseInt(tagIds[i])
                            ).getTag();
                        filterValues.add(param + i, tag);
                    }
                    continue;
                }

                //Check if Filter is an in filter
                if (filterType.equals("generictext")
                    || filterType.equals("text")
                ) {
                    if (customColumn.getFilterRegex() != null
                        && !customColumn.getFilterRegex()
                    ) {
                        filterValue += "%";
                        filterValue = translateToRegex(filterValue);
                    }
                    try {
                        Pattern.compile(filterValue);
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                }

                if (!filter.getFilterType().getMultiselect()) {
                    if (filter.getFilterType().getType().equals("number")) {
                        String[] params = filter.getParameter().split(",");
                        Matcher matcher =
                            multiselectNumberPattern.matcher(filterValue);
                        if (matcher.find()) {
                            String[] values = matcher.group(0).split(",", -1);
                            double from =
                                values[0].equals("")
                                ? 0 : Double.valueOf(values[0]);
                            double to =
                                values[1].equals("")
                                ? Double.MAX_VALUE : Double.valueOf(values[1]);
                            //Add parameters and values to filter map
                            filterValues.add(params[0], from);
                            filterValues.add(params[1], to);
                        }
                    } else {
                        filterValues.add(currentFilterParam, filterValue);
                    }
                } else {
                    //If filter is a multiselect date filter
                    if (filter.getFilterType().getType()
                            .equals("listdatetime")
                    ) {
                        // Get parameters as comma separated values,
                        // expected to be in milliseconds
                        String[] params = filter.getParameter().split(",");
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
                            filterValues.add(params[0], String.valueOf(from));
                            filterValues.add(params[1], String.valueOf(to));
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
                                filterValues.add(
                                    filter.getParameter(), vNumber);
                            }
                        } else {
                            for (String value : multiselect) {
                                filterValues.add(filter.getParameter(), value);
                            }
                        }
                    }
                }
            }
        }

        return filterValues;
    }

    private List<Map<String, Object>> execQuery(
        String sql,
        MultivaluedMap<String, Object> filterValues,
        List<GridColumn> columns
    ) {
        try {
            javax.persistence.Query q = prepareQuery(sql, filterValues);
            if (q == null) {
                return new ArrayList<>();
            }
            return prepareResult(q.getResultList(), columns);
        } catch (Exception e) {
            logger.debug("Exception", e);
            logger.debug("SQL: \n" + sql);
            logger.debug("filterValues:  " + filterValues);
            throw e;
        }
    }

    /**
     * Creates a query from a given sql and inserts the given parameters.
     * @param sql The query sql string
     * @param params A map containing parameter names and values
     * @param manager Entity manager
     * @return The query
     */
    public javax.persistence.Query prepareQuery(
        String sql,
        MultivaluedMap<String, Object> params
    ) {
        javax.persistence.Query query = repository.queryFromString(sql);
        Set<String> keys = params.keySet();
        for (String key : keys) {
            List<Object> values = new ArrayList<>();
            for (Object value: params.get(key)) {
                values.add(value);
            }
            query.setParameter(key, values);
        }
        return query;

    }

    /**
     * Prepares the query result for the client.
     * @param result A list of query results
     * @param names The columns queried by the client
     * @return List of result maps, containing only the configured columns
     */
    public List<Map<String, Object>> prepareResult(
        List<Object[]> result,
        List<GridColumn> names
    ) {
        if (result.size() == 0) {
            return null;
        }
        List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
        for (Object[] row: result) {
            Map<String, Object> set = new HashMap<String, Object>();
            for (int i = 0; i < names.size(); i++) {
                set.put(
                    names.get(i).getDataIndex(),
                    row[names.get(i).getPosition() - 1]);
            }
            ret.add(set);
        }
        return ret;
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
