/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import de.intevation.lada.util.auth.UserInfo;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.model.master.Network;

/**
 * REST service for Network objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "network")
public class NetworkService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    /**
     * Get all Network objects.
     *
     * @return all NetzBetreiber objects.
     */
    @GET
    public List<Network> get() {
        return repository.getAll(Network.class);
    }

    /**
     * Get a single Network object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return a single Network.
     */
    @GET
    @Path("{id}")
    public Network getById(
        @PathParam("id") String id
    ) {
        UserInfo userInfo = authorization.getInfo();
        if (userInfo.getNetzbetreiber().contains(id)) {
            return repository.getById(Network.class, id);
        }
        // TODO: Move to authorization
        throw new ForbiddenException();
    }
}
