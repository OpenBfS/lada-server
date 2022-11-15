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

import de.intevation.lada.model.stammdaten.DatensatzErzeuger;
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
 *  "success": [boolean];
 *  "message": [string],
 *  "data":[{
 *      "id": [number],
 *      "bezeichnung": [string],
 *      "daErzeugerId": [string],
 *      "letzteAenderung": [timestamp],
 *      "mstId": [string],
 *      "netzbetreiberId": [string]
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
@Path("rest/datensatzerzeuger")
public class DatensatzErzeugerService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    @Inject
    @AuthorizationConfig(type = AuthorizationType.HEADER)
    private Authorization authorization;

    /**
     * Get all DatensatzErzeuger objects.
     *
     * @return Response containing requested objects.
     */
    @GET
    @Path("/")
    public Response get() {
        List<DatensatzErzeuger> erzeuger =
            repository.getAllPlain(DatensatzErzeuger.class);

        for (DatensatzErzeuger erz : erzeuger) {
            // TODO Do not iterate all the objects if its not necessary
            erz.setReadonly(true);
                // !authorization.isAuthorized(
                //     erz,
                //     RequestMethod.POST,
                //     DatensatzErzeuger.class));
        }
        return new Response(true, StatusCodes.OK, erzeuger, erzeuger.size());
    }

    /**
     * Get a single object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object
     */
    @GET
    @Path("/{id}")
    public Response getById(
        @PathParam("id") Integer id
    ) {
        DatensatzErzeuger erzeuger = repository.getByIdPlain(
            DatensatzErzeuger.class, id);
        erzeuger.setReadonly(
            !authorization.isAuthorized(
                erzeuger,
                RequestMethod.POST,
                DatensatzErzeuger.class
            )
        );
        return new Response(true, StatusCodes.OK, erzeuger);
    }

    @POST
    @Path("/")
    public Response create(
        DatensatzErzeuger datensatzerzeuger
    ) {
        if (!authorization.isAuthorized(
            datensatzerzeuger,
            RequestMethod.POST,
            DatensatzErzeuger.class)
        ) {
            return new Response(
                false, StatusCodes.NOT_ALLOWED, datensatzerzeuger);
        }
        QueryBuilder<DatensatzErzeuger> builder =
            repository.queryBuilder(DatensatzErzeuger.class);
        builder.and(
            "extId", datensatzerzeuger.getExtId());
        builder.and("networkId", datensatzerzeuger.getNetworkId());
        builder.and("measFacilId", datensatzerzeuger.getMeasFacilId());
        List<DatensatzErzeuger> erzeuger =
            repository.filterPlain(builder.getQuery());
        if (erzeuger.isEmpty()) {
            return repository.create(datensatzerzeuger);
        }
        return new Response(false, StatusCodes.IMP_DUPLICATE, null);
    }

    @PUT
    @Path("/{id}")
    public Response update(
        @PathParam("id") Integer id,
        DatensatzErzeuger datensatzerzeuger
    ) {
        if (!authorization.isAuthorized(
            datensatzerzeuger,
            RequestMethod.PUT,
            DatensatzErzeuger.class)
        ) {
            return new Response(
                false, StatusCodes.NOT_ALLOWED, datensatzerzeuger);
        }
        QueryBuilder<DatensatzErzeuger> builder =
            repository.queryBuilder(DatensatzErzeuger.class);
        builder.and(
            "extId", datensatzerzeuger.getExtId());
        builder.and("networkId", datensatzerzeuger.getNetworkId());
        builder.and("measFacilId", datensatzerzeuger.getMeasFacilId());
        List<DatensatzErzeuger> erzeuger =
            repository.filterPlain(builder.getQuery());
        if (!erzeuger.isEmpty()
            && !erzeuger.get(0).getId().equals(datensatzerzeuger.getId())
        ) {
            return new Response(false, StatusCodes.IMP_DUPLICATE, null);
        }
        return repository.update(datensatzerzeuger);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(
        @PathParam("id") Integer id
    ) {
        DatensatzErzeuger datensatzerzeuger = repository.getByIdPlain(
            DatensatzErzeuger.class, id);
        if (datensatzerzeuger == null
            || !authorization.isAuthorized(
                datensatzerzeuger,
                RequestMethod.DELETE,
                DatensatzErzeuger.class
            )
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }
        return repository.delete(datensatzerzeuger);
    }
}
