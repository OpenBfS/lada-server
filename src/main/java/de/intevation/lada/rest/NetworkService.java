/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.util.ArrayList;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.auth.UserInfo;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.model.master.Network;

/**
 * REST service for Network objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("network")
public class NetworkService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    /**
     * The authorization module.
     */
    @Inject
    @AuthorizationConfig(type = AuthorizationType.HEADER)
    private Authorization authorization;

    /**
     * Get all Network objects.
     *
     * @return Response object containing all NetzBetreiber objects.
     */
    @GET
    public Response get() {
        return repository.getAll(Network.class);
    }

    /**
     * Get a single Network object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object containing a single Network.
     */
    @GET
    @Path("{id}")
    public Response getById(
        @PathParam("id") String id
    ) {
        UserInfo userInfo = authorization.getInfo();
        if (userInfo.getNetzbetreiber().contains(id)) {
            return repository.getById(Network.class, id);
        }
        return new Response(
            false, StatusCodes.CHANGED_VALUE, new ArrayList<Network>());
    }
}
