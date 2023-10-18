/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.StatusProt;
import de.intevation.lada.model.master.StatusAccessMpView;
import de.intevation.lada.model.master.StatusMp;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.Response;

/**
 * REST service for StatusMp objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("statusmp")
public class StatusMpService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    @Inject
    @AuthorizationConfig(type = AuthorizationType.HEADER)
    private Authorization authorization;

    /**
     * Get all StatusMp objects.
     *
     * @return Response object containing all StatusMp objects.
     */
    @GET
    public Response get() {
        return repository.getAll(StatusMp.class);
    }

    /**
     * Get a single StatusMp object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object
     */
    @GET
    @Path("{id}")
    public Response getById(
        @PathParam("id") Integer id
    ) {
        return repository.getById(StatusMp.class, id);
    }

    @POST
    @Path("getbyids")
    public Response getById(
        List<Integer> messIds
    ) {
        QueryBuilder<Measm> messungQuery = repository
            .queryBuilder(Measm.class)
            .orIn("id", messIds);
        List<Measm> messungen = repository.filterPlain(
            messungQuery.getQuery());

        Map<Integer, StatusAccessMpView> erreichbare =
            new HashMap<Integer, StatusAccessMpView>();
        for (Measm messung : messungen) {
            StatusProt status = repository.getByIdPlain(
                StatusProt.class, messung.getStatus());
            StatusMp kombi = repository.getByIdPlain(
                StatusMp.class, status.getStatusMpId());

            QueryBuilder<StatusAccessMpView> errFilter = repository
                .queryBuilder(StatusAccessMpView.class)
                .andIn("statusLevId", authorization.getInfo().getFunktionen())
                .and("curLevId", kombi.getStatusLev().getId())
                .and("curValId", kombi.getStatusVal().getId());
            List<StatusAccessMpView> err = repository.filterPlain(
                errFilter.getQuery());
            for (StatusAccessMpView e : err) {
                erreichbare.put(e.getId(), e);
            }
        }

        if (erreichbare.size() == 0) {
            return new Response(
                true, StatusCodes.OK, new ArrayList<StatusMp>());
        }

        QueryBuilder<StatusMp> kombiFilter =
            repository.queryBuilder(StatusMp.class);
        for (Entry<Integer, StatusAccessMpView> erreichbar
            : erreichbare.entrySet()
        ) {
            QueryBuilder<StatusMp> tmp = kombiFilter.getEmptyBuilder()
                .and("statusVal", erreichbar.getValue().getStatusValId())
                .and("statusLev", erreichbar.getValue().getStatusLevId());
            kombiFilter.or(tmp);
        }

        return new Response(
            true,
            StatusCodes.OK,
            repository.filterPlain(kombiFilter.getQuery()));
    }
}
