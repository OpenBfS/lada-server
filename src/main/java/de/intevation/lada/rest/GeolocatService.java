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
import de.intevation.lada.model.lada.Geolocat;
import de.intevation.lada.model.lada.Geolocat_;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;

/**
 * REST service for Geolocat objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "geolocat")
public class GeolocatService extends LadaService {

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
     * @param id The id is appended to the URL as a path parameter.
     * @return a single Geolocat.
     */
    @GET
    @Path("{id}")
    public Geolocat getById(
        @PathParam("id") Integer id
    ) {
        return repository.getById(Geolocat.class, id);
    }

    /**
     * Create a new Geolocat object.
     *
     * @return A response object containing the created Ort.
     * @throws BadRequestException if any constraint violations are detected.
     */
    @POST
    public Geolocat create(
        @Valid Geolocat ort
    ) throws BadRequestException {
        return repository.create(ort);
    }

    /**
     * Update an existing Geolocat object.
     *
     * @return the updated Geolocat object.
     * @throws BadRequestException if any constraint violations are detected.
     */
    @PUT
    @Path("{id}")
    public Geolocat update(
        @PathParam("id") Integer id,
        @Valid Geolocat ort
    ) throws BadRequestException {
        lock.isLocked(ort);
        return repository.update(ort);
    }

    /**
     * Delete an existing Geolocat object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     */
    @DELETE
    @Path("{id}")
    public void delete(
        @PathParam("id") Integer id
    ) {
        Geolocat ortObj = repository.getById(Geolocat.class, id);
        authorization.authorize(ortObj, RequestMethod.DELETE);
        lock.isLocked(ortObj);

        repository.delete(ortObj);
    }
}
