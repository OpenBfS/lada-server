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

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

import de.intevation.lada.lock.LockConfig;
import de.intevation.lada.lock.LockType;
import de.intevation.lada.lock.ObjectLocker;
import de.intevation.lada.model.lada.Geolocat;
import de.intevation.lada.model.lada.MeasVal;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.lada.StatusProt;
import de.intevation.lada.model.master.Site;
import de.intevation.lada.model.master.StatusMp;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.auth.UserInfo;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.validation.Validator;
import de.intevation.lada.validation.Violation;

/**
 * REST service for StatusProt objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("statusprot")
public class StatusProtService extends LadaService {

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
    private Validator<StatusProt> validator;

    @Inject
    private Validator<MeasVal> messwertValidator;

    @Inject
    private Validator<Measm> messungValidator;

    @Inject
    private Validator<Sample> probeValidator;

    @Inject
    private Validator<Site> ortValidator;

    /**
     * Get StatusProt objects.
     *
     * @param measmId The requested objects have to be filtered
     * using an URL parameter named measmId.
     *
     * @return Response containing requested objects.
     * Status-Code 699 if parameter is missing.
     */
    @GET
    public Response get(
        @QueryParam("measmId") @NotNull Integer measmId
    ) {
        QueryBuilder<StatusProt> builder =
            repository.queryBuilder(StatusProt.class);
        builder.and("measmId", measmId);
        Response r = authorization.filter(
            repository.filter(builder.getQuery()),
            StatusProt.class);
        @SuppressWarnings("unchecked")
        List<StatusProt> status = (List<StatusProt>) r.getData();
        for (StatusProt s: status) {
            validator.validate(s);
        }
        return new Response(true, StatusCodes.OK, status);
    }

    /**
     * Get a single StatusProt object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object containing a single StatusProt.
     */
    @GET
    @Path("{id}")
    public Response getById(
        @PathParam("id") Integer id
    ) {
        Response response = repository.getById(StatusProt.class, id);
        return authorization.filter(
            response,
            StatusProt.class);
    }

    /**
     * Create a StatusProt object.
     *
     * @return A response object containing the created StatusProt.
     */
    @POST
    public Response create(
        @Valid StatusProt status
    ) {
        if (status.getMeasmId() == null
            || status.getMeasFacilId() == null
        ) {
            return new Response(false, StatusCodes.VALUE_MISSING, status);
        }

        UserInfo userInfo = authorization.getInfo();
        Measm messung = repository.getByIdPlain(
            Measm.class, status.getMeasmId());
        lock.isLocked(messung);

        // Is user authorized to edit status at all?
        // TODO: Move to authorization
        Response r = authorization.filter(
            new Response(true, StatusCodes.OK, messung),
            Measm.class);
        Measm filteredMessung = (Measm) r.getData();
        if (!filteredMessung.getStatusEdit()) {
            throw new ForbiddenException();
        }

        if (messung.getStatus() == null) {
            // set the first status as default
            status.setStatusMpId(1);
            return new Response(false, StatusCodes.OP_NOT_POSSIBLE, status);
        } else {
            StatusProt oldStatus = repository.getByIdPlain(
                StatusProt.class, messung.getStatus());
            StatusMp newKombi =
                repository.getByIdPlain(
                    StatusMp.class, status.getStatusMpId());

            // Check if the user is allowed to change to the requested
            // status_kombi
            // TODO: Move to authorization
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
                        StatusProt.class);
                } else {
                    // 2. user wants to set new status
                    return setNewStatus(status, newKombi, messung);
                }
            } else {
                throw new ForbiddenException();
            }
        }
    }

    private Response setNewStatus(
        StatusProt status,
        StatusMp newKombi,
        Measm messung
    ) {
        Violation violationCollection = new Violation();
        int newStatusWert = newKombi.getStatusVal().getId();
        if (newStatusWert == 1
            || newStatusWert == 2
            || newStatusWert == 7
        ) {
            Sample probe = repository.getByIdPlain(
                Sample.class, messung.getSampleId());
            // init violation_collection with probe validation
            probeValidator.validate(probe);
            violationCollection.addErrors(probe.getErrors());
            violationCollection.addWarnings(probe.getWarnings());
            violationCollection.addNotifications(
                probe.getNotifications());

            //validate messung object
            messungValidator.validate(messung);
            violationCollection.addErrors(messung.getErrors());
            violationCollection.addWarnings(messung.getWarnings());
            violationCollection.addNotifications(messung.getNotifications());

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
                    violationCollection.addErrors(error.getErrors());
                }

                messwertValidator.validate(messwert);
                if (messwert.hasErrors() || messwert.hasWarnings()) {
                    violationCollection.addErrors(messwert.getErrors());
                    violationCollection.addWarnings(messwert.getWarnings());
                }
                violationCollection.addNotifications(
                    messwert.getNotifications());

            }
            } else if (newStatusWert != 7) {
                    Violation error = new Violation();
                    error.addError("measVal", StatusCodes.VALUE_MISSING);
                    violationCollection.addErrors(error.getErrors());
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
                Site site = repository.getByIdPlain(Site.class, o.getSiteId());
                ortValidator.validate(site);
                violationCollection.addErrors(site.getErrors());
                violationCollection.addWarnings(site.getWarnings());
                violationCollection.addNotifications(site.getNotifications());
            }

            // validate statusobject
            validator.validate(status);
            violationCollection.addErrors(status.getErrors());
            violationCollection.addWarnings(status.getWarnings());
            violationCollection.addNotifications(status.getNotifications());

            if (newStatusWert != 7
                && (violationCollection.hasErrors()
                || violationCollection.hasWarnings())
            ) {
                status.setErrors(violationCollection.getErrors());
                status.setWarnings(violationCollection.getWarnings());
                status.setNotifications(
                    violationCollection.getNotifications());
                return new Response(false, StatusCodes.ERROR_MERGING, status);
            } else if (newStatusWert == 7
                && (probe.hasErrors() || probe.hasWarnings())
            ) {
                status.setErrors(violationCollection.getErrors());
                status.setWarnings(violationCollection.getWarnings());
                status.setNotifications(
                    violationCollection.getNotifications());
                return new Response(false, StatusCodes.ERROR_MERGING, status);
            }
        }
        //Set datum to null to use database timestamp
        status.setDate(null);
        if (violationCollection != null) {
            status.setNotifications(violationCollection.getNotifications());
        }
        //NOTE: The referenced messung status field is updated by a DB trigger
        return authorization.filter(
            repository.create(status),
            StatusProt.class);
    }

    private Response resetStatus(
        StatusProt newStatus,
        StatusProt oldStatus,
        Measm messung
    ) {
        // Create a new Status with value = 8.
        StatusMp oldKombi =
            repository.getByIdPlain(
                StatusMp.class, oldStatus.getStatusMpId());

        StatusMp newKombi = (StatusMp) repository.entityManager()
            .createNativeQuery("SELECT * FROM master.status_mp "
                + "WHERE status_lev_id = :statusLev AND status_val_id = 8",
                StatusMp.class)
            .setParameter("statusLev", oldKombi.getStatusLev().getId())
            .getSingleResult();
        StatusProt statusNew = new StatusProt();
        statusNew.setDate(new Timestamp(new Date().getTime()));
        statusNew.setMeasFacilId(newStatus.getMeasFacilId());
        statusNew.setMeasmId(newStatus.getMeasmId());
        statusNew.setStatusMpId(newKombi.getId());
        statusNew.setText(newStatus.getText());

        repository.create(statusNew);

        Response retValue;
        StatusMp kombi = repository.getByIdPlain(
            StatusMp.class,
            oldStatus.getStatusMpId()
        );
        if (kombi.getStatusLev().getId() == 1) {
            StatusProt nV = new StatusProt();
            nV.setDate(new Timestamp(new Date().getTime()));
            nV.setMeasFacilId(newStatus.getMeasFacilId());
            nV.setMeasmId(newStatus.getMeasmId());
            nV.setStatusMpId(1);
            nV.setText(null);
            retValue = repository.create(nV);
        } else {
            QueryBuilder<StatusProt> lastFilter =
                repository.queryBuilder(StatusProt.class);
            lastFilter.and("measmId", newStatus.getMeasmId());
            lastFilter.orderBy("date", true);
            List<StatusProt> proto =
                repository.filterPlain(lastFilter.getQuery());
            // Find a status that has "status_stufe" = "old status_stufe - 1"
            int ndx = -1;
            for (int i = proto.size() - 1; i >= 0; i--) {
                int curKom = proto.get(i).getStatusMpId();
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
            StatusProt copy = new StatusProt();
            StatusProt orig = proto.get(ndx);
            copy.setDate(new Timestamp(new Date().getTime()));
            copy.setMeasFacilId(orig.getMeasFacilId());
            copy.setMeasmId(orig.getMeasmId());
            copy.setStatusMpId(orig.getStatusMpId());
            copy.setText(null);
            retValue = repository.create(copy);
        }
        return retValue;
    }
}
