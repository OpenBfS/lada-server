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
import de.intevation.lada.model.land.Ortszuordnung;
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
 * REST service for Ort objects.
 * <p>
 * The services produce data in the application/json media type.
 * All HTTP methods use the authorization module to determine if the user is
 * allowed to perform the requested action.
 * A typical response holds information about the action performed and the data.
 * <pre>
 * <code>
 * {
 *  "success": [boolean];
 *  "message": [string],
 *  "data":[{
 *      "id": [number],
 *      "letzteAenderung": [timestamp],
 *      "ortsTyp": [string],
 *      "ortszusatztext": [string],
 *      "probeId": [number],
 *      "ort": [number],
 *      "owner": [boolean],
 *      "readonly": [boolean],
 *      "treeModified": [timestamp],
 *      "parentModified": [timestamp]
 *  }],
 *  "errors": [object],
 *  "warnings": [object],
 *  "readonly": [boolean],
 *  "totalCount": [number]
 * }
 * </code>
 * </pre>
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("rest/ortszuordnung")
public class OrtszuordnungService extends LadaService {

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
     * Get Ortszuordnung objects.
     *
     * @param probeId The requested objects can be filtered using
     * a URL parameter named probeId.
     *
     * @return Response containing requested objects.
     */
    @GET
    @Path("/")
    public Response get(
        @QueryParam("probeId") Integer probeId
    ) {
        if (probeId == null) {
            return repository.getAll(Ortszuordnung.class);
        }
        QueryBuilder<Ortszuordnung> builder =
            repository.queryBuilder(Ortszuordnung.class);
        builder.and("probeId", probeId);
        Response r = authorization.filter(
            repository.filter(builder.getQuery()),
            Ortszuordnung.class);
        if (r.getSuccess()) {
            @SuppressWarnings("unchecked")
            List<Ortszuordnung> ortszuordnungs =
                (List<Ortszuordnung>) r.getData();
            for (Ortszuordnung otz: ortszuordnungs) {
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
     * Get a Ort object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object containing a single Ort.
     */
    @GET
    @Path("/{id}")
    public Response getById(
        @PathParam("id") Integer id
    ) {
        Response response = repository.getById(Ortszuordnung.class, id);
        Ortszuordnung ort = (Ortszuordnung) response.getData();
        Violation violation = validator.validate(ort);
        if (violation.hasErrors() || violation.hasWarnings()) {
            response.setErrors(violation.getErrors());
            response.setWarnings(violation.getWarnings());
        }
        return authorization.filter(
            response,
            Ortszuordnung.class);
    }

    /**
     * Create a new Ort object.
     * <p>
     * The new object is embedded in the post data as JSON formatted string.
     * <p>
     * <pre>
     * <code>
     * {
     *  "owner": [boolean],
     *  "ort": [number],
     *  "probeId": [number],
     *  "ortsTyp": [string],
     *  "ortszusatztext": [string],
     *  "treeModified": null,
     *  "parentModified": null,
     *  "letzteAenderung": [date]
     * }
     * </code>
     * </pre>
     *
     * @return A response object containing the created Ort.
     */
    @POST
    @Path("/")
    public Response create(
        Ortszuordnung ort
    ) {
        if (!authorization.isAuthorized(
                ort,
                RequestMethod.POST,
                Ortszuordnung.class)) {
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
            Ortszuordnung.class);
    }

    /**
     * Update an existing Ort object.
     * <p>
     * The object to update should come as JSON formatted string.
     * <pre>
     * <code>
     * {
     *  "id": [number],
     *  "owner": [boolean],
     *  "ort": [number],
     *  "probeId": [number],
     *  "ortsTyp": [string],
     *  "ortszusatztext": [string],
     *  "treeModified": [timestamp],
     *  "parentModified": [timestamp],
     *  "letzteAenderung": [date]
     * }
     * </code>
     * </pre>
     *
     * @return Response object containing the updated Ort object.
     */
    @PUT
    @Path("/{id}")
    public Response update(
        @PathParam("id") Integer id,
        Ortszuordnung ort
    ) {
        if (!authorization.isAuthorized(
                ort,
                RequestMethod.PUT,
                Ortszuordnung.class)) {
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
            Ortszuordnung.class);
    }

    /**
     * Delete an existing Ort object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object.
     */
    @DELETE
    @Path("/{id}")
    public Response delete(
        @PathParam("id") Integer id
    ) {
        Ortszuordnung ortObj = repository.getByIdPlain(Ortszuordnung.class, id);
        if (!authorization.isAuthorized(
                ortObj,
                RequestMethod.PUT,
                Ortszuordnung.class)) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }
        if (lock.isLocked(ortObj)) {
            return new Response(false, StatusCodes.CHANGED_VALUE, null);
        }

        return repository.delete(ortObj);
    }
}
