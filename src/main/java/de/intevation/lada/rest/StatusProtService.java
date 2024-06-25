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
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

import de.intevation.lada.lock.LockConfig;
import de.intevation.lada.lock.LockType;
import de.intevation.lada.lock.ObjectLocker;
import de.intevation.lada.model.lada.MeasVal;
import de.intevation.lada.model.lada.MeasVal_;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.StatusProt;
import de.intevation.lada.model.lada.StatusProt_;
import de.intevation.lada.model.master.StatusMp;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.auth.UserInfo;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;


/**
 * REST service for StatusProt objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "statusprot")
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

    /**
     * Get StatusProt objects.
     *
     * @param measmId The requested objects have to be filtered
     * using an URL parameter named measmId.
     *
     * @return requested objects.
     * Status-Code 699 if parameter is missing.
     */
    @GET
    public List<StatusProt> get(
        @QueryParam("measmId") @NotNull Integer measmId
    ) {
        QueryBuilder<StatusProt> builder = repository
            .queryBuilder(StatusProt.class)
            .and(StatusProt_.measmId, measmId);
        return authorization.filter(
            repository.filter(builder.getQuery()),
            StatusProt.class);
    }

    /**
     * Get a single StatusProt object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return a single StatusProt.
     */
    @GET
    @Path("{id}")
    public StatusProt getById(
        @PathParam("id") Integer id
    ) {
        return authorization.filter(
            repository.getById(StatusProt.class, id),
            StatusProt.class);
    }

    /**
     * Create a StatusProt object.
     *
     * @return A response object containing the created StatusProt.
     * @throws BadRequestException if any constraint violations are detected.
     */
    @POST
    public StatusProt create(
        @Valid StatusProt status
    ) throws BadRequestException {
        UserInfo userInfo = authorization.getInfo();
        Measm messung = repository.getById(
            Measm.class, status.getMeasmId());
        lock.isLocked(messung);

        // Is user authorized to edit status at all?
        // TODO: Move to authorization
        Measm filteredMessung = authorization.filter(messung, Measm.class);
        if (!filteredMessung.getStatusEdit()) {
            throw new ForbiddenException();
        }

        StatusProt oldStatus = repository.getById(
            StatusProt.class, messung.getStatus());
        StatusMp newKombi = repository.getById(
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
            }
            // 2. user wants to set new status
            return setNewStatus(status, newKombi, messung);
        }
        throw new ForbiddenException();
    }

    private StatusProt setNewStatus(
        StatusProt status,
        StatusMp newKombi,
        Measm messung
    ) {
        boolean hasValidMesswerte = repository.filter(repository
                .queryBuilder(MeasVal.class)
                .andIsNull(MeasVal_.measVal)
                .andIsNull(MeasVal_.lessThanLOD)
                .not()
                .and(MeasVal_.measmId, messung.getId())
                .getQuery()
            ).isEmpty();
        if (newKombi.getStatusVal().getId() == 7 && !hasValidMesswerte) {
            List<MeasVal> messwerte = repository.filter(repository
                .queryBuilder(MeasVal.class)
                .and(MeasVal_.measmId, messung.getId())
                .getQuery());
            for (MeasVal measVal : messwerte) {
                repository.delete(measVal);
            }
        }

        //Set datum to null to use database timestamp
        status.setDate(null);

        //NOTE: The referenced messung status field is updated by a DB trigger
        return authorization.filter(
            repository.create(status),
            StatusProt.class);
    }

    private StatusProt resetStatus(
        StatusProt newStatus,
        StatusProt oldStatus,
        Measm messung
    ) {
        // Create a new Status with value = 8.
        StatusMp oldKombi = repository.getById(
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

        StatusMp kombi = repository.getById(
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
            return repository.create(nV);
        }
        QueryBuilder<StatusProt> lastFilter = repository
            .queryBuilder(StatusProt.class)
            .and(StatusProt_.measmId, newStatus.getMeasmId());
        lastFilter.orderBy(StatusProt_.date, true);
        List<StatusProt> proto = repository.filter(lastFilter.getQuery());
        // Find a status that has "status_stufe" = "old status_stufe - 1"
        int ndx = -1;
        for (int i = proto.size() - 1; i >= 0; i--) {
            int curKom = proto.get(i).getStatusMpId();
            StatusMp sk = repository.getById(StatusMp.class, curKom);
            if (sk.getStatusLev().getId() < kombi.getStatusLev().getId()) {
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
        return repository.create(copy);
    }
}
