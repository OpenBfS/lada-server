/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import de.intevation.lada.factory.ProbeFactory;
import de.intevation.lada.model.land.Messprogramm;
import de.intevation.lada.model.land.Probe;
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
 * REST service for Messprogramm objects.
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
 *      "baId": [string],
 *      "datenbasisId": [number],
 *      "gemId": [string],
 *      "gueltigBis": [date],
 *      "gueltigVon": [date],
 *      "intervallOffset": [number],
 *      "letzteAenderung": [timestamp],
 *      "mediaDesk": [string],
 *      "mstId": [string],
 *      "mplId": [number],
 *      "name": [string],
 *      "netzbetreiberId": [string],
 *      "ortId": [string],
 *      "probeKommentar": [string],
 *      "probeNehmerId": [number],
 *      "probenartId": [number],
 *      "probenintervall": [string],
 *      "teilintervallBis": [number],
 *      "teilintervallVon": [number],
 *      "test": [boolean],
 *      "umwId": [string]
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
@Path("rest/messprogramm")
public class MessprogrammService extends LadaService {

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
     * The validator used for Messprogramm objects.
     */
    @Inject
    @ValidationConfig(type = "Messprogramm")
    private Validator validator;

    @Inject
    private ProbeFactory factory;

    /**
     * Get a Messprogramm object by id.
     * <p>
     * The id is appended to the URL as a path parameter.
     * <p>
     * Example: http://example.com/messprogramm/{id}
     *
     * @return Response object containing a single Messprogramm.
     */
    @GET
    @Path("/{id}")
    public Response getById(
        @PathParam("id") String id
    ) {
        Response response =
            authorization.filter(
                repository.getById(
                    Messprogramm.class, Integer.valueOf(id)),
                Messprogramm.class);
        return response;
    }

