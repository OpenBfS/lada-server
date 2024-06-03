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
import de.intevation.lada.model.master.Regulation;

/**
 * REST service for Regulation objects.
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "regulation")
public class RegulationService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

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
     * @param id The id is appended to the URL as a path parameter.
     * @return a single Regulation.
     */
    @GET
    @Path("{id}")
    public Regulation getById(
        @PathParam("id") Integer id
    ) {
        return repository.getById(Regulation.class, id);
    }
}
