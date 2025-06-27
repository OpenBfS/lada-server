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
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.lada.SampleSpecifMeasVal;
import de.intevation.lada.util.rest.RequestMethod;


/**
 * REST service for SampleSpecifMeasVal objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "samplespecifmeasval")
public class SampleSpecifMeasValService extends LadaIntegerIdEntityService {

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
     * Create a SampleSpecifMeasVal object.
     *
     * @return A response object containing the created SampleSpecifMeasVal.
     * @throws BadRequestException if any constraint violations are detected.
     */
    @POST
    public SampleSpecifMeasVal create(
        @Valid SampleSpecifMeasVal zusatzwert
    ) throws BadRequestException {
        return repository.create(zusatzwert);
    }

    /**
     * Update an existing SampleSpecifMeasVal object.
     *
     * @return the updated SampleSpecifMeasVal object.
     * @throws BadRequestException if any constraint violations are detected.
     */
    @PUT
    @Path("{id}")
    public SampleSpecifMeasVal update(
        @Valid SampleSpecifMeasVal zusatzwert
    ) throws BadRequestException {
        lock.isLocked(zusatzwert);

        return repository.update(zusatzwert);
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
