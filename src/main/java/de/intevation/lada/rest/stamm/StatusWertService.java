/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest.stamm;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import de.intevation.lada.model.land.Messung;
import de.intevation.lada.model.land.StatusProtokoll;
import de.intevation.lada.model.stammdaten.StatusErreichbar;
import de.intevation.lada.model.stammdaten.StatusKombi;
import de.intevation.lada.model.stammdaten.StatusWert;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.auth.UserInfo;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.Response;

/**
 * REST service for StatusWert objects.
 * <p>
 * The services produce data in the application/json media type.
 * A typical response holds information about the action performed and the data.
 * <pre>
 * <code>
 * {
 *  "success": [boolean];
 *  "message": [string],
 *  "data":[{
 *      "id": [number],
 *      "wert": [string],
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
@Path("rest/statuswert")
@RequestScoped
public class StatusWertService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    @Inject
    @AuthorizationConfig(type = AuthorizationType.HEADER)
    private Authorization authorization;

    /**
     * Get all StatusWert objects.
     * <p>
     * Example: http://example.com/statuwert
     *
     * @return Response object containing all StatusWert objects.
     */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
        @Context HttpHeaders headers,
        @Context HttpServletRequest request,
        @Context UriInfo info
    ) {
        MultivaluedMap<String, String> params = info.getQueryParameters();
        if (params.isEmpty() || !params.containsKey("messungsId")) {
            return repository.getAll(StatusWert.class);
        }

        List<Integer> mIds = new ArrayList<Integer>();
        for (String messId : params.getFirst("messungsId").split(",")) {
            try {
                mIds.add(Integer.valueOf(messId));
            } catch (NumberFormatException nfe) {
                return
                    new Response(false, StatusCodes.VALUE_OUTSIDE_RANGE, null);
            }
        }
        UserInfo user = authorization.getInfo(request);
        List<StatusWert> werte = getReachable(mIds, user);
        Response response = new Response(true, StatusCodes.OK, werte);
        return response;
    }

    /**
     * Get a single StatusWert object by id.
     * <p>
     * The id is appended to the URL as a path parameter.
     * <p>
     * Example: http://example.com/statuswert/{id}
     *
     * @return Response object containing a single StatusWert.
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(
        @Context HttpHeaders headers,
        @PathParam("id") String id
    ) {
        return repository.getById(
            StatusWert.class,
            Integer.valueOf(id)
        );
    }

    /**
     * Get the list of possible status values following the actual status
     * values of the Messungen represented by the given IDs.
     *
     * @return Disjunction of possible status values for all Messungen
     */
    private List<StatusWert> getReachable(
        List<Integer> messIds,
        UserInfo user
    ) {
        QueryBuilder<Messung> messungQuery =
            repository.queryBuilder(Messung.class);
        messungQuery.orIn("id", messIds);
        List<Messung> messungen = repository.filterPlain(
            messungQuery.getQuery());

        List<StatusErreichbar> erreichbare = new ArrayList<StatusErreichbar>();
        for (Messung messung : messungen) {
            StatusProtokoll status = repository.getByIdPlain(
                StatusProtokoll.class, messung.getStatus());
            StatusKombi kombi = repository.getByIdPlain(
                StatusKombi.class, status.getStatusKombi());

            QueryBuilder<StatusErreichbar> errFilter =
                repository.queryBuilder(StatusErreichbar.class);
            errFilter.andIn("stufeId", user.getFunktionen());
            errFilter.and("curStufe", kombi.getStatusStufe().getId());
            errFilter.and("curWert", kombi.getStatusWert().getId());
            erreichbare.addAll(repository.filterPlain(
                    errFilter.getQuery()));
        }

        QueryBuilder<StatusWert> werteFilter =
            repository.queryBuilder(StatusWert.class);
        for (int i = 0; i < erreichbare.size(); i++) {
            werteFilter.or("id", erreichbare.get(i).getWertId());
        }

        return repository.filterPlain(werteFilter.getQuery());
    }
}
