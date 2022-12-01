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

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.model.master.EnvMedium;
import de.intevation.lada.model.master.ReiAgGrEnvMediumMp;

/**
 * REST service for EnvMedium objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("envmedium")
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
     * @return Response containing requested objects.
     */
    @GET
    public Response get(
        @QueryParam("reiAgGrId") Integer reiAgGrId
    ) {
        if (reiAgGrId == null) {
            return repository.getAll(EnvMedium.class);
        }
        QueryBuilder<ReiAgGrEnvMediumMp> builder =
            repository.queryBuilder(ReiAgGrEnvMediumMp.class);
        builder.and("reiAgGrId", reiAgGrId);
        List<ReiAgGrEnvMediumMp> zuord =
            repository.filterPlain(builder.getQuery());
        if (zuord.isEmpty()) {
            return new Response(true, StatusCodes.OK, null);
        }
        QueryBuilder<EnvMedium> builder1 =
            repository.queryBuilder(EnvMedium.class);
        List<String> ids = new ArrayList<String>();
        for (int i = 0; i < zuord.size(); i++) {
            ids.add(zuord.get(i).getEnvMediumId());
        }
        builder1.orIn("id", ids);
        return repository.filter(builder1.getQuery());
    }

    /**
     * Get a single EnvMedium object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object containing a single EnvMedium.
     */
    @GET
    @Path("{id}")
    public Response getById(
        @PathParam("id") String id
    ) {
        return repository.getById(EnvMedium.class, id);
    }
}
