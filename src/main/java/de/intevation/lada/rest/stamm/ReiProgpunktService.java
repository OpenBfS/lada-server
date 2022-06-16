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

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import de.intevation.lada.model.stammdaten.ReiProgpunkt;
import de.intevation.lada.model.stammdaten.ReiProgpunktGrpZuord;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.rest.LadaService;

/**
 * REST service for ReiProgpunkt objects.
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
 *      "reiid": [string],
 *      "reiProgPunkt": [string]
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
@Path("rest/reiprogpunkt")
public class ReiProgpunktService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    /**
     * Get ReiProgpunkt objects.
     *
     * @param reiProgpunktGrpId URL parameter "reiprogpunktgruppe" to filter
     * using reiProgpunktGrpId.
     * @return Response containing requested objects.
     */
    @GET
    @Path("/")
    public Response get(
        @QueryParam("reiprogpunktgruppe") Integer reiProgpunktGrpId
    ) {
        if (reiProgpunktGrpId == null) {
            return repository.getAll(ReiProgpunkt.class);
        }
        QueryBuilder<ReiProgpunktGrpZuord> builder =
            repository.queryBuilder(ReiProgpunktGrpZuord.class);
        builder.and("reiProgpunktGrpId", reiProgpunktGrpId);
        List<ReiProgpunktGrpZuord> zuord =
            repository.filterPlain(builder.getQuery());
        if (zuord.isEmpty()) {
            return new Response(true, StatusCodes.OK, null);
        }
        QueryBuilder<ReiProgpunkt> builder1 =
            repository.queryBuilder(ReiProgpunkt.class);
        List<Integer> ids = new ArrayList<Integer>();
        for (int i = 0; i < zuord.size(); i++) {
            ids.add(zuord.get(i).getReiProgpunktId());
        }
        builder1.orIn("id", ids);
        return repository.filter(builder1.getQuery());
    }

    /**
     * Get a single ReiProgpunkt object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object containing a single ReiProgpunkt.
     */
    @GET
    @Path("/{id}")
    public Response getById(
        @PathParam("id") Integer id
    ) {
        return repository.getById(ReiProgpunkt.class, id);
    }
}
