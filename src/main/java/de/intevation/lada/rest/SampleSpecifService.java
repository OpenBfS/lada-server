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
import de.intevation.lada.model.master.SampleSpecif;
import de.intevation.lada.model.master.SampleSpecif_;

/**
 * REST service for SampleSpecif objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "samplespecif")
public class SampleSpecifService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    /**
     * Get SampleSpecif objects.
     *
     * @param envMediumId URL parameter to filter using envMediumId.
     * Might be null (i.e. not given at all) but not an empty string.
     * @return requested objects.
     */
    @GET
    public List<SampleSpecif> get(
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
                QueryBuilder<SampleSpecif> builder =
                    repository.queryBuilder(SampleSpecif.class)
                    .orIn(SampleSpecif_.id, ids);
                return repository.filter(builder.getQuery());
            }
        }

        return repository.getAll(SampleSpecif.class);
    }

    /**
     * Get a single SampleSpecif object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return a single SampleSpecif.
     */
    @GET
    @Path("{id}")
    public SampleSpecif getById(
        @PathParam("id") String id
    ) {
        return repository.getById(SampleSpecif.class, id);
    }
}
