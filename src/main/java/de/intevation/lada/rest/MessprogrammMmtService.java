/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import de.intevation.lada.model.land.Messprogramm;
import de.intevation.lada.model.land.MessprogrammMmt;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.util.rest.Response;

/**
 * REST service for MessprogrammMmt objects.
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
 *      "id": [number],
 *      "letzteAenderung": [timestamp],
 *      "messgroessen": [array],
 *      "mmtId": [string],
 *      "messprogrammId": [number]
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
@Path("rest/messprogrammmmt")
@RequestScoped
public class MessprogrammMmtService {

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

    /**
     * Get all MessprogrammMmt objects.
     * <p>
     * The requested objects can be filtered using a URL parameter named
     * messprogrammId.
     * <p>
     * Example: http://example.com/messprogrammmmt?messprogrammId=[ID]
     *
     * @return Response object containing all MessprogrammMmt objects.
     */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
        @Context UriInfo info,
        @Context HttpServletRequest request
    ) {
        MultivaluedMap<String, String> params = info.getQueryParameters();
        if (params.isEmpty() || !params.containsKey("messprogrammId")) {
            return repository.getAll(MessprogrammMmt.class);
        }
        String messprogrammId = params.getFirst("messprogrammId");
        QueryBuilder<MessprogrammMmt> builder =
            new QueryBuilder<MessprogrammMmt>(
                repository.entityManager(),
                MessprogrammMmt.class);
        builder.and("messprogrammId", messprogrammId);
        return authorization.filter(
            request,
            repository.filter(builder.getQuery()),
            MessprogrammMmt.class);
    }

    /**
     * Get a MessprogrammMmt object by id.
     * <p>
     * The id is appended to the URL as a path parameter.
     * <p>
     * Example: http://example.com/messprogrammmmt/{id}
     *
     * @return Response object containing a single MessprogrammMmt.
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(
        @Context HttpServletRequest request,
        @PathParam("id") String id
    ) {
        return authorization.filter(
            request,
            repository.getById(
                MessprogrammMmt.class, Integer.valueOf(id)),
            MessprogrammMmt.class);
    }

    /**
     * Create a MessprogrammMmt object.
     * <p>
     * The new object is embedded in the post data as JSON formatted string.
     * <p>
     * <pre>
     * <code>
     * {
     *  "messprogrammId": [number],
     *  "mmtId": [string],
     *  "messgroessen": [array],
     *  "letzteAenderung": [date]
     * }
     * </code>
     * </pre>
     *
     * @return A response object containing the created MessprogrammMmt.
     */
    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(
        @Context HttpServletRequest request,
        MessprogrammMmt messprogrammmmt
    ) {
        if (!authorization.isAuthorized(
                request,
                messprogrammmmt,
                RequestMethod.POST,
                MessprogrammMmt.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }

        /* Persist the new messprogrammmmt object*/
        Response response = repository.create(messprogrammmmt);
        MessprogrammMmt ret = (MessprogrammMmt) response.getData();
        Response created =
            repository.getById(
                MessprogrammMmt.class, ret.getId());
        return authorization.filter(
            request,
            new Response(true, StatusCodes.OK, created.getData()),
            MessprogrammMmt.class);
    }

    /**
     * Update an existing MessprogrammMmt object.
     * <p>
     * The object to update should come as JSON formatted string.
     * <pre>
     * <code>
     * {
     *  "id": [number],
     *  "messprogrammId": [number],
     *  "mmtId": [string],
     *  "messgroessen": [array],
     *  "letzteAenderung": [date]
     * }
     * </code>
     * </pre>
     *
     * @return Response object containing the updated MessprogrammMmt object.
     */
    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(
        @Context HttpServletRequest request,
        @PathParam("id") String id,
        MessprogrammMmt messprogrammmmt
    ) {
        if (!authorization.isAuthorized(
                request,
                messprogrammmmt,
                RequestMethod.PUT,
                MessprogrammMmt.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }

        Response response = repository.update(messprogrammmmt);
        if (!response.getSuccess()) {
            return response;
        }
        Response updated = repository.getById(
            MessprogrammMmt.class,
            ((MessprogrammMmt) response.getData()).getId());
        return authorization.filter(
            request,
            updated,
            MessprogrammMmt.class);
    }

    /**
     * Delete an existing MessprogrammMmt object by id.
     * <p>
     * The id is appended to the URL as a path parameter.
     * <p>
     * Example: http://example.com/messprogammmmt/{id}
     *
     * @return Response object.
     */
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(
        @Context HttpServletRequest request,
        @PathParam("id") String id
    ) {
        /* Get the messprogrammmmt object by id*/
        Response messprogrammmmt =
            repository.getById(
                MessprogrammMmt.class, Integer.valueOf(id));
        MessprogrammMmt messprogrammmmtObj =
            (MessprogrammMmt) messprogrammmmt.getData();
        if (!authorization.isAuthorized(
                request,
                messprogrammmmtObj,
                RequestMethod.DELETE,
                Messprogramm.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }
        /* Delete the messprogrammmmt object*/
        return repository.delete(messprogrammmmtObj);
    }
}
