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

import de.intevation.lada.model.master.MeasFacil;

/**
 * REST service for MeasFacil objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "measfacil")
public class MeasFacilService extends LadaStringIdEntityService {

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
     * @return a single MeasFacil.
     */
    @GET
    @Path("{id}")
    public MeasFacil getById() {
        return repository.getById(MeasFacil.class, id);
    }
}
