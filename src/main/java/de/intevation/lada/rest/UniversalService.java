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

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.MultivaluedHashMap;

import de.intevation.lada.model.QueryColumns;
import de.intevation.lada.model.land.Messprogramm;
import de.intevation.lada.model.land.Messung;
import de.intevation.lada.model.land.Sample;
import de.intevation.lada.model.stammdaten.DatasetCreator;
import de.intevation.lada.model.stammdaten.GridColMp;
import de.intevation.lada.model.stammdaten.GridColumnValue;
import de.intevation.lada.model.stammdaten.MessprogrammKategorie;
import de.intevation.lada.model.stammdaten.Ort;
import de.intevation.lada.model.stammdaten.Probenehmer;
import de.intevation.lada.model.stammdaten.ResultType;
import de.intevation.lada.model.stammdaten.Tag;
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
public class UniversalService extends LadaService {

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

    /**
     * Execute query, using the given result columns.
     *
     * @param columns The query can contain the following post data:
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
     * @param start URL parameter used as offset for paging
     * @param limit URL parameter used as limit for paging
     * @return JSON encoded query results
     */
    @POST
    @Path("/")
    public Response execute(
        @QueryParam("start") int start, // default for primitive type: 0
        @QueryParam("limit") Integer limit,
        QueryColumns columns
    ) {
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
        hierarchy.put("tagId",       Tag.class);
        hierarchy.put("mprkat",      MessprogrammKategorie.class);
        hierarchy.put("dsatzerz",    DatasetCreator.class);
        hierarchy.put("probenehmer", Probenehmer.class);
        hierarchy.put("ortId",       Ort.class);
        hierarchy.put("mpId",        Messprogramm.class);
        hierarchy.put("probeId",     Sample.class);
        hierarchy.put("messungId",   Messung.class);
        int resultNdx = hierarchy.size();
        for (GridColumnValue columnValue : gridColumnValues) {
            GridColMp gridColumn = repository.getByIdPlain(
                GridColMp.class,
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

        try {
            QueryTools queryTools = new QueryTools(
                repository, columns.getColumns());
            List<Map<String, Object>> result = queryTools.getResultForQuery(
                start, limit);

            if (result == null) {
                return new Response(true, StatusCodes.OK, null);
            }

            // TODO: This issues a potentially costly 'SELECT count(*)'
            // for every request. Better not to rely on total count at client side?
            int size = queryTools.getTotalCountForQuery();
            boolean doAuthorize = true;
            if (result.size() > 500) {
                doAuthorize = false;
            }

            for (Map<String, Object> row: result) {
                Object idToAuthorize = row.get(authorizationColumnIndex);
                boolean readonly;
                if (doAuthorize) {
                    if (idToAuthorize != null) {
                        //If column is an ort, get Netzbetreiberid
                        if (authorizationColumnType == Ort.class) {
                            Ort ort = repository.getByIdPlain(
                                Ort.class, idToAuthorize);
                            idToAuthorize = ort.getNetzbetreiberId();
                        }
                        if (authorizationColumnType == DatasetCreator.class) {
                            DatasetCreator de = repository.getByIdPlain(
                                DatasetCreator.class, idToAuthorize);
                            idToAuthorize = de.getNetworkId();
                        }
                        if (authorizationColumnType == Probenehmer.class) {
                            Probenehmer pn = repository.getByIdPlain(
                                Probenehmer.class, idToAuthorize);
                            idToAuthorize = pn.getNetzbetreiberId();
                        }
                        if (authorizationColumnType == MessprogrammKategorie.class) {
                            MessprogrammKategorie mk = repository.getByIdPlain(
                                MessprogrammKategorie.class, idToAuthorize);
                            idToAuthorize = mk.getNetzbetreiberId();
                        }
                        if (authorizationColumnType == Tag.class) {
                            Tag tag = repository.getByIdPlain(
                                Tag.class, idToAuthorize);
                            idToAuthorize = tag.getId();
                        }

                        readonly = !authorization.isAuthorizedById(
                            idToAuthorize,
                            RequestMethod.PUT,
                            authorizationColumnType);
                    } else {
                        readonly = true;
                    }
                } else {
                    readonly = true;
                }
                row.put("readonly", readonly);
            }

            return new Response(true, StatusCodes.OK, result, size);
        } catch (IllegalArgumentException iae) {
            Response r = new Response(false, StatusCodes.SQL_INVALID_FILTER, null);
            MultivaluedMap<String, Integer> error =
                new MultivaluedHashMap<String, Integer>();
            error.add(iae.getMessage(), StatusCodes.SQL_INVALID_FILTER);
            r.setErrors(error);
            return r;
        }
    }
}
