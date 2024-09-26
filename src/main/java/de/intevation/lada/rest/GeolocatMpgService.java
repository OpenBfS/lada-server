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
import de.intevation.lada.model.lada.GeolocatMpg;
import de.intevation.lada.model.lada.GeolocatMpg_;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;

/**
 * REST service for GeolocatMpg objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "geolocatmpg")
public class GeolocatMpgService extends LadaService {

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
     * Get GeolocatMpg objects.
     *
     * @param mpgId The requested objects will be filtered
     * using a URL parameter named mpgId.
     *
     * @return requested objects.
     */
    @GET
    public List<GeolocatMpg> get(
        @QueryParam("mpgId") @NotNull Integer mpgId
    ) {
        QueryBuilder<GeolocatMpg> builder = repository
            .queryBuilder(GeolocatMpg.class)
            .and(GeolocatMpg_.mpgId, mpgId);
        return repository.filter(builder.getQuery());
    }

    /**
     * Get single object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return GeolocatMpg object
     */
    @GET
    @Path("{id}")
    public GeolocatMpg getById(
        @PathParam("id") Integer id
    ) {
        return repository.getById(GeolocatMpg.class, id);
    }

    /**
     * Create a new GeolocatMpg object.
     *
     * @return A response object containing the created Ort.
     * @throws BadRequestException if any constraint violations are detected.
     */
    @POST
    public GeolocatMpg create(
        @Valid GeolocatMpg ort
    ) throws BadRequestException {
        return repository.create(ort);
    }

    /**
     * Update an existing GeolocatMpg object.
     *
     * @return the updated GeolocatMpg object.
     * @throws BadRequestException if any constraint violations are detected.
     */
    @PUT
    @Path("{id}")
    public GeolocatMpg update(
        @PathParam("id") Integer id,
        @Valid GeolocatMpg ort
    ) throws BadRequestException {
        return repository.update(ort);
    }

    /**
     * Delete object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     */
    @DELETE
    @Path("{id}")
    public void delete(
        @PathParam("id") Integer id
    ) {
        GeolocatMpg ortObj = repository.getById(
            GeolocatMpg.class, id);
        authorization.authorize(
            ortObj,
            RequestMethod.DELETE,
            GeolocatMpg.class);

        repository.delete(ortObj);
    }
}
