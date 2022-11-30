/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.model.master.State;

/**
 * REST service for State objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("/state")
public class StateService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    /**
     * Get all State objects.
     *
     * @return Response object containing all State objects.
     */
    @GET
    @Path("/")
    public Response get() {
        return repository.getAll(State.class);
    }

    /**
     * Get a single State object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object containing a single State.
     */
    @GET
    @Path("/{id}")
    public Response getById(
        @PathParam("id") Integer id
    ) {
        return repository.getById(State.class, id);
    }
}
