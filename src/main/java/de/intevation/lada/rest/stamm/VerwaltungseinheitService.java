/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest.stamm;

import javax.inject.Inject;
import javax.validation.constraints.Pattern;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import de.intevation.lada.model.stammdaten.Verwaltungseinheit;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.rest.LadaService;

/**
 * REST service for Verwaltungseinheit  objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("rest/verwaltungseinheit")
public class VerwaltungseinheitService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    /**
     * Get Verwaltungseinheit objects.
     *
     * @param query The result list can be filtered using the URL parameter
     * 'query'. A filter is defined as the first letters of the 'bezeichnung'.
     * Might be null (i.e. not given at all) but not an empty string.
     *
     * @return Response containing requested objects.
     */
    @GET
    @Path("/")
    public Response get(
        @QueryParam("query") @Pattern(regexp = ".+") String query
    ) {
        if (query == null) {
            return repository.getAll(Verwaltungseinheit.class);
        }
        QueryBuilder<Verwaltungseinheit> builder =
            repository.queryBuilder(Verwaltungseinheit.class);
        builder.andLike("name", query + "%");
        return repository.filter(builder.getQuery());
    }

    /**
     * Get a single Verwaltungseinheit object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object containing a single Verwaltungseinheit.
     */
    @GET
    @Path("/{id}")
    public Response getById(
        @PathParam("id") String id
    ) {
        return repository.getById(Verwaltungseinheit.class, id);
    }
}
