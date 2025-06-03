/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import de.intevation.lada.model.master.State;


/**
 * REST service for State objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "state")
public class StateService extends LadaIntegerIdEntityService {

    /**
     * Get all State objects.
     *
     * @return all State objects.
     */
    @GET
    public List<State> get() {
        return repository.getAll(State.class);
    }

    /**
     * Get a single State object by id.
     *
     * @return a single State.
     */
    @GET
    @Path("{id}")
    public State getById() {
        return repository.getById(State.class, id);
    }
}
