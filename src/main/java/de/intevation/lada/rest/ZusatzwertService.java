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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import de.intevation.lada.lock.LockConfig;
import de.intevation.lada.lock.LockType;
import de.intevation.lada.lock.ObjectLocker;
import de.intevation.lada.model.land.ZusatzWert;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.util.rest.Response;

/**
 * REST service for Zusatzwert objects.
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
 *      "messfehler": [number],
 *      "messwertPzs": [number],
 *      "nwgZuMesswert": [number],
 *      "probeId": [number],
 *      "pzsId": [string],
 *      "owner": [boolean],
 *      "readonly": [boolean],
 *      "treeModified": [timestamp],
 *      "parentModified": [timestamp]
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
@Path("rest/zusatzwert")
public class ZusatzwertService extends LadaService {

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
     * Get all Zusatzwert objects.
     * <p>
     * The requested objects can be filtered using a URL parameter named
     * probeId.
     * <p>
     * Example: http://example.com/zusatzwert?probeId=[ID]
     *
     *
     * @return Response object containing all Zusatzwert objects.
     */
    @GET
    @Path("/")
    public Response get(
        @Context HttpHeaders headers,
        @Context UriInfo info
    ) {
        MultivaluedMap<String, String> params = info.getQueryParameters();
        if (params.isEmpty() || !params.containsKey("probeId")) {
            return repository.getAll(ZusatzWert.class);
        }
        String probeId = params.getFirst("probeId");
        QueryBuilder<ZusatzWert> builder =
            repository.queryBuilder(ZusatzWert.class);
        builder.and("probeId", probeId);
        return authorization.filter(
            repository.filter(builder.getQuery()),
            ZusatzWert.class);
    }

    /**
     * Get a Zusatzwert object by id.
     * <p>
     * The id is appended to the URL as a path parameter.
     * <p>
     * Example: http://example.com/zusatzwert/{id}
     *
     * @return Response object containing a single Zusatzwert.
     */
    @GET
    @Path("/{id}")
    public Response getById(
        @Context HttpHeaders headers,
        @PathParam("id") String id
    ) {
        return authorization.filter(
            repository.getById(
                ZusatzWert.class, Integer.valueOf(id)),
            ZusatzWert.class);
    }

    /**
     * Create a Zusatzwert object.
     * <p>
     * The new object is embedded in the post data as JSON formatted string.
     * <p>
     * <pre>
     * <code>
     * {
     *  "owner": [boolean],
     *  "probeId": [number],
     *  "pzsId": [string],
     *  "nwgZuMesswert": [number],
     *  "messwertPzs": [number],
     *  "messfehler": [number],
     *  "treeModified": null,
     *  "parentModified": null,
     *  "letzteAenderung": [date]
     * }
     * </code>
     * </pre>
     *
     * @return A response object containing the created Zusatzwert.
     */
    @POST
    @Path("/")
    public Response create(
        @Context HttpHeaders headers,
        ZusatzWert zusatzwert
    ) {
        if (!authorization.isAuthorized(
                zusatzwert,
                RequestMethod.POST,
                ZusatzWert.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }

        // TODO: perform validation to avoid violating database constraints
        return authorization.filter(
            repository.create(zusatzwert),
            ZusatzWert.class);
    }

    /**
     * Update an existing Zusatzwert object.
     * <p>
     * The object to update should come as JSON formatted string.
     * <pre>
     * <code>
     * {
     *  "id": [number],
     *  "owner": [boolean],
     *  "probeId": [number],
     *  "pzsId": [string],
     *  "nwgZuMesswert": [number],
     *  "messwertPzs": [number],
     *  "messfehler": [number],
     *  "treeModified": [timestamp],
     *  "parentModified": [timestamp],
     *  "letzteAenderung": [date]
     * }
     * </code>
     * </pre>
     *
     * @return Response object containing the updated Zusatzwert object.
     */
    @PUT
    @Path("/{id}")
    public Response update(
        @Context HttpHeaders headers,
        @PathParam("id") String id,
        ZusatzWert zusatzwert
    ) {
        if (!authorization.isAuthorized(
                zusatzwert,
                RequestMethod.PUT,
                ZusatzWert.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }
        if (lock.isLocked(zusatzwert)) {
            return new Response(false, StatusCodes.CHANGED_VALUE, null);
        }
        Response response = repository.update(zusatzwert);
        if (!response.getSuccess()) {
            return response;
        }
        return authorization.filter(
            response,
            ZusatzWert.class);
    }

    /**
     * Delete an existing Zusatzwert object by id.
     * <p>
     * The id is appended to the URL as a path parameter.
     * <p>
     * Example: http://example.com/zusatzwert/{id}
     *
     * @return Response object.
     */
    @DELETE
    @Path("/{id}")
    public Response delete(
        @Context HttpHeaders headers,
        @PathParam("id") String id
    ) {
        /* Get the object by id*/
        Response object =
            repository.getById(
                ZusatzWert.class, Integer.valueOf(id));
        ZusatzWert obj = (ZusatzWert) object.getData();
        if (!authorization.isAuthorized(
                obj,
                RequestMethod.DELETE,
                ZusatzWert.class)
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
