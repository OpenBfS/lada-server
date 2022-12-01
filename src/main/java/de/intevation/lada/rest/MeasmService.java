/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.util.List;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import de.intevation.lada.lock.LockConfig;
import de.intevation.lada.lock.LockType;
import de.intevation.lada.lock.ObjectLocker;
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
 * REST service for Measm objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("measm")
public class MeasmService extends LadaService {

    /**
     * The data repository granting read/write access.
     */
    @Inject
    private Repository repository;

    /**
     * The object lock mechanism.
     */
    @Inject
    @LockConfig(type = LockType.TIMESTAMP)
    private ObjectLocker lock;

    /**
     * The authorization module.
     */
    @Inject
    @AuthorizationConfig(type = AuthorizationType.HEADER)
    private Authorization authorization;

    @Inject
    @ValidationConfig(type = "Messung")
    private Validator validator;

    /**
     * Get Measm objects.
     *
     * @param sampleId URL parameter sampleId to use as filter (required).
     * @return Response containing requested objects.
     */
    @GET
    public Response get(
        @QueryParam("sampleId") @NotNull Integer sampleId
    ) {
        QueryBuilder<Measm> builder = repository.queryBuilder(Measm.class)
            .and("sampleId", sampleId);
        Response r = authorization.filter(
            repository.filter(builder.getQuery()),
            Measm.class);
        @SuppressWarnings("unchecked")
        List<Measm> messungs = (List<Measm>) r.getData();
        for (Measm messung: messungs) {
            // TODO: Should have been set by authorization.filter() already
            messung.setReadonly(
                !authorization.isAuthorized(
                    messung,
                    RequestMethod.PUT,
                    Measm.class));
            Violation violation = validator.validate(messung);
            if (violation.hasErrors()
                || violation.hasWarnings()
                || violation.hasNotifications()
            ) {
                messung.setErrors(violation.getErrors());
                messung.setWarnings(violation.getWarnings());
                messung.setNotifications(
                    violation.getNotifications());
            }
        }
        return new Response(true, StatusCodes.OK, messungs);
    }

    /**
     * Get a Measm object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object containing a single Measm.
     */
    @GET
    @Path("{id}")
    public Response getById(
        @PathParam("id") Integer id
    ) {
        Response response = repository.getById(Measm.class, id);
        Measm messung = (Measm) response.getData();
        Violation violation = validator.validate(messung);
        if (violation.hasErrors()
            || violation.hasWarnings()
            || violation.hasNotifications()
        ) {
            response.setErrors(violation.getErrors());
            response.setWarnings(violation.getWarnings());
            response.setNotifications(violation.getNotifications());
        }
        return authorization.filter(
            response,
            Measm.class);
    }

    /**
     * Create a Measm object.
     *
     * @return A response object containing the created Measm.
     */
    @POST
    public Response create(
        Measm messung
    ) {
        if (!authorization.isAuthorized(
                messung,
                RequestMethod.POST,
                Measm.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }

        Violation violation = validator.validate(messung);
        if (violation.hasErrors()) {
            Response response =
                new Response(false, StatusCodes.ERROR_VALIDATION, messung);
            response.setErrors(violation.getErrors());
            response.setWarnings(violation.getWarnings());
            response.setNotifications(violation.getNotifications());
            return response;
        }

        /* Persist the new messung object*/
        Response response = repository.create(messung);
        if (violation.hasWarnings()) {
            response.setWarnings(violation.getWarnings());
        }
        if (violation.hasNotifications()) {
            response.setNotifications(violation.getNotifications());
        }
        return authorization.filter(
            response,
            Measm.class);
    }

    /**
     * Update an existing Measm object.
     *
     * @return Response object containing the updated Measm object.
     */
    @PUT
    @Path("{id}")
    public Response update(
        @PathParam("id") Integer id,
        Measm messung
    ) {
        if (!authorization.isAuthorized(
                messung,
                RequestMethod.PUT,
                Measm.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }
        if (lock.isLocked(messung)) {
            return new Response(false, StatusCodes.CHANGED_VALUE, null);
        }
        Violation violation = validator.validate(messung);
        if (violation.hasErrors()) {
            Response response =
                new Response(false, StatusCodes.ERROR_VALIDATION, messung);
            response.setErrors(violation.getErrors());
            response.setWarnings(violation.getWarnings());
            response.setNotifications(violation.getNotifications());
            return response;
        }
        Response response = repository.update(messung);
        if (!response.getSuccess()) {
            return response;
        }
        if (violation.hasWarnings()) {
            response.setWarnings(violation.getWarnings());
        }
        if (violation.hasNotifications()) {
            response.setNotifications(violation.getNotifications());
        }
        return authorization.filter(
            response,
            Measm.class);
    }

    /**
     * Delete an existing Measm object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object.
     */
    @DELETE
    @Path("{id}")
    public Response delete(
        @PathParam("id") Integer id
    ) {
        Measm messungObj = repository.getByIdPlain(Measm.class, id);
        if (!authorization.isAuthorized(
                messungObj,
                RequestMethod.DELETE,
                Measm.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }
        if (lock.isLocked(messungObj)) {
            return new Response(false, StatusCodes.CHANGED_VALUE, null);
        }

        /* Delete the messung object*/
        return repository.delete(messungObj);
    }
}
