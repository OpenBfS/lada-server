/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest.stamm;

import java.util.Arrays;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.jboss.logging.Logger;

import de.intevation.lada.model.stammdaten.Auth;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.Response;
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
@Path("rest/messstellenkombi")
public class MessstellenkombiService extends LadaService {

    @Inject Logger logger;

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    /**
     * Get all MessStellenKombi objects.
     * <p>
     * The requested objects can be filtered using a URL parameter named
     * netzbetreiberId.
     * <p>
      * Example: http://example.com/messstelle
     *
     * @return Response object containing all MessStelle objects.
     */
    @GET
    @Path("/")
    public Response get(
        @Context UriInfo info
    ) {
        MultivaluedMap<String, String> params = info.getQueryParameters();

        QueryBuilder<Auth> mstMlQuery = repository.queryBuilder(Auth.class);
        mstMlQuery.orIntList("funktionId", Arrays.asList(0, 1));

        if (params.containsKey("netzbetreiberId")) {
            mstMlQuery.andIn(
                "netzbetreiberId",
                Arrays.asList(params.getFirst("netzbetreiberId").split(",")));
        }

        return repository.filter(mstMlQuery.getQuery());
   }
}
