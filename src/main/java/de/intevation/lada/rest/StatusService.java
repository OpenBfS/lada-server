/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import de.intevation.lada.lock.LockConfig;
import de.intevation.lada.lock.LockType;
import de.intevation.lada.lock.ObjectLocker;
import de.intevation.lada.model.land.Messung;
import de.intevation.lada.model.land.Messwert;
import de.intevation.lada.model.land.Probe;
import de.intevation.lada.model.land.StatusProtokoll;
import de.intevation.lada.model.stammdaten.StatusKombi;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.auth.UserInfo;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.validation.Validator;
import de.intevation.lada.validation.Violation;
import de.intevation.lada.validation.annotation.ValidationConfig;

/**
 * REST service for Status objects.
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
 *      "erzeuger": [string],
 *      "messungsId": [number],
 *      "status": [number],
 *      "owner": [boolean],
 *      "readonly": [boolean],
 *      "treeModified": [timestamp],
 *      "parentModified": [timestamp],
 *      "sdatum": [timestamp],
 *      "skommentar": [string]
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
@Path("rest/status")
@RequestScoped
public class StatusService {

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
    @ValidationConfig(type = "Status")
    private Validator validator;

    @Inject
    @ValidationConfig(type = "Messwert")
    private Validator messwertValidator;

    @Inject
    @ValidationConfig(type = "Messung")
    private Validator messungValidator;

    @Inject
    @ValidationConfig(type = "Probe")
    private Validator probeValidator;

    /**
     * Get all Status objects.
     * <p>
     * The requested objects have to be filtered using an URL parameter named
     * messungsId.
     * <p>
     * Example: http://example.com/status?messungsId=[ID]
     *
     * @return Response object containing filtered Status objects.
     * Status-Code 699 if parameter is missing or requested objects are
     * not authorized.
     */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
        @Context HttpHeaders headers,
        @Context UriInfo info,
        @Context HttpServletRequest request
    ) {
        MultivaluedMap<String, String> params = info.getQueryParameters();
        if (params.isEmpty() || !params.containsKey("messungsId")) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }
        String messungId = params.getFirst("messungsId");
        int id;
        try {
            id = Integer.valueOf(messungId);
        } catch (NumberFormatException nfe) {
            return new Response(false, StatusCodes.NO_ACCESS, null);
        }

        QueryBuilder<StatusProtokoll> builder =
            repository.queryBuilder(StatusProtokoll.class);
        builder.and("messungsId", id);
        Response r = authorization.filter(
            request,
            repository.filter(builder.getQuery()),
            StatusProtokoll.class);
        if (r.getSuccess()) {
            @SuppressWarnings("unchecked")
            List<StatusProtokoll> status = (List<StatusProtokoll>) r.getData();
            for (StatusProtokoll s: status) {
                Violation violation = validator.validate(s);
                if (violation.hasErrors() || violation.hasWarnings()) {
                    s.setErrors(violation.getErrors());
                    s.setWarnings(violation.getWarnings());
                    s.setNotifications(violation.getNotifications());
                }
            }
            return new Response(true, StatusCodes.OK, status);
        } else {
            return r;
        }
    }

    /**
     * Get a single Status object by id.
     * <p>
     * The id is appended to the URL as a path parameter.
     * <p>
     * Example: http://example.com/status/{id}
     *
     * @return Response object containing a single Status.
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(
        @Context HttpHeaders headers,
        @Context HttpServletRequest request,
        @PathParam("id") String id
    ) {
        Response response = repository.getById(
            StatusProtokoll.class,
            Integer.valueOf(id)
        );

        return authorization.filter(
            request,
            response,
            StatusProtokoll.class);
    }

    /**
     * Create a Status object.
     * <p>
     * The new object is embedded in the post data as JSON formatted string.
     * <p>
     * <pre>
     * <code>
     * {
     *  "owner": [boolean],
     *  "messungsId": [number],
     *  "erzeuger": [string],
     *  "status": [number],
     *  "skommentar": [string],
     *  "treeModified":null,
     *  "parentModified":null,
     *  "sdatum": [date]
     * }
     * </code>
     * </pre>
     *
     * @return A response object containing the created Status.
     */
    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(
        @Context HttpHeaders headers,
        @Context HttpServletRequest request,
        StatusProtokoll status
    ) {
        if (status.getMessungsId() == null
            || status.getMstId() == null
        ) {
            return new Response(false, StatusCodes.VALUE_MISSING, status);
        }

        UserInfo userInfo = authorization.getInfo(request);
        Messung messung = repository.getByIdPlain(
            Messung.class, status.getMessungsId());
        if (lock.isLocked(messung)) {
            return new Response(false, StatusCodes.CHANGED_VALUE, status);
        }

        // Is user authorized to edit status at all?
        Response r = authorization.filter(
            request,
            new Response(true, StatusCodes.OK, messung),
            Messung.class);
        Messung filteredMessung = (Messung) r.getData();
        if (!filteredMessung.getStatusEdit()) {
            return new Response(false, StatusCodes.NOT_ALLOWED, status);
        }

        if (messung.getStatus() == null) {
            // set the first status as default
            status.setStatusKombi(1);
            return new Response(false, StatusCodes.OP_NOT_POSSIBLE, status);
        } else {
            StatusProtokoll oldStatus = repository.getByIdPlain(
                StatusProtokoll.class, messung.getStatus());
            StatusKombi newKombi =
                repository.getByIdPlain(
                    StatusKombi.class, status.getStatusKombi());

            // Check if the user is allowed to change to the requested
            // status_kombi
            if (userInfo.getFunktionenForMst(
                    status.getMstId()).contains(
                        newKombi.getStatusStufe().getId())
                && (newKombi.getStatusStufe().getId().equals(1)
                    && messung.getStatusEditMst()
                    || newKombi.getStatusStufe().getId().equals(2)
                    && messung.getStatusEditLand()
                    || newKombi.getStatusStufe().getId().equals(3)
                    && messung.getStatusEditLst())
                ) {
                // 1. user user wants to reset the current status
                //    'status wert' == 8
                if (newKombi.getStatusWert().getId() == 8) {
                    return authorization.filter(
                        request,
                        resetStatus(status, oldStatus, messung),
                        StatusProtokoll.class);
                } else {
                    // 2. user wants to set new status
                    return setNewStatus(status, newKombi, messung, request);
                }
            } else {
                // Not allowed.
                return new Response(false, StatusCodes.NOT_ALLOWED, status);
            }
        }
    }

    private Response setNewStatus(
        StatusProtokoll status,
        StatusKombi newKombi,
        Messung messung,
        HttpServletRequest request
    ) {
        Violation violation = new Violation();
        Violation violationCollection = new Violation();
        int newStatusWert = newKombi.getStatusWert().getId();
        if (newStatusWert == 1
            || newStatusWert == 2
            || newStatusWert == 7
        ) {
            Probe probe = repository.getByIdPlain(
                Probe.class, messung.getProbeId());
            // init violation_collection with probe validation
            Violation probeViolation = probeValidator.validate(probe);
            violationCollection.addErrors(probeViolation.getErrors());
            violationCollection.addWarnings(probeViolation.getWarnings());
            violationCollection.addNotifications(
                probeViolation.getNotifications());

            //validate messung object
            violation  = messungValidator.validate(messung);
            violationCollection.addErrors(violation.getErrors());
            violationCollection.addWarnings(violation.getWarnings());
            violationCollection.addNotifications(violation.getNotifications());

            //validate messwert objects
            QueryBuilder<Messwert> builder =
                repository.queryBuilder(Messwert.class);
            builder.and("messungsId", messung.getId());
            Response messwertQry =
                repository.filter(builder.getQuery());
            @SuppressWarnings("unchecked")
            List<Messwert> messwerte = (List<Messwert>) messwertQry.getData();
            boolean hasValidMesswerte = false;
            if (!messwerte.isEmpty()) {
            for (Messwert messwert: messwerte) {
                violation = messwertValidator.validate(messwert);

                boolean hasNoMesswert = false;

                if (messwert.getMesswert() == null
                     && messwert.getMesswertNwg() == null) {
                     hasNoMesswert = true;
                }
                if (newStatusWert == 7
                    && !hasNoMesswert
                ) {
                    hasValidMesswerte = true;
                    Violation error = new Violation();
                    error.addError("status", StatusCodes.STATUS_RO);
                    violation.addErrors(error.getErrors());
                }
                if (violation.hasErrors() || violation.hasWarnings()) {
                    violationCollection.addErrors(violation.getErrors());
                    violationCollection.addWarnings(violation.getWarnings());
                }
                violationCollection.addNotifications(
                    violation.getNotifications());
                if (hasValidMesswerte) {
                    break;
                }
            }
            } else if (newStatusWert != 7) {
                    Violation error = new Violation();
                    error.addError("messwert", StatusCodes.VALUE_MISSING);
                    violation.addErrors(error.getErrors());
                    violationCollection.addErrors(violation.getErrors());
            }
            if (newStatusWert == 7 && !hasValidMesswerte) {
                for (int i = 0; i < messwerte.size(); i++) {
                    repository.delete(messwerte.get(i));
                }
            }
            // validate statusobject
            violation = validator.validate(status);
            violationCollection.addErrors(violation.getErrors());
            violationCollection.addWarnings(violation.getWarnings());
            violationCollection.addNotifications(violation.getNotifications());

            if (newStatusWert != 7
                && (violationCollection.hasErrors()
                || violationCollection.hasWarnings())
            ) {
                Response response =
                    new Response(false, StatusCodes.ERROR_MERGING, status);
                response.setErrors(violationCollection.getErrors());
                response.setWarnings(violationCollection.getWarnings());
                response.setNotifications(
                    violationCollection.getNotifications());
                return response;
            } else if (newStatusWert == 7
                && (probeViolation.hasErrors()
                || probeViolation.hasWarnings())
            ) {
                Response response =
                new Response(false, StatusCodes.ERROR_MERGING, status);
                response.setErrors(violationCollection.getErrors());
                response.setWarnings(violationCollection.getWarnings());
                response.setNotifications(
                    violationCollection.getNotifications());
                return response;
            }
        }
        //Set datum to null to use database timestamp
        status.setDatum(null);
        Response response = repository.create(status);
        //NOTE: The referenced messung status field is updated by a DB trigger
        if (violationCollection != null) {
            response.setNotifications(violationCollection.getNotifications());
        }
        return authorization.filter(
            request,
            response,
            StatusProtokoll.class);
    }

    /**
     * Update an existing Status object.
     * <p>
     * The object to update should come as JSON formatted string.
     * <pre>
     * <code>
     * {
     *  "id": [number],
     *  "owner": [boolean],
     *  "messungsId": [number],
     *  "erzeuger": [string],
     *  "status": [number],
     *  "skommentar": [string],
     *  "treeModified": [timestamp],
     *  "parentModified": [timestamp],
     *  "sdatum": [date]
     * }
     * </code>
     * </pre>
     *
     * @return Response object containing the updated Status object.
     */
    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(
        @Context HttpHeaders headers,
        @Context HttpServletRequest request,
        @PathParam("id") String id,
        StatusProtokoll status
    ) {
        return new Response(false, StatusCodes.NOT_ALLOWED, status);
    }

    /**
     * Delete an existing Status object by id.
     * <p>
     * The id is appended to the URL as a path parameter.
     * <p>
     * Example: http://example.com/status/{id}
     *
     * @return Response object.
     */
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(
        @Context HttpHeaders headers,
        @Context HttpServletRequest request,
        @PathParam("id") String id
    ) {
        /* Get the object by id*/
        Response object =
            repository.getById(
                StatusProtokoll.class, Integer.valueOf(id));
        StatusProtokoll obj = (StatusProtokoll) object.getData();
        if (!authorization.isAuthorized(
                request,
                obj,
                RequestMethod.DELETE,
                StatusProtokoll.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }
        if (lock.isLocked(obj)) {
            return new Response(false, StatusCodes.CHANGED_VALUE, null);
        }
        /* Delete the object*/
        return repository.delete(obj);
    }

    private Response resetStatus(
        StatusProtokoll newStatus,
        StatusProtokoll oldStatus,
        Messung messung
    ) {
        // Create a new Status with value = 8.
        QueryBuilder<StatusKombi> kombiFilter =
            repository.queryBuilder(StatusKombi.class);
        StatusKombi oldKombi =
            repository.getByIdPlain(
                StatusKombi.class, oldStatus.getStatusKombi());

        kombiFilter.and("statusStufe", oldKombi.getStatusStufe().getId());
        kombiFilter.and("statusWert", 8);
        List<StatusKombi> newKombi =
            repository.filterPlain(kombiFilter.getQuery());
        StatusProtokoll statusNew = new StatusProtokoll();
        statusNew.setDatum(new Timestamp(new Date().getTime()));
        statusNew.setMstId(newStatus.getMstId());
        statusNew.setMessungsId(newStatus.getMessungsId());
        statusNew.setStatusKombi(newKombi.get(0).getId());
        statusNew.setText(newStatus.getText());

        repository.create(statusNew);

        Response retValue;
        StatusKombi kombi = repository.getByIdPlain(
            StatusKombi.class,
            oldStatus.getStatusKombi()
        );
        if (kombi.getStatusStufe().getId() == 1) {
            StatusProtokoll nV = new StatusProtokoll();
            nV.setDatum(new Timestamp(new Date().getTime()));
            nV.setMstId(newStatus.getMstId());
            nV.setMessungsId(newStatus.getMessungsId());
            nV.setStatusKombi(1);
            nV.setText("");
            retValue = repository.create(nV);
        } else {
            QueryBuilder<StatusProtokoll> lastFilter =
                repository.queryBuilder(StatusProtokoll.class);
            lastFilter.and("messungsId", newStatus.getMessungsId());
            lastFilter.orderBy("datum", true);
            List<StatusProtokoll> proto =
                repository.filterPlain(lastFilter.getQuery());
            // Find a status that has "status_stufe" = "old status_stufe - 1"
            int ndx = -1;
            for (int i = proto.size() - 1; i >= 0; i--) {
                int curKom = proto.get(i).getStatusKombi();
                StatusKombi sk =
                    repository.getByIdPlain(
                        StatusKombi.class, curKom);
                if (sk.getStatusStufe().getId()
                    < kombi.getStatusStufe().getId()
                ) {
                    ndx = i;
                    break;
                }
            }
            StatusProtokoll copy = new StatusProtokoll();
            StatusProtokoll orig = proto.get(ndx);
            copy.setDatum(new Timestamp(new Date().getTime()));
            copy.setMstId(orig.getMstId());
            copy.setMessungsId(orig.getMessungsId());
            copy.setStatusKombi(orig.getStatusKombi());
            copy.setText("");
            retValue = repository.create(copy);
        }
        return retValue;
    }
}
