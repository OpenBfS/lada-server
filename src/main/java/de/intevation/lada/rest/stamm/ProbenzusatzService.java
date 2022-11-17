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

import de.intevation.lada.model.stammdaten.SampleSpecif;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.rest.LadaService;

/**
 * REST service for ProbenZusatz objects.
 * <p>
 * The services produce data in the application/json media type.
 * A typical response holds information about the action performed and the data.
 * <pre>
 * <code>
 * {
 *  "success": [boolean];
 *  "message": [string],
 *  "data":[{
 *      "id": [string],
 *      "beschreibung": [string],
 *      "eudfKeyword": [string],
 *      "zusatzwert": [string],
 *      "mehId": [number]
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
@Path("rest/probenzusatz")
public class ProbenzusatzService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    /**
     * Get ProbenZusatz objects.
     *
     * @param umwId URL parameter to filter using umwId. Might be null
     * (i.e. not given at all) but not an empty string.
     * @return Response containing requested objects.
     */
    @GET
    @Path("/")
    public Response get(
        @QueryParam("umwId") @Pattern(regexp = ".+") String umwId
    ) {
        if (umwId != null) {
            Query query =
                repository.queryFromString(
                    "SELECT pzs_id FROM "
                    + de.intevation.lada.model.stammdaten.SchemaName.LEGACY_NAME
                    + ".umwelt_zusatz "
                    + "WHERE umw_id = :umw"
                ).setParameter("umw", umwId);
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
     * Get a single ProbenZusatz object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object containing a single ProbenZusatz.
     */
    @GET
    @Path("/{id}")
    public Response getById(
        @PathParam("id") String id
    ) {
        return repository.getById(SampleSpecif.class, id);
    }
}
