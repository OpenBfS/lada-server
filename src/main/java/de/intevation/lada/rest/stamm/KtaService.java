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

import de.intevation.lada.model.stammdaten.Kta;
import de.intevation.lada.model.stammdaten.KtaGrpZuord;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.rest.LadaService;

/**
 * REST service for Kta objects.
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
 *      "bezeichnung": [string],
 *      "code": [string}
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
@Path("rest/kta")
public class KtaService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    /**
     * Get Kta objects.
     *
     * @param ktagruppe URL parameter to filter by ktaGrpId
     * @return Response object containing all Kta objects.
     */
    @GET
    @Path("/")
    public Response get(
        @QueryParam("ktagruppe") Integer ktagruppe
    ) {
        if (ktagruppe == null) {
            return repository.getAll(Kta.class);
        }
        QueryBuilder<KtaGrpZuord> builder =
            repository.queryBuilder(KtaGrpZuord.class);
        builder.and("ktaGrpId", ktagruppe);
        List<KtaGrpZuord> zuord =
            repository.filterPlain(builder.getQuery());
        if (zuord.isEmpty()) {
            return new Response(true, StatusCodes.OK, null);
        }
        QueryBuilder<Kta> builder1 =
            repository.queryBuilder(Kta.class);
        List<Integer> ids = new ArrayList<Integer>();
        for (int i = 0; i < zuord.size(); i++) {
            ids.add(zuord.get(i).getKtaId());
        }
        builder1.orIn("id", ids);
        return repository.filter(builder1.getQuery());
    }

    /**
     * Get a single Kta object by id.
     * <p>
     * The id is appended to the URL as a path parameter.
     * <p>
     * Example: http://example.com/kta/{id}
     *
     * @return Response object containing a single Kta.
     */
    @GET
    @Path("/{id}")
    public Response getById(
        @PathParam("id") String id
    ) {
        return repository.getById(Kta.class, Integer.valueOf(id));
    }
}
