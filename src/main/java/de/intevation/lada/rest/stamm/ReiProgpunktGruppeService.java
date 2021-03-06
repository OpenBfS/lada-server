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
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import de.intevation.lada.model.stammdaten.ReiProgpunktGrpUmwZuord;
import de.intevation.lada.model.stammdaten.ReiProgpunktGrpZuord;
import de.intevation.lada.model.stammdaten.ReiProgpunktGruppe;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.Response;

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
@RequestScoped
public class ReiProgpunktGruppeService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    /**
     * Get all ReiProgpunktGruppe objects.
     * <p>
     * Example: http://example.com/reiprogpunkgruppe
     *
     * @return Response object containing all Datenbasis objects.
     */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
        @Context HttpHeaders headers,
        @Context UriInfo info
    ) {
        MultivaluedMap<String, String> params = info.getQueryParameters();
        if (params.isEmpty()
            || (!params.containsKey("reiprogpunkt")
            && !params.containsKey("umwelt"))
        ) {
            return repository.getAll(ReiProgpunktGruppe.class);
        }
        List<ReiProgpunktGruppe> list = new ArrayList<ReiProgpunktGruppe>();
        if (params.containsKey("reiprogpunkt")) {
            Integer id = null;
            try {
                id = Integer.valueOf(params.getFirst("reiprogpunkt"));
            } catch (NumberFormatException e) {
                return new Response(
                    false,
                    StatusCodes.ERROR_DB_CONNECTION,
                    "Not a valid filter id");
            }
            QueryBuilder<ReiProgpunktGrpZuord> builder =
                repository.queryBuilder(ReiProgpunktGrpZuord.class);
            builder.and("reiProgpunktId", id);
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
        } else if (params.containsKey("umwelt")) {
            QueryBuilder<ReiProgpunktGrpUmwZuord> builder =
                repository.queryBuilder(ReiProgpunktGrpUmwZuord.class);
            builder.and("umwId", params.getFirst("umwelt"));
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
     * <p>
     * The id is appended to the URL as a path parameter.
     * <p>
     * Example: http://example.com/reiprogpunkgruppe/{id}
     *
     * @return Response object containing a single ReiProgpunktGruppe.
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(
        @Context HttpHeaders headers,
        @PathParam("id") String id
    ) {
        return repository.getById(ReiProgpunktGruppe.class, Integer.valueOf(id));
    }
}
