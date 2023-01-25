/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.model.master.GridColMp;

/**
 * REST-Service for preconfigured columns.
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("gridcolmp")
public class GridColMpService extends LadaService {

    @Inject
    private Repository repository;

    @Inject
    @AuthorizationConfig(type = AuthorizationType.HEADER)
    private Authorization authorization;

    /**
     * Request all predefined GridColMp objects connected to the given query.
     * @return All GridColMp objects referencing the given query.
     */
    @GET
    public Response getQueries(
        @QueryParam("baseQueryId") @NotNull Integer baseQuery
    ) {

        QueryBuilder<GridColMp> builder =
            repository.queryBuilder(GridColMp.class);
        builder.and("baseQueryId", baseQuery);

        return repository.filter(builder.getQuery());
    }
}
