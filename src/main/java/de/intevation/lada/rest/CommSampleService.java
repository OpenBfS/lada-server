/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

import de.intevation.lada.model.lada.CommSample;
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

/**
 * REST service to operate on CommSample objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("commsample")
public class CommSampleService extends LadaService {

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
    private Validator<CommSample> validator;


    /**
     * Get CommSample objects.
     *
     * @param sampleId The requested objects will be filtered
     * using an URL parameter named sampleId.
     *
     * @return Response object containing requested objects.
     */
    @GET
    public Response get(
        @QueryParam("sampleId") @NotNull Integer sampleId
    ) {
        QueryBuilder<CommSample> builder =
            repository.queryBuilder(CommSample.class);
        builder.and("sampleId", sampleId);
        return authorization.filter(
            repository.filter(builder.getQuery()),
            CommSample.class);
    }

    /**
     * Get a single CommSample object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object containing a single CommSample.
     */
    @GET
    @Path("{id}")
    public Response getById(
        @PathParam("id") Integer id
    ) {
        return authorization.filter(
            repository.getById(CommSample.class, id),
            CommSample.class);
    }

    /**
     * Create a new CommSample object.
     *
     * @return Response object containing the new CommSample.
     */
    @POST
    public Response create(
        CommSample kommentar
    ) {
        if (!authorization.isAuthorized(
                kommentar,
                RequestMethod.POST,
                CommSample.class)
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
            return authorization.filter(response, CommSample.class);
        }
    }

    /**
     * Update an existing CommSample object.
     *
     * @return Response object containing the updated CommSample object.
     */
    @PUT
    @Path("{id}")
    public Response update(
        @PathParam("id") Integer id,
        CommSample kommentar
    ) {
        if (!authorization.isAuthorized(
                kommentar,
                RequestMethod.PUT,
                CommSample.class)
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
            CommSample.class);
        }
    }

    /**
     * Delete an existing CommSample by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object.
     */
    @DELETE
    @Path("{id}")
    public Response delete(
        @PathParam("id") Integer id
    ) {
        CommSample kommentarObj = repository.getByIdPlain(CommSample.class, id);
        if (!authorization.isAuthorized(
                kommentarObj,
                RequestMethod.DELETE,
                CommSample.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }
        return repository.delete(kommentarObj);
    }
}
