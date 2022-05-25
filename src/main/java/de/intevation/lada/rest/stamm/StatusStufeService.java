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

import de.intevation.lada.model.stammdaten.StatusStufe;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.rest.LadaService;

/**
 * REST service for StatusStufe objects.
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
 *      "stufe": [string],
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
@Path("rest/statusstufe")
public class StatusStufeService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    /**
     * Get all StatusStufe objects.
     * <p>
     * Example: http://example.com/statustufe
     *
     * @return Response object containing all StatusStufe objects.
     */
    @GET
    @Path("/")
    public Response get(
        @Context UriInfo info
    ) {
        return repository.getAll(StatusStufe.class);
    }

    /**
     * Get a single StatusStufe object by id.
     * <p>
     * The id is appended to the URL as a path parameter.
     * <p>
     * Example: http://example.com/statusstufe/{id}
     *
     * @return Response object containing a single StatusStufe.
     */
    @GET
    @Path("/{id}")
    public Response getById(
        @PathParam("id") String id
    ) {
        return repository.getById(StatusStufe.class, Integer.valueOf(id));
    }
}
