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

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import de.intevation.lada.lock.LockConfig;
import de.intevation.lada.lock.LockType;
import de.intevation.lada.lock.ObjectLocker;
import de.intevation.lada.model.land.Measm;
import de.intevation.lada.model.land.MeasVal;
import de.intevation.lada.model.land.Geolocat;
import de.intevation.lada.model.land.Sample;
import de.intevation.lada.model.land.StatusProtokoll;
import de.intevation.lada.model.master.Site;
import de.intevation.lada.model.master.StatusMp;
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
public class StatusService extends LadaService {

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
    @ValidationConfig(type = "Sample")
    private Validator probeValidator;

    @Inject
    @ValidationConfig(type = "Ort")
    private Validator ortValidator;

    /**
     * Get StatusProtokoll objects.
     *
     * @param messungsId The requested objects have to be filtered
     * using an URL parameter named messungsId.
     * Example: http://example.com/status?messungsId=[ID]
     *
     * @return Response containing requested objects.
     * Status-Code 699 if parameter is missing.
     */
    @GET
    @Path("/")
    public Response get(
        @QueryParam("messungsId") @NotNull Integer messungsId
    ) {
        QueryBuilder<StatusProtokoll> builder =
            repository.queryBuilder(StatusProtokoll.class);
        builder.and("measmId", messungsId);
        Response r = authorization.filter(
            repository.filter(builder.getQuery()),
            StatusProtokoll.class);
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
    }

