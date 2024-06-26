/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.model.master.DatasetCreator;

/**
 * REST service for DatasetCreator objects.
 * <p>
 * The services produce data in the application/json media type.
 * A typical response holds information about the action performed and the data.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "datasetcreator")
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
     * @return requested objects.
     */
    @GET
    public List<DatasetCreator> get() {
        return authorization.filter(
            repository.getAll(DatasetCreator.class), DatasetCreator.class);
    }

    /**
     * Get a single object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return DatasetCreator object
     */
    @GET
    @Path("{id}")
    public DatasetCreator getById(
        @PathParam("id") Integer id
    ) {
        return authorization.filter(repository.getById(
                DatasetCreator.class, id), DatasetCreator.class);
    }

    @POST
    public DatasetCreator create(
        @Valid DatasetCreator datensatzerzeuger
    ) {
        authorization.authorize(
            datensatzerzeuger,
            RequestMethod.POST,
            DatasetCreator.class);
        return repository.create(datensatzerzeuger);
    }

    @PUT
    @Path("{id}")
    public DatasetCreator update(
        @PathParam("id") Integer id,
        @Valid DatasetCreator datensatzerzeuger
    ) {
        authorization.authorize(
            datensatzerzeuger,
            RequestMethod.PUT,
            DatasetCreator.class);
        return repository.update(datensatzerzeuger);
    }

    @DELETE
    @Path("{id}")
    public void delete(
        @PathParam("id") Integer id
    ) {
        DatasetCreator datensatzerzeuger = repository.getById(
            DatasetCreator.class, id);
        authorization.authorize(
            datensatzerzeuger,
            RequestMethod.DELETE,
            DatasetCreator.class);
        repository.delete(datensatzerzeuger);
    }
}
