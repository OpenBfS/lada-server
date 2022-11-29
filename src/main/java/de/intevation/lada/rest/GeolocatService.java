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
import de.intevation.lada.model.lada.Geolocat;
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
 * REST service for Geolocat objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("rest/geolocat")
public class GeolocatService extends LadaService {

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
    @ValidationConfig(type = "Ortszuordnung")
    private Validator validator;

    /**
     * Get Geolocat objects.
     *
     * @param sampleId The requested objects can be filtered using
     * a URL parameter named sampleId.
     *
     * @return Response containing requested objects.
     */
    @GET
    @Path("/")
    public Response get(
        @QueryParam("sampleId") @NotNull Integer sampleId
    ) {
        QueryBuilder<Geolocat> builder =
            repository.queryBuilder(Geolocat.class);
        builder.and("sampleId", sampleId);
        Response r = authorization.filter(
            repository.filter(builder.getQuery()),
            Geolocat.class);
        if (r.getSuccess()) {
            @SuppressWarnings("unchecked")
            List<Geolocat> ortszuordnungs =
                (List<Geolocat>) r.getData();
            for (Geolocat otz: ortszuordnungs) {
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
     * Get a Geolocat object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object containing a single Geolocat.
     */
    @GET
    @Path("/{id}")
    public Response getById(
        @PathParam("id") Integer id
    ) {
        Response response = repository.getById(Geolocat.class, id);
        Geolocat ort = (Geolocat) response.getData();
        Violation violation = validator.validate(ort);
        if (violation.hasErrors() || violation.hasWarnings()) {
            response.setErrors(violation.getErrors());
            response.setWarnings(violation.getWarnings());
        }
        return authorization.filter(
            response,
            Geolocat.class);
    }

    /**
     * Create a new Geolocat object.
     *
     * @return A response object containing the created Ort.
     */
    @POST
    @Path("/")
    public Response create(
        Geolocat ort
    ) {
        if (!authorization.isAuthorized(
                ort,
                RequestMethod.POST,
                Geolocat.class)) {
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
            Geolocat.class);
    }

    /**
     * Update an existing Geolocat object.
     *
     * @return Response object containing the updated Geolocat object.
     */
    @PUT
    @Path("/{id}")
    public Response update(
        @PathParam("id") Integer id,
        Geolocat ort
    ) {
        if (!authorization.isAuthorized(
                ort,
                RequestMethod.PUT,
                Geolocat.class)) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }
        if (lock.isLocked(ort)) {
            return new Response(false, StatusCodes.CHANGED_VALUE, null);
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
        if (!response.getSuccess()) {
            return response;
        }
        if (violation.hasWarnings()) {
            response.setWarnings(violation.getWarnings());
        }
        return authorization.filter(
            response,
            Geolocat.class);
    }

    /**
     * Delete an existing Geolocat object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object.
     */
    @DELETE
    @Path("/{id}")
    public Response delete(
        @PathParam("id") Integer id
    ) {
        Geolocat ortObj = repository.getByIdPlain(Geolocat.class, id);
        if (!authorization.isAuthorized(
                ortObj,
                RequestMethod.PUT,
                Geolocat.class)) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }
        if (lock.isLocked(ortObj)) {
            return new Response(false, StatusCodes.CHANGED_VALUE, null);
        }

        return repository.delete(ortObj);
    }
}
