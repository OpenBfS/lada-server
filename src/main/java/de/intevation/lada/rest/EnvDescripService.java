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
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.model.master.EnvDescrip;
import de.intevation.lada.model.master.EnvDescrip_;

/**
 * The services produce data in the application/json media type.
 * All HTTP methods use the authorization module to determine if the user is
 * allowed to perform the requested action.
 * A typical response holds information about the action performed and the data.
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "envdescrip")
public class EnvDescripService extends LadaService {

    /**
     * The data repository granting read/write access.
     */
    @Inject
    private Repository repository;

    /**
     * Get EnvDescrip objects.
     *
     * The requested objects can be filtered using the following URL
     * parameters:
     * @param lev The layer of the requested EnvDescrip
     * @param predIds The parents of the requested EnvDescrip, each given
     * using an URL parameter named "predId".
     * @return the EnvDescrip objects.
     */
    @GET
    public List<EnvDescrip> get(
        @QueryParam("lev") @NotNull Integer lev,
        @QueryParam("predId") List<Integer> predIds
    ) {
        QueryBuilder<EnvDescrip> builder =
            repository.queryBuilder(EnvDescrip.class);
        builder.and(EnvDescrip_.levVal, 0).not();
        builder.and(EnvDescrip_.lev, lev);
        if (predIds != null && !predIds.isEmpty()) {
            builder.andIn(EnvDescrip_.predId, predIds);
        }
        return repository.filter(builder.getQuery());
    }

    /**
     * Get a single EnvDescrip object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return a single EnvDescrip.
     */
    @GET
    @Path("{id}")
    public EnvDescrip getById(
        @PathParam("id") Integer id
    ) {
        return repository.getById(EnvDescrip.class, id);
    }
}
