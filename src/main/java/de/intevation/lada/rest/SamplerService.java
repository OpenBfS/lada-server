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
import jakarta.validation.Valid;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import de.intevation.lada.model.master.Sampler;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;

/**
 * REST service for Sampler objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("sampler")
public class SamplerService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    @Inject
    @AuthorizationConfig(type = AuthorizationType.HEADER)
    private Authorization authorization;

    /**
     * Get all Sampler objects.
     *
     * @return all objects.
     */
    @GET
    public List<Sampler> get() {
        return authorization.filter(
            repository.getAll(Sampler.class), Sampler.class);
    }

    /**
     * Get a single Sampler object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return a single object.
     */
    @GET
    @Path("{id}")
    public Sampler getById(
        @PathParam("id") Integer id
    ) {
        return authorization.filter(
            repository.getById(Sampler.class, id), Sampler.class);
    }

    @POST
    public Sampler create(
        @Valid Sampler probenehmer
    ) {
        authorization.authorize(
            probenehmer,
            RequestMethod.POST,
            Sampler.class);
        return repository.create(probenehmer);
    }

    @PUT
    @Path("{id}")
    public Sampler update(
        @PathParam("id") Integer id,
        @Valid Sampler probenehmer
    ) {
        authorization.authorize(
            probenehmer,
            RequestMethod.PUT,
            Sampler.class);
        return repository.update(probenehmer);
    }

    @DELETE
    @Path("{id}")
    public void delete(
        @PathParam("id") Integer id
    ) {
        Sampler probenehmer = repository.getById(
            Sampler.class, id);
        authorization.authorize(
            probenehmer,
            RequestMethod.DELETE,
            Sampler.class);
        repository.delete(probenehmer);
    }
}
