/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest.stamm;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import de.intevation.lada.model.land.Messung;
import de.intevation.lada.model.land.StatusProtokoll;
import de.intevation.lada.model.stammdaten.StatusErreichbar;
import de.intevation.lada.model.stammdaten.StatusKombi;
import de.intevation.lada.model.stammdaten.StatusWert;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.rest.LadaService;

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
public class StatusWertService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    @Inject
    @AuthorizationConfig(type = AuthorizationType.HEADER)
    private Authorization authorization;

    /**
     * Get StatusWert objects.
     *
     * @param messungsId URL parameter to filter using messungsId
     * @return Response object containing all StatusWert objects.
     */
    @GET
    @Path("/")
    public Response get(
        @QueryParam("messungsId") Integer messungsId
    ) {
        if (messungsId == null) {
            return repository.getAll(StatusWert.class);
        }
        return getReachable(messungsId);
    }

    /**
     * Get a single StatusWert object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object containing a single StatusWert.
     */
    @GET
    @Path("/{id}")
    public Response getById(
        @PathParam("id") Integer id
    ) {
        return repository.getById(StatusWert.class, id);
    }

    /**
     * Get the list of possible status values following the actual status
     * value of the Messung represented by the given ID.
     *
     * @param messungsId Id of a Messung instance
     * @return Possible status values for given Messung
     */
    private Response getReachable(Integer messungsId) {
        Messung messung = repository.getByIdPlain(Messung.class, messungsId);

        StatusProtokoll status = repository.getByIdPlain(
            StatusProtokoll.class, messung.getStatus());
        StatusKombi kombi = repository.getByIdPlain(
            StatusKombi.class, status.getStatusKombi());

        QueryBuilder<StatusErreichbar> errFilter = repository
            .queryBuilder(StatusErreichbar.class)
            .andIn("stufeId", authorization.getInfo().getFunktionen())
            .and("curStufe", kombi.getStatusStufe().getId())
            .and("curWert", kombi.getStatusWert().getId());
        List<StatusErreichbar> erreichbare = repository.filterPlain(
            errFilter.getQuery());

        QueryBuilder<StatusWert> werteFilter =
            repository.queryBuilder(StatusWert.class);
        for (StatusErreichbar erreichbar: erreichbare) {
            werteFilter.or("id", erreichbar.getWertId());
        }
        return repository.filter(werteFilter.getQuery());
    }
}
