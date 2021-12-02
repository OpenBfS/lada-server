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
import javax.persistence.Query;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import de.intevation.lada.model.stammdaten.Messgroesse;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.rest.LadaService;

/**
 * REST service for Messgroesse objects.
 * <p>
 * The services produce data in the application/json media type.
 * A typical response holds information about the action performed and the data.
 * <pre>
 * <code>
 * {
 *  "success": [boolean];
 *  "message": [string],
 *  "data":[{
 *      "id":[number],
 *      "beschreibung": [string],
 *      "defaultFarbe": [string],
 *      "eudfNuklidId": [number],
 *      "idfNuklidKey": [string],
 *      "istLeitnuklid": [boolean],
 *      "kennungBvl": [string],
 *      "messgroesse": [string]
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
@Path("rest/messgroesse")
public class MessgroesseService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    /**
     * Get all Messgroesse objects.
     * <p>
     * Example: http://example.com/messgroesse
     *
     * @return Response object containing all Messgroesse objects.
     */
    @GET
    @Path("/")
    public Response get(
        @Context HttpHeaders headers,
        @Context UriInfo info
    ) {
        MultivaluedMap<String, String> params = info.getQueryParameters();
        if (params.isEmpty() || !params.containsKey("mmtId")) {
            return repository.getAll(Messgroesse.class);
        }
        String mmtId = params.getFirst("mmtId");

        Query query =
            repository.queryFromString(
                "SELECT messgroesse_id FROM "
                + de.intevation.lada.model.stammdaten.SchemaName.NAME
                + ".mmt_messgroesse "
                + "WHERE mmt_id = :mmt"
            ).setParameter("mmt", mmtId);
        @SuppressWarnings("unchecked")
        List<Integer> ids = query.getResultList();
        QueryBuilder<Messgroesse> builder2 =
            repository.queryBuilder(Messgroesse.class);
        builder2.orIntList("id", ids);
        return repository.filter(builder2.getQuery());
    }

    /**
     * Get a single Messgroesse object by id.
     * <p>
     * The id is appended to the URL as a path parameter.
     * <p>
     * Example: http://example.com/messgroesse/{id}
     *
     * @return Response object containing a single Messgroesse.
     */
    @GET
    @Path("/{id}")
    public Response getById(
        @Context HttpHeaders headers,
        @PathParam("id") String id
    ) {
        return repository.getById(Messgroesse.class, Integer.valueOf(id));
    }
}
