/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import de.intevation.lada.model.master.StatusMp;
import de.intevation.lada.util.data.Repository;


/**
 * REST service for StatusMp objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "statusmp")
public class StatusMpService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    /**
     * Get all StatusMp objects.
     *
     * @return all StatusMp objects.
     */
    @GET
    public List<StatusMp> get() {
        return repository.getAll(StatusMp.class);
    }

    /**
     * Get a single StatusMp object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return StatusMp object
     */
    @GET
    @Path("{id}")
    public StatusMp getById(
        @PathParam("id") Integer id
    ) {
        return repository.getById(StatusMp.class, id);
    }

    /**
     * Get the union of status mappings that can be set on given measms.
     *
     * @param measmIds IDs of measms for which status mappings are requested
     * @return Status mappings that can be set on given measms
     */
    @POST
    @Path("getbyids")
    @SuppressWarnings("unchecked")
    public List<StatusMp> getById(
        List<Integer> measmIds
    ) {
        return repository.entityManager().createNativeQuery(
                "SELECT * FROM master.status_mp "
                + "WHERE id IN(SELECT to_id FROM master.status_ord_mp "
                + "  JOIN lada.status_prot ON from_id = status_mp_id"
                + "  JOIN lada.measm ON status_prot.id = status "
                + "  WHERE measm.id IN(:measmIds)) "
                + "AND status_lev_id IN(:levIds)",
                StatusMp.class)
            .setParameter("measmIds", measmIds)
            .setParameter("levIds", authorization.getInfo().getFunktionen())
            .getResultList();
    }
}
