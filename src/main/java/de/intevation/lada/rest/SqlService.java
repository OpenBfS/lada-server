/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import de.intevation.lada.model.QueryColumns;
import de.intevation.lada.model.stammdaten.GridColumn;
import de.intevation.lada.model.stammdaten.GridColumnValue;
import de.intevation.lada.model.stammdaten.ResultType;
import de.intevation.lada.query.QueryTools;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.annotation.RepositoryConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.RepositoryType;
import de.intevation.lada.util.data.Strings;
import de.intevation.lada.util.rest.Response;

/**
 * REST service to get the sql statement for a query.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("rest/sql")
@RequestScoped
public class SqlService {

    /**
     * The data repository granting read/write access.
     */
    @Inject
    @RepositoryConfig(type = RepositoryType.RW)
    private Repository repository;

    /**
     * The header authorization module.
     */
    @Inject
    @AuthorizationConfig(type = AuthorizationType.HEADER)
    private Authorization authorization;

    @Inject
    private QueryTools queryTools;


    /**
     * Execute query, using the given result columns.
     * The query can contain the following post data:
     * <pre>
     * <code>
     * {
     *   columns[{
     *     gridColumnId: [number],
     *     sort: [string],
     *     sortIndex: [number],
     *     filterValue: [string],
     *     filterActive: [boolean],
     *   }]
     * }
     * </code>
     * </pre>
     * @return JSON encoded query results
     */
    @POST
    @Path("/")
    @Consumes("application/json")
    @Produces("application/json")
    public Response execute(
        @Context HttpServletRequest request,
        @Context UriInfo info,
        QueryColumns columns
    ) {
        // There is nothing to authorize and it is ensured
        // that a user is authenticated.
        Integer qid;
        List<GridColumnValue> gridColumnValues = columns.getColumns();

        if (gridColumnValues == null
            || gridColumnValues.isEmpty()) {
            //TODO Error code if no columns are given
            return new Response(false, 999, null);
        }
        for (GridColumnValue columnValue : gridColumnValues) {
            GridColumn gridColumn = repository.getByIdPlain(
                GridColumn.class,
                Integer.valueOf(columnValue.getGridColumnId()),
                Strings.STAMM);
            columnValue.setGridColumn(gridColumn);
        }

        GridColumn gridColumn = repository.getByIdPlain(
            GridColumn.class,
            Integer.valueOf(gridColumnValues.get(0).getGridColumnId()),
        Strings.STAMM);

        qid = gridColumn.getBaseQuery();
        String sql =
            queryTools.prepareSql(columns.getColumns(), qid);
        if (sql == null) {
            return new Response(true, 200, null);
        }
        MultivaluedMap<String, Object> filterValues =
            queryTools.prepareFilters(columns.getColumns(), qid);

        String statement = prepareStatement(sql, filterValues);
        return new Response(true, 200, statement);
    }

    private String prepareStatement(
        String sql,
        MultivaluedMap<String, Object> filters
    ) {
        String stmt1 = "PREPARE request(text) AS ";
        String stmt2 = "EXECUTE request(";
        String stmt3 = "); DEALLOCATE request;";

        sql = sql.replaceAll("\n", " ");

        Set<String> filterKeys = filters.keySet();
        int i = 1;
        for (String key : filterKeys) {
            List<Object> v = filters.get(key);
            if (v.size() == 1) {
                if (v.get(0) instanceof String) {
                    stmt2 += "'" + v.get(0).toString() + "'";
                } else {
                    stmt2 += v.get(0).toString();
                }
            } else {
                stmt2 += "'{";
                for (Object value: filters.get(key)) {
                    stmt2 += value.toString();
                    stmt2 += ",";
                }
                stmt2 = stmt2.substring(0, stmt2.length() - 1);
                stmt2 += "}'";
            }
            stmt2 += ",";
            sql = sql.replace(":" + key, "$" + i);
            i++;
        }
        stmt2 = stmt2.substring(0, stmt2.length() - 1);

        return stmt1 + sql + stmt2 + stmt3;
    }
}