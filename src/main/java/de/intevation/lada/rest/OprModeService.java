/* Copyright (C) 2026 by Bundesamt fuer Strahlenschutz
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

import de.intevation.lada.model.master.OprMode;

/**
 * REST service for OprMode objects.
 */
@Path(LadaService.PATH_REST + "oprmode")
public class OprModeService extends LadaIntegerIdEntityService {

    /**
     * Get all OprMode objects.
     * @return all OprMode objects.
     */
    @GET
    public List<OprMode> get() {
        return repository.getAll(OprMode.class);
    }

    /**
     * Get a single OprMode object by id.
     *
     * @return a single OprMode.
     */
    @GET
    @Path("{id}")
    public OprMode getById() {
        return repository.getById(OprMode.class, id);
    }
}
