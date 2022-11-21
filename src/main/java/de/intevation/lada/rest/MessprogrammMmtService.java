/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import de.intevation.lada.model.land.Mpg;
import de.intevation.lada.model.land.MessprogrammMmt;
import de.intevation.lada.model.master.Measd;
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
public class MessprogrammMmtService extends LadaService {

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
    @ValidationConfig(type = "MessprogrammMmt")
    private Validator validator;

    /**
     * Get MessprogrammMmt objects.
     *
     * @param messprogrammId The requested objects will be filtered
     * using a URL parameter named messprogrammId.
     * Example: http://example.com/messprogrammmmt?messprogrammId=[ID]
     *
     * @return Response containing requested objects.
     */
    @GET
    @Path("/")
    public Response get(
        @QueryParam("messprogrammId") @NotNull Integer messprogrammId
    ) {
        QueryBuilder<MessprogrammMmt> builder =
            repository.queryBuilder(MessprogrammMmt.class);
        builder.and("mpgId", messprogrammId);
        return authorization.filter(
            repository.filter(builder.getQuery()),
            MessprogrammMmt.class);
    }

    /**
     * Get a MessprogrammMmt object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object containing a single MessprogrammMmt.
     */
    @GET
    @Path("/{id}")
    public Response getById(
        @PathParam("id") Integer id
    ) {
        return authorization.filter(
            repository.getById(MessprogrammMmt.class, id),
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
    public Response create(
        MessprogrammMmt messprogrammmmt
    ) {
        if (!authorization.isAuthorized(
                messprogrammmmt,
                RequestMethod.POST,
                MessprogrammMmt.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }

        Violation violation = validator.validate(messprogrammmmt);
        if (violation.hasErrors()) {
            Response response = new Response(
                false, StatusCodes.ERROR_VALIDATION, messprogrammmmt);
            response.setErrors(violation.getErrors());
            response.setWarnings(violation.getWarnings());
            return response;
        }

        setMessgroesseObjects(messprogrammmmt);

        /* Persist the new messprogrammmmt object*/
        return authorization.filter(
            repository.create(messprogrammmmt),
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
    public Response update(
        @PathParam("id") Integer id,
        MessprogrammMmt messprogrammmmt
    ) {
        if (!authorization.isAuthorized(
                messprogrammmmt,
                RequestMethod.PUT,
                MessprogrammMmt.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }

        Violation violation = validator.validate(messprogrammmmt);
        if (violation.hasErrors()) {
            Response response = new Response(
                false, StatusCodes.ERROR_VALIDATION, messprogrammmmt);
            response.setErrors(violation.getErrors());
            response.setWarnings(violation.getWarnings());
            return response;
        }

        setMessgroesseObjects(messprogrammmmt);

        Response response = repository.update(messprogrammmmt);
        if (!response.getSuccess()) {
            return response;
        }
        return authorization.filter(
            response,
            MessprogrammMmt.class);
    }

    /**
     * Delete an existing MessprogrammMmt object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object.
     */
    @DELETE
    @Path("/{id}")
    public Response delete(
        @PathParam("id") Integer id
    ) {
        MessprogrammMmt messprogrammmmtObj = repository.getByIdPlain(
            MessprogrammMmt.class, id);
        if (!authorization.isAuthorized(
                messprogrammmmtObj,
                RequestMethod.DELETE,
                Mpg.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }
        /* Delete the messprogrammmmt object*/
        return repository.delete(messprogrammmmtObj);
    }

    /**
     * Initialize referenced objects from given IDs.
     */
    private void setMessgroesseObjects(MessprogrammMmt mm) {
        Set<Measd> mos = new HashSet<>();
        for (Integer mId: mm.getMeasds()) {
            Measd m = repository.getByIdPlain(Measd.class, mId);
            if (m != null) {
                mos.add(m);
            }
        }
        mm.setMeasdObjects(mos);
    }
}
