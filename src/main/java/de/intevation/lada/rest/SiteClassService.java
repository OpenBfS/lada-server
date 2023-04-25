/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.model.master.SiteClass;

/**
 * REST service for SiteClass objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("siteclass")
public class SiteClassService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    /**
     * Get all SiteClass objects.
     *
     * @return Response object containing all SiteClass objects.
     */
    @GET
    public Response get() {
        return repository.getAll(SiteClass.class);
    }

    /**
     * Get a single SiteClass object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object containing a single SiteClass.
     */
    @GET
    @Path("{id}")
    public Response getById(
        @PathParam("id") Integer id
    ) {
        return repository.getById(SiteClass.class, id);
    }
}
