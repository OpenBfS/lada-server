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
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

import de.intevation.lada.lock.LockConfig;
import de.intevation.lada.lock.LockType;
import de.intevation.lada.lock.ObjectLocker;
import de.intevation.lada.model.lada.SampleSpecifMeasVal;
import de.intevation.lada.model.lada.SampleSpecifMeasVal_;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;

/**
 * REST service for SampleSpecifMeasVal objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "samplespecifmeasval")
public class SampleSpecifMeasValService extends LadaService {

    /**
     * The data repository granting read/write access.
     */
    @Inject
    private Repository repository;

    /**
     * The object lock mechanism.
     */
    @Inject
    @LockConfig(type = LockType.TIMESTAMP)
    private ObjectLocker lock;

    /**
     * Get SampleSpecifMeasVal objects.
     *
     * @param sampleId The requested objects will be filtered using
     * a URL parameter named sampleId.
     *
     * @return requested objects.
     */
    @GET
    public List<SampleSpecifMeasVal> get(
        @QueryParam("sampleId") @NotNull Integer sampleId
    ) {
        QueryBuilder<SampleSpecifMeasVal> builder =
            repository.queryBuilder(SampleSpecifMeasVal.class);
        builder.and(SampleSpecifMeasVal_.sampleId, sampleId);
        return repository.filter(builder.getQuery());
    }

    /**
     * Get a SampleSpecifMeasVal object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return a single SampleSpecifMeasVal.
     */
    @GET
    @Path("{id}")
    public SampleSpecifMeasVal getById(
        @PathParam("id") Integer id
    ) {
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
        authorization.authorize(
            zusatzwert,
            RequestMethod.POST,
            SampleSpecifMeasVal.class);

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
        @PathParam("id") Integer id,
        @Valid SampleSpecifMeasVal zusatzwert
    ) throws BadRequestException {
        authorization.authorize(
            zusatzwert,
            RequestMethod.PUT,
            SampleSpecifMeasVal.class);
        lock.isLocked(zusatzwert);

        return repository.update(zusatzwert);
    }

    /**
     * Delete an existing SampleSpecifMeasVal object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     */
    @DELETE
    @Path("{id}")
    public void delete(
        @PathParam("id") Integer id
    ) {
        SampleSpecifMeasVal obj = repository.getById(
            SampleSpecifMeasVal.class, id);
        authorization.authorize(
            obj,
            RequestMethod.DELETE,
            SampleSpecifMeasVal.class);
        lock.isLocked(obj);
        /* Delete the object*/
        repository.delete(obj);
    }
}
