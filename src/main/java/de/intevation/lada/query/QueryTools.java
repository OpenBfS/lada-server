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

import org.jboss.logging.Logger;

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

    static final String GENERICID_FILTER_TYPE = "genericid";
    static final String GENERICTEXT_FILTER_TYPE = "generictext";
    static final String TAG_FILTER_TYPE = "tag";
    static final String TEXT_FILTER_TYPE = "text";

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
            prepareFilters(customColumns);
        List<GridColumn> columns = new ArrayList<GridColumn>();
        for (GridColumnValue customColumn : customColumns) {
            columns.add(customColumn.getGridColumn());
        }
        return execQuery(sql, filterValues, columns);

    }

    /**
     * Complement SQL statement from base query with filter and sort settings.
     *
     * @param customColumns List<GridColumnValue> with filter and sort settings.
     * @param qId Database ID of the base query.
     * @return The completed query string.
     */
    public String prepareSql(List<GridColumnValue> customColumns, Integer qId) {
        BaseQuery query = repository.getByIdPlain(BaseQuery.class, qId);

        return prepareSql(customColumns, query.getSql());
    }

    /**
     * Generate SQL statement from base query and filter and sort settings.
     *
     * Static method for unit testing.
     */
    static String prepareSql(List<GridColumnValue> customColumns, String sql) {
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
            if (customColumn.getFilterActive()
                && customColumn.getFilterValue() != null
                && !customColumn.getFilterValue().isEmpty()
                && !customColumn.getFilterIsNull()
            ) {
                Filter filter = customColumn.getGridColumn().getFilter();
                String filterValue = customColumn.getFilterValue();
                String currentFilterString = filter.getSql();
                String currentFilterParam = filter.getParameter();
                String filterType = filter.getFilterType().getType();
                if (GENERICTEXT_FILTER_TYPE.equals(filterType)
                    || GENERICID_FILTER_TYPE.equals(filterType)
                ) {
                    subquery = true;
                    generic = true;
                }
                if (GENERICTEXT_FILTER_TYPE.equals(filterType)) {
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
                } else if (TAG_FILTER_TYPE.equals(filterType)) {
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
                if (customColumn.getFilterNegate()) {
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
            } else if (customColumn.getFilterActive()
                       && customColumn.getFilterIsNull()
            ) {
                Filter filter = customColumn.getGridColumn().getFilter();
                String currentFilterString = filter.getSql();
                String filterType = filter.getFilterType().getType();
                if (GENERICTEXT_FILTER_TYPE.equals(filterType)) {
                    currentFilterString =
                        customColumn.getGridColumn().getDataIndex()
                        + " IS NULL";
                    if (customColumn.getFilterNegate()) {
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
                    if (customColumn.getFilterNegate()) {
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

        // Append (possibly empty) WHERE and ORDER BY clause
        sql += filterSql + sortSql;

        //TODO Avoid using subqueries to use aliases in the where clause
        //Append generic and/or tag filter sql seperated from other filters
        if (subquery) {
            sql = "SELECT * FROM (" + sql + ") AS inner_query";
            sql += genericFilterSql;
        }
        return sql;
    }

    /**
     * Generate map of parameter names and values to be interpolated into
     * the queries WHERE clause.
     *
     * @param customColumns List of GridColumnValues containing filter settings
     * @return The generated map
     */
    public MultivaluedMap<String, Object> prepareFilters(
        List<GridColumnValue> customColumns
    ) {
        //A pattern for finding multiselect date filter values
        Pattern multiselectPattern = Pattern.compile("[0-9]*,[0-9]*");
        Pattern multiselectNumberPattern = Pattern.compile("[0-9.]*,[0-9.]*");

        //Map containing all filters and filter values
        MultivaluedMap<String, Object> filterValues =
            new MultivaluedHashMap<String, Object>();

        for (GridColumnValue customColumn : customColumns) {
            if (customColumn.getFilterActive()
                && customColumn.getFilterValue() != null
                && !customColumn.getFilterValue().isEmpty()
                && !customColumn.getFilterIsNull()
            ) {

                Filter filter = customColumn.getGridColumn().getFilter();
                String filterValue = customColumn.getFilterValue();
                String currentFilterParam = filter.getParameter();
                String filterType = filter.getFilterType().getType();

                //Check if filter is generic and replace param and value param
                if (GENERICTEXT_FILTER_TYPE.equals(filterType)) {
                    String genTextValue = filter.getParameter() + "Value";
                    currentFilterParam =
                        genTextValue + customColumn.getGridColumnId();
                }

                // If a tag filter is applied, split param into n
                // numbered params for n tags to filter
                if (TAG_FILTER_TYPE.equals(filterType)) {
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
                if (GENERICTEXT_FILTER_TYPE.equals(filterType)
                    || TEXT_FILTER_TYPE.equals(filterType)
                ) {
                    if (!customColumn.getFilterRegex()) {
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
        List result,
        List<GridColumn> names
    ) {
        List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
        for (Object row: result) {
            Map<String, Object> set = new HashMap<String, Object>();
            for (int i = 0; i < names.size(); i++) {
                set.put(
                    names.get(i).getDataIndex(),
                    row instanceof Object[]
                        ? ((Object[]) row)[names.get(i).getPosition() - 1]
                        : row);
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
