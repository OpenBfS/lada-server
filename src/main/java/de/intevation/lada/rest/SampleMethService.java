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
import de.intevation.lada.model.master.SampleMeth;

/**
 * REST service for SampleMeth objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("samplemeth")
public class SampleMethService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    /**
     * Get all SampleMeth objects.
     *
     * @return Response object containing all SampleMeth objects.
     */
    @GET
    public Response get() {
        return repository.getAll(SampleMeth.class);
    }

    /**
     * Get a single SampleMeth object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object containing a single SampleMeth.
     */
    @GET
    @Path("{id}")
    public Response getById(
        @PathParam("id") Integer id
    ) {
        return repository.getById(SampleMeth.class, id);
    }
}
