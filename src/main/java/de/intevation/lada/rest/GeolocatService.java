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
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.validation.Validator;

/**
 * REST service for Geolocat objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("geolocat")
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
     * The authorization module.
     */
    @Inject
    @AuthorizationConfig(type = AuthorizationType.HEADER)
    private Authorization authorization;

    @Inject
    private Validator<Geolocat> validator;

    /**
     * Get Geolocat objects.
     *
     * @param sampleId The requested objects can be filtered using
     * a URL parameter named sampleId.
     *
     * @return Response containing requested objects.
     */
    @GET
    @SuppressWarnings("unchecked")
    public Response get(
        @QueryParam("sampleId") @NotNull Integer sampleId
    ) {
        QueryBuilder<Geolocat> builder = repository
            .queryBuilder(Geolocat.class)
            .and("sampleId", sampleId);
        Response r = authorization.filter(
            repository.filter(builder.getQuery()),
            Geolocat.class);
        for (Geolocat otz: (List<Geolocat>) r.getData()) {
            validator.validate(otz);
        }
        return r;
    }

    /**
     * Get a Geolocat object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object containing a single Geolocat.
     */
    @GET
    @Path("{id}")
    public Response getById(
        @PathParam("id") Integer id
    ) {
        Response response = repository.getById(Geolocat.class, id);
        validator.validate(response.getData());
        return authorization.filter(
            response,
            Geolocat.class);
    }

    /**
     * Create a new Geolocat object.
     *
     * @return A response object containing the created Ort.
     */
    @POST
    public Response create(
        @Valid Geolocat ort
    ) {
        authorization.authorize(
            ort,
            RequestMethod.POST,
            Geolocat.class);
        validator.validate(ort);
        if (ort.hasErrors()) {
            return new Response(false, StatusCodes.ERROR_VALIDATION, ort);
        }

        return authorization.filter(
            repository.create(ort),
            Geolocat.class);
    }

    /**
     * Update an existing Geolocat object.
     *
     * @return Response object containing the updated Geolocat object.
     */
    @PUT
    @Path("{id}")
    public Response update(
        @PathParam("id") Integer id,
        @Valid Geolocat ort
    ) {
        authorization.authorize(
                ort,
                RequestMethod.PUT,
                Geolocat.class);
        lock.isLocked(ort);

        validator.validate(ort);
        if (ort.hasErrors()) {
            return new Response(false, StatusCodes.ERROR_VALIDATION, ort);
        }

        Response response = repository.update(ort);
        validator.validate(response.getData());
        return authorization.filter(
            response,
            Geolocat.class);
    }

    /**
     * Delete an existing Geolocat object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object.
     */
    @DELETE
    @Path("{id}")
    public Response delete(
        @PathParam("id") Integer id
    ) {
        Geolocat ortObj = repository.getByIdPlain(Geolocat.class, id);
        authorization.authorize(
            ortObj,
            RequestMethod.PUT,
            Geolocat.class);
        lock.isLocked(ortObj);

        return repository.delete(ortObj);
    }
}
