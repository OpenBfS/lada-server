/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import de.intevation.lada.model.master.TypeRegulation;


/**
 * REST service for TypeRegulation objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "typeregulation")
public class TypeRegulationService extends LadaStringIdEntityService {

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
     * @return a single TypeRegulation.
     */
    @GET
    @Path("{id}")
    public TypeRegulation getById() {
        return repository.getById(TypeRegulation.class, id);
    }
}
