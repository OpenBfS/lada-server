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
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Measm_;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.rest.RequestMethod;


/**
 * REST service for Measm objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "measm")
public class MeasmService extends LadaIntegerIdEntityEditingService<Measm> {

    /**
     * The object lock mechanism.
     */
    @Inject
    private TimestampLocker<BelongsToSample> lock;

    /**
     * Get Measm objects.
     *
     * @param sampleId URL parameter sampleId to use as filter (required).
     * @return requested objects.
     */
    @GET
    public List<Measm> get(
        @QueryParam("sampleId") @NotNull Integer sampleId
    ) {
        QueryBuilder<Measm> builder = repository.queryBuilder(Measm.class)
            .and(Measm_.sampleId, sampleId);
        return repository.filter(builder.getQuery());
    }

    /**
     * Get a Measm object by id.
     *
     * @return a single Measm.
     */
    @GET
    @Path("{id}")
    public Measm getById() {
        return repository.getById(Measm.class, id);
    }

    /**
     * Update an existing Measm object.
     *
     * @param measm the object to be updated
     * @return the updated Measm object
     * @throws ClientErrorException if object has been altered since loaded
     * @throws BadRequestException if any constraint violations are detected
     */
    @Override
    public Measm update(Measm measm) throws BadRequestException {
        lock.isLocked(measm);

        return super.update(measm);
    }

    /**
     * Delete an existing Measm object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     */
    @DELETE
    @Path("{id}")
    public void delete() {
        Measm messungObj = repository.getById(Measm.class, id);
        authorization.authorize(messungObj, RequestMethod.DELETE);
        lock.isLocked(messungObj);

        repository.delete(messungObj);
    }
}
