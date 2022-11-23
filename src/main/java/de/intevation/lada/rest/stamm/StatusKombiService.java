/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest.stamm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonNumber;
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
import de.intevation.lada.util.auth.UserInfo;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.rest.LadaService;

/**
 * REST service for StatusKombi objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("rest/statuskombi")
public class StatusKombiService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    @Inject
    @AuthorizationConfig(type = AuthorizationType.HEADER)
    private Authorization authorization;

    /**
     * Get all StatusKombi objects.
     * <p>
     * Example: http://example.com/statuskombi
     *
     * @return Response object containing all StatusStufe objects.
     */
    @GET
    @Path("/")
    public Response get() {
        return repository.getAll(StatusMp.class);
    }

    /**
     * Get a single StatusKombi object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object
     */
    @GET
    @Path("/{id}")
    public Response getById(
        @PathParam("id") Integer id
    ) {
        return repository.getById(StatusMp.class, id);
    }

    @POST
    @Path("/getbyids")
    public Response getById(
        JsonArray ids
    ) {
        UserInfo user = authorization.getInfo();
        List<JsonNumber> idList = ids.getValuesAs(JsonNumber.class);
        List<Integer> intList = new ArrayList<>();
        for (JsonNumber id : idList) {
            intList.add(id.intValue());
        }
        return new Response(true, StatusCodes.OK, getReachable(intList, user));
    }

    /**
     * Get the list of possible status values following the actual status
     * values of the Messungen represented by the given IDs.
     *
     * @return Disjunction of possible status values for all Messungen
     */
    private List<StatusMp> getReachable(
        List<Integer> messIds,
        UserInfo user
    ) {
        QueryBuilder<Measm> messungQuery =
            repository.queryBuilder(Measm.class);
        messungQuery.orIn("id", messIds);
        List<Measm> messungen = repository.filterPlain(
            messungQuery.getQuery());

        Map<Integer, StatusAccessMpView> erreichbare =
            new HashMap<Integer, StatusAccessMpView>();
        for (Measm messung : messungen) {
            StatusProt status = repository.getByIdPlain(
                StatusProt.class, messung.getStatus());
            StatusMp kombi = repository.getByIdPlain(
                StatusMp.class, status.getStatusComb());

            QueryBuilder<StatusAccessMpView> errFilter =
                repository.queryBuilder(StatusAccessMpView.class);
            errFilter.andIn("levId", user.getFunktionen());
            errFilter.and("curLevId", kombi.getStatusLev().getId());
            errFilter.and("curValId", kombi.getStatusVal().getId());
            List<StatusAccessMpView> err = repository.filterPlain(
                    errFilter.getQuery());
            for (StatusAccessMpView e : err) {
                erreichbare.put(e.getId(), e);
            }
        }

        if (erreichbare.size() == 0) {
            return new ArrayList<StatusMp>();
        }

        QueryBuilder<StatusMp> kombiFilter =
            repository.queryBuilder(StatusMp.class);
        for (Entry<Integer, StatusAccessMpView> erreichbar
            : erreichbare.entrySet()
        ) {
                QueryBuilder<StatusMp> tmp = kombiFilter.getEmptyBuilder();
                tmp.and("statusVal", erreichbar.getValue().getValId())
                    .and("statusLev", erreichbar.getValue().getLevId());
                kombiFilter.or(tmp);
        }

        return repository.filterPlain(kombiFilter.getQuery());
    }
}
