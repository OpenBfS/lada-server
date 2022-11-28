/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest.stamm;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.model.master.Auth;
import de.intevation.lada.rest.LadaService;

/**
 * REST service for MessStellenKomi objects.
 * <p>
 * The services produce data in the application/json media type.
 * A typical response holds information about the action performed and the data.
 * <pre>
 * <code>
 * {
 *  "success": [boolean];
 *  "message": [string],
 *  "data":[{
 *      "netzbetreiberId": [string],
 *      "mstId": [string],
 *      "laborMstId": [string],
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
@Path("rest/auth")
public class AuthService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    /**
     * @param networkIds Multiple URL parameters "networkId"
     * can be given to filter the result.
     *
     * @return Response containing requested objects.
     */
    @GET
    @Path("/")
    public Response get(
        @QueryParam("networkId") List<String> networkIds
    ) {
        QueryBuilder<Auth> mstMlQuery = repository.queryBuilder(Auth.class);
        mstMlQuery.orIntList("authFunctId", List.of(0, 1));

        if (networkIds != null && !networkIds.isEmpty()) {
            mstMlQuery.andIn("networkId", networkIds);
        }

        return repository.filter(mstMlQuery.getQuery());
   }
}
