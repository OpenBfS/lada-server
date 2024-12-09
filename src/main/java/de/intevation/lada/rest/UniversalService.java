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

import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import jakarta.validation.constraints.NotEmpty;

import de.intevation.lada.model.BaseModel;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Mpg;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.master.DatasetCreator;
import de.intevation.lada.model.master.Disp;
import de.intevation.lada.model.master.GridColConf;
import de.intevation.lada.model.master.GridColMp;
import de.intevation.lada.model.master.MpgCateg;
import de.intevation.lada.model.master.Sampler;
import de.intevation.lada.model.master.Site;
import de.intevation.lada.model.master.Tag;
import de.intevation.lada.query.QueryTools;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;


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
@Path(LadaService.PATH_REST + "universal")
public class UniversalService extends LadaService {

    /**
     * The data repository granting read/write access.
     */
    @Inject
    private Repository repository;

    public static class UniversalResponse {
        private List<Map<String, Object>> data;
        private int totalCount;

        /**
         * Default constructor for JSON-B.
         */
        public UniversalResponse() { };

        private UniversalResponse(
            List<Map<String, Object>> data, int totalCount
        ) {
            this.data = data;
            this.totalCount = totalCount;
        }

        public List<Map<String, Object>> getData() {
            return this.data;
        }

        public void setData(List<Map<String, Object>> data) {
            this.data = data;
        }

        public int getTotalCount() {
            return this.totalCount;
        }

        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
        }
    }

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
     * @throws BadRequestException
     */
    @POST
    public UniversalResponse execute(
        @QueryParam("start") int start, // default for primitive type: 0
        @QueryParam("limit") Integer limit,
        @NotEmpty List<GridColConf> gridColumnValues
    ) {
        String authorizationColumnIndex = null;
        Class<? extends BaseModel> authorizationColumnType = null;

        /**
         * Determines the class used for authorizing result entries:
         * Later entries overrule earlier ones.
         */
        final LinkedHashMap<String, Class<? extends BaseModel>> hierarchy
            = new LinkedHashMap<>();
        hierarchy.put("tagId",       Tag.class);
        hierarchy.put("mprkat",      MpgCateg.class);
        hierarchy.put("dsatzerz",    DatasetCreator.class);
        hierarchy.put("probenehmer", Sampler.class);
        hierarchy.put("ortId",       Site.class);
        hierarchy.put("mpId",        Mpg.class);
        hierarchy.put("probeId",     Sample.class);
        hierarchy.put("messungId",   Measm.class);
        int resultNdx = -1;
        for (GridColConf columnValue : gridColumnValues) {
            GridColMp gridColumn = repository.getById(
                GridColMp.class,
                columnValue.getGridColMpId()
            );
            //Check if column can be used for authorization
            Disp resultType = gridColumn.getDisp();
            if (resultType != null) {
                int ndx = -1, i = 0;
                for (String authType: hierarchy.keySet()) {
                    if (authType.equals(resultType.getName())) {
                        ndx = i;
                    }
                    i++;
                }
                if (ndx > resultNdx) {
                    resultNdx = ndx;
                    authorizationColumnIndex = gridColumn.getDataIndex();
                    authorizationColumnType = hierarchy.get(
                        resultType.getName());
                }
            }
            columnValue.setGridColMp(gridColumn);
        }

        try {
            QueryTools queryTools = new QueryTools(
                repository, gridColumnValues);
            List<Map<String, Object>> result = queryTools.getResultForQuery(
                start, limit);

            // TODO: This issues a potentially costly 'SELECT count(*)'
            // for every request. Better not to rely on total count at client side?
            int size = queryTools.getTotalCountForQuery();

            // Skip authorization of large pages to improve performance
            boolean doAuthorize = result.size() <= 500;
            for (Map<String, Object> row: result) {
                Object idToAuthorize = row.get(authorizationColumnIndex);
                boolean readonly = true;
                if (doAuthorize && idToAuthorize != null) {
                    readonly = !authorization.isAuthorized(
                        repository.getById(
                            authorizationColumnType, idToAuthorize),
                        RequestMethod.PUT);
                }
                row.put("readonly", readonly);
            }

            return new UniversalResponse(result, size);
        } catch (IllegalArgumentException iae) {
            throw new BadRequestException(
                Response.status(Response.Status.BAD_REQUEST)
                .entity(iae.getMessage()).build());
        }
    }
}
