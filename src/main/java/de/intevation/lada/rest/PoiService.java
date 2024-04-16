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
import de.intevation.lada.model.master.Poi;

/**
 * REST service for Poi objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("poi")
public class PoiService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

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
     * @param id The id is appended to the URL as a path parameter.
     * @return a single Poi.
     */
    @GET
    @Path("{id}")
    public Poi getById(
        @PathParam("id") String id
    ) {
        return repository.getById(Poi.class, id);
    }
}
