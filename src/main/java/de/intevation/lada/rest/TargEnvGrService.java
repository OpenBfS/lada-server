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

import de.intevation.lada.util.data.Repository;
import de.intevation.lada.model.master.TargEnvGr;

/**
 * REST service for TargEnvGr objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("targenvgr")
public class TargEnvGrService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

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
