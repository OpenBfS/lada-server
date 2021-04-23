/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest.stamm;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import de.intevation.lada.model.stammdaten.GridColumn;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.rest.Response;

/**
 * REST-Service for preconfigured columns.
 * <p>
 * The services produce data in the application/json media type.
 * A typical response holds information about the action performed and the data.
 * <pre>
 * <code>
 * {
 *  "success": [boolean];
 *  "message": [string],
 *  "data":[{
 *      "id": [string],
 *      "name": [string],
 *      "base_query": [number],
 *      "data_index": [string],
 *      "position": [number],
 *      "filter": [object],
 *      "data_type": [object]
 *  }],
 *  "errors": [object],
 *  "warnings": [object],
 *  "readonly": [boolean],
 *  "totalCount": [number]
 * }
 * </code>
 * </pre>
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("rest/column")
@RequestScoped
public class ColumnService {

    @Inject
    private Repository repository;

    @Inject
    @AuthorizationConfig(type = AuthorizationType.HEADER)
    private Authorization authorization;

    /**
     * Request all predefined grid_column objects connected to the given query.
     * @return All GridColumn objects referencing the given query.
     */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getQueries(
        @Context HttpServletRequest request,
        @QueryParam("qid") Integer qid
    ) {
        //If no qid is given, return all grid_column objects
        if (qid == null) {
            return repository.getAll(GridColumn.class);
        }

        QueryBuilder<GridColumn> builder =
            repository.queryBuilder(GridColumn.class);
        builder.and("baseQuery", qid);

        return repository.filter(builder.getQuery());
    }
}
