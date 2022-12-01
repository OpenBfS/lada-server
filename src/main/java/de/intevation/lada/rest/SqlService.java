/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.MultivaluedHashMap;

import de.intevation.lada.model.QueryColumns;
import de.intevation.lada.model.master.GridColConf;
import de.intevation.lada.query.QueryTools;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.Response;

/**
 * REST service to get the sql statement for a query.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("sql")
public class SqlService extends LadaService {

    /**
     * The data repository granting read/write access.
     */
    @Inject
    private Repository repository;

    /**
     * The header authorization module.
     */
    @Inject
    @AuthorizationConfig(type = AuthorizationType.HEADER)
    private Authorization authorization;

    /**
     * Return SQL as would be executed for the given query.
     * The request can contain the following post data:
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
     * @return JSON object with query string as data element
     */
    @POST
    public Response execute(
        QueryColumns columns
    ) {
        // There is nothing to authorize and it is ensured
        // that a user is authenticated.
        List<GridColConf> gridColumnValues = columns.getColumns();

        if (gridColumnValues == null
            || gridColumnValues.isEmpty()) {
            //TODO Error code if no columns are given
            return new Response(false, StatusCodes.NOT_EXISTING, null);
        }

        try {
            QueryTools queryTools = new QueryTools(repository, gridColumnValues);
            String sql = queryTools.getSql();
            if (sql == null) {
                return new Response(true, StatusCodes.OK, null);
            }
            String statement =
                prepareStatement(sql, queryTools.getFilterValues());
            return new Response(true, StatusCodes.OK, statement);
        } catch (IllegalArgumentException iae) {
            Response r = new Response(false, StatusCodes.SQL_INVALID_FILTER, null);
            MultivaluedMap<String, Integer> error =
                new MultivaluedHashMap<String, Integer>();
            error.add(iae.getMessage(), StatusCodes.SQL_INVALID_FILTER);
            r.setErrors(error);
            return r;
        }
    }

    private String prepareStatement(
        String sql,
        MultivaluedMap<String, Object> filters
    ) {
        String parameters = "";
        Set<String> filterKeys = filters.keySet();
        if (!filterKeys.isEmpty()) {
            parameters += "(";

            // Counter for total number of resulting parameters
            int nparams = 1;

            for (String key : filterKeys) {
                if (nparams > 1) {
                    // separate next parameter in list
                    parameters += ", ";
                }

                // Possibly a list of scalar values for 'IN (...)'
                List<Object> values = filters.get(key);
                int j = 1;
                String paramNumbers = "";
                for (Object value: values) {
                    if (j > 1) {
                        // separate next parameter in list
                        parameters += ", ";
                        paramNumbers += ", ";
                    }
                    if (value instanceof String) {
                        parameters += "'" + value.toString() + "'";
                    } else {
                        parameters += value.toString();
                    }
                    paramNumbers += "$" + nparams;
                    j++;
                    nparams++;
                }

                // Replace named parameter with parameter numbers
                sql = sql.replace(":" + key, paramNumbers);

            }
            parameters += ")";
        }

        return "PREPARE request AS \n"
            + sql + ";\n"
            + "EXECUTE request" + parameters + ";\n"
            + "DEALLOCATE request;";
    }
}
