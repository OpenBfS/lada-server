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
import javax.json.JsonObject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import com.fasterxml.jackson.databind.node.ObjectNode;

import de.intevation.lada.model.stammdaten.KoordinatenArt;
import de.intevation.lada.util.annotation.RepositoryConfig;
import de.intevation.lada.util.data.KdaUtil;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.RepositoryType;
import de.intevation.lada.util.data.Strings;
import de.intevation.lada.util.rest.Response;

/**
 * REST service for KoordinatenArt objects.
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
 *      "idfGeoKey": [string],
 *      "koordinatenArt": [string]
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
@Path("rest/koordinatenart")
@RequestScoped
public class KoordinatenartService {

    /**
     * The data repository granting read access.
     */
    @Inject
    @RepositoryConfig(type = RepositoryType.RO)
    private Repository defaultRepo;

    /**
     * Get all KoordinatenArt objects.
     * <p>
     * Example: http://example.com/koordinatenart
     *
     * @return Response object containing all KoordinatenArt objects.
     */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
        @Context HttpHeaders headers,
        @Context UriInfo info
    ) {
        return defaultRepo.getAll(KoordinatenArt.class, Strings.STAMM);
    }

    /**
     * Get a single KoordinatenArt object by id.
     * <p>
     * The id is appended to the URL as a path parameter.
     * <p>
     * Example: http://example.com/koordinatenart/{id}
     *
     * @return Response object containing a single KoordinatenArt.
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(
        @Context HttpHeaders headers,
        @PathParam("id") String id
    ) {
        return defaultRepo.getById(
            KoordinatenArt.class,
            Integer.valueOf(id),
            Strings.STAMM);
    }

    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response recalculate(
        @Context HttpHeaders headers,
        JsonObject object
    ) {
        int kdaFrom = object.getInt("from");
        int kdaTo = object.getInt("to");
        String x = object.getString("x");
        String y = object.getString("y");
        KdaUtil transformer = new KdaUtil();
        ObjectNode result = transformer.transform(kdaFrom, kdaTo, x, y);
        if (result == null) {
            return new Response(false, 652, null);
        }
        return new Response(true, 200, result.toString());
    }
}
