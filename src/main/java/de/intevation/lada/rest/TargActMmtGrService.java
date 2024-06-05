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
import de.intevation.lada.model.master.TargActMmtGr;

/**
 * REST service for TargActMmtGr objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "targactmmtgr")
public class TargActMmtGrService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    /**
     * Get all TargActMmtGr objects.
     *
     * @return all TargActMmtGr objects.
     */
    @GET
    public List<TargActMmtGr> get() {
        return repository.getAll(TargActMmtGr.class);
    }
}
