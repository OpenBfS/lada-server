/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.util.List;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import de.intevation.lada.model.master.Sampler;
import de.intevation.lada.util.rest.RequestMethod;

/**
 * REST service for Sampler objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "sampler")
public class SamplerService extends LadaIntegerIdEntityEditingService<Sampler> {

    /**
     * Get all Sampler objects.
     *
     * @return all objects.
     */
    @GET
    public List<Sampler> get() {
        return repository.getAll(Sampler.class);
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

    @DELETE
    @Path("{id}")
    public void delete() {
        Sampler probenehmer = repository.getById(
            Sampler.class, id);
        authorization.authorize(probenehmer, RequestMethod.DELETE);
        repository.delete(probenehmer);
    }
}
