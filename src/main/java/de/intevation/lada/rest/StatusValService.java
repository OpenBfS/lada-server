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
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.master.StatusAccessMpView;
import de.intevation.lada.model.master.StatusAccessMpView_;
import de.intevation.lada.model.master.StatusMp;
import de.intevation.lada.model.master.StatusVal;
import de.intevation.lada.model.master.StatusVal_;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;

/**
 * REST service for StatusVal objects.
 * </pre>
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "statusval")
public class StatusValService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    /**
     * Get StatusVal objects.
     *
     * @param measmId URL parameter to filter using measmId
     * @return all StatusVal objects.
     */
    @GET
    public List<StatusVal> get(
        @QueryParam("measmId") Integer measmId
    ) {
        if (measmId == null) {
            return repository.getAll(StatusVal.class);
        }
        return getReachable(measmId);
    }

    /**
     * Get a single StatusVal object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return a single StatusVal.
     */
    @GET
    @Path("{id}")
    public StatusVal getById(
        @PathParam("id") Integer id
    ) {
        return repository.getById(StatusVal.class, id);
    }

    /**
     * Get the list of possible status values following the actual status
     * value of the Messung represented by the given ID.
     *
     * @param messungsId Id of a Messung instance
     * @return Possible status values for given Messung
     */
    private List<StatusVal> getReachable(Integer messungsId) {
        Measm messung = repository.getById(Measm.class, messungsId);

        StatusMp kombi = repository.getById(
            StatusMp.class, messung.getStatusProt().getStatusMpId());

        QueryBuilder<StatusAccessMpView> errFilter = repository
            .queryBuilder(StatusAccessMpView.class)
            .andIn(StatusAccessMpView_.statusLevId,
                authorization.getInfo().getFunktionen())
            .and(StatusAccessMpView_.curLevId, kombi.getStatusLev().getId())
            .and(StatusAccessMpView_.curValId, kombi.getStatusVal().getId());
        List<StatusAccessMpView> erreichbare = repository.filter(
            errFilter.getQuery());

        QueryBuilder<StatusVal> werteFilter =
            repository.queryBuilder(StatusVal.class);
        for (StatusAccessMpView erreichbar: erreichbare) {
            werteFilter.or(StatusVal_.id, erreichbar.getStatusValId());
        }
        return repository.filter(werteFilter.getQuery());
    }
}
