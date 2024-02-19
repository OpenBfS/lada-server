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
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

import de.intevation.lada.lock.LockConfig;
import de.intevation.lada.lock.LockType;
import de.intevation.lada.lock.ObjectLocker;
import de.intevation.lada.model.lada.MeasVal;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.master.EnvMedium;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.data.MesswertNormalizer;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.validation.Validator;

/**
 * REST service for MeasVal objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("measval")
public class MeasValService extends LadaService {

    /**
     * The data repository granting read/write access.
     */
    @Inject
    private Repository repository;

    /**
     * The object lock mechanism.
     */
    @Inject
    @LockConfig(type = LockType.TIMESTAMP)
    private ObjectLocker lock;

    /**
     * The authorization module.
     */
    @Inject
    @AuthorizationConfig(type = AuthorizationType.HEADER)
    private Authorization authorization;

    @Inject
    private Validator<MeasVal> validator;

    @Inject
    private MesswertNormalizer messwertNormalizer;

    /**
     * Get MeasVal objects.
     *
     * @param measmId The requested objects have to be filtered
     * using an URL parameter named measmId.
     *
     * @return Response object containing filtered Messwert objects.
     * Status-Code 699 if parameter is missing or requested objects are
     * not authorized.
     */
    @GET
    @SuppressWarnings("unchecked")
    public Response get(
        @QueryParam("measmId") @NotNull Integer measmId
    ) {
        Measm messung = repository.getByIdPlain(Measm.class, measmId);
        authorization.authorize(
                messung,
                RequestMethod.GET,
                Measm.class);

        QueryBuilder<MeasVal> builder = repository
            .queryBuilder(MeasVal.class)
            .and("measmId", measmId);
        Response r = authorization.filter(
            repository.filter(builder.getQuery()),
            MeasVal.class);
        for (MeasVal messwert: (List<MeasVal>) r.getData()) {
            validator.validate(messwert);
        }
        return r;
    }

    /**
     * Get a MeasVal object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object containing a single MeasVal.
     */
    @GET
    @Path("{id}")
    public Response getById(
        @PathParam("id") Integer id
    ) {
        Response response = repository.getById(MeasVal.class, id);
        MeasVal messwert = (MeasVal) response.getData();
        Measm messung = repository.getByIdPlain(
            Measm.class, messwert.getMeasmId());
        authorization.authorize(
            messung,
            RequestMethod.GET,
            Measm.class);
        validator.validate(messwert);
        return authorization.filter(
            response,
            MeasVal.class);
    }

    /**
     * Create a MeasVal object.
     *
     * @return A response object containing the created MeasVal.
     */
    @POST
    public Response create(
        @Valid MeasVal messwert
    ) {
        authorization.authorize(
                messwert,
                RequestMethod.POST,
                MeasVal.class);
        validator.validate(messwert);
        if (messwert.hasErrors()) {
            return new Response(false, StatusCodes.ERROR_VALIDATION, messwert);
        }

        /* Persist the new messung object*/
        return authorization.filter(
            repository.create(messwert),
            MeasVal.class);
    }

    /**
     * Update an existing MeasVal object.
     *
     * @return Response object containing the updated MeasVal object.
     */
    @PUT
    @Path("{id}")
    public Response update(
        @PathParam("id") Integer id,
        @Valid MeasVal messwert
    ) {
        authorization.authorize(
                messwert,
                RequestMethod.PUT,
                MeasVal.class);
        lock.isLocked(messwert);
        validator.validate(messwert);
        if (messwert.hasErrors()) {
            return new Response(false, StatusCodes.ERROR_VALIDATION, messwert);
        }

        Response response = repository.update(messwert);
        validator.validate(response.getData());
        return authorization.filter(
            response,
            MeasVal.class);
    }

    /**
     * Normalise all MeasVal objects connected to the given Messung.
     * @param measmId The measm ID needs to be given as URL parameter 'measmId'.
     * @return Response object containing the updated MeasVal objects.
     */
    @PUT
    @Path("normalize")
    public Response normalize(
        @QueryParam("measmId") @NotNull Integer measmId
    ) {
        //Load messung, probe and umwelt to get MessEinheit to convert to
        Measm messung = repository.getByIdPlain(Measm.class, measmId);
        authorization.authorize(
            messung,
            RequestMethod.PUT,
            Measm.class);

        Sample probe =
            repository.getByIdPlain(
                Sample.class, messung.getSampleId());
        if (probe.getEnvMediumId() == null
            || probe.getEnvMediumId().equals("")
        ) {
            return new Response(true, StatusCodes.OP_NOT_POSSIBLE, null);
        }
        EnvMedium umwelt =
            repository.getByIdPlain(
                EnvMedium.class, probe.getEnvMediumId());
        //Get all Messwert objects to convert
        QueryBuilder<MeasVal> messwertBuilder =
            repository.queryBuilder(MeasVal.class);
        messwertBuilder.and("measmId", measmId);
        List<MeasVal> messwerte = messwertNormalizer.normalizeMesswerte(
            repository.filterPlain(messwertBuilder.getQuery()),
            umwelt.getId());

        for (MeasVal messwert: messwerte) {
            authorization.authorize(
                messwert,
                RequestMethod.PUT,
                MeasVal.class);
            lock.isLocked(messwert);
            validator.validate(messwert);
            if (messwert.hasErrors()) {
                return new Response(
                    false, StatusCodes.ERROR_VALIDATION, messwert);
            }

            Response response = repository.update(messwert);
            validator.validate(response.getData());
            authorization.filter(
                response,
                MeasVal.class);
        }
        return new Response(true, StatusCodes.OK, messwerte);
    }

    /**
     * Delete an existing MeasVal object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object.
     */
    @DELETE
    @Path("{id}")
    public Response delete(
        @PathParam("id") Integer id
    ) {
        MeasVal messwertObj = repository.getByIdPlain(MeasVal.class, id);
        authorization.authorize(
            messwertObj,
            RequestMethod.DELETE,
            MeasVal.class);
        lock.isLocked(messwertObj);
        /* Delete the messwert object*/
        return repository.delete(messwertObj);
    }
}
