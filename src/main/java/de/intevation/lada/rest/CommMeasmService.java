/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.util.Set;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import de.intevation.lada.model.lada.CommMeasm;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.util.rest.RequestMethod;


/**
 * REST service for CommMeasm objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "commmeasm")
public class CommMeasmService extends LadaIntegerIdEntityService {

    /**
     * Get CommMeasm objects.
     *
     * @param measmId The requested objects have to be filtered
     * using an URL parameter named measmId.
     *
     * @return filtered CommMeasm objects.
     */
    @GET
    public Set<CommMeasm> get(
        @QueryParam("measmId") @NotNull Integer measmId
    ) {
        Measm messung = repository.getById(Measm.class, measmId);
        authorization.authorize(messung, RequestMethod.GET);

        return messung.getCommMeasms();
    }

    /**
     * Get a single CommMeasm object by id.
     *
     * @return a single CommMeasm.
     */
    @GET
    @Path("{id}")
    public CommMeasm getById() {
        return authorization.authorize(
            repository.getById(CommMeasm.class, id),
            RequestMethod.GET);
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
        @Valid CommMeasm kommentar
    ) throws BadRequestException {
        return repository.update(kommentar);
    }

    /**
     * Delete an existing CommMeasm object by id.
     */
    @DELETE
    @Path("{id}")
    public void delete() {
        CommMeasm kommentarObj = repository.getById(CommMeasm.class, id);
        authorization.authorize(kommentarObj, RequestMethod.DELETE);
        kommentarObj.getMeasm().getCommMeasms().remove(kommentarObj);
        repository.delete(kommentarObj);
    }
}
