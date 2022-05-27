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

import de.intevation.lada.model.land.KommentarM;
import de.intevation.lada.model.land.Messung;
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
 * REST service for KommentarM objects.
 * <p>
 * The services produce data in the application/json media type.
 * All HTTP methods use the authorization module to determine if the user is
 * allowed to perform the requested action.
 * A typical response holds information about the action performed and the data.
 * <pre>
 * <code>
 * {
 *  "success": [boolean];
 *  "message": [string],
 *  "data":[{
 *      "messungsId": [number],
 *      "datum": [timestamp],
 *      "erzeuger": [string],
 *      "id": [number],
 *      "text": [string],
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
@Path("rest/mkommentar")
public class KommentarMService extends LadaService {

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
    @ValidationConfig(type = "KommentarM")
    private Validator validator;

    /**
     * Get KommentarM objects.
     *
     * @param messungsId The requested objects have to be filtered
     * using an URL parameter named messungsId.
     * Example: http://example.com/mkommentar?messungsId=[ID]
     *
     * @return Response object containing filtered KommentarM objects.
     * Status-Code 699 if parameter is missing or requested objects are
     * not authorized.
     */
    @GET
    @Path("/")
    public Response get(
        @QueryParam("messungsId") Integer messungsId
    ) {
        if (messungsId == null) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }

        Messung messung = repository.getByIdPlain(Messung.class, messungsId);
        if (!authorization.isAuthorized(
                messung, RequestMethod.GET, Messung.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }

        QueryBuilder<KommentarM> builder =
            repository.queryBuilder(KommentarM.class);
        builder.and("messungsId", messungsId);
        return authorization.filter(
            repository.filter(builder.getQuery()),
            KommentarM.class);
    }

    /**
     * Get a single KommentarM object by id.
     * <p>
     * The id is appended to the URL as a path parameter.
     * <p>
     * Example: http://example.com/mkommentar/{id}
     *
     * @return Response object containing a single KommentarM.
     */
    @GET
    @Path("/{id}")
    public Response getById(
        @PathParam("id") String id
    ) {
        Response response =
            repository.getById(
                KommentarM.class, Integer.valueOf(id));
        KommentarM kommentar = (KommentarM) response.getData();
        Messung messung = repository.getByIdPlain(
            Messung.class, kommentar.getMessungsId());
        if (!authorization.isAuthorized(
                messung, RequestMethod.GET, Messung.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }

        return authorization.filter(
            response,
            KommentarM.class);
    }

    /**
     * Create a KommentarM object.
     * <p>
     * The new object is embedded in the post data as JSON formatted string.
     * <p>
     * <pre>
     * <code>
     * {
     *  messungsId: [number],
     *  erzeuger: [string],
     *  text: [string],
     *  datum: [date]
     *  owner: [boolean],
     * }
     * </code>
     * </pre>
     * @return A response object containing the created KommentarM.
     */
    @POST
    @Path("/")
    public Response create(
        KommentarM kommentar
    ) {
        if (!authorization.isAuthorized(
                kommentar,
                RequestMethod.POST,
                KommentarM.class)
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
            KommentarM.class);
        }
    }

    /**
     * Update an existing KommentarM object.
     * <p>
     * The object to update should come as JSON formatted string.
     * <pre>
     * <code>
     * {
     *  "id": [number],
     *  "owner": [boolean],
     *  "messungsId": [number],
     *  "erzeuger": [string],
     *  "text": [string],
     *  "datum": [date]
     * }
     * </code>
     * </pre>
     *
     * @return Response object containing the updated KommentarM object.
     */
    @PUT
    @Path("/{id}")
    public Response update(
        @PathParam("id") String id,
        KommentarM kommentar
    ) {
        if (!authorization.isAuthorized(
                kommentar,
                RequestMethod.PUT,
                KommentarM.class)
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
            KommentarM.class);
        }
    }

    /**
     * Delete an existing KommentarM object by id.
     * <p>
     * The id is appended to the URL as a path parameter.
     * <p>
     * Example: http://example.com/mkommentar/{id}
     *
     * @return Response object.
     */
    @DELETE
    @Path("/{id}")
    public Response delete(
        @PathParam("id") String id
    ) {
        /* Get the object by id*/
        Response kommentar =
            repository.getById(
                KommentarM.class, Integer.valueOf(id));
        KommentarM kommentarObj = (KommentarM) kommentar.getData();
        if (!authorization.isAuthorized(
                kommentarObj,
                RequestMethod.DELETE,
                KommentarM.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }
        return repository.delete(kommentarObj);
    }
}
