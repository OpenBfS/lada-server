/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import de.intevation.lada.model.land.KommentarP;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.validation.Validator;
import de.intevation.lada.validation.Violation;
import de.intevation.lada.validation.annotation.ValidationConfig;

/**
 * REST service to operate on KommentarP objects.
 * <p>
 * The services produce data in the application/json media type.
 * All HTTP methods use the authorization module to determine if the user is
 * allowed to perform the requested action.
 * A typical response holds information about the action performed and the data.
 * <pre>
 * <code>
 * {
 *  "success": [boolean],
 *  "message": [string],
 *  "data":[{
 *      "datum": [timestamp],
 *      "erzeuger": [string],
 *      "id": [number],
 *      "text": [string],
 *      "probeId": [number],
 *      "owner": [boolean],
 *      "readonly": [boolean]
 *  }],
 *  "errors": [object],
 *  "warnings": [object],
 *  "readonly": [boolean],
 *  "totalCount": [number]
 * }
 * </code>
 * </pre>
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("rest/pkommentar")
public class KommentarPService extends LadaService {

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
    @ValidationConfig(type = "KommentarP")
    private Validator validator;


    /**
     * Get KommentarP objects.
     *
     * @param probeId The requested objects can be filtered
     * using an URL parameter named probeId.
     * Example: http://example.com/pkommentar?probeId=[ID]
     *
     * @return Response object containing requested objects.
     */
    @GET
    @Path("/")
    public Response get(
        @QueryParam("probeId") Integer probeId
    ) {
        if (probeId == null) {
            return repository.getAll(KommentarP.class);
        }
        QueryBuilder<KommentarP> builder =
            repository.queryBuilder(KommentarP.class);
        builder.and("probeId", probeId);
        return authorization.filter(
            repository.filter(builder.getQuery()),
            KommentarP.class);
    }

    /**
     * Get a single KommentarP object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object containing a single KommentarP.
     */
    @GET
    @Path("/{id}")
    public Response getById(
        @PathParam("id") Integer id
    ) {
        return authorization.filter(
            repository.getById(KommentarP.class, id),
            KommentarP.class);
    }

    /**
     * Create a new KommentarP object.
     * <p>
     * The new object is embedded in the post data as JSON formatted string.
     * <p>
     * <pre>
     * <code>
     * {
     *  "probeId": [number],
     *  "erzeuger": [string],
     *  "text": [string],
     *  "datum": [date],
     *  "owner": [boolean]
     * }
     * </code>
     * </pre>
     *
     * @return Response object containing the new KommentarP.
     */
    @POST
    @Path("/")
    public Response create(
        KommentarP kommentar
    ) {
        if (!authorization.isAuthorized(
                kommentar,
                RequestMethod.POST,
                KommentarP.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }
        Violation violation = validator.validate(kommentar);
        if (violation.hasErrors()) {
            Response response =
                new Response(false, StatusCodes.VAL_EXISTS, kommentar);
            return response;
        } else {
        /* Persist the new object*/
        return authorization.filter(
            repository.create(kommentar),
            KommentarP.class);
        }
    }

    /**
     * Update an existing KommentarP object.
     * <p>
     * The object to update should come as JSON formatted string.
     * <pre>
     * <code>
     * {
     *  "id": [number],
     *  "owner": [boolean],
     *  "probeId": [number],
     *  "erzeuger": [string],
     *  "text": [string],
     *  "datum": [date]
     * }
     * </code>
     * </pre>
     *
     * @return Response object containing the updated KommentarP object.
     */
    @PUT
    @Path("/{id}")
    public Response update(
        @PathParam("id") Integer id,
        KommentarP kommentar
    ) {
        if (!authorization.isAuthorized(
                kommentar,
                RequestMethod.PUT,
                KommentarP.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }
        Violation violation = validator.validate(kommentar);
        if (violation.hasErrors()) {
            Response response =
                new Response(false, StatusCodes.VAL_EXISTS, kommentar);
            return response;
        } else {
        return authorization.filter(
            repository.update(kommentar),
            KommentarP.class);
        }
    }

    /**
     * Delete an existing KommentarP by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object.
     */
    @DELETE
    @Path("/{id}")
    public Response delete(
        @PathParam("id") Integer id
    ) {
        KommentarP kommentarObj = repository.getByIdPlain(KommentarP.class, id);
        if (!authorization.isAuthorized(
                kommentarObj,
                RequestMethod.DELETE,
                KommentarP.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }
        return repository.delete(kommentarObj);
    }
}
