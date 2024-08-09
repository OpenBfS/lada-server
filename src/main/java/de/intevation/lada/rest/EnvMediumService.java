/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.model.master.EnvMedium;
import de.intevation.lada.model.master.EnvMedium_;
import de.intevation.lada.model.master.ReiAgGrEnvMediumMp;
import de.intevation.lada.model.master.ReiAgGrEnvMediumMp_;

/**
 * REST service for EnvMedium objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "envmedium")
public class EnvMediumService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    /**
     * Get Umwelt objects.
     *
     * @param reiAgGrId URL parameter "reiAgGrId" to filter
     * using reiAgGrId.
     * @return requested objects.
     */
    @GET
    public List<EnvMedium> get(
        @QueryParam("reiAgGrId") Integer reiAgGrId
    ) {
        if (reiAgGrId == null) {
            return repository.getAll(EnvMedium.class);
        }
        QueryBuilder<ReiAgGrEnvMediumMp> builder =
            repository.queryBuilder(ReiAgGrEnvMediumMp.class);
        builder.and(ReiAgGrEnvMediumMp_.reiAgGrId, reiAgGrId);
        List<ReiAgGrEnvMediumMp> zuord =
            repository.filter(builder.getQuery());
        if (zuord.isEmpty()) {
            return null;
        }
        QueryBuilder<EnvMedium> builder1 =
            repository.queryBuilder(EnvMedium.class);
        List<String> ids = new ArrayList<String>();
        for (int i = 0; i < zuord.size(); i++) {
            ids.add(zuord.get(i).getEnvMediumId());
        }
        builder1.orIn(EnvMedium_.id, ids);
        return repository.filter(builder1.getQuery());
    }

    /**
     * Get a single EnvMedium object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return a single EnvMedium.
     */
    @GET
    @Path("{id}")
    public EnvMedium getById(
        @PathParam("id") String id
    ) {
        return repository.getById(EnvMedium.class, id);
    }
}
