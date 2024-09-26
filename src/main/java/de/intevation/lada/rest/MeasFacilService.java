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
import de.intevation.lada.model.master.MeasFacil;

/**
 * REST service for MeasFacil objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "measfacil")
public class MeasFacilService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    /**
     * Get all MeasFacil objects.
     *
     * @return all MeasFacil objects.
     */
    @GET
    public List<MeasFacil> get() {
        return repository.getAll(MeasFacil.class);
    }

    /**
     * Get a single MeasFacil object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return a single MeasFacil.
     */
    @GET
    @Path("{id}")
    public MeasFacil getById(
        @PathParam("id") String id
    ) {
        return repository.getById(MeasFacil.class, id);
    }
}
