/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest.stamm;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import de.intevation.lada.model.stammdaten.PflichtMessgroesse;
import de.intevation.lada.util.annotation.RepositoryConfig;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.RepositoryType;
import de.intevation.lada.util.data.Strings;
import de.intevation.lada.util.rest.Response;

/**
 * REST service for PflichtMessgroesse objects.
 * <p>
 * The services produce data in the application/json media type.
 * A typical response holds information about the action performed and the data.
 * <pre>
 * <code>
 * {
 *  "success": [boolean];
 *  "message": [string],
 *  "data":[{
 *      "id": [number],
 *      "messgroesseId": [number],
 *      "datenbasisId": [number],
 *      "mmtId": [string],
 *      "umweltId": [string]
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
@Path("rest/pflichtmessgroesse")
@RequestScoped
public class PflichtmessgroesseService {

    /**
     * The data repository granting read access.
     */
    @Inject
    @RepositoryConfig(type = RepositoryType.RO)
    private Repository defaultRepo;

    /**
     * Get all PflichtMessgroesse objects.
     * <p>
     * Example: http://example.com/pflichtmessgroesse
     *
     * @return Response object containing all PflichtMessgroesse objects.
     */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
        @Context HttpHeaders headers,
        @Context UriInfo info
    ) {
        return defaultRepo.getAll(PflichtMessgroesse.class, Strings.STAMM);
    }

    /**
     * Get a single PflichtMessgroesse object by id.
     * <p>
     * The id is appended to the URL as a path parameter.
     * <p>
     * Example: http://example.com/pflichtmessgroesse/{id}
     *
     * @return Response object containing a single PflichtMessgroesse.
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(
        @Context HttpHeaders headers,
        @PathParam("id") String id
    ) {
        QueryBuilder<PflichtMessgroesse> builder =
            new QueryBuilder<PflichtMessgroesse>(
                defaultRepo.entityManager(Strings.STAMM),
                PflichtMessgroesse.class
            );
        builder.and("messMethodeId", id);
        List<PflichtMessgroesse> result =
            defaultRepo.filterPlain(builder.getQuery(), Strings.STAMM);
        if (!result.isEmpty()) {
            return new Response(true, 200, result.get(0));
        }
        return new Response(false, 600, null);
    }
}
