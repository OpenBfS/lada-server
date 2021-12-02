/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest.stamm;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import de.intevation.lada.model.stammdaten.Verwaltungseinheit;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.rest.LadaService;

/**
 * REST service for Verwaltungseinheit  objects.
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
 *      "bezeichnung": [string],
 *      "bundesland": [string],
 *      "isBundesland": [string],
 *      "isGemeinde": [string],
 *      "isLandkreis": [string],
 *      "isRegbezirk": [string],
 *      "koordXExtern": [string],
 *      "koordYExtern": [string],
 *      "kreis": [string],
 *      "latitude": [number],
 *      "longitude": [number],
 *      "nuts": [string],
 *      "plz": [string],
 *      "regbezirk": [string],
 *      "koordinatenartId":[number]
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
@Path("rest/verwaltungseinheit")
public class VerwaltungseinheitService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    /**
     * Get all Verwaltungseinheit objects.
     * <p>
     * The result list can be filtered using the URL parameter 'query'. A filter
     * is defined as the first letters of the 'bezeichnung'
     * <p>
     * Example: http://example.com/verwaltungseinheit?query=[string]
     *
     * @return Response object containing all Verwaltungseinheit objects.
     */
    @GET
    @Path("/")
    public Response get(
        @Context HttpHeaders headers,
        @Context UriInfo info
    ) {
        MultivaluedMap<String, String> params = info.getQueryParameters();
        if (params.isEmpty() || !params.containsKey("query")) {
            return repository.getAll(Verwaltungseinheit.class);
        }
        String filter = params.getFirst("query");
        QueryBuilder<Verwaltungseinheit> builder =
            repository.queryBuilder(Verwaltungseinheit.class);
        builder.andLike("bezeichnung", filter + "%");
        return repository.filter(builder.getQuery());
    }

    /**
     * Get a single Verwaltungseinheit object by id.
     * <p>
     * The id is appended to the URL as a path parameter.
     * <p>
     * Example: http://example.com/verwaltungseinheit/{id}
     *
     * @return Response object containing a single Verwaltungseinheit.
     */
    @GET
    @Path("/{id}")
    public Response getById(
        @Context HttpHeaders headers,
        @PathParam("id") String id
    ) {
        return repository.getById(Verwaltungseinheit.class, id);
    }
}
