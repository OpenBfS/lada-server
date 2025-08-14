/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.util.List;

import org.jboss.logging.Logger;

import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;

import de.intevation.lada.model.master.Sampler;
import de.intevation.lada.util.rest.RequestMethod;

/**
 * REST service for Sampler objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "sampler")
public class SamplerService extends LadaIntegerIdEntityService {

    private static final Logger LOG = Logger.getLogger(SamplerService.class);

    /**
     * Get all Sampler objects.
     *
     * @return all objects.
     */
    @GET
    public List<Sampler> get() {
        LOG.debug("Start fetching samplers");
        List<Sampler> result = repository.getAll(Sampler.class);
        LOG.debug("Finished fetching samplers");
        return result;
    }

    /**
     * Get a single Sampler object by id.
     *
     * @return a single object.
     */
    @GET
    @Path("{id}")
    public Sampler getById() {
        return repository.getById(Sampler.class, id);
    }

    /**
     * Create a sampler
     * @param probenehmer Sampler to create
     * @return Created sampler
     * @throws BadRequestException if any constraint violations are detected.
     */
    @POST
    public Sampler create(
        @Valid Sampler probenehmer
    ) throws BadRequestException {
        return repository.create(probenehmer);
    }

    /**
     * Update a sampler
     *
     * @param probenehmer Sampler to update
     * @return Updated sampler
     * @throws BadRequestException if any constraint violations are detected.
     */
    @PUT
    @Path("{id}")
    public Sampler update(
        @Valid Sampler probenehmer
    ) throws BadRequestException {
        return repository.update(probenehmer);
    }

    @DELETE
    @Path("{id}")
    public void delete() {
        Sampler probenehmer = repository.getById(
            Sampler.class, id);
        authorization.authorize(probenehmer, RequestMethod.DELETE);
        repository.delete(probenehmer);
    }
}
