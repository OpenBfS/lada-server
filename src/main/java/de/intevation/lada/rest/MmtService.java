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
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import de.intevation.lada.util.data.Repository;
import de.intevation.lada.model.master.Mmt;

/**
 * REST service for Mmt objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "mmt")
public class MmtService extends LadaService {

    /**
     * The data repository granting read/write access.
     */
    @Inject
    private Repository repository;

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
     * @param id The id is appended to the URL as a path parameter.
     * @return a single Mmt.
     */
    @GET
    @Path("{id}")
    public Mmt getById(
        @PathParam("id") String id
    ) {
        return repository.getById(Mmt.class, id);
    }
}
