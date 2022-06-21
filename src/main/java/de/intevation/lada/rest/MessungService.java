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
import de.intevation.lada.model.land.Messung;
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
 * REST service for Messung objects.
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
 *      "fertig": [boolean],
 *      "letzteAenderung": [timestamp],
 *      "messdauer": [number],
 *      "messzeitpunkt": [timestamp],
 *      "mmtId": [string],
 *      "probeId": [number],
 *      "owner": [boolean],
 *      "readonly": [boolean],
 *      "nebenprobenNr": [string],
 *      "geplant": [boolean],
 *      "treeModified": [timestamp],
 *      "parentModified": [timestamp],
 *      "externeMessunsgsId": [number]
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
@Path("rest/messung")
public class MessungService extends LadaService {

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
     * Get Messung objects.
     *
     * @param probeId URL parameter probeId to use as filter (required).
     * @return Response containing requested objects.
     */
    @GET
    @Path("/")
    public Response get(
        @QueryParam("probeId") @NotNull Integer probeId
    ) {
        QueryBuilder<Messung> builder = repository.queryBuilder(Messung.class)
            .and("probeId", probeId);
        Response r = authorization.filter(
            repository.filter(builder.getQuery()),
            Messung.class);
        @SuppressWarnings("unchecked")
        List<Messung> messungs = (List<Messung>) r.getData();
        for (Messung messung: messungs) {
            // TODO: Should have been set by authorization.filter() already
            messung.setReadonly(
                !authorization.isAuthorized(
                    messung,
                    RequestMethod.PUT,
                    Messung.class));
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
     * Get a Messung object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object containing a single Messung.
     */
    @GET
    @Path("/{id}")
    public Response getById(
        @PathParam("id") Integer id
    ) {
        Response response = repository.getById(Messung.class, id);
        Messung messung = (Messung) response.getData();
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
            Messung.class);
    }

    /**
     * Create a Messung object.
     * <p>
     * The new object is embedded in the post data as JSON formatted string.
     * <p>
     * <pre>
     * <code>
     * {
     *  "owner": [boolean],
     *  "probeId": [number],
     *  "mmtId": [string],
     *  "nebenprobenNr": [string],
     *  "messdauer": [number],
     *  "fertig": [boolean],
     *  "geplant": [boolean],
     *  "externeMessungsId": [string],
     *  "treeModified": null,
     *  "parentModified": null,
     *  "messzeitpunkt": [date],
     *  "letzteAenderung": [date]
     * }
     * </code>
     * </pre>
     *
     * @return A response object containing the created Messung.
     */
    @POST
    @Path("/")
    public Response create(
        Messung messung
    ) {
        if (!authorization.isAuthorized(
                messung,
                RequestMethod.POST,
                Messung.class)
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
            Messung.class);
    }

    /**
     * Update an existing Messung object.
     * <p>
     * The object to update should come as JSON formatted string.
     * <pre>
     * <code>
     * {
     *  "id": [number],
     *  "owner": [boolean],
     *  "probeId": [number],
     *  "mmtId": [string],
     *  "nebenprobenNr": [string],
     *  "messdauer": [number],
     *  "fertig": [boolean],
     *  "geplant": [boolean],
     *  "externeMessungsId": [number],
     *  "treeModified": [timestamp],
     *  "parentModified": [timestamp],
     *  "messzeitpunkt": [date],
     *  "letzteAenderung": [date]
     * }
     * </code>
     * </pre>
     *
     * @return Response object containing the updated Messung object.
     */
    @PUT
    @Path("/{id}")
    public Response update(
        @PathParam("id") Integer id,
        Messung messung
    ) {
        if (!authorization.isAuthorized(
                messung,
                RequestMethod.PUT,
                Messung.class)
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
            Messung.class);
    }

    /**
     * Delete an existing Messung object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object.
     */
    @DELETE
    @Path("/{id}")
    public Response delete(
        @PathParam("id") Integer id
    ) {
        Messung messungObj = repository.getByIdPlain(Messung.class, id);
        if (!authorization.isAuthorized(
                messungObj,
                RequestMethod.DELETE,
                Messung.class)
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
