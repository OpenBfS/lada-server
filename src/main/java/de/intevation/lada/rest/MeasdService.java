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
import jakarta.persistence.Query;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.model.master.Measd;

/**
 * REST service for Measd objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("measd")
public class MeasdService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

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

        Query query =
            repository.queryFromString(
                "SELECT measd_id FROM "
                + de.intevation.lada.model.master.SchemaName.NAME
                + ".mmt_measd_view "
                + "WHERE mmt_id = :mmt"
            ).setParameter("mmt", mmtId);
        @SuppressWarnings("unchecked")
        List<Integer> ids = query.getResultList();
        QueryBuilder<Measd> builder2 =
            repository.queryBuilder(Measd.class);
        builder2.orIntList("id", ids);
        return repository.filter(builder2.getQuery());
    }

    /**
     * Get a single Measd object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return a single Measd.
     */
    @GET
    @Path("{id}")
    public Measd getById(
        @PathParam("id") Integer id
    ) {
        return repository.getById(Measd.class, id);
    }
}
