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
import de.intevation.lada.model.master.MpgCateg;

/**
 * REST service for MpgCateg objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("mpgcateg")
public class MpgCategService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    @Inject
    @AuthorizationConfig(type = AuthorizationType.HEADER)
    private Authorization authorization;

    /**
     * Get all MpgCateg objects.
     *
     * @return Response containing requested objects.
     */
    @GET
    public Response get() {
        return authorization.filter(
            repository.getAll(MpgCateg.class), MpgCateg.class);
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
        return authorization.filter(
            repository.getById(MpgCateg.class, id), MpgCateg.class);
    }

    @POST
    public Response create(
        @Valid MpgCateg kategorie
    ) {
        authorization.authorize(
            kategorie,
            RequestMethod.POST,
            MpgCateg.class);
        QueryBuilder<MpgCateg> builder =
            repository.queryBuilder(MpgCateg.class);
        builder.and("extId", kategorie.getExtId());
        builder.and("networkId", kategorie.getNetworkId());
        List<MpgCateg> kategorien =
            repository.filterPlain(builder.getQuery());
        if (kategorien.isEmpty()) {
            return repository.create(kategorie);
        }
        return new Response(false, StatusCodes.IMP_DUPLICATE, null);
    }

    @PUT
    @Path("{id}")
    public Response update(
        @PathParam("id") Integer id,
        @Valid MpgCateg kategorie
    ) {
        authorization.authorize(
            kategorie,
            RequestMethod.PUT,
            MpgCateg.class);
        QueryBuilder<MpgCateg> builder =
            repository.queryBuilder(MpgCateg.class);
        builder.and("extId", kategorie.getExtId());
        builder.and("networkId", kategorie.getNetworkId());
        List<MpgCateg> kategorien =
            repository.filterPlain(builder.getQuery());
        if (!kategorien.isEmpty()
            && !kategorien.get(0).getId().equals(kategorie.getId())
        ) {
            return new Response(false, StatusCodes.IMP_DUPLICATE, null);
        }
        return repository.update(kategorie);
    }

    @DELETE
    @Path("{id}")
    public Response delete(
        @PathParam("id") Integer id
    ) {
        MpgCateg kategorie = repository.getByIdPlain(
            MpgCateg.class, id);
        authorization.authorize(
                kategorie,
                RequestMethod.DELETE,
                MpgCateg.class);
        return repository.delete(kategorie);
    }
}
