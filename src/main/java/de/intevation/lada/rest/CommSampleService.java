/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.util.Collection;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import de.intevation.lada.model.lada.CommSample;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.util.rest.RequestMethod;


/**
 * REST service to operate on CommSample objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "commsample")
public class CommSampleService
    extends LadaIntegerIdEntityEditingService<CommSample> {

    /**
     * Get CommSample objects.
     *
     * @param sampleId The requested objects will be filtered
     * using an URL parameter named sampleId.
     *
     * @return requested objects.
     */
    @GET
    public Collection<CommSample> get(
        @QueryParam("sampleId") @NotNull Integer sampleId
    ) {
        return repository.getById(Sample.class, sampleId).getCommSamples();

    }

    /**
     * Get a single CommSample object by id.
     *
     * @return a single CommSample.
     */
    @GET
    @Path("{id}")
    public CommSample getById() {
        return repository.getById(CommSample.class, id);
    }

    /**
     * Delete an existing CommSample by id.
     */
    @DELETE
    @Path("{id}")
    public void delete() {
        CommSample kommentarObj = repository.getById(CommSample.class, id);
        authorization.authorize(kommentarObj, RequestMethod.DELETE);
        repository.delete(kommentarObj);
    }
}
