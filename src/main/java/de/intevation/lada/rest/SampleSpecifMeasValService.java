/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import de.intevation.lada.lock.LockConfig;
import de.intevation.lada.lock.LockType;
import de.intevation.lada.lock.ObjectLocker;
import de.intevation.lada.model.lada.SampleSpecifMeasVal;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.util.rest.Response;

/**
 * REST service for SampleSpecifMeasVal objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("samplespecifmeasval")
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
     * The authorization module.
     */
    @Inject
    @AuthorizationConfig(type = AuthorizationType.HEADER)
    private Authorization authorization;

    /**
     * Get SampleSpecifMeasVal objects.
     *
     * @param sampleId The requested objects will be filtered using
     * a URL parameter named sampleId.
     *
     * @return Response containing requested objects.
     */
    @GET
    public Response get(
        @QueryParam("sampleId") @NotNull Integer sampleId
    ) {
        QueryBuilder<SampleSpecifMeasVal> builder =
            repository.queryBuilder(SampleSpecifMeasVal.class);
        builder.and("sampleId", sampleId);
        return authorization.filter(
            repository.filter(builder.getQuery()),
            SampleSpecifMeasVal.class);
    }

    /**
     * Get a SampleSpecifMeasVal object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object containing a single SampleSpecifMeasVal.
     */
    @GET
    @Path("{id}")
    public Response getById(
        @PathParam("id") Integer id
    ) {
        return authorization.filter(
            repository.getById(SampleSpecifMeasVal.class, id),
            SampleSpecifMeasVal.class);
    }

    /**
     * Create a SampleSpecifMeasVal object.
     *
     * @return A response object containing the created SampleSpecifMeasVal.
     */
    @POST
    public Response create(
        @Valid SampleSpecifMeasVal zusatzwert
    ) {
        if (!authorization.isAuthorized(
                zusatzwert,
                RequestMethod.POST,
                SampleSpecifMeasVal.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }

        // TODO: perform validation to avoid violating database constraints
        return authorization.filter(
            repository.create(zusatzwert),
            SampleSpecifMeasVal.class);
    }

    /**
     * Update an existing SampleSpecifMeasVal object.
     *
     * @return Response object containing the updated SampleSpecifMeasVal object.
     */
    @PUT
    @Path("{id}")
    public Response update(
        @PathParam("id") Integer id,
        @Valid SampleSpecifMeasVal zusatzwert
    ) {
        if (!authorization.isAuthorized(
                zusatzwert,
                RequestMethod.PUT,
                SampleSpecifMeasVal.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }
        if (lock.isLocked(zusatzwert)) {
            return new Response(false, StatusCodes.CHANGED_VALUE, null);
        }

        return authorization.filter(
            repository.update(zusatzwert),
            SampleSpecifMeasVal.class);
    }

    /**
     * Delete an existing SampleSpecifMeasVal object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object.
     */
    @DELETE
    @Path("{id}")
    public Response delete(
        @PathParam("id") Integer id
    ) {
        SampleSpecifMeasVal obj = repository.getByIdPlain(SampleSpecifMeasVal.class, id);
        if (!authorization.isAuthorized(
                obj,
                RequestMethod.DELETE,
                SampleSpecifMeasVal.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }
        if (lock.isLocked(obj)) {
            return new Response(false, StatusCodes.CHANGED_VALUE, null);
        }
        /* Delete the object*/
        return repository.delete(obj);
    }
}
