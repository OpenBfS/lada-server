/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.util.List;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.model.master.DatasetCreator;

/**
 * REST service for DatasetCreator objects.
 * <p>
 * The services produce data in the application/json media type.
 * A typical response holds information about the action performed and the data.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("datasetcreator")
public class DatasetCreatorService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    @Inject
    @AuthorizationConfig(type = AuthorizationType.HEADER)
    private Authorization authorization;

    /**
     * Get all DatasetCreator objects.
     *
     * @return Response containing requested objects.
     */
    @GET
    public Response get() {
        List<DatasetCreator> datasetCreators =
            repository.getAllPlain(DatasetCreator.class);

        for (DatasetCreator erz : datasetCreators) {
            // TODO Do not iterate all the objects if its not necessary
            erz.setReadonly(true);
                // !authorization.isAuthorized(
                //     erz,
                //     RequestMethod.POST,
                //     DatensatzErzeuger.class));
        }
        return new Response(true, StatusCodes.OK,
            datasetCreators, datasetCreators.size());
    }

    /**
     * Get a single object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object
     */
    @GET
    @Path("{id}")
    public Response getById(
        @PathParam("id") Integer id
    ) {
        DatasetCreator erzeuger = repository.getByIdPlain(
            DatasetCreator.class, id);
        erzeuger.setReadonly(
            !authorization.isAuthorized(
                erzeuger,
                RequestMethod.POST,
                DatasetCreator.class
            )
        );
        return new Response(true, StatusCodes.OK, erzeuger);
    }

    @POST
    public Response create(
        @Valid DatasetCreator datensatzerzeuger
    ) {
        if (!authorization.isAuthorized(
            datensatzerzeuger,
            RequestMethod.POST,
            DatasetCreator.class)
        ) {
            return new Response(
                false, StatusCodes.NOT_ALLOWED, datensatzerzeuger);
        }
        QueryBuilder<DatasetCreator> builder =
            repository.queryBuilder(DatasetCreator.class);
        builder.and(
            "extId", datensatzerzeuger.getExtId());
        builder.and("networkId", datensatzerzeuger.getNetworkId());
        builder.and("measFacilId", datensatzerzeuger.getMeasFacilId());
        List<DatasetCreator> erzeuger =
            repository.filterPlain(builder.getQuery());
        if (erzeuger.isEmpty()) {
            return repository.create(datensatzerzeuger);
        }
        return new Response(false, StatusCodes.IMP_DUPLICATE, null);
    }

    @PUT
    @Path("{id}")
    public Response update(
        @PathParam("id") Integer id,
        @Valid DatasetCreator datensatzerzeuger
    ) {
        if (!authorization.isAuthorized(
            datensatzerzeuger,
            RequestMethod.PUT,
            DatasetCreator.class)
        ) {
            return new Response(
                false, StatusCodes.NOT_ALLOWED, datensatzerzeuger);
        }
        QueryBuilder<DatasetCreator> builder =
            repository.queryBuilder(DatasetCreator.class);
        builder.and(
            "extId", datensatzerzeuger.getExtId());
        builder.and("networkId", datensatzerzeuger.getNetworkId());
        builder.and("measFacilId", datensatzerzeuger.getMeasFacilId());
        List<DatasetCreator> erzeuger =
            repository.filterPlain(builder.getQuery());
        if (!erzeuger.isEmpty()
            && !erzeuger.get(0).getId().equals(datensatzerzeuger.getId())
        ) {
            return new Response(false, StatusCodes.IMP_DUPLICATE, null);
        }
        return repository.update(datensatzerzeuger);
    }

    @DELETE
    @Path("{id}")
    public Response delete(
        @PathParam("id") Integer id
    ) {
        DatasetCreator datensatzerzeuger = repository.getByIdPlain(
            DatasetCreator.class, id);
        if (datensatzerzeuger == null
            || !authorization.isAuthorized(
                datensatzerzeuger,
                RequestMethod.DELETE,
                DatasetCreator.class
            )
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }
        return repository.delete(datensatzerzeuger);
    }
}
