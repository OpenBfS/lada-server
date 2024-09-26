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
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.model.master.MunicDiv;

/**
 * REST service for MunicDiv objects.
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "municdiv")
public class MunicDivService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    /**
     * Get all MunicDiv objects.
     *
     * @return requested objects.
     */
    @GET
    public List<MunicDiv> get() {
        return repository.getAll(MunicDiv.class);
    }

    /**
     * Get a single object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return a single object.
     */
    @GET
    @Path("{id}")
    public MunicDiv getById(
        @PathParam("id") Integer id
    ) {
        return repository.getById(MunicDiv.class, id);
    }

    /**
     * Create a MunicDiv
     * @param gemUntergliederung Object to create
     * @return Created object
     * @throws BadRequestException if any constraint violations are detected.
     */
    @POST
    public MunicDiv create(
        @Valid MunicDiv gemUntergliederung
    ) throws BadRequestException {
        return repository.create(gemUntergliederung);
    }

    /**
     * Update the given MunicDiv
     * @param id Object id
     * @param gemUntergliederung Updated object
     * @return Updated object
     * @throws BadRequestException if any constraint violations are detected.
     */
    @PUT
    @Path("{id}")
    public MunicDiv update(
        @PathParam("id") Integer id,
        @Valid MunicDiv gemUntergliederung
    ) throws BadRequestException {
        return repository.update(gemUntergliederung);
    }

    @DELETE
    @Path("{id}")
    public void delete(
        @PathParam("id") Integer id
    ) {
        MunicDiv gemUntergliederung = repository.getById(
            MunicDiv.class, id);
        authorization.authorize(
            gemUntergliederung,
            RequestMethod.DELETE,
            MunicDiv.class);
        repository.delete(gemUntergliederung);
    }
}
