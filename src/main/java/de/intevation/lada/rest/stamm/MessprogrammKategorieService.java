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

import de.intevation.lada.model.stammdaten.MessprogrammKategorie;
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
 * REST service for DatensatzErzeuger objects.
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
 *      "bezeichnung": [string],
 *      "letzteAenderung": [timestamp],
 *      "netzbetreiberId": [string],
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
@Path("rest/messprogrammkategorie")
public class MessprogrammKategorieService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    @Inject
    @AuthorizationConfig(type = AuthorizationType.HEADER)
    private Authorization authorization;

    /**
     * Get all MessprogrammKategorie objects.
     *
     * @return Response containing requested objects.
     */
    @GET
    @Path("/")
    public Response get() {
        List<MessprogrammKategorie> kategorie =
            repository.getAllPlain(MessprogrammKategorie.class);
        for (MessprogrammKategorie kat: kategorie) {
            // TODO Do not iterate all the objects if its not necessary
            kat.setReadonly(true);
                // !authorization.isAuthorized(
                //     kat,
                //     RequestMethod.POST,
                //     MessprogrammKategorie.class));
        }
        return new Response(true, StatusCodes.OK, kategorie, kategorie.size());
    }

    /**
     * Get a single object by id.
     * <p>
     * The id is appended to the URL as a path parameter.
     * <p>
     * Example: http://example.com/messprogrammkategorie/{id}
     *
     * @return Response object containing a single object.
     */
    @GET
    @Path("/{id}")
    public Response getById(
        @PathParam("id") String id
    ) {
        MessprogrammKategorie mpk = repository.getByIdPlain(
            MessprogrammKategorie.class, Integer.valueOf(id));
        mpk.setReadonly(
            !authorization.isAuthorized(
                mpk,
                RequestMethod.POST,
                MessprogrammKategorie.class
            )
        );
        return new Response(true, StatusCodes.OK, mpk);
    }

    @POST
    @Path("/")
    public Response create(
        MessprogrammKategorie kategorie
    ) {
        if (!authorization.isAuthorized(
            kategorie,
            RequestMethod.POST,
            MessprogrammKategorie.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, kategorie);
        }
        QueryBuilder<MessprogrammKategorie> builder =
            repository.queryBuilder(MessprogrammKategorie.class);
        builder.and("code", kategorie.getCode());
        builder.and("netzbetreiberId", kategorie.getNetzbetreiberId());
        List<MessprogrammKategorie> kategorien =
            repository.filterPlain(builder.getQuery());
        if (kategorien.isEmpty()) {
            return repository.create(kategorie);
        }
        return new Response(false, StatusCodes.IMP_DUPLICATE, null);
    }

    @PUT
    @Path("/{id}")
    public Response update(
        @PathParam("id") String id,
        MessprogrammKategorie kategorie
    ) {
        if (!authorization.isAuthorized(
            kategorie,
            RequestMethod.PUT,
            MessprogrammKategorie.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, kategorie);
        }
        QueryBuilder<MessprogrammKategorie> builder =
            repository.queryBuilder(MessprogrammKategorie.class);
        builder.and("code", kategorie.getCode());
        builder.and("netzbetreiberId", kategorie.getNetzbetreiberId());
        List<MessprogrammKategorie> kategorien =
            repository.filterPlain(builder.getQuery());
        if (!kategorien.isEmpty()
            && !kategorien.get(0).getId().equals(kategorie.getId())
        ) {
            return new Response(false, StatusCodes.IMP_DUPLICATE, null);
        }
        return repository.update(kategorie);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(
        @PathParam("id") String id
    ) {
        MessprogrammKategorie kategorie = repository.getByIdPlain(
            MessprogrammKategorie.class, Integer.valueOf(id));
        if (kategorie == null
            || !authorization.isAuthorized(
                kategorie,
                RequestMethod.DELETE,
                MessprogrammKategorie.class
            )
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }
        return repository.delete(kategorie);
    }
}
