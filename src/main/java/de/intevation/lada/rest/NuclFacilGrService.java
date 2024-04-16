/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.model.master.NuclFacilGr;
import de.intevation.lada.model.master.NuclFacilGrMp;

/**
 * REST service for NuclFacilGr objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("nuclfacilgr")
public class NuclFacilGrService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    /**
     * Get NuclFacilGr objects.
     *
     * @param nuclFacilId URL parameter to filter by nuclFacilId
     * @return the requested objects.
     */
    @GET
    public List<NuclFacilGr> get(
        @QueryParam("nuclFacilId") Integer nuclFacilId
    ) {
        if (nuclFacilId == null) {
            return repository.getAll(NuclFacilGr.class);
        }
        QueryBuilder<NuclFacilGrMp> builder =
            repository.queryBuilder(NuclFacilGrMp.class);
        builder.and("nuclFacilId", nuclFacilId);
        List<NuclFacilGrMp> zuord =
            repository.filter(builder.getQuery());
        if (zuord.isEmpty()) {
            return null;
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
     * Get a single NuclFacilGr object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return a single NuclFacilGr.
     */
    @GET
    @Path("{id}")
    public NuclFacilGr getById(
        @PathParam("id") Integer id
    ) {
        return repository.getById(NuclFacilGr.class, id);
    }
}
