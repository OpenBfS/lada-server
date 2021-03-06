/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import de.intevation.lada.model.QueryColumns;
import de.intevation.lada.model.land.Messprogramm;
import de.intevation.lada.model.land.Messung;
import de.intevation.lada.model.land.Probe;
import de.intevation.lada.model.stammdaten.DatensatzErzeuger;
import de.intevation.lada.model.stammdaten.GridColumn;
import de.intevation.lada.model.stammdaten.GridColumnValue;
import de.intevation.lada.model.stammdaten.MessprogrammKategorie;
import de.intevation.lada.model.stammdaten.Ort;
import de.intevation.lada.model.stammdaten.Probenehmer;
import de.intevation.lada.model.stammdaten.ResultType;
import de.intevation.lada.query.QueryTools;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.util.rest.Response;

/**
 * REST service for universal objects.
 * <p>
 * The services produce data in the application/json media type.
 * All HTTP methods use the authorization module to determine if the user is
 * allowed to perform the requested action.
 * </p>
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("rest/universal")
@RequestScoped
public class UniversalService {

    /**
     * The data repository granting read/write access.
     */
    @Inject
    private Repository repository;

    /**
     * The header authorization module.
     */
    @Inject
    @AuthorizationConfig(type = AuthorizationType.HEADER)
    private Authorization authorization;

    @Inject
    private QueryTools queryTools;


    /**
     * Execute query, using the given result columns.
     * The query can contain the following post data:
     * <pre>
     * <code>
     * {
     *   columns[{
     *     gridColumnId: [number],
     *     sort: [string],
     *     sortIndex: [number],
     *     filterValue: [string],
     *     filterActive: [boolean],
     *   }]
     * }
     * </code>
     * </pre>
     * @return JSON encoded query results
     */
    @POST
    @Path("/")
    @Consumes("application/json")
    @Produces("application/json")
    public Response execute(
        @Context HttpServletRequest request,
        @Context UriInfo info,
        QueryColumns columns
    ) {
        Integer qid;
        MultivaluedMap<String, String> params = info.getQueryParameters();
        List<GridColumnValue> gridColumnValues = columns.getColumns();

        String authorizationColumnIndex = null;
        Class<?> authorizationColumnType = null;
        if (gridColumnValues == null
            || gridColumnValues.isEmpty()) {
            //TODO Error code if no columns are given
            return new Response(false, StatusCodes.NOT_EXISTING, null);
        }

        /**
         * Determines the class used for authorizing result entries:
         * Later entries overrule earlier ones.
         */
        final LinkedHashMap<String, Class<?>> hierarchy
            = new LinkedHashMap<String, Class<?>>();
        hierarchy.put("mprkat",      MessprogrammKategorie.class);
        hierarchy.put("dsatzerz",    DatensatzErzeuger.class);
        hierarchy.put("probenehmer", Probenehmer.class);
        hierarchy.put("ortId",       Ort.class);
        hierarchy.put("mpId",        Messprogramm.class);
        hierarchy.put("probeId",     Probe.class);
        hierarchy.put("messungId",   Messung.class);
        int resultNdx = hierarchy.size();
        for (GridColumnValue columnValue : gridColumnValues) {
            GridColumn gridColumn = repository.getByIdPlain(
                GridColumn.class,
                Integer.valueOf(columnValue.getGridColumnId())
            );
            //Check if column can be used for authorization
            ResultType resultType =
                repository.getByIdPlain(
                    ResultType.class,
                    gridColumn.getDataType().getId()
                );
            if (resultType != null) {
                int ndx = -1, i = 0;
                for (String authType: hierarchy.keySet()) {
                    if (authType.equals(resultType.getName())) {
                        ndx = i;
                    }
                    i++;
                }
                if (ndx > -1 && ndx < resultNdx) {
                    resultNdx = ndx;
                    authorizationColumnIndex = gridColumn.getDataIndex();
                    authorizationColumnType = hierarchy.get(
                        resultType.getName());
                }
            }
            columnValue.setGridColumn(gridColumn);
        }

        GridColumn gridColumn = repository.getByIdPlain(
            GridColumn.class,
            Integer.valueOf(gridColumnValues.get(0).getGridColumnId())
        );

        qid = gridColumn.getBaseQuery();
        List<Map<String, Object>> result =
            queryTools.getResultForQuery(columns.getColumns(), qid);
        if (result == null) {
            return new Response(true, StatusCodes.OK, null);
        }

        for (Map<String, Object> row: result) {
            Object idToAuthorize = row.get(authorizationColumnIndex);
            boolean readonly;

            if (idToAuthorize != null) {
                //If column is an ort, get Netzbetreiberid
                if (authorizationColumnType == Ort.class) {
                    Ort ort = (Ort) repository.getByIdPlain(
                        Ort.class, idToAuthorize);
                    idToAuthorize = ort.getNetzbetreiberId();
                }
                if (authorizationColumnType == DatensatzErzeuger.class) {
                    DatensatzErzeuger de =
                        (DatensatzErzeuger) repository.getByIdPlain(
                            DatensatzErzeuger.class, idToAuthorize);
                    idToAuthorize = de.getNetzbetreiberId();
                }
                if (authorizationColumnType == Probenehmer.class) {
                    Probenehmer pn = (Probenehmer) repository.getByIdPlain(
                        Probenehmer.class, idToAuthorize);
                    idToAuthorize = pn.getNetzbetreiberId();
                }
                if (authorizationColumnType == MessprogrammKategorie.class) {
                    MessprogrammKategorie mk =
                        (MessprogrammKategorie) repository.getByIdPlain(
                            MessprogrammKategorie.class, idToAuthorize);
                    idToAuthorize = mk.getNetzbetreiberId();
                }

                readonly = !authorization.isAuthorizedById(
                    request,
                    idToAuthorize,
                    RequestMethod.POST,
                    authorizationColumnType);
            } else {
                readonly = true;
            }
            row.put("readonly", readonly);
        }
        int size = result.size();

        if (params.containsKey("start") && params.containsKey("limit")) {
            int start = Integer.valueOf(params.getFirst("start"));
            int limit = Integer.valueOf(params.getFirst("limit"));
            int end = limit + start;
            if (start + limit > result.size()) {
                end = result.size();
            }
            result = result.subList(start, end);
        }

        return new Response(true, StatusCodes.OK, result, size);
    }
}
