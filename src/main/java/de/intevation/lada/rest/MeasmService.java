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
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

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
    private Validator<Measm> validator;

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
            // TODO: Should have been set by authorization.filter() already,
            // but that's unfortunately not the same as authorizing PUT.
            messung.setReadonly(
                !authorization.isAuthorized(
                    messung,
                    RequestMethod.PUT,
                    Measm.class));
            validator.validate(messung);
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
        validator.validate(response.getData());
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
        @Valid Measm messung
    ) {
        authorization.authorize(
            messung,
            RequestMethod.POST,
            Measm.class);

        validator.validate(messung);
        if (messung.hasErrors()) {
            return new Response(false, StatusCodes.ERROR_VALIDATION, messung);
        }

        return authorization.filter(
            repository.create(messung),
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
        @Valid Measm messung
    ) {
        authorization.authorize(
            messung,
            RequestMethod.PUT,
            Measm.class);
        lock.isLocked(messung);
        validator.validate(messung);
        if (messung.hasErrors()) {
            return new Response(false, StatusCodes.ERROR_VALIDATION, messung);
        }

        Response response = repository.update(messung);
        validator.validate(response.getData());
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
        authorization.authorize(
            messungObj,
            RequestMethod.DELETE,
            Measm.class);
        lock.isLocked(messungObj);

        /* Delete the messung object*/
        return repository.delete(messungObj);
    }
}
