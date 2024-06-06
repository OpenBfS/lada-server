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

import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.model.master.MpgCateg;

/**
 * REST service for MpgCateg objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "mpgcateg")
public class MpgCategService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    @Inject
    @AuthorizationConfig(type = AuthorizationType.HEADER)
    private Authorization authorization;

    /**
     * Get all MpgCateg objects.
     *
     * @return requested objects.
     */
    @GET
    public List<MpgCateg> get() {
        return authorization.filter(
            repository.getAll(MpgCateg.class), MpgCateg.class);
    }

    /**
     * Get a single object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return a single object.
     */
    @GET
    @Path("{id}")
    public MpgCateg getById(
        @PathParam("id") Integer id
    ) {
        return authorization.filter(
            repository.getById(MpgCateg.class, id), MpgCateg.class);
    }

    /**
     * Create a MpgCateg
     * @param kategorie Object to create
     * @return Created object
     * @throws BadRequestException if any constraint violations are detected.
     */
    @POST
    public MpgCateg create(
        @Valid MpgCateg kategorie
    ) throws BadRequestException {
        authorization.authorize(
            kategorie,
            RequestMethod.POST,
            MpgCateg.class);
        return repository.create(kategorie);
    }


    /**
     * Update the given MpgCateg
     * @param id Id
     * @param kategorie Updated object
     * @return Updated object
     * @throws BadRequestException if any constraint violations are detected.
     */
    @PUT
    @Path("{id}")
    public MpgCateg update(
        @PathParam("id") Integer id,
        @Valid MpgCateg kategorie
    ) throws BadRequestException {
        authorization.authorize(
            kategorie,
            RequestMethod.PUT,
            MpgCateg.class);
        return repository.update(kategorie);
    }

    @DELETE
    @Path("{id}")
    public void delete(
        @PathParam("id") Integer id
    ) {
        MpgCateg kategorie = repository.getById(
            MpgCateg.class, id);
        authorization.authorize(
                kategorie,
                RequestMethod.DELETE,
                MpgCateg.class);
        repository.delete(kategorie);
    }
}
