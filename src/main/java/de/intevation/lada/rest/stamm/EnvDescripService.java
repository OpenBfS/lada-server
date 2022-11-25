/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest.stamm;

import java.util.List;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.model.master.EnvDescrip;
import de.intevation.lada.rest.LadaService;

/**
 * The services produce data in the application/json media type.
 * All HTTP methods use the authorization module to determine if the user is
 * allowed to perform the requested action.
 * A typical response holds information about the action performed and the data.
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("rest/envdescrip")
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
     * @param predId The parents of the requested EnvDescrip, each given
     * using an URL parameter named "predId".
     * @return Response object containing the EnvDescrip objects.
     */
    @GET
    @Path("/")
    public Response get(
        @QueryParam("lev") @NotNull Integer lev,
        @QueryParam("predId") List<Integer> predId
    ) {
        QueryBuilder<EnvDescrip> builder =
            repository.queryBuilder(EnvDescrip.class);
        builder.and("levVal", 0).not();
        builder.and("lev", lev);
        if (predId != null && !predId.isEmpty()) {
            builder.andIn("predId", predId);
        }
        return repository.filter(builder.getQuery());
    }

    /**
     * Get a single EnvDescrip object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object containing a single EnvDescrip.
     */
    @GET
    @Path("/{id}")
    public Response getById(
        @PathParam("id") Integer id
    ) {
        return repository.getById(EnvDescrip.class, id);
    }
}
