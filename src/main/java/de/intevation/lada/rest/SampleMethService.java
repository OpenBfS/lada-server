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
import de.intevation.lada.model.master.SampleMeth;

/**
 * REST service for SampleMeth objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "samplemeth")
public class SampleMethService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    /**
     * Get all SampleMeth objects.
     *
     * @return all SampleMeth objects.
     */
    @GET
    public List<SampleMeth> get() {
        return repository.getAll(SampleMeth.class);
    }

    /**
     * Get a single SampleMeth object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return a single SampleMeth.
     */
    @GET
    @Path("{id}")
    public SampleMeth getById(
        @PathParam("id") Integer id
    ) {
        return repository.getById(SampleMeth.class, id);
    }
}
