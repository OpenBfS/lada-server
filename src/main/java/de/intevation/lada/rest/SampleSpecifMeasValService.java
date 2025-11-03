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
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.lada.SampleSpecifMeasVal;
import de.intevation.lada.util.rest.RequestMethod;


/**
 * REST service for SampleSpecifMeasVal objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "samplespecifmeasval")
public class SampleSpecifMeasValService
    extends LadaIntegerIdEntityEditingService<SampleSpecifMeasVal> {

    /**
     * The object lock mechanism.
     */
    @Inject
    private TimestampLocker<BelongsToSample> lock;

    /**
     * Get SampleSpecifMeasVal objects.
     *
     * @param sampleId The requested objects will be filtered using
     * a URL parameter named sampleId.
     *
     * @return requested objects.
     */
    @GET
    public Collection<SampleSpecifMeasVal> get(
        @QueryParam("sampleId") @NotNull Integer sampleId
    ) {
        return repository.getById(Sample.class, sampleId)
            .getSampleSpecifMeasVals();
    }

    /**
     * Get a SampleSpecifMeasVal object by id.
     *
     * @return a single SampleSpecifMeasVal.
     */
    @GET
    @Path("{id}")
    public SampleSpecifMeasVal getById() {
        return repository.getById(SampleSpecifMeasVal.class, id);
    }

    /**
     * Update an existing SampleSpecifMeasVal object.
     *
     * @param sampleSpecifMeasVal the object to be updated
     * @return the updated SampleSpecifMeasVal object
     * @throws ClientErrorException if object has been altered since loaded
     * @throws BadRequestException if any constraint violations are detected
     */
    @Override
    public SampleSpecifMeasVal update(
        SampleSpecifMeasVal sampleSpecifMeasVal
    ) throws BadRequestException, ClientErrorException {
        lock.isLocked(sampleSpecifMeasVal);

        return super.update(sampleSpecifMeasVal);
    }

    /**
     * Delete an existing SampleSpecifMeasVal object by id.
     */
    @DELETE
    @Path("{id}")
    public void delete() {
        SampleSpecifMeasVal obj = repository.getById(
            SampleSpecifMeasVal.class, id);
        authorization.authorize(obj, RequestMethod.DELETE);
        lock.isLocked(obj);
        /* Delete the object*/
        repository.delete(obj);
    }
}
