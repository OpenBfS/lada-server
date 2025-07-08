/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.util.Collection;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import de.intevation.lada.lock.TimestampLocker;
import de.intevation.lada.model.lada.BelongsToSample;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.util.rest.RequestMethod;


/**
 * REST service for Measm objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "measm")
public class MeasmService extends LadaIntegerIdEntityService {

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
    public Collection<Measm> get(
        @QueryParam("sampleId") @NotNull Integer sampleId
    ) {
        return repository.getById(Sample.class, sampleId).getMeasms();
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
     * Create a Measm object.
     *
     * @return A response object containing the created Measm.
     * @throws BadRequestException if any constraint violations are detected.
     */
    @POST
    public Measm create(
        @Valid Measm messung
    ) throws BadRequestException {
        clearAssociations(messung);
        return repository.create(messung);
    }

    /**
     * Update an existing Measm object.
     *
     * @return the updated Measm object.
     * @throws BadRequestException if any constraint violations are detected.
     */
    @PUT
    @Path("{id}")
    public Measm update(
        @Valid Measm messung
    ) throws BadRequestException {
        lock.isLocked(messung);
        clearAssociations(messung);
        return repository.update(messung);
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

    /**
     * Clears associated objects.
     * Not cascading persistence operations is not enough
     * to prevent accidents in persistence layer.
     *
     * @param m the actual measm
     */
    public void clearAssociations(Measm m) {
        m.setTags(null);
        m.setStatusProts(null);
        m.setCommMeasms(null);
        m.setMeasVals(null);
    }
}
