/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest.stamm;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.Query;
import javax.validation.constraints.Pattern;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.model.master.Measd;
import de.intevation.lada.rest.LadaService;

/**
 * REST service for Measd objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("rest/measd")
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
     * @return Response containing requested objects.
     */
    @GET
    @Path("/")
    public Response get(
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
     * @return Response object containing a single Measd.
     */
    @GET
    @Path("/{id}")
    public Response getById(
        @PathParam("id") Integer id
    ) {
        return repository.getById(Measd.class, id);
    }
}
