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

import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.model.master.MeasFacil;

/**
 * REST service for MeasFacil objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("measfacil")
public class MeasFacilService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    /**
     * The authorization module.
     */
    @Inject
    @AuthorizationConfig(type = AuthorizationType.HEADER)
    private Authorization authorization;

    /**
     * Get all MeasFacil objects.
     *
     * @return Response object containing all MeasFacil objects.
     */
    @GET
    public Response get() {
        return repository.getAll(MeasFacil.class);
    }

    /**
     * Get a single MeasFacil object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object containing a single MeasFacil.
     */
    @GET
    @Path("{id}")
    public Response getById(
        @PathParam("id") String id
    ) {
        return repository.getById(MeasFacil.class, id);
    }
}
