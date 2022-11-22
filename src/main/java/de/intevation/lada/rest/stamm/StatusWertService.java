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

import de.intevation.lada.model.land.Measm;
import de.intevation.lada.model.land.StatusProt;
import de.intevation.lada.model.master.StatusAccessMpView;
import de.intevation.lada.model.master.StatusMp;
import de.intevation.lada.model.master.StatusVal;
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
            return repository.getAll(StatusVal.class);
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
            StatusMp.class, status.getStatusComb());

        QueryBuilder<StatusAccessMpView> errFilter = repository
            .queryBuilder(StatusAccessMpView.class)
            .andIn("levId", authorization.getInfo().getFunktionen())
            .and("curLev", kombi.getStatusLev().getId())
            .and("curVal", kombi.getStatusVal().getId());
        List<StatusAccessMpView> erreichbare = repository.filterPlain(
            errFilter.getQuery());

        QueryBuilder<StatusVal> werteFilter =
            repository.queryBuilder(StatusVal.class);
        for (StatusAccessMpView erreichbar: erreichbare) {
            werteFilter.or("id", erreichbar.getValId());
        }
        return repository.filter(werteFilter.getQuery());
    }
}
