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

import de.intevation.lada.model.master.Mmt;


/**
 * REST service for Mmt objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "mmt")
public class MmtService extends LadaStringIdEntityService {

    /**
     * Get all Mmt objects.
     *
     * @return all Mmt objects.
     */
    @GET
    public List<Mmt> get() {
        return repository.getAll(Mmt.class);
    }

    /**
     * Get a single Mmt object by id.
     *
     * @return a single Mmt.
     */
    @GET
    @Path("{id}")
    public Mmt getById() {
        return repository.getById(Mmt.class, id);
    }
}
