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
import de.intevation.lada.model.land.Messung;
import de.intevation.lada.model.land.Messwert;
import de.intevation.lada.model.land.Probe;
import de.intevation.lada.model.stammdaten.Umwelt;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.data.MesswertNormalizer;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.validation.Validator;
import de.intevation.lada.validation.Violation;
import de.intevation.lada.validation.annotation.ValidationConfig;

/**
 * REST service for Messwert objects.
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
 *      "grenzwertueberschreitung": [boolean],
 *      "letzteAenderung": [timestamp],
 *      "mehId": [number],
 *      "messfehler": [number],
 *      "messgroesseId": [number],
 *      "messungsId": [number],
 *      "messwert": [number],
 *      "messwertNwg": [string],
 *      "nwgZuMesswert": [number],
 *      "owner": [boolean],
 *      "readonly":[boolean],
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
@Path("rest/messwert")
public class MesswertService extends LadaService {

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
    @ValidationConfig(type = "Messwert")
    private Validator validator;

    @Inject
    private MesswertNormalizer messwertNormalizer;

    /**
     * Get Messwert objects.
     *
     * @param messungsId The requested objects have to be filtered
     * using an URL parameter named messungsId.
     * Example: http://example.com/messwert?messungsId=[ID]
     *
     * @return Response object containing filtered Messwert objects.
     * Status-Code 699 if parameter is missing or requested objects are
     * not authorized.
     */
    @GET
    @Path("/")
    public Response get(
        @QueryParam("messungsId") Integer messungsId
    ) {
        if (messungsId == null) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }
        Messung messung = repository.getByIdPlain(Messung.class, messungsId);
        if (!authorization.isAuthorized(
                messung,
                RequestMethod.GET,
                Messung.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }

        QueryBuilder<Messwert> builder =
            repository.queryBuilder(Messwert.class);
        builder.and("messungsId", messungsId);
        Response r = authorization.filter(
            repository.filter(builder.getQuery()),
            Messwert.class);
        if (r.getSuccess()) {
            @SuppressWarnings("unchecked")
            List<Messwert> messwerts = (List<Messwert>) r.getData();
            for (Messwert messwert: messwerts) {
                Violation violation = validator.validate(messwert);
                if (violation.hasErrors()
                    || violation.hasWarnings()
                    || violation.hasNotifications()
                ) {
                    messwert.setErrors(violation.getErrors());
                    messwert.setWarnings(violation.getWarnings());
                    messwert.setNotifications(violation.getNotifications());
                }
            }
            return new Response(true, StatusCodes.OK, messwerts);
        } else {
            return r;
        }
    }

    /**
     * Get a Messwert object by id.
     * <p>
     * The id is appended to the URL as a path parameter.
     * <p>
     * Example: http://example.com/messwert/{id}
     *
     * @return Response object containing a single Messwert.
     */
    @GET
    @Path("/{id}")
    public Response getById(
        @PathParam("id") String id
    ) {
        Response response =
            repository.getById(
                Messwert.class, Integer.valueOf(id));
        Messwert messwert = (Messwert) response.getData();
        Messung messung = repository.getByIdPlain(
            Messung.class, messwert.getMessungsId());
        if (!authorization.isAuthorized(
            messung,
            RequestMethod.GET,
            Messung.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }
        Violation violation = validator.validate(messwert);
        if (violation.hasErrors() || violation.hasWarnings()) {
            response.setErrors(violation.getErrors());
            response.setWarnings(violation.getWarnings());
            response.setNotifications(violation.getNotifications());
        }
        return authorization.filter(
            response,
            Messwert.class);
    }

    /**
     * Create a Messwert object.
     * <p>
     * The new object is embedded in the post data as JSON formatted string.
     * <p>
     * <pre>
     * <code>
     * {
     *  "owner": [boolean],
     *  "messungsId": [number],
     *  "messgroesseId": [number],
     *  "messwert": [number],
     *  "messwertNwg": [string],
     *  "messfehler": [number],
     *  "nwgZuMesswert": [number],
     *  "mehId": [number],
     *  "grenzwertueberschreitung": [boolean],
     *  "treeModified": null,
     *  "parentModified": null,
     *  "letzteAenderung": [date]
     * }
     * </code>
     * </pre>
     *
     * @return A response object containing the created Messwert.
     */
    @POST
    @Path("/")
    public Response create(
        Messwert messwert
    ) {
        if (!authorization.isAuthorized(
                messwert,
                RequestMethod.POST,
                Messwert.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }
        Violation violation = validator.validate(messwert);
        if (violation.hasErrors()) {
            Response response =
                new Response(false, StatusCodes.ERROR_VALIDATION, messwert);
            response.setErrors(violation.getErrors());
            response.setWarnings(violation.getWarnings());
            response.setNotifications(violation.getNotifications());
            return response;
        }

        /* Persist the new messung object*/
        Response response = repository.create(messwert);
        if (violation.hasWarnings()) {
            response.setWarnings(violation.getWarnings());
        }
        if (violation.hasNotifications()) {
           response.setNotifications(violation.getNotifications());
        }
        return authorization.filter(
            response,
            Messwert.class);
    }

    /**
     * Update an existing Messwert object.
     * <p>
     * The object to update should come as JSON formatted string.
     * <pre>
     * <code>
     * {
     *  "id": [number],
     *  "owner": [boolean],
     *  "messungsId": [number],
     *  "messgroesseId": [number],
     *  "messwert": [number],
     *  "messwertNwg": [string],
     *  "messfehler": [number],
     *  "nwgZuMesswert": [number],
     *  "mehId": [number],
     *  "grenzwertueberschreitung": [boolean],
     *  "treeModified": [timestamp],
     *  "parentModified": [timestamp],
     *  "letzteAenderung": [date]
     * }
     * </code>
     * </pre>
     *
     * @return Response object containing the updated Messwert object.
     */
    @PUT
    @Path("/{id}")
    public Response update(
        @PathParam("id") String id,
        Messwert messwert
    ) {
        if (!authorization.isAuthorized(
                messwert,
                RequestMethod.PUT,
                Messwert.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }
        if (lock.isLocked(messwert)) {
            return new Response(false, StatusCodes.CHANGED_VALUE, null);
        }
        Violation violation = validator.validate(messwert);
        if (violation.hasErrors()) {
            Response response =
                new Response(false, StatusCodes.ERROR_VALIDATION, messwert);
            response.setErrors(violation.getErrors());
            response.setWarnings(violation.getWarnings());
            response.setNotifications(violation.getNotifications());
            return response;
        }

        Response response = repository.update(messwert);
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
            Messwert.class);
    }

    /**
     * Normalise all Messwert objects connected to the given Messung.
     * @param messungsId The messung id needs to be given
     * as URL parameter 'messungsId'.
     * @return Response object containing the updated Messwert objects.
     */
    @PUT
    @Path("/normalize")
    public Response normalize(
        @QueryParam("messungsId") Integer messungsId
    ) {
        if (messungsId == null) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }

        //Load messung, probe and umwelt to get MessEinheit to convert to
        Messung messung = repository.getByIdPlain(Messung.class, messungsId);
        if (!authorization.isAuthorized(
            messung,
            RequestMethod.PUT,
            Messung.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }

        Probe probe =
            repository.getByIdPlain(
                Probe.class, messung.getProbeId());
        if (probe.getUmwId() == null || probe.getUmwId().equals("")) {
            return new Response(true, StatusCodes.OP_NOT_POSSIBLE, null);
        }
        Umwelt umwelt =
            repository.getByIdPlain(
                Umwelt.class, probe.getUmwId());
        //Get all Messwert objects to convert
        QueryBuilder<Messwert> messwertBuilder =
            repository.queryBuilder(Messwert.class);
        messwertBuilder.and("messungsId", messungsId);
        List<Messwert> messwerte = messwertNormalizer.normalizeMesswerte(
            repository.filterPlain(messwertBuilder.getQuery()),
            umwelt.getId());

        for (Messwert messwert: messwerte) {
            if (!authorization.isAuthorized(
                messwert,
                RequestMethod.PUT,
                Messwert.class)
            ) {
                return new Response(false, StatusCodes.NOT_ALLOWED, null);
            }
            if (lock.isLocked(messwert)) {
                return new Response(false, StatusCodes.CHANGED_VALUE, null);
            }
            Violation violation = validator.validate(messwert);
            if (violation.hasErrors()) {
                Response response =
                    new Response(false, StatusCodes.ERROR_VALIDATION, messwert);
                response.setErrors(violation.getErrors());
                response.setWarnings(violation.getWarnings());
                response.setNotifications(violation.getNotifications());
                return response;
            }
            Response response = repository.update(messwert);
            if (!response.getSuccess()) {
                return response;
            }
            Response updated = repository.getById(
                Messwert.class,
                ((Messwert) response.getData()).getId());
            if (violation.hasWarnings()) {
                updated.setWarnings(violation.getWarnings());
            }
            if (violation.hasNotifications()) {
                updated.setNotifications(violation.getNotifications());
            }
            authorization.filter(
                    updated,
                    Messwert.class);
        }
        return new Response(true, StatusCodes.OK, messwerte);
    }

    /**
     * Delete an existing Messwert object by id.
     * <p>
     * The id is appended to the URL as a path parameter.
     * <p>
     * Example: http://example.com/messwert/{id}
     *
     * @return Response object.
     */
    @DELETE
    @Path("/{id}")
    public Response delete(
        @PathParam("id") String id
    ) {
        /* Get the messwert object by id*/
        Response messwert =
            repository.getById(
                Messwert.class, Integer.valueOf(id));
        Messwert messwertObj = (Messwert) messwert.getData();
        if (!authorization.isAuthorized(
                messwertObj,
                RequestMethod.DELETE,
                Messwert.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }
        if (lock.isLocked(messwertObj)) {
            return new Response(false, StatusCodes.NO_ACCESS, null);
        }
        /* Delete the messwert object*/
        return repository.delete(messwertObj);
    }
}
