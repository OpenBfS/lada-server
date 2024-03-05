/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

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

import de.intevation.lada.model.lada.CommSample;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.validation.Validator;

/**
 * REST service to operate on CommSample objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("commsample")
public class CommSampleService extends LadaService {

    /**
     * The data repository granting read/write access.
     */
    @Inject
    private Repository repository;

    /**
     * The authorization module.
     */
    @Inject
    @AuthorizationConfig(type = AuthorizationType.HEADER)
    private Authorization authorization;

    @Inject
    private Validator<CommSample> validator;


    /**
     * Get CommSample objects.
     *
     * @param sampleId The requested objects will be filtered
     * using an URL parameter named sampleId.
     *
     * @return Response object containing requested objects.
     */
    @GET
    public Response get(
        @QueryParam("sampleId") @NotNull Integer sampleId
    ) {
        QueryBuilder<CommSample> builder =
            repository.queryBuilder(CommSample.class);
        builder.and("sampleId", sampleId);
        return authorization.filter(
            repository.filter(builder.getQuery()),
            CommSample.class);
    }

    /**
     * Get a single CommSample object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object containing a single CommSample.
     */
    @GET
    @Path("{id}")
    public Response getById(
        @PathParam("id") Integer id
    ) {
        return authorization.filter(
            repository.getById(CommSample.class, id),
            CommSample.class);
    }

    /**
     * Create a new CommSample object.
     *
     * @return Response object containing the new CommSample.
     */
    @POST
    public Response create(
        @Valid CommSample kommentar
    ) {
        authorization.authorize(
            kommentar,
            RequestMethod.POST,
            CommSample.class);
        validator.validate(kommentar);
        return authorization.filter(
            repository.create(kommentar), CommSample.class);
    }

    /**
     * Update an existing CommSample object.
     *
     * @return Response object containing the updated CommSample object.
     */
    @PUT
    @Path("{id}")
    public Response update(
        @PathParam("id") Integer id,
        @Valid CommSample kommentar
    ) {
        authorization.authorize(
            kommentar,
            RequestMethod.PUT,
            CommSample.class);
        validator.validate(kommentar);
        return authorization.filter(
            repository.update(kommentar), CommSample.class);
    }

    /**
     * Delete an existing CommSample by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object.
     */
    @DELETE
    @Path("{id}")
    public Response delete(
        @PathParam("id") Integer id
    ) {
        CommSample kommentarObj = repository.getByIdPlain(CommSample.class, id);
        authorization.authorize(
            kommentarObj,
            RequestMethod.DELETE,
            CommSample.class);
        return repository.delete(kommentarObj);
    }
}