    /**
     * Create a Messprogramm object.
     * <p>
     * The new object is embedded in the post data as JSON formatted string.
     * <p>
     * <pre>
     * <code>
     * {
     *  "test": [boolean],
     *  "netzbetreiberId": [string],
     *  "mstId": [string],
     *  "name": [string],
     *  "datenbasisId": [number],
     *  "baId": [string],
     *  "gemId": [string],
     *  "ortId": [string],
     *  "mediaDesk": [string],
     *  "mplId": [number],
     *  "umwId": [string],
     *  "probenartId": [number],
     *  "probenintervall": [string],
     *  "teilintervallVon": [number],
     *  "teilintervallBis": [number],
     *  "intervallOffset": [string],
     *  "probeNehmerId": [number],
     *  "probeKommentar": [string],
     *  "letzteAenderung": [date]
     * }
     * </code>
     * </pre>
     *
     * @return A response object containing the created Messprogramm.
     */
    @POST
    @Path("/")
    public Response create(
        Messprogramm messprogramm
    ) {
        if (!authorization.isAuthorized(
                messprogramm,
                RequestMethod.POST,
                Messprogramm.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }

        Violation violation = validator.validate(messprogramm);
        if (violation.hasErrors()) {
            Response response = new Response(
                false, StatusCodes.ERROR_VALIDATION, messprogramm);
            response.setErrors(violation.getErrors());
            response.setWarnings(violation.getWarnings());
            return response;
        }

        if (messprogramm.getUmwId() == null
            || messprogramm.getUmwId().length() == 0
        ) {
            messprogramm = factory.findUmweltId(messprogramm);
        } else if ((messprogramm.getUmwId() != null
                || !messprogramm.getUmwId().equals(""))
            && (messprogramm.getMediaDesk() == null
                || messprogramm.getMediaDesk().equals(""))
        ) {
            messprogramm = factory.getInitialMediaDesk(messprogramm);
        }

        /* Persist the new messprogramm object*/
        return authorization.filter(
            repository.create(messprogramm),
            Messprogramm.class);
    }

    /**
     * Update an existing Messprogramm object.
     * <p>
     * The object to update should come as JSON formatted string.
     * <pre>
     * <code>
     * {
     *  "id": [number],
     *  "test": [boolean],
     *  "netzbetreiberId": [string],
     *  "mstId": [string],
     *  "name": [string],
     *  "datenbasisId": [number],
     *  "baId": [string],
     *  "gemId": [string],
     *  "mplId": [number],
     *  "ortId": [string],
     *  "mediaDesk": [string],
     *  "umwId": [string],
     *  "probenartId": [number],
     *  "probenintervall": [string],
     *  "teilintervallVon": [number],
     *  "teilintervallBis": [number],
     *  "intervallOffset": [string],
     *  "probeNehmerId": [number],
     *  "probeKommentar": [string],
     *  "letzteAenderung": [date]
     * }
     * </code>
     * </pre>
     *
     * @return Response object containing the updated Messprogramm object.
     */
    @PUT
    @Path("/{id}")
    public Response update(
        @PathParam("id") String id,
        Messprogramm messprogramm
    ) {
        if (!authorization.isAuthorized(
                messprogramm,
                RequestMethod.PUT,
                Messprogramm.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }

        Violation violation = validator.validate(messprogramm);
        if (violation.hasErrors()) {
            Response response =
                new Response(false, StatusCodes.ERROR_VALIDATION, messprogramm);
            response.setErrors(violation.getErrors());
            response.setWarnings(violation.getWarnings());
            return response;
        }

        if ((messprogramm.getUmwId() == null
                || messprogramm.getUmwId().equals(""))
            && !(messprogramm.getMediaDesk() == null
                || messprogramm.getMediaDesk().equals(""))
        ) {
            messprogramm = factory.findUmweltId(messprogramm);
        } else if (!(messprogramm.getUmwId() == null
                || messprogramm.getUmwId().equals(""))
            && (messprogramm.getMediaDesk() == null
                || messprogramm.getMediaDesk().equals(""))
            ) {
            messprogramm = factory.getInitialMediaDesk(messprogramm);
        }
        Response response = repository.update(messprogramm);
        if (!response.getSuccess()) {
            return response;
        }
        return authorization.filter(
            response,
            Messprogramm.class);
    }

    /**
     * Update the active attribute of existing Messprogramm objects as bulk
     * operation.
     * <p>
     * The object to update should come as JSON formatted string.
     * <pre>
     * <code>
     * {
     *  "aktiv: [boolean],
     *  "ids": [Array[Number]]
     * }
     * </code>
     * </pre>
     *
     * @return Response object containing the success status of the operation
     * per messprogramm.
     */
    @PUT
    @Path("/aktiv")
    public Response setAktiv(
        JsonObject data
    ) {
        Boolean active;
        try {
            active = data.getBoolean("aktiv");
        } catch (NullPointerException npe) {
            return new Response(false, StatusCodes.NOT_EXISTING, null);
        }

        List<Integer> idList = new ArrayList<>();
        try {
            JsonArray ids = data.getJsonArray("ids");
            if (ids.size() == 0) {
                return new Response(false, StatusCodes.NOT_EXISTING, null);
            }
            for (int i = 0; i < ids.size(); i++) {
                idList.add(ids.getInt(i));
            }
        } catch (NullPointerException npe) {
            return new Response(false, StatusCodes.NOT_EXISTING, null);
        }

        QueryBuilder<Messprogramm> builder =
            repository.queryBuilder(Messprogramm.class);
        builder.orIn("id", idList);
        List<Messprogramm> messprogramme =
            repository.filterPlain(builder.getQuery());

        List<Map<String, Integer>> result = new ArrayList<>();
        for (Messprogramm m : messprogramme) {
            Map<String, Integer> mpResult = new HashMap<>();
            int id = m.getId().intValue();
            mpResult.put("id", id);
            if (authorization.isAuthorized(
                    m, RequestMethod.PUT, Messprogramm.class)
            ) {
                m.setAktiv(active);
                Response r = repository.update(m);
                int code = Integer.valueOf(r.getMessage()).intValue();
                mpResult.put("success", code);
            } else {
                mpResult.put("success", StatusCodes.NOT_ALLOWED);
            }
            result.add(mpResult);
        }

        return new Response(true, StatusCodes.OK, result);
    }

    /**
     * Delete an existing Messprogramm object by id.
     * <p>
     * The id is appended to the URL as a path parameter.
     * <p>
     * Example: http://example.com/messprogamm/{id}
     *
     * @return Response object.
     */
    @DELETE
    @Path("/{id}")
    public Response delete(
        @PathParam("id") String id
    ) {
        /* Get the messprogamm object by id*/
        Response messprogramm =
            repository.getById(
                Messprogramm.class, Integer.valueOf(id));
        Messprogramm messprogrammObj = (Messprogramm) messprogramm.getData();
        /* check if probe references to the messprogramm exists */
        QueryBuilder<Probe> builder = repository.queryBuilder(Probe.class);
        builder.and("mprId",  ((Messprogramm) messprogramm.getData()).getId());
        List<Probe> probes =
            repository.filterPlain(builder.getQuery());
        if (probes.size() > 0) {
            return new Response(false, StatusCodes.ERROR_DELETE, null);
        }

        if (!authorization.isAuthorized(
                messprogrammObj,
                RequestMethod.DELETE,
                Messprogramm.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }
        /* Delete the messprogramm object*/
        Response response = repository.delete(messprogrammObj);
        return response;
    }
}
