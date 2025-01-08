/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response.Status;
import jakarta.validation.constraints.NotEmpty;

import de.intevation.lada.model.master.GridColConf;
import de.intevation.lada.query.QueryTools;
import de.intevation.lada.util.data.Repository;


/**
 * REST service to get the sql statement for a query.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "sql")
public class SqlService extends LadaService {

    /**
     * The data repository granting read/write access.
     */
    @Inject
    private Repository repository;

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
     * @throws BadRequestException
     */
    @POST
    public String execute(
        @NotEmpty List<GridColConf> gridColumnValues
    ) {
        try {
            QueryTools queryTools =
                new QueryTools(repository, gridColumnValues);
            return prepareStatement(
                queryTools.getSql(), queryTools.getFilterValues());
        } catch (IllegalArgumentException iae) {
            throw new BadRequestException(
                jakarta.ws.rs.core.Response
                .status(Status.BAD_REQUEST)
                .entity(iae.getMessage()).build());
        }
    }

    private String prepareStatement(
        String sql,
        Map<String, List<Object>> filters
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
