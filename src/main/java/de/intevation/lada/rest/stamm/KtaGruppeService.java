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

import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.model.master.NuclFacilGr;
import de.intevation.lada.model.master.NuclFacilGrMp;
import de.intevation.lada.rest.LadaService;

/**
 * REST service for KtaGruppe objects.
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
 *      "beschreibung": [string],
 *      "ktaGruppe": [string]
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
@Path("rest/ktagruppe")
public class KtaGruppeService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    /**
     * Get KtaGruppe objects.
     *
     * @param kta URL parameter to filter by ktaId
     * @return Response containing the requested objects.
     */
    @GET
    @Path("/")
    public Response get(
        @QueryParam("kta") Integer kta
    ) {
        if (kta == null) {
            return repository.getAll(NuclFacilGr.class);
        }
        QueryBuilder<NuclFacilGrMp> builder =
            repository.queryBuilder(NuclFacilGrMp.class);
        builder.and("nuclFacilId", kta);
        List<NuclFacilGrMp> zuord =
            repository.filterPlain(builder.getQuery());
        if (zuord.isEmpty()) {
            return new Response(true, StatusCodes.OK, null);
        }
        QueryBuilder<NuclFacilGr> builder1 =
            repository.queryBuilder(NuclFacilGr.class);
        List<Integer> ids = new ArrayList<Integer>();
        for (int i = 0; i < zuord.size(); i++) {
            ids.add(zuord.get(i).getNuclFacilGrId());
        }
        builder1.orIn("id", ids);
        return repository.filter(builder1.getQuery());
    }

    /**
     * Get a single KtaGruppe object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object containing a single KtaGruppe.
     */
    @GET
    @Path("/{id}")
    public Response getById(
        @PathParam("id") Integer id
    ) {
        return repository.getById(NuclFacilGr.class, id);
    }
}
