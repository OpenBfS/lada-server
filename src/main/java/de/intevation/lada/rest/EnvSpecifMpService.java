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

import de.intevation.lada.model.master.EnvSpecifMp;


/**
 * REST service for EnvSpecifMp objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "envspecifmp")
public class EnvSpecifMpService extends LadaIntegerIdEntityService {

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
     * @return EnvSpecifMp object
     */
    @GET
    @Path("{id}")
    public EnvSpecifMp getById() {
        return repository.getById(EnvSpecifMp.class, id);
    }
}
