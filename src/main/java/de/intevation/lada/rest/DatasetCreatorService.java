/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.util.List;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

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
public class DatasetCreatorService
    extends LadaIntegerIdEntityEditingService<DatasetCreator> {

    /**
     * Get all DatasetCreator objects.
     *
     * @return requested objects.
     */
    @GET
    public List<DatasetCreator> get() {
        return repository.getAll(DatasetCreator.class);
    }

    /**
     * Get a single object by id.
     *
     * @return DatasetCreator object
     */
    @GET
    @Path("{id}")
    public DatasetCreator getById() {
        return repository.getById(DatasetCreator.class, id);
    }

    @DELETE
    @Path("{id}")
    public void delete() {
        DatasetCreator datensatzerzeuger = repository.getById(
            DatasetCreator.class, id);
        authorization.authorize(datensatzerzeuger, RequestMethod.DELETE);
        repository.delete(datensatzerzeuger);
    }
}
