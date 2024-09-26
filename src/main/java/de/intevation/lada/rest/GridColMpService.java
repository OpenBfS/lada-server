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
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.model.master.GridColMp;
import de.intevation.lada.model.master.GridColMp_;

/**
 * REST-Service for preconfigured columns.
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "gridcolmp")
public class GridColMpService extends LadaService {

    @Inject
    private Repository repository;

    /**
     * Request all predefined GridColMp objects connected to the given query.
     * @return All GridColMp objects referencing the given query.
     */
    @GET
    public List<GridColMp> getQueries(
        @QueryParam("baseQueryId") @NotNull Integer baseQuery
    ) {

        QueryBuilder<GridColMp> builder =
            repository.queryBuilder(GridColMp.class);
        builder.and(GridColMp_.baseQueryId, baseQuery);

        return repository.filter(builder.getQuery());
    }
}
