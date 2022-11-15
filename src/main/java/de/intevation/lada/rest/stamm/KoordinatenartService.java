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
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import de.intevation.lada.model.stammdaten.SpatRefSys;
import de.intevation.lada.util.data.KdaUtil;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.rest.LadaService;

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
public class KoordinatenartService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    /**
     * Expected format for the payload in POST request to recalculate().
     */
    public static class PostData {
        private int from;
        private int to;
        private String x;
        private String y;

        public void setFrom(int from) {
            this.from = from;
        }

        public void setTo(int to) {
            this.to = to;
        }

        public void setX(String x) {
            this.x = x;
        }

        public void setY(String y) {
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
    public Response get() {
        return repository.getAll(SpatRefSys.class);
    }

    /**
     * Get a single KoordinatenArt object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object containing a single KoordinatenArt.
     */
    @GET
    @Path("/{id}")
    public Response getById(
        @PathParam("id") Integer id
    ) {
        return repository.getById(SpatRefSys.class, id);
    }

    @POST
    @Path("/")
    public Response recalculate(
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
