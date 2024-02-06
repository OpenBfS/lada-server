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
import de.intevation.lada.model.lada.StatusProt;
import de.intevation.lada.model.master.StatusAccessMpView;
import de.intevation.lada.model.master.StatusMp;
import de.intevation.lada.model.master.StatusVal;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.Response;

/**
 * REST service for StatusVal objects.
 * </pre>
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("statusval")
public class StatusValService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    @Inject
    @AuthorizationConfig(type = AuthorizationType.HEADER)
    private Authorization authorization;

    /**
     * Get StatusVal objects.
     *
     * @param measmId URL parameter to filter using measmId
     * @return Response object containing all StatusVal objects.
     */
    @GET
    public Response get(
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
     * @return Response object containing a single StatusVal.
     */
    @GET
    @Path("{id}")
    public Response getById(
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
    private Response getReachable(Integer messungsId) {
        Measm messung = repository.getByIdPlain(Measm.class, messungsId);

        StatusProt status = repository.getByIdPlain(
            StatusProt.class, messung.getStatus());
        StatusMp kombi = repository.getByIdPlain(
            StatusMp.class, status.getStatusMpId());

        QueryBuilder<StatusAccessMpView> errFilter = repository
            .queryBuilder(StatusAccessMpView.class)
            .andIn("statusLevId", authorization.getInfo().getFunktionen())
            .and("curLevId", kombi.getStatusLev().getId())
            .and("curValId", kombi.getStatusVal().getId());
        List<StatusAccessMpView> erreichbare = repository.filterPlain(
            errFilter.getQuery());

        QueryBuilder<StatusVal> werteFilter =
            repository.queryBuilder(StatusVal.class);
        for (StatusAccessMpView erreichbar: erreichbare) {
            werteFilter.or("id", erreichbar.getStatusValId());
        }
        return repository.filter(werteFilter.getQuery());
    }
}
