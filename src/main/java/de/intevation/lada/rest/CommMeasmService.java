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
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

import de.intevation.lada.model.lada.CommMeasm;
import de.intevation.lada.model.lada.CommMeasm_;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;

/**
 * REST service for CommMeasm objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "commmeasm")
public class CommMeasmService extends LadaService {

    /**
     * The data repository granting read/write access.
     */
    @Inject
    private Repository repository;

    /**
     * Get CommMeasm objects.
     *
     * @param measmId The requested objects have to be filtered
     * using an URL parameter named measmId.
     *
     * @return filtered CommMeasm objects.
     */
    @GET
    public List<CommMeasm> get(
        @QueryParam("measmId") @NotNull Integer measmId
    ) {
        Measm messung = repository.getById(Measm.class, measmId);
        authorization.authorize(
                messung, RequestMethod.GET, Measm.class);

        QueryBuilder<CommMeasm> builder =
            repository.queryBuilder(CommMeasm.class);
        builder.and(CommMeasm_.measmId, measmId);
        return repository.filter(builder.getQuery());
    }

    /**
     * Get a single CommMeasm object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return a single CommMeasm.
     */
    @GET
    @Path("{id}")
    public CommMeasm getById(
        @PathParam("id") Integer id
    ) {
        return authorization.authorize(
            repository.getById(CommMeasm.class, id),
            RequestMethod.GET,
            CommMeasm.class);
    }

    /**
     * Create a CommMeasm object.
     * @return A response containing the created CommMeasm.
     * @throws BadRequestException if any constraint violations are detected.
     */
    @POST
    public CommMeasm create(
        @Valid CommMeasm kommentar
    ) throws BadRequestException {
        return repository.create(kommentar);
    }

    /**
     * Update an existing CommMeasm object.
     *
     * @return the updated CommMeasm object.
     * @throws BadRequestException if any constraint violations are detected.
     */
    @PUT
    @Path("{id}")
    public CommMeasm update(
        @PathParam("id") Integer id,
        @Valid CommMeasm kommentar
    ) throws BadRequestException {
        return repository.update(kommentar);
    }

    /**
     * Delete an existing CommMeasm object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     */
    @DELETE
    @Path("{id}")
    public void delete(
        @PathParam("id") Integer id
    ) {
        CommMeasm kommentarObj = repository.getById(CommMeasm.class, id);
        authorization.authorize(
            kommentarObj,
            RequestMethod.DELETE,
            CommMeasm.class);
        repository.delete(kommentarObj);
    }
}
