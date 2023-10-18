/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import de.intevation.lada.model.master.StatusMp;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.Response;

/**
 * REST service for StatusMp objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("statusmp")
public class StatusMpService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    @Inject
    @AuthorizationConfig(type = AuthorizationType.HEADER)
    private Authorization authorization;

    /**
     * Get all StatusMp objects.
     *
     * @return Response object containing all StatusMp objects.
     */
    @GET
    public Response get() {
        return repository.getAll(StatusMp.class);
    }

    /**
     * Get a single StatusMp object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object
     */
    @GET
    @Path("{id}")
    public Response getById(
        @PathParam("id") Integer id
    ) {
        return repository.getById(StatusMp.class, id);
    }

    /**
     * Get the union of status mappings that can be set on given measms.
     *
     * @param measmIds IDs of measms for which status mappings are requested
     * @return Status mappings that can be set on given measms
     */
    @POST
    @Path("getbyids")
    public Response getById(
        List<Integer> measmIds
    ) {
        return new Response(
            true,
            StatusCodes.OK,
            repository.entityManager().createNativeQuery(
                "SELECT * FROM master.status_mp "
                + "WHERE id IN(SELECT to_id FROM master.status_ord_mp "
                + "  JOIN lada.measm ON from_id = status "
                + "  WHERE measm.id IN(:measmIds))",
                StatusMp.class)
            .setParameter("measmIds", measmIds)
            .getResultList());
    }
}
