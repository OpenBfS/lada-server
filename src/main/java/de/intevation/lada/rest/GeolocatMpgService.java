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
import de.intevation.lada.model.lada.GeolocatMpg;
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
 * REST service for GeolocatMpg objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("geolocatmpg")
public class GeolocatMpgService extends LadaService {

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
    private Validator<GeolocatMpg> validator;

    /**
     * Get GeolocatMpg objects.
     *
     * @param mpgId The requested objects will be filtered
     * using a URL parameter named mpgId.
     *
     * @return Response containing requested objects.
     */
    @GET
    public Response get(
        @QueryParam("mpgId") @NotNull Integer mpgId
    ) {
        QueryBuilder<GeolocatMpg> builder =
            repository.queryBuilder(GeolocatMpg.class);
        builder.and("mpgId", mpgId);
        Response r =  authorization.filter(
            repository.filter(builder.getQuery()),
            GeolocatMpg.class);
            if (r.getSuccess()) {
                @SuppressWarnings("unchecked")
                List<GeolocatMpg> ortszuordnungs =
                    (List<GeolocatMpg>) r.getData();
                for (GeolocatMpg otz: ortszuordnungs) {
                    Violation violation = validator.validate(otz);
                    if (violation.hasErrors() || violation.hasWarnings()) {
                        otz.setErrors(violation.getErrors());
                        otz.setWarnings(violation.getWarnings());
                    }
                }
                return new Response(true, StatusCodes.OK, ortszuordnungs);
            } else {
                return r;
            }
    }

    /**
     * Get single object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object
     */
    @GET
    @Path("{id}")
    public Response getById(
        @PathParam("id") Integer id
    ) {
        Response response = repository.getById(GeolocatMpg.class, id);
        GeolocatMpg ort = (GeolocatMpg) response.getData();
        Violation violation = validator.validate(ort);
        if (violation.hasErrors() || violation.hasWarnings()) {
            response.setErrors(violation.getErrors());
            response.setWarnings(violation.getWarnings());
        }
        return authorization.filter(
            response,
            GeolocatMpg.class);
    }

    /**
     * Create a new GeolocatMpg object.
     *
     * @return A response object containing the created Ort.
     */
    @POST
    public Response create(
        @Valid GeolocatMpg ort
    ) {
        if (!authorization.isAuthorized(
                ort,
                RequestMethod.POST,
                GeolocatMpg.class)) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }
        Violation violation = validator.validate(ort);
        if (violation.hasErrors()) {
            Response response =
                new Response(false, StatusCodes.ERROR_VALIDATION, ort);
            response.setErrors(violation.getErrors());
            response.setWarnings(violation.getWarnings());
            return response;
        }

        /* Persist the new object*/
        Response response = repository.create(ort);
        if (violation.hasWarnings()) {
            response.setWarnings(violation.getWarnings());
        }

        return authorization.filter(
            response,
            GeolocatMpg.class);
    }

    /**
     * Update an existing GeolocatMpg object.
     *
     * @return Response object containing the updated GeolocatMpg object.
     */
    @PUT
    @Path("{id}")
    public Response update(
        @PathParam("id") Integer id,
        @Valid GeolocatMpg ort
    ) {
        if (!authorization.isAuthorized(
                ort,
                RequestMethod.PUT,
                GeolocatMpg.class)) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }
        Violation violation = validator.validate(ort);
        if (violation.hasErrors()) {
            Response response =
                new Response(false, StatusCodes.ERROR_VALIDATION, ort);
            response.setErrors(violation.getErrors());
            response.setWarnings(violation.getWarnings());
            return response;
        }

        Response response = repository.update(ort);
        if (violation.hasWarnings()) {
            response.setWarnings(violation.getWarnings());
        }

        return authorization.filter(
            response,
            GeolocatMpg.class);
    }

    /**
     * Delete object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object.
     */
    @DELETE
    @Path("{id}")
    public Response delete(
        @PathParam("id") Integer id
    ) {
        GeolocatMpg ortObj = repository.getByIdPlain(
            GeolocatMpg.class, id);
        if (!authorization.isAuthorized(
                ortObj,
                RequestMethod.PUT,
                GeolocatMpg.class)) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }

        return repository.delete(ortObj);
    }
}
