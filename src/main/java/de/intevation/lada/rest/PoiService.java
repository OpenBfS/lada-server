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

import de.intevation.lada.model.master.Poi;


/**
 * REST service for Poi objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "poi")
public class PoiService extends LadaStringIdEntityService {

    /**
     * Get all Poi objects.
     *
     * @return all Poi objects.
     */
    @GET
    public List<Poi> get() {
        return repository.getAll(Poi.class);
    }

    /**
     * Get a single Poi object by id.
     *
     * @return a single Poi.
     */
    @GET
    @Path("{id}")
    public Poi getById() {
        return repository.getById(Poi.class, id);
    }
}
