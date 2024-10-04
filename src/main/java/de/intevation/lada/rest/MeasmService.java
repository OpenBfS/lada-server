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
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Measm_;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;

/**
 * REST service for Measm objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "measm")
public class MeasmService extends LadaService {

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
     * @param id The id is appended to the URL as a path parameter.
     * @return a single Measm.
     */
    @GET
    @Path("{id}")
    public Measm getById(
        @PathParam("id") Integer id
    ) {
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
        @PathParam("id") Integer id,
        @Valid Measm messung
    ) throws BadRequestException {
        lock.isLocked(messung);

        return repository.update(messung);
    }

    /**
     * Delete an existing Measm object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     */
    @DELETE
    @Path("{id}")
    public void delete(
        @PathParam("id") Integer id
    ) {
        Measm messungObj = repository.getById(Measm.class, id);
        authorization.authorize(messungObj, RequestMethod.DELETE);
        lock.isLocked(messungObj);

        repository.delete(messungObj);
    }
}
