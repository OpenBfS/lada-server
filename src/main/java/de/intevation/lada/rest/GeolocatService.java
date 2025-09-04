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
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import de.intevation.lada.lock.TimestampLocker;
import de.intevation.lada.model.lada.BelongsToSample;
import de.intevation.lada.model.lada.Geolocat;
import de.intevation.lada.model.lada.Geolocat_;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.rest.RequestMethod;

/**
 * REST service for Geolocat objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "geolocat")
public class GeolocatService
    extends LadaIntegerIdEntityEditingService<Geolocat> {

    /**
     * The object lock mechanism.
     */
    @Inject
    private TimestampLocker<BelongsToSample> lock;

    /**
     * Get Geolocat objects.
     *
     * @param sampleId The requested objects can be filtered using
     * a URL parameter named sampleId.
     *
     * @return requested objects.
     */
    @GET
    public List<Geolocat> get(
        @QueryParam("sampleId") @NotNull Integer sampleId
    ) {
        QueryBuilder<Geolocat> builder = repository
            .queryBuilder(Geolocat.class)
            .and(Geolocat_.sampleId, sampleId);
        return repository.filter(builder.getQuery());
    }

    /**
     * Get a Geolocat object by id.
     *
     * @return a single Geolocat.
     */
    @GET
    @Path("{id}")
    public Geolocat getById() {
        return repository.getById(Geolocat.class, id);
    }

    /**
     * Update an existing Geolocat object.
     *
     * @param geolocat the object to be updated
     * @return the updated object
     * @throws ClientErrorException if object has been altered since loaded
     * @throws BadRequestException if any constraint violations are detected
     */
    @Override
    public Geolocat update(Geolocat geolocat) throws BadRequestException {
        lock.isLocked(geolocat);
        return super.update(geolocat);
    }

    /**
     * Delete an existing Geolocat object by id.
     */
    @DELETE
    @Path("{id}")
    public void delete() {
        Geolocat ortObj = repository.getById(Geolocat.class, id);
        authorization.authorize(ortObj, RequestMethod.DELETE);
        lock.isLocked(ortObj);

        repository.delete(ortObj);
    }
}
