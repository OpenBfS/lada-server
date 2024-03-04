/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

import de.intevation.lada.model.lada.CommMeasm;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.validation.Validator;

/**
 * REST service for CommMeasm objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("commmeasm")
public class CommMeasmService extends LadaService {

    /**
     * The data repository granting read/write access.
     */
    @Inject
    private Repository repository;

    /**
     * The authorization module.
     */
    @Inject
    @AuthorizationConfig(type = AuthorizationType.HEADER)
    private Authorization authorization;

    @Inject
    private Validator<CommMeasm> validator;

    /**
     * Get CommMeasm objects.
     *
     * @param measmId The requested objects have to be filtered
     * using an URL parameter named measmId.
     *
     * @return Response object containing filtered CommMeasm objects.
     * Status-Code 699 if requested objects are
     * not authorized.
     */
    @GET
    public Response get(
        @QueryParam("measmId") @NotNull Integer measmId
    ) {
        Measm messung = repository.getByIdPlain(Measm.class, measmId);
        authorization.authorize(
                messung, RequestMethod.GET, Measm.class);

        QueryBuilder<CommMeasm> builder =
            repository.queryBuilder(CommMeasm.class);
        builder.and("measmId", measmId);
        return authorization.filter(
            repository.filter(builder.getQuery()),
            CommMeasm.class);
    }

    /**
     * Get a single CommMeasm object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object containing a single CommMeasm.
     */
    @GET
    @Path("{id}")
    public Response getById(
        @PathParam("id") Integer id
    ) {
        Response response = repository.getById(CommMeasm.class, id);
        CommMeasm kommentar = (CommMeasm) response.getData();
        Measm messung = repository.getByIdPlain(
            Measm.class, kommentar.getMeasmId());
        authorization.authorize(
            messung, RequestMethod.GET, Measm.class);

        return authorization.filter(
            response,
            CommMeasm.class);
    }

    /**
     * Create a CommMeasm object.
     * @return A response object containing the created CommMeasm.
     */
    @POST
    public Response create(
        @Valid CommMeasm kommentar
    ) {
        authorization.authorize(
            kommentar,
            RequestMethod.POST,
            CommMeasm.class);
        validator.validate(kommentar);
        if (kommentar.hasErrors()) {
            return new Response(false, StatusCodes.ERROR_VALIDATION, kommentar);
        }
        return authorization.filter(
            repository.create(kommentar), CommMeasm.class);
    }

    /**
     * Update an existing CommMeasm object.
     *
     * @return Response object containing the updated CommMeasm object.
     */
    @PUT
    @Path("{id}")
    public Response update(
        @PathParam("id") Integer id,
        @Valid CommMeasm kommentar
    ) {
        authorization.authorize(
                kommentar,
                RequestMethod.PUT,
                CommMeasm.class);
        validator.validate(kommentar);
        if (kommentar.hasErrors() || kommentar.hasWarnings()) {
            return new Response(false, StatusCodes.VAL_EXISTS, kommentar);
        }
        return authorization.filter(
            repository.update(kommentar), CommMeasm.class);
    }

    /**
     * Delete an existing CommMeasm object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object.
     */
    @DELETE
    @Path("{id}")
    public Response delete(
        @PathParam("id") Integer id
    ) {
        CommMeasm kommentarObj = repository.getByIdPlain(CommMeasm.class, id);
        authorization.authorize(
            kommentarObj,
            RequestMethod.DELETE,
            CommMeasm.class);
        return repository.delete(kommentarObj);
    }
}
