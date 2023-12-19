/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import de.intevation.lada.util.data.KdaUtil;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.model.master.SpatRefSys;

/**
 * REST service for SpatRefSys objects.
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("spatrefsys")
public class SpatRefSysService extends LadaService {

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
     * Get all SpatRefSys objects.
     * @return Response object containing all SpatRefSys objects.
     */
    @GET
    public Response get() {
        return repository.getAll(SpatRefSys.class);
    }

    /**
     * Get a single SpatRefSys object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object containing a single SpatRefSys.
     */
    @GET
    @Path("{id}")
    public Response getById(
        @PathParam("id") Integer id
    ) {
        return repository.getById(SpatRefSys.class, id);
    }

    @POST
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
