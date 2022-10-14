/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest.stamm;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import de.intevation.lada.model.stammdaten.GemeindeUntergliederung;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.rest.LadaService;

/**
 * REST service for GemeindeUntergliederung objects.
 * <p>
 * The services produce data in the application/json media type.
 * A typical response holds information about the action performed and the data.
 * <pre>
 * <code>
 * {
 *  "success": [boolean],
 *  "message": [string],
 *  "data":[{
 *      "id": [number],
 *      "netzbetreiber_id": [string],
 *      "gemId": [string],
 *      "ozkId": [number],
 *      "letzteAenderung": [timestamp],
 *      "readonly": [boolean]
 *  }],
 *  "errors": [object],
 *  "warnings": [object],
 *  "notifications": [object],
 *  "readonly": [boolean],
 *  "totalCount": [number]
 * }
 * </code>
 * </pre>
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("rest/gemeindeuntergliederung")
public class GemeindeUntergliederungService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    @Inject
    @AuthorizationConfig(type = AuthorizationType.HEADER)
    private Authorization authorization;

    /**
     * Get all GemeindeUntergliederung objects.
     *
     * @return Response containing requested objects.
     */
    @GET
    @Path("/")
    public Response get() {
        List<GemeindeUntergliederung> gemUntergliederung =
            repository.getAllPlain(GemeindeUntergliederung.class);
        for (GemeindeUntergliederung gu: gemUntergliederung) {
            // TODO Do not iterate all the objects if its not necessary
            gu.setReadonly(true);
                // !authorization.isAuthorized(
                //     gu,
                //     RequestMethod.POST,
                //     GemeindeUntergliederung.class));
        }
        return new Response(true, StatusCodes.OK, gemUntergliederung, gemUntergliederung.size());
    }

    /**
     * Get a single object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object containing a single object.
     */
    @GET
    @Path("/{id}")
    public Response getById(
        @PathParam("id") Integer id
    ) {
        GemeindeUntergliederung gu = repository.getByIdPlain(
            GemeindeUntergliederung.class, id);
        gu.setReadonly(
            !authorization.isAuthorized(
                gu,
                RequestMethod.POST,
                GemeindeUntergliederung.class
            )
        );
        return new Response(true, StatusCodes.OK, gu);
    }

    @POST
    @Path("/")
    public Response create(
        GemeindeUntergliederung gemUntergliederung
    ) {
        if (!authorization.isAuthorized(
            gemUntergliederung,
            RequestMethod.POST,
            GemeindeUntergliederung.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, gemUntergliederung);
        }
        QueryBuilder<GemeindeUntergliederung> builder =
            repository.queryBuilder(GemeindeUntergliederung.class);
        builder.and("ozkId", gemUntergliederung.getOzkId());
        builder.and("netzbetreiberId", gemUntergliederung.getNetzbetreiberId());
        List<GemeindeUntergliederung> gemUntergliederungn =
            repository.filterPlain(builder.getQuery());
        if (gemUntergliederungn.isEmpty()) {
            return repository.create(gemUntergliederung);
        }
        return new Response(false, StatusCodes.IMP_DUPLICATE, null);
    }

    @PUT
    @Path("/{id}")
    public Response update(
        @PathParam("id") Integer id,
        GemeindeUntergliederung gemUntergliederung
    ) {
        if (!authorization.isAuthorized(
            gemUntergliederung,
            RequestMethod.PUT,
            GemeindeUntergliederung.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, gemUntergliederung);
        }
        QueryBuilder<GemeindeUntergliederung> builder =
            repository.queryBuilder(GemeindeUntergliederung.class);
        builder.and("ozkId", gemUntergliederung.getOzkId());
        builder.and("netzbetreiberId", gemUntergliederung.getNetzbetreiberId());
        List<GemeindeUntergliederung> gemUntergliederungn =
            repository.filterPlain(builder.getQuery());
        if (!gemUntergliederungn.isEmpty()
            && !gemUntergliederungn.get(0).getId().equals(gemUntergliederung.getId())
        ) {
            return new Response(false, StatusCodes.IMP_DUPLICATE, null);
        }
        return repository.update(gemUntergliederung);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(
        @PathParam("id") Integer id
    ) {
        GemeindeUntergliederung gemUntergliederung = repository.getByIdPlain(
            GemeindeUntergliederung.class, id);
        if (gemUntergliederung == null
            || !authorization.isAuthorized(
                gemUntergliederung,
                RequestMethod.DELETE,
                GemeindeUntergliederung.class
            )
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }
        return repository.delete(gemUntergliederung);
    }
}
