/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.util.List;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.model.master.MunicDiv;


/**
 * REST service for MunicDiv objects.
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "municdiv")
public class MunicDivService
    extends LadaIntegerIdEntityEditingService<MunicDiv> {

    /**
     * Get all MunicDiv objects.
     *
     * @return requested objects.
     */
    @GET
    public List<MunicDiv> get() {
        return repository.getAll(MunicDiv.class);
    }

    /**
     * Get a single object by id.
     *
     * @return a single object.
     */
    @GET
    @Path("{id}")
    public MunicDiv getById() {
        return repository.getById(MunicDiv.class, id);
    }

    @DELETE
    @Path("{id}")
    public void delete() {
        MunicDiv gemUntergliederung = repository.getById(
            MunicDiv.class, id);
        authorization.authorize(gemUntergliederung, RequestMethod.DELETE);
        repository.delete(gemUntergliederung);
    }
}
