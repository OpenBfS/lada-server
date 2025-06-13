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

import de.intevation.lada.model.master.Regulation;

/**
 * REST service for Regulation objects.
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "regulation")
public class RegulationService extends LadaIntegerIdEntityService {

    /**
     * Get all Regulation objects.
     * @return all Regulation objects.
     */
    @GET
    public List<Regulation> get() {
        return repository.getAll(Regulation.class);
    }

    /**
     * Get a single Regulation object by id.
     *
     * @return a single Regulation.
     */
    @GET
    @Path("{id}")
    public Regulation getById() {
        return repository.getById(Regulation.class, id);
    }
}
