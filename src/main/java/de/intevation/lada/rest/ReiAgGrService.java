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
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.model.master.ReiAgGr;
import de.intevation.lada.model.master.ReiAgGrEnvMediumMp;
import de.intevation.lada.model.master.ReiAgGrMp;

/**
 * REST service for ReiAgGr objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("reiaggr")
public class ReiAgGrService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    /**
     * Get ReiAgGr objects.
     *
     * @param reiAgId URL parameter "reiAgId" to filter
     * using reiAgId
     * @param envMediumId URL parameter to filter using envMediumId. Might be null
     * (i.e. not given at all) but not an empty string.
     * @return all ReiAgGr objects.
     */
    @GET
    public List<ReiAgGr> get(
        @QueryParam("reiAgId") Integer reiAgId,
        @QueryParam("envMediumId") @Pattern(regexp = ".+") String envMediumId
    ) {
        if (reiAgId == null && envMediumId == null) {
            return repository.getAll(ReiAgGr.class);
        }
        List<ReiAgGr> list = new ArrayList<ReiAgGr>();
        if (reiAgId != null) {
            QueryBuilder<ReiAgGrMp> builder =
                repository.queryBuilder(ReiAgGrMp.class);
            builder.and("reiAgId", reiAgId);
            List<ReiAgGrMp> zuord =
                repository.filter(builder.getQuery());
            if (zuord.isEmpty()) {
                return null;
            }
            QueryBuilder<ReiAgGr> builder1 =
                repository.queryBuilder(ReiAgGr.class);
            List<Integer> ids = new ArrayList<Integer>();
            for (int i = 0; i < zuord.size(); i++) {
                ids.add(zuord.get(i).getReiAgGrId());
            }
            builder1.orIn("id", ids);
            list = repository.filter(builder1.getQuery());
        } else if (envMediumId != null) {
            QueryBuilder<ReiAgGrEnvMediumMp> builder =
                repository.queryBuilder(ReiAgGrEnvMediumMp.class);
            builder.and("envMediumId", envMediumId);
            List<ReiAgGrEnvMediumMp> zuord =
                repository.filter(builder.getQuery());
            if (zuord.isEmpty()) {
                return null;
            }
            QueryBuilder<ReiAgGr> builder1 =
                repository.queryBuilder(ReiAgGr.class);
            List<Integer> ids = new ArrayList<Integer>();
            for (int i = 0; i < zuord.size(); i++) {
                ids.add(zuord.get(i).getReiAgGrId());
            }
            builder1.orIn("id", ids);
            list = repository.filter(builder1.getQuery());
        }

        return list;
    }

    /**
     * Get a single ReiAgGr object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return a single ReiAgGr.
     */
    @GET
    @Path("{id}")
    public ReiAgGr getById(
        @PathParam("id") Integer id
    ) {
        return repository.getById(ReiAgGr.class, id);
    }
}
