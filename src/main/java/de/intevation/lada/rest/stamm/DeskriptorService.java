/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest.stamm;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import de.intevation.lada.model.stammdaten.Deskriptoren;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.rest.LadaService;

/**
 * REST service for Probe objects.
 * <p>
 * The services produce data in the application/json media type.
 * All HTTP methods use the authorization module to determine if the user is
 * allowed to perform the requested action.
 * A typical response holds information about the action performed and the data.
 * <pre>
 * <code>
 * {
 *  "success": [boolean];
 *  "message": [string],
 *  "data":[{
 *      "id": [number],
 *      "bedeutung": [string],
 *      "beschreibung": [string],
 *      "ebene": [number],
 *      "sn": [number],
 *      "vorgaenger": [number],
 *      "sxx": [number]
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
@Path("rest/deskriptor")
public class DeskriptorService extends LadaService {

    /**
     * The data repository granting read/write access.
     */
    @Inject
    private Repository repository;

    /**
     * Get Deskriptor objects.
     *
     * The requested objects can be filtered using the following URL
     * parameters:
     * @param layer The layer of the reqested deskriptor
     * @param parents The parents of the requested deskriptor
     * <br>
     * The response data contains a stripped set of deskriptor objects.
     * <p>
     * Example:
     * http://example.com/deskriptor?layer=[LAYER]
     *
     * @return Response object containing the Deskriptor objects.
     */
    @GET
    @Path("/")
    public Response get(
        @QueryParam("layer") Integer layer,
        @QueryParam("parents") String parents
    ) {
        if (layer == null) {
            return repository.getAll(Deskriptoren.class);
        }
        QueryBuilder<Deskriptoren> builder =
            repository.queryBuilder(Deskriptoren.class);
        builder.and("sn", 0).not();
        builder.and("ebene", layer);
        if (parents != null) {
            String[] parentArray = parents.split(", ");
            List<String> parentList = Arrays.asList(parentArray);
            builder.andIn("vorgaenger", parentList);
        }
        return repository.filter(builder.getQuery());
    }

    /**
     * Get a single Deskriptor object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object containing a single Deskriptor.
     */
    @GET
    @Path("/{id}")
    public Response getById(
        @PathParam("id") Integer id
    ) {
        return repository.getById(Deskriptoren.class, id);
    }
}
