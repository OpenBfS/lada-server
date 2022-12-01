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
import de.intevation.lada.model.lada.Mpg;
import de.intevation.lada.model.lada.Sample;
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
 * REST service for Mpg objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("mpg")
public class MpgService extends LadaService {

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
     * The validator used for Mpg objects.
     */
    @Inject
    @ValidationConfig(type = "Messprogramm")
    private Validator validator;

    @Inject
    private ProbeFactory factory;

    /**
     * Get a Mpg object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object containing a single Mpg.
     */
    @GET
    @Path("{id}")
    public Response getById(
        @PathParam("id") Integer id
    ) {
        Response response =
            authorization.filter(
                repository.getById(Mpg.class, id),
                Mpg.class);
        return response;
    }

    /**
     * Create a Mpg object.
     *
     * @return A response object containing the created Mpg.
     */
    @POST
    public Response create(
        Mpg messprogramm
    ) {
        if (!authorization.isAuthorized(
                messprogramm,
                RequestMethod.POST,
                Mpg.class)
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

        if (messprogramm.getEnvMediumId() == null
            || messprogramm.getEnvMediumId().length() == 0
        ) {
            messprogramm = factory.findUmweltId(messprogramm);
        } else if ((messprogramm.getEnvMediumId() != null
                || !messprogramm.getEnvMediumId().equals(""))
            && (messprogramm.getEnvDescripId() == null
                || messprogramm.getEnvDescripId().equals(""))
        ) {
            messprogramm = factory.getInitialMediaDesk(messprogramm);
        }

        /* Persist the new messprogramm object*/
        return authorization.filter(
            repository.create(messprogramm),
            Mpg.class);
    }

    /**
     * Update an existing Mpg object.
     *
     * @return Response object containing the updated Mpg object.
     */
    @PUT
    @Path("{id}")
    public Response update(
        @PathParam("id") Integer id,
        Mpg messprogramm
    ) {
        if (!authorization.isAuthorized(
                messprogramm,
                RequestMethod.PUT,
                Mpg.class)
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

        if ((messprogramm.getEnvMediumId() == null
                || messprogramm.getEnvMediumId().equals(""))
            && !(messprogramm.getEnvDescripId() == null
                || messprogramm.getEnvDescripId().equals(""))
        ) {
            messprogramm = factory.findUmweltId(messprogramm);
        } else if (!(messprogramm.getEnvMediumId() == null
                || messprogramm.getEnvMediumId().equals(""))
            && (messprogramm.getEnvDescripId() == null
                || messprogramm.getEnvDescripId().equals(""))
            ) {
            messprogramm = factory.getInitialMediaDesk(messprogramm);
        }
        Response response = repository.update(messprogramm);
        if (!response.getSuccess()) {
            return response;
        }
        return authorization.filter(
            response,
            Mpg.class);
    }

    /**
     * Update the active attribute of existing Mpg objects as bulk
     * operation.
     *
     * @return Response object containing the success status of the operation
     * per Mpg.
     */
    @PUT
    @Path("aktiv")
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

        QueryBuilder<Mpg> builder =
            repository.queryBuilder(Mpg.class);
        builder.orIn("id", idList);
        List<Mpg> messprogramme =
            repository.filterPlain(builder.getQuery());

        List<Map<String, Integer>> result = new ArrayList<>();
        for (Mpg m : messprogramme) {
            Map<String, Integer> mpResult = new HashMap<>();
            int id = m.getId().intValue();
            mpResult.put("id", id);
            if (authorization.isAuthorized(
                    m, RequestMethod.PUT, Mpg.class)
            ) {
                m.setIsActive(active);
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
     * Delete an existing Mpg object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object.
     */
    @DELETE
    @Path("{id}")
    public Response delete(
        @PathParam("id") Integer id
    ) {
        Mpg messprogrammObj = repository.getByIdPlain(
            Mpg.class, id);
        /* check if probe references to the messprogramm exists */
        // TODO: This is a nice example of ORM-induced database misuse:
        QueryBuilder<Sample> builder = repository.queryBuilder(Sample.class);
        builder.and("mpgId", messprogrammObj.getId());
        List<Sample> probes =
            repository.filterPlain(builder.getQuery());
        if (probes.size() > 0) {
            return new Response(false, StatusCodes.ERROR_DELETE, null);
        }

        if (!authorization.isAuthorized(
                messprogrammObj,
                RequestMethod.DELETE,
                Mpg.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }
        /* Delete the messprogramm object*/
        Response response = repository.delete(messprogrammObj);
        return response;
    }
}