    /**
     * Get a single Status object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object containing a single Status.
     */
    @GET
    @Path("/{id}")
    public Response getById(
        @PathParam("id") Integer id
    ) {
        Response response = repository.getById(StatusProtokoll.class, id);
        return authorization.filter(
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
    public Response create(
        StatusProtokoll status
    ) {
        if (status.getMeasmId() == null
            || status.getMeasFacilId() == null
        ) {
            return new Response(false, StatusCodes.VALUE_MISSING, status);
        }

        UserInfo userInfo = authorization.getInfo();
        Measm messung = repository.getByIdPlain(
            Measm.class, status.getMeasmId());
        if (lock.isLocked(messung)) {
            return new Response(false, StatusCodes.CHANGED_VALUE, status);
        }

        // Is user authorized to edit status at all?
        Response r = authorization.filter(
            new Response(true, StatusCodes.OK, messung),
            Measm.class);
        Measm filteredMessung = (Measm) r.getData();
        if (!filteredMessung.getStatusEdit()) {
            return new Response(false, StatusCodes.NOT_ALLOWED, status);
        }

        if (messung.getStatus() == null) {
            // set the first status as default
            status.setStatusComb(1);
            return new Response(false, StatusCodes.OP_NOT_POSSIBLE, status);
        } else {
            StatusProtokoll oldStatus = repository.getByIdPlain(
                StatusProtokoll.class, messung.getStatus());
            StatusMp newKombi =
                repository.getByIdPlain(
                    StatusMp.class, status.getStatusComb());

            // Check if the user is allowed to change to the requested
            // status_kombi
            if (userInfo.getFunktionenForMst(
                    status.getMeasFacilId()).contains(
                        newKombi.getStatusLev().getId())
                && (newKombi.getStatusLev().getId().equals(1)
                    && messung.getStatusEditMst()
                    || newKombi.getStatusLev().getId().equals(2)
                    && messung.getStatusEditLand()
                    || newKombi.getStatusLev().getId().equals(3)
                    && messung.getStatusEditLst())
                ) {
                // 1. user user wants to reset the current status
                //    'status wert' == 8
                if (newKombi.getStatusVal().getId() == 8) {
                    return authorization.filter(
                        resetStatus(status, oldStatus, messung),
                        StatusProtokoll.class);
                } else {
                    // 2. user wants to set new status
                    return setNewStatus(status, newKombi, messung);
                }
            } else {
                // Not allowed.
                return new Response(false, StatusCodes.NOT_ALLOWED, status);
            }
        }
    }

    private Response setNewStatus(
        StatusProtokoll status,
        StatusMp newKombi,
        Measm messung
    ) {
        Violation violation = new Violation();
        Violation violationCollection = new Violation();
        int newStatusWert = newKombi.getStatusVal().getId();
        if (newStatusWert == 1
            || newStatusWert == 2
            || newStatusWert == 7
        ) {
            Sample probe = repository.getByIdPlain(
                Sample.class, messung.getSampleId());
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
            QueryBuilder<MeasVal> builder =
                repository.queryBuilder(MeasVal.class);
            builder.and("measmId", messung.getId());
            Response messwertQry =
                repository.filter(builder.getQuery());
            @SuppressWarnings("unchecked")
            List<MeasVal> messwerte = (List<MeasVal>) messwertQry.getData();
            boolean hasValidMesswerte = false;
            if (!messwerte.isEmpty()) {
            for (MeasVal messwert: messwerte) {
                violation = messwertValidator.validate(messwert);

                boolean hasNoMesswert = false;

                if (messwert.getMeasVal() == null
                     && messwert.getLessThanLOD() == null) {
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

            // validate orte
            QueryBuilder<Geolocat> ortBuilder =
                repository.queryBuilder(Geolocat.class);
                ortBuilder.and("sampleId", probe.getId());
            List<Geolocat> assignedOrte = repository.filterPlain(ortBuilder.getQuery());

            for (Geolocat o : assignedOrte){
                violation = ortValidator.validate(repository.getByIdPlain(Site.class, o.getSiteId()));
                violationCollection.addErrors(violation.getErrors());
                violationCollection.addWarnings(violation.getWarnings());
                violationCollection.addNotifications(violation.getNotifications());
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
        status.setDate(null);
        Response response = repository.create(status);
        //NOTE: The referenced messung status field is updated by a DB trigger
        if (violationCollection != null) {
            response.setNotifications(violationCollection.getNotifications());
        }
        return authorization.filter(
            response,
            StatusProtokoll.class);
    }

    /**
     * Delete an existing Status object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object.
     */
    @DELETE
    @Path("/{id}")
    public Response delete(
        @PathParam("id") Integer id
    ) {
        StatusProtokoll obj = repository.getByIdPlain(
            StatusProtokoll.class, id);
        if (!authorization.isAuthorized(
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
        Measm messung
    ) {
        // Create a new Status with value = 8.
        QueryBuilder<StatusMp> kombiFilter =
            repository.queryBuilder(StatusMp.class);
        StatusMp oldKombi =
            repository.getByIdPlain(
                StatusMp.class, oldStatus.getStatusComb());

        kombiFilter.and("statusLev", oldKombi.getStatusLev().getId());
        kombiFilter.and("statusVal", 8);
        List<StatusMp> newKombi =
            repository.filterPlain(kombiFilter.getQuery());
        StatusProtokoll statusNew = new StatusProtokoll();
        statusNew.setDate(new Timestamp(new Date().getTime()));
        statusNew.setMeasFacilId(newStatus.getMeasFacilId());
        statusNew.setMeasmId(newStatus.getMeasmId());
        statusNew.setStatusComb(newKombi.get(0).getId());
        statusNew.setText(newStatus.getText());

        repository.create(statusNew);

        Response retValue;
        StatusMp kombi = repository.getByIdPlain(
            StatusMp.class,
            oldStatus.getStatusComb()
        );
        if (kombi.getStatusLev().getId() == 1) {
            StatusProtokoll nV = new StatusProtokoll();
            nV.setDate(new Timestamp(new Date().getTime()));
            nV.setMeasFacilId(newStatus.getMeasFacilId());
            nV.setMeasmId(newStatus.getMeasmId());
            nV.setStatusComb(1);
            nV.setText("");
            retValue = repository.create(nV);
        } else {
            QueryBuilder<StatusProtokoll> lastFilter =
                repository.queryBuilder(StatusProtokoll.class);
            lastFilter.and("measmId", newStatus.getMeasmId());
            lastFilter.orderBy("date", true);
            List<StatusProtokoll> proto =
                repository.filterPlain(lastFilter.getQuery());
            // Find a status that has "status_stufe" = "old status_stufe - 1"
            int ndx = -1;
            for (int i = proto.size() - 1; i >= 0; i--) {
                int curKom = proto.get(i).getStatusComb();
                StatusMp sk =
                    repository.getByIdPlain(
                        StatusMp.class, curKom);
                if (sk.getStatusLev().getId()
                    < kombi.getStatusLev().getId()
                ) {
                    ndx = i;
                    break;
                }
            }
            StatusProtokoll copy = new StatusProtokoll();
            StatusProtokoll orig = proto.get(ndx);
            copy.setDate(new Timestamp(new Date().getTime()));
            copy.setMeasFacilId(orig.getMeasFacilId());
            copy.setMeasmId(orig.getMeasmId());
            copy.setStatusComb(orig.getStatusComb());
            copy.setText("");
            retValue = repository.create(copy);
        }
        return retValue;
    }
}
