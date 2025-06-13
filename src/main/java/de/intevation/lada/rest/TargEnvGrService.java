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

import de.intevation.lada.model.master.TargEnvGr;


/**
 * REST service for TargEnvGr objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "targenvgr")
public class TargEnvGrService extends LadaIntegerIdEntityService {

    /**
     * Get all TargEnvGr objects.
     *
     * @return all TargEnvGr objects.
     */
    @GET
    public List<TargEnvGr> get() {
        return repository.getAll(TargEnvGr.class);
    }
}
