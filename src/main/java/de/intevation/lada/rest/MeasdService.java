/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.util.List;

import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import de.intevation.lada.model.master.Measd;
import de.intevation.lada.model.master.Names;


/**
 * REST service for Measd objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "measd")
public class MeasdService extends LadaStringIdEntityService {

    /**
     * Get Measd objects.
     *
     * @param mmtId URL parameter to filter by mmtId. Might be null
     * (i.e. not given at all) but not an empty string.
     * @return requested objects.
     */
    @GET
    public List<Measd> get(
        @QueryParam("mmtId") @Pattern(regexp = ".+") String mmtId
    ) {
        if (mmtId == null) {
            return repository.getAll(Measd.class);
        }

        return repository.entityManager()
            .createNamedQuery(Names.QUERY_GET_MEASD_FOR_MMT, Measd.class)
            .setParameter("mmt", mmtId)
            .getResultList();
    }

    /**
     * Get a single Measd object by id.
     *
     * @return a single Measd.
     */
    @GET
    @Path("{id}")
    public Measd getById() {
        return repository.getById(Measd.class, id);
    }
}
