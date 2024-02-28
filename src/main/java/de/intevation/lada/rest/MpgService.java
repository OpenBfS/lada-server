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

import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import de.intevation.lada.factory.ProbeFactory;
import de.intevation.lada.model.lada.Mpg;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.validation.constraints.IsValidPrimaryKey;
import de.intevation.lada.validation.Validator;

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
    private Validator<Mpg> validator;

    @Inject
    private ProbeFactory factory;

    /**
     * Expected format for payload in PUT request to setActive.
     */
    public static class SetActive {
        @NotNull
        private Boolean active;

        @NotNull
        private List<@NotNull @IsValidPrimaryKey(
            clazz = Mpg.class) Integer> ids;

        public Boolean isActive() {
            return this.active;
        }
        public void setActive(Boolean active) {
            this.active = active;
        }

        public List<Integer> getIds() {
            return this.ids;
        }
        public void setIds(List<Integer> ids) {
            this.ids = ids;
        }
    }

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
        @Valid Mpg messprogramm
    ) {
        authorization.authorize(
            messprogramm,
            RequestMethod.POST,
            Mpg.class);

        validator.validate(messprogramm);

        if (messprogramm.getEnvMediumId() == null
            || messprogramm.getEnvMediumId().length() == 0
        ) {
            messprogramm = factory.findUmweltId(messprogramm);
        } else if ((messprogramm.getEnvMediumId() != null
                || !messprogramm.getEnvMediumId().equals(""))
            && (messprogramm.getEnvDescripDisplay() == null
                || messprogramm.getEnvDescripDisplay().equals(""))
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
        @Valid Mpg messprogramm
    ) {
        authorization.authorize(
            messprogramm,
            RequestMethod.PUT,
            Mpg.class);

        validator.validate(messprogramm);

        if ((messprogramm.getEnvMediumId() == null
                || messprogramm.getEnvMediumId().equals(""))
            && !(messprogramm.getEnvDescripDisplay() == null
                || messprogramm.getEnvDescripDisplay().equals(""))
        ) {
            messprogramm = factory.findUmweltId(messprogramm);
        } else if (!(messprogramm.getEnvMediumId() == null
                || messprogramm.getEnvMediumId().equals(""))
            && (messprogramm.getEnvDescripDisplay() == null
                || messprogramm.getEnvDescripDisplay().equals(""))
            ) {
            messprogramm = factory.getInitialMediaDesk(messprogramm);
        }

        return authorization.filter(
            repository.update(messprogramm),
            Mpg.class);
    }

    /**
     * Update the active attribute of existing Mpg objects as bulk
     * operation.
     *
     * @param data Object representing active status and list of IDs
     * @return Response object containing the success status of the operation
     * per Mpg.
     */
    @PUT
    @Path("active")
    public Response setActive(
        @Valid SetActive data
    ) {
        QueryBuilder<Mpg> builder = repository.queryBuilder(Mpg.class)
            .orIn("id", data.getIds());
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
                m.setIsActive(data.isActive());
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
        authorization.authorize(
            messprogrammObj,
            RequestMethod.DELETE,
            Mpg.class);
        /* Delete the messprogramm object*/
        Response response = repository.delete(messprogrammObj);
        return response;
    }
}
