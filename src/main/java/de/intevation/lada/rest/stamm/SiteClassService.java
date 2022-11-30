/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest.stamm;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.model.master.SiteClass;
import de.intevation.lada.rest.LadaService;

/**
 * REST service for SiteClass objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("/siteclass")
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
    @Path("/")
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
    @Path("/{id}")
    public Response getById(
        @PathParam("id") Integer id
    ) {
        return repository.getById(SiteClass.class, id);
    }
}
