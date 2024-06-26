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
import de.intevation.lada.model.master.EnvSpecifMp;


/**
 * REST service for EnvSpecifMp objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "envspecifmp")
public class EnvSpecifMpService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    /**
     * Get all EnvSpecifMp objects.
     *
     * @return requested objects.
     */
    @GET
    public List<EnvSpecifMp> get() {
        return repository.getAll(EnvSpecifMp.class);
    }

    /**
     * Get a single EnvSpecifMp object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return EnvSpecifMp object
     */
    @GET
    @Path("{id}")
    public EnvSpecifMp getById(
        @PathParam("id") Integer id
    ) {
        return repository.getById(EnvSpecifMp.class, id);
    }
}
