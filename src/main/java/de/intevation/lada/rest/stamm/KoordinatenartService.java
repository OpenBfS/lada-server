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
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import de.intevation.lada.model.stammdaten.KoordinatenArt;
import de.intevation.lada.util.data.KdaUtil;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
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
    private Repository repository;

    /**
     * Expected format for the payload in POST request to recalculate().
     */
    private static class PostData {
        int from;
        int to;
        String x;
        String y;

        // Setters needed for JSON deserialization:
        void setFrom(int from) {
            this.from = from;
        }

        void setTo(int to) {
            this.to = to;
        }

        void setX(String x) {
            this.x = x;
        }

        void setY(String y) {
            this.y = y;
        }
    }

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
        return repository.getAll(KoordinatenArt.class);
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
        return repository.getById(KoordinatenArt.class, Integer.valueOf(id));
    }

    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response recalculate(
        @Context HttpHeaders headers,
        PostData object
    ) {
        KdaUtil.Result result = new KdaUtil().transform(
            object.from, object.to, object.x, object.y);
        if (result == null) {
            return new Response(false, StatusCodes.GEO_NOT_MATCHING, null);
        }
        return new Response(true, StatusCodes.OK, result);
    }
}
