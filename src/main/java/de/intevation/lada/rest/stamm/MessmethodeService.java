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
import javax.ws.rs.core.UriInfo;

import de.intevation.lada.model.stammdaten.MessMethode;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.rest.LadaService;

/**
 * REST service for MessMethode objects.
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
 *      "beschreibung": [string],
 *      "messmethode": [string]
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
@Path("rest/messmethode")
public class MessmethodeService extends LadaService {

    /**
     * The data repository granting read/write access.
     */
    @Inject
    private Repository repository;

    /**
     * Get all MessMethode objects.
     * <p>
     * Example: http://example.com/messmethode
     *
     * @return Response object containing all MessMethode objects.
     */
    @GET
    @Path("/")
    public Response get(
        @Context UriInfo info
    ) {
        return repository.getAll(MessMethode.class);
    }

    /**
     * Get a single MessMethode object by id.
     * <p>
     * The id is appended to the URL as a path parameter.
     * <p>
     * Example: http://example.com/messmethode/{id}
     *
     * @return Response object containing a single MessMethode.
     */
    @GET
    @Path("/{id}")
    public Response getById(
        @PathParam("id") String id
    ) {
        return repository.getById(MessMethode.class, id);
    }
}
