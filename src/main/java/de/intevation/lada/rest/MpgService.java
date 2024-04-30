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
import de.intevation.lada.validation.constraints.IsValidPrimaryKey;

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
     * @return a single Mpg.
     */
    @GET
    @Path("{id}")
    public Mpg getById(
        @PathParam("id") Integer id
    ) {
        return authorization.filter(
            repository.getById(Mpg.class, id), Mpg.class);
    }

    /**
     * Create a Mpg object.
     *
     * @return A response object containing the created Mpg.
     */
    @POST
    public Mpg create(
        @Valid Mpg messprogramm
    ) {
        authorization.authorize(
            messprogramm,
            RequestMethod.POST,
            Mpg.class);

        if (messprogramm.getEnvMediumId() == null) {
            factory.findUmweltId(messprogramm);
        } else if (messprogramm.getEnvMediumId() != null
            && messprogramm.getEnvDescripDisplay() == null
        ) {
            factory.getInitialMediaDesk(messprogramm);
        }

        /* Persist the new messprogramm object*/
        return authorization.filter(
            repository.create(messprogramm),
            Mpg.class);
    }

    /**
     * Update an existing Mpg object.
     *
     * @return the updated Mpg object.
     */
    @PUT
    @Path("{id}")
    public Mpg update(
        @PathParam("id") Integer id,
        @Valid Mpg messprogramm
    ) {
        authorization.authorize(
            messprogramm,
            RequestMethod.PUT,
            Mpg.class);

        if (messprogramm.getEnvMediumId() == null) {
            factory.findUmweltId(messprogramm);
        } else if (messprogramm.getEnvMediumId() != null
            && messprogramm.getEnvDescripDisplay() == null
        ) {
            factory.getInitialMediaDesk(messprogramm);
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
     * @return the success status of the operation
     * per Mpg.
     */
    @PUT
    @Path("active")
    public List<Map<String, Integer>> setActive(
        @Valid SetActive data
    ) {
        QueryBuilder<Mpg> builder = repository.queryBuilder(Mpg.class)
            .orIn("id", data.getIds());
        List<Mpg> messprogramme =
            repository.filter(builder.getQuery());

        List<Map<String, Integer>> result = new ArrayList<>();
        for (Mpg m : messprogramme) {
            Map<String, Integer> mpResult = new HashMap<>();
            int id = m.getId().intValue();
            mpResult.put("id", id);
            if (authorization.isAuthorized(
                    m, RequestMethod.PUT, Mpg.class)
            ) {
                m.setIsActive(data.isActive());
                repository.update(m);
                mpResult.put("success", StatusCodes.OK);
            } else {
                mpResult.put("success", StatusCodes.NOT_ALLOWED);
            }
            result.add(mpResult);
        }

        return result;
    }

    /**
     * Delete an existing Mpg object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     */
    @DELETE
    @Path("{id}")
    public void delete(
        @PathParam("id") Integer id
    ) {
        Mpg messprogrammObj = repository.getById(
            Mpg.class, id);
        authorization.authorize(
            messprogrammObj,
            RequestMethod.DELETE,
            Mpg.class);
        repository.delete(messprogrammObj);
    }
}
