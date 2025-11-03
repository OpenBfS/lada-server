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
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
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

    @Override
    public Measm create(Measm messung) throws BadRequestException {
        clearAssociations(messung);
        return super.create(messung);
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
    public Measm update(Measm measm)
        throws BadRequestException, ClientErrorException {
        lock.isLocked(measm);
        clearAssociations(measm);

        return super.update(measm);
    }

    /**
     * Delete an existing Measm object by id.
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
