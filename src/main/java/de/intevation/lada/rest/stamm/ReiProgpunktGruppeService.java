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
import javax.validation.constraints.Pattern;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import de.intevation.lada.model.stammdaten.ReiProgpunktGrpUmwZuord;
import de.intevation.lada.model.stammdaten.ReiProgpunktGrpZuord;
import de.intevation.lada.model.stammdaten.ReiProgpunktGruppe;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.rest.LadaService;

/**
 * REST service for ReiProgpunktGruppe objects.
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
 *      "reiProgpunktGruppe": [string]
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
@Path("rest/reiprogpunktgruppe")
public class ReiProgpunktGruppeService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    /**
     * Get ReiProgpunktGruppe objects.
     *
     * @param reiProgpunktId URL parameter "reiprogpunkt" to filter
     * using reiProgpunktId
     * @param umwelt URL parameter to filter using umwId. Might be null
     * (i.e. not given at all) but not an empty string.
     * @return Response object containing all Datenbasis objects.
     */
    @GET
    @Path("/")
    public Response get(
        @QueryParam("reiprogpunkt") Integer reiProgpunktId,
        @QueryParam("umwelt") @Pattern(regexp = ".+") String umwelt
    ) {
        if (reiProgpunktId == null && umwelt == null) {
            return repository.getAll(ReiProgpunktGruppe.class);
        }
        List<ReiProgpunktGruppe> list = new ArrayList<ReiProgpunktGruppe>();
        if (reiProgpunktId != null) {
            QueryBuilder<ReiProgpunktGrpZuord> builder =
                repository.queryBuilder(ReiProgpunktGrpZuord.class);
            builder.and("reiProgpunktId", reiProgpunktId);
            List<ReiProgpunktGrpZuord> zuord =
                repository.filterPlain(builder.getQuery());
            if (zuord.isEmpty()) {
                return new Response(true, StatusCodes.OK, null);
            }
            QueryBuilder<ReiProgpunktGruppe> builder1 =
                repository.queryBuilder(ReiProgpunktGruppe.class);
            List<Integer> ids = new ArrayList<Integer>();
            for (int i = 0; i < zuord.size(); i++) {
                ids.add(zuord.get(i).getReiProgpunktGrpId());
            }
            builder1.orIn("id", ids);
            list = repository.filterPlain(builder1.getQuery());
        } else if (umwelt != null) {
            QueryBuilder<ReiProgpunktGrpUmwZuord> builder =
                repository.queryBuilder(ReiProgpunktGrpUmwZuord.class);
            builder.and("umwId", umwelt);
            List<ReiProgpunktGrpUmwZuord> zuord =
                repository.filterPlain(builder.getQuery());
            if (zuord.isEmpty()) {
                return new Response(true, StatusCodes.OK, null);
            }
            QueryBuilder<ReiProgpunktGruppe> builder1 =
                repository.queryBuilder(ReiProgpunktGruppe.class);
            List<Integer> ids = new ArrayList<Integer>();
            for (int i = 0; i < zuord.size(); i++) {
                ids.add(zuord.get(i).getReiProgpunktGrpId());
            }
            builder1.orIn("id", ids);
            list = repository.filterPlain(builder1.getQuery());
        }

        return new Response(true, StatusCodes.OK, list);
    }

    /**
     * Get a single ReiProgpunktGruppe object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object containing a single ReiProgpunktGruppe.
     */
    @GET
    @Path("/{id}")
    public Response getById(
        @PathParam("id") Integer id
    ) {
        return repository.getById(ReiProgpunktGruppe.class, id);
    }
}
