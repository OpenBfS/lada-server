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
import de.intevation.lada.model.master.TypeRegulation;

/**
 * REST service for TypeRegulation objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "typeregulation")
public class TypeRegulationService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    /**
     * Get all TypeRegulation objects.
     *
     * @return all TypeRegulation objects.
     */
    @GET
    public List<TypeRegulation> get() {
        return repository.getAll(TypeRegulation.class);
    }

    /**
     * Get a single TypeRegulation object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return a single TypeRegulation.
     */
    @GET
    @Path("{id}")
    public TypeRegulation getById(
        @PathParam("id") String id
    ) {
        return repository.getById(TypeRegulation.class, id);
    }
}
