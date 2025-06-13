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

import de.intevation.lada.model.master.StatusLev;

/**
 * REST service for StatusLev objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "statuslev")
public class StatusLevService extends LadaIntegerIdEntityService {

    /**
     * Get all StatusLev objects.
     *
     * @return all StatusLev objects.
     */
    @GET
    public List<StatusLev> get() {
        return repository.getAll(StatusLev.class);
    }

    /**
     * Get a single StatusLev object by id.
     *
     * @return a single StatusLev.
     */
    @GET
    @Path("{id}")
    public StatusLev getById() {
        return repository.getById(StatusLev.class, id);
    }
}
