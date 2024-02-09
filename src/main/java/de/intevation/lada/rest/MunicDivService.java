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
import jakarta.validation.Valid;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.model.master.MunicDiv;

/**
 * REST service for MunicDiv objects.
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("municdiv")
public class MunicDivService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    @Inject
    @AuthorizationConfig(type = AuthorizationType.HEADER)
    private Authorization authorization;

    /**
     * Get all MunicDiv objects.
     *
     * @return Response containing requested objects.
     */
    @GET
    public Response get() {
        List<MunicDiv> gemUntergliederung =
            repository.getAllPlain(MunicDiv.class);
        for (MunicDiv gu: gemUntergliederung) {
            // TODO Do not iterate all the objects if its not necessary
            gu.setReadonly(true);
                // !authorization.isAuthorized(
                //     gu,
                //     RequestMethod.POST,
                //     GemeindeUntergliederung.class));
        }
        return new Response(true, StatusCodes.OK, gemUntergliederung, gemUntergliederung.size());
    }

    /**
     * Get a single object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object containing a single object.
     */
    @GET
    @Path("{id}")
    public Response getById(
        @PathParam("id") Integer id
    ) {
        MunicDiv gu = repository.getByIdPlain(
            MunicDiv.class, id);
        gu.setReadonly(
            !authorization.isAuthorized(
                gu,
                RequestMethod.POST,
                MunicDiv.class
            )
        );
        return new Response(true, StatusCodes.OK, gu);
    }

    @POST
    public Response create(
        @Valid MunicDiv gemUntergliederung
    ) {
        authorization.authorize(
            gemUntergliederung,
            RequestMethod.POST,
            MunicDiv.class);
        QueryBuilder<MunicDiv> builder =
            repository.queryBuilder(MunicDiv.class);
        builder.and("siteId",
            gemUntergliederung.getSiteId());
        builder.and("networkId",
            gemUntergliederung.getNetworkId());
        List<MunicDiv> gemUntergliederungn =
            repository.filterPlain(builder.getQuery());
        if (gemUntergliederungn.isEmpty()) {
            return repository.create(gemUntergliederung);
        }
        return new Response(false, StatusCodes.IMP_DUPLICATE, null);
    }

    @PUT
    @Path("{id}")
    public Response update(
        @PathParam("id") Integer id,
        @Valid MunicDiv gemUntergliederung
    ) {
        authorization.authorize(
            gemUntergliederung,
            RequestMethod.PUT,
            MunicDiv.class);
        QueryBuilder<MunicDiv> builder =
            repository.queryBuilder(MunicDiv.class);
        builder.and("siteId", gemUntergliederung.getSiteId());
        builder.and("networkId", gemUntergliederung.getNetworkId());
        List<MunicDiv> gemUntergliederungn =
            repository.filterPlain(builder.getQuery());
        if (!gemUntergliederungn.isEmpty()
            && !gemUntergliederungn.get(0).getId().equals(gemUntergliederung.getId())
        ) {
            return new Response(false, StatusCodes.IMP_DUPLICATE, null);
        }
        return repository.update(gemUntergliederung);
    }

    @DELETE
    @Path("{id}")
    public Response delete(
        @PathParam("id") Integer id
    ) {
        MunicDiv gemUntergliederung = repository.getByIdPlain(
            MunicDiv.class, id);
        authorization.authorize(
            gemUntergliederung,
            RequestMethod.DELETE,
            MunicDiv.class);
        return repository.delete(gemUntergliederung);
    }
}
