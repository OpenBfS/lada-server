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

import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.model.master.State;
import de.intevation.lada.rest.LadaService;

/**
 * REST service for Staat objects.
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
 *      "eu": [string],
 *      "hklId": [number],
 *      "koordXExtern": [string],
 *      "koordYExtern": [string],
 *      "staat": [string],
 *      "staatIso": [string],
 *      "staatKurz": [string],
 *      "koordinatenartId": [number]
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
@Path("rest/staat")
public class StaatService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    /**
     * Get all Staat objects.
     * <p>
     * Example: http://example.com/staat
     *
     * @return Response object containing all Staat objects.
     */
    @GET
    @Path("/")
    public Response get() {
        return repository.getAll(State.class);
    }

    /**
     * Get a single Staat object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object containing a single Staat.
     */
    @GET
    @Path("/{id}")
    public Response getById(
        @PathParam("id") Integer id
    ) {
        return repository.getById(State.class, id);
    }
}
