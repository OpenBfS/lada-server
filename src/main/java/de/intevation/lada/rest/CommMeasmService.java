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
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

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
import de.intevation.lada.validation.Violation;
import de.intevation.lada.validation.annotation.ValidationConfig;
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
    @ValidationConfig(type = "KommentarM")
    private Validator validator;

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
        if (!authorization.isAuthorized(
                messung, RequestMethod.GET, Measm.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }

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
        if (!authorization.isAuthorized(
                messung, RequestMethod.GET, Measm.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }

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
        CommMeasm kommentar
    ) {
        if (!authorization.isAuthorized(
                kommentar,
                RequestMethod.POST,
                CommMeasm.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }
        Violation violation = validator.validate(kommentar);
        if (violation.hasErrors()) {
            Response response =
                new Response(false, StatusCodes.ERROR_VALIDATION, kommentar);
            response.setErrors(violation.getErrors());
            response.setWarnings(violation.getWarnings());
            response.setNotifications(violation.getNotifications());
            return response;
        } else {
            /* Persist the new object*/
            Response response = repository.create(kommentar);
            if (violation.hasWarnings()) {
                response.setWarnings(violation.getWarnings());
            }
            if (violation.hasNotifications()) {
                response.setNotifications(violation.getNotifications());
            }
            return authorization.filter(response, CommMeasm.class);
        }
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
        CommMeasm kommentar
    ) {
        if (!authorization.isAuthorized(
                kommentar,
                RequestMethod.PUT,
                CommMeasm.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }
        Violation violation = validator.validate(kommentar);
        if (violation.hasErrors()||violation.hasWarnings()) {
            Response response =
                new Response(false, StatusCodes.VAL_EXISTS, kommentar);
            return response;
        } else {
        return authorization.filter(
            repository.update(kommentar),
            CommMeasm.class);
        }
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
        if (!authorization.isAuthorized(
                kommentarObj,
                RequestMethod.DELETE,
                CommMeasm.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }
        return repository.delete(kommentarObj);
    }
}
