/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.util.Collection;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import de.intevation.lada.lock.TimestampLocker;
import de.intevation.lada.model.lada.BelongsToSample;
import de.intevation.lada.model.lada.MeasVal;
import de.intevation.lada.model.lada.MeasVal_;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Names;
import de.intevation.lada.model.lada.StatusProt;
import de.intevation.lada.model.lada.StatusProt_;
import de.intevation.lada.model.master.StatusMp;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.rest.RequestMethod;


/**
 * REST service for StatusProt objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "statusprot")
public class StatusProtService extends LadaIntegerIdEntityService {

    /**
     * The object lock mechanism.
     */
    @Inject
    private TimestampLocker<BelongsToSample> lock;

    /**
     * Get StatusProt objects.
     *
     * @param measmId The requested objects have to be filtered
     * using an URL parameter named measmId.
     *
     * @return requested objects.
     */
    @GET
    public Collection<StatusProt> get(
        @QueryParam("measmId") @NotNull Integer measmId
    ) {
        Measm messung = repository.getById(Measm.class, measmId);
        authorization.authorize(messung, RequestMethod.GET);

        return messung.getStatusProts();
    }

    /**
     * Get a single StatusProt object by id.
     *
     * @return a single StatusProt.
     */
    @GET
    @Path("{id}")
    public StatusProt getById() {
        return authorization.authorize(
            repository.getById(StatusProt.class, id),
            RequestMethod.GET);
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
        Measm messung = status.getMeasm();
        lock.isLocked(messung);

        StatusMp newKombi = repository.getById(
            StatusMp.class, status.getStatusMpId());

        // 1. user user wants to reset the current status
        //    'status wert' == 8
        if (newKombi.getStatusVal().getId() == 8) {
            return resetStatus(status, messung);
        }
        // 2. user wants to set new status
        return setNewStatus(status, newKombi, messung);
    }

    private StatusProt setNewStatus(
        StatusProt status,
        StatusMp newKombi,
        Measm messung
    ) {
        boolean hasNoValidMeasVals = repository.filter(repository
                .queryBuilder(MeasVal.class)
                .andIsNull(MeasVal_.measVal)
                .andIsNull(MeasVal_.lessThanLOD)
                .not()
                .and(MeasVal_.measm, messung)
                .getQuery()
            ).isEmpty();
        if (newKombi.getStatusVal().getId() == 7 && hasNoValidMeasVals) {
            repository.entityManager()
                .createNamedQuery(Names.QUERY_DELETE_MEAS_VALS)
                .setParameter("m", messung)
                .executeUpdate();
        }

        //NOTE: The referenced messung status field is updated by a DB trigger
        return repository.create(status);
    }

    private StatusProt resetStatus(
        StatusProt newStatus,
        Measm messung
    ) {
        // Create a new Status with value = 8.
        StatusMp oldKombi = repository.getById(
            StatusMp.class, messung.getStatusProt().getStatusMpId());

        StatusMp newKombi = (StatusMp) repository.entityManager()
            .createNativeQuery("SELECT * FROM master.status_mp "
                + "WHERE status_lev_id = :statusLev AND status_val_id = 8",
                StatusMp.class)
            .setParameter("statusLev", oldKombi.getStatusLev().getId())
            .getSingleResult();
        StatusProt statusNew = new StatusProt();
        statusNew.setMeasFacilId(newStatus.getMeasFacilId());
        statusNew.setMeasm(newStatus.getMeasm());
        statusNew.setStatusMpId(newKombi.getId());
        statusNew.setText(newStatus.getText());

        repository.create(statusNew);

        if (oldKombi.getStatusLev().getId() == 1) {
            StatusProt nV = new StatusProt();
            nV.setMeasFacilId(newStatus.getMeasFacilId());
            nV.setMeasm(newStatus.getMeasm());
            nV.setStatusMpId(1);
            nV.setText(null);
            return repository.create(nV);
        }
        QueryBuilder<StatusProt> lastFilter = repository
            .queryBuilder(StatusProt.class)
            .and(StatusProt_.measm, newStatus.getMeasm());
        lastFilter.orderBy(StatusProt_.date, true);
        List<StatusProt> proto = repository.filter(lastFilter.getQuery());
        // Find a status that has "status_stufe" = "old status_stufe - 1"
        int ndx = -1;
        for (int i = proto.size() - 1; i >= 0; i--) {
            int curKom = proto.get(i).getStatusMpId();
            StatusMp sk = repository.getById(StatusMp.class, curKom);
            if (sk.getStatusLev().getId() < oldKombi.getStatusLev().getId()) {
                ndx = i;
                break;
            }
        }
        StatusProt copy = new StatusProt();
        StatusProt orig = proto.get(ndx);
        copy.setMeasFacilId(orig.getMeasFacilId());
        copy.setMeasm(orig.getMeasm());
        copy.setStatusMpId(orig.getStatusMpId());
        copy.setText(null);
        return repository.create(copy);
    }
}
