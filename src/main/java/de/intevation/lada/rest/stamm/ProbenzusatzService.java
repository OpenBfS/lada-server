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
import de.intevation.lada.model.master.SampleSpecif;
import de.intevation.lada.rest.LadaService;

/**
 * REST service for SampleSpecif objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("rest/samplespecif")
public class ProbenzusatzService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    /**
     * Get SampleSpecif objects.
     *
     * @param envMediumId URL parameter to filter using envMediumId. Might be null
     * (i.e. not given at all) but not an empty string.
     * @return Response containing requested objects.
     */
    @GET
    @Path("/")
    public Response get(
        @QueryParam("envMediumId") @Pattern(regexp = ".+") String envMediumId
    ) {
        if (envMediumId != null) {
            Query query =
                repository.queryFromString(
                    "SELECT sample_specif_id FROM "
                    + de.intevation.lada.model.master.SchemaName.NAME
                    + ".env_specif_mp "
                    + "WHERE env_medium_id = :envMediumId"
                ).setParameter("envMediumId", envMediumId);
            @SuppressWarnings("unchecked")
            List<String> ids = query.getResultList();

            if (!ids.isEmpty()) {
            QueryBuilder<SampleSpecif> builder2 =
                repository.queryBuilder(SampleSpecif.class);
            builder2.orIn("id", ids);
            return repository.filter(builder2.getQuery());
            }
        }

        return repository.getAll(SampleSpecif.class);
    }

    /**
     * Get a single SampleSpecif object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object containing a single SampleSpecif.
     */
    @GET
    @Path("/{id}")
    public Response getById(
        @PathParam("id") String id
    ) {
        return repository.getById(SampleSpecif.class, id);
    }
}
