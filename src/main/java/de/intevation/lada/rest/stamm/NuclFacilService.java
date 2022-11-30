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
import de.intevation.lada.model.master.NuclFacil;
import de.intevation.lada.model.master.NuclFacilGrMp;
import de.intevation.lada.rest.LadaService;

/**
 * REST service for NuclFacil objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("/nuclfacil")
public class NuclFacilService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    /**
     * Get NuclFacil objects.
     *
     * @param nuclFacilGrId URL parameter to filter by nuclFacilGrId
     * @return Response object containing all NuclFacil objects.
     */
    @GET
    @Path("/")
    public Response get(
        @QueryParam("nuclFacilGrId") Integer nuclFacilGrId
    ) {
        if (nuclFacilGrId == null) {
            return repository.getAll(NuclFacil.class);
        }
        QueryBuilder<NuclFacilGrMp> builder =
            repository.queryBuilder(NuclFacilGrMp.class);
        builder.and("nuclFacilGrId", nuclFacilGrId);
        List<NuclFacilGrMp> zuord =
            repository.filterPlain(builder.getQuery());
        if (zuord.isEmpty()) {
            return new Response(true, StatusCodes.OK, null);
        }
        QueryBuilder<NuclFacil> builder1 =
            repository.queryBuilder(NuclFacil.class);
        List<Integer> ids = new ArrayList<Integer>();
        for (int i = 0; i < zuord.size(); i++) {
            ids.add(zuord.get(i).getNuclFacilId());
        }
        builder1.orIn("id", ids);
        return repository.filter(builder1.getQuery());
    }

    /**
     * Get a single NuclFacil object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object containing a single NuclFacil.
     */
    @GET
    @Path("/{id}")
    public Response getById(
        @PathParam("id") Integer id
    ) {
        return repository.getById(NuclFacil.class, id);
    }
}
