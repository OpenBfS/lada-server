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
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

import de.intevation.lada.i18n.I18n;
import de.intevation.lada.lock.TimestampLocker;
import de.intevation.lada.model.lada.BelongsToMeasm;
import de.intevation.lada.model.lada.MeasVal;
import de.intevation.lada.model.lada.MeasVal_;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.master.EnvMedium;
import de.intevation.lada.util.data.MesswertNormalizer;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;

/**
 * REST service for MeasVal objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "measval")
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
    private TimestampLocker<BelongsToMeasm> lock;

    @Inject
    private MesswertNormalizer messwertNormalizer;

    @Inject
    private I18n i18n;

    /**
     * Get MeasVal objects.
     *
     * @param measmId The requested objects have to be filtered
     * using an URL parameter named measmId.
     *
     * @return Filtered Messwert objects.
     */
    @GET
    @SuppressWarnings("unchecked")
    public List<MeasVal> get(
        @QueryParam("measmId") @NotNull Integer measmId
    ) {
        Measm messung = repository.getById(Measm.class, measmId);
        authorization.authorize(messung, RequestMethod.GET);

        QueryBuilder<MeasVal> builder = repository
            .queryBuilder(MeasVal.class)
            .and(MeasVal_.measmId, measmId);
        return repository.filter(builder.getQuery());
    }

    /**
     * Get a MeasVal object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return a single MeasVal.
     */
    @GET
    @Path("{id}")
    public MeasVal getById(
        @PathParam("id") Integer id
    ) {
        return authorization.authorize(
            repository.getById(MeasVal.class, id),
            RequestMethod.GET);
    }

    /**
     * Create a MeasVal object.
     *
     * @return A response object containing the created MeasVal.
     * @throws BadRequestException if any constraint violations are detected.
     */
    @POST
    public MeasVal create(
        @Valid MeasVal messwert
    ) throws BadRequestException {
        return repository.create(messwert);
    }

    /**
     * Update an existing MeasVal object.
     *
     * @return the updated MeasVal object.
     * @throws BadRequestException if any constraint violations are detected.
     */
    @PUT
    @Path("{id}")
    public MeasVal update(
        @PathParam("id") Integer id,
        @Valid MeasVal messwert
    ) throws BadRequestException {
        lock.isLocked(messwert);

        return repository.update(messwert);
    }

    /**
     * Normalise all MeasVal objects connected to the given Messung.
     * @param measmId The measm ID needs to be given as URL parameter 'measmId'.
     * @return the updated MeasVal objects.
     */
    @PUT
    @Path("normalize")
    public List<MeasVal> normalize(
        @QueryParam("measmId") @NotNull Integer measmId
    ) {
        //Load messung, probe and umwelt to get MessEinheit to convert to
        Measm messung = repository.getById(Measm.class, measmId);
        authorization.authorize(messung, RequestMethod.PUT);

        Sample probe = repository.getById(Sample.class, messung.getSampleId());
        if (probe.getEnvMediumId() == null) {
            throw new ClientErrorException(jakarta.ws.rs.core.Response
                .status(Status.CONFLICT)
                .entity(i18n.getString("op_not_possible")).build());
        }
        EnvMedium umwelt = repository.getById(
            EnvMedium.class, probe.getEnvMediumId());
        //Get all Messwert objects to convert
        QueryBuilder<MeasVal> messwertBuilder =
            repository.queryBuilder(MeasVal.class);
        messwertBuilder.and(MeasVal_.measmId, measmId);
        List<MeasVal> messwerte = messwertNormalizer.normalizeMesswerte(
            repository.filter(messwertBuilder.getQuery()),
            umwelt.getId());

        for (MeasVal messwert: messwerte) {
            authorization.authorize(messwert, RequestMethod.PUT);
            lock.isLocked(messwert);
            repository.update(messwert);
        }
        return messwerte;
    }

    /**
     * Delete an existing MeasVal object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     */
    @DELETE
    @Path("{id}")
    public void delete(
        @PathParam("id") Integer id
    ) {
        MeasVal messwertObj = repository.getById(MeasVal.class, id);
        authorization.authorize(messwertObj, RequestMethod.DELETE);
        lock.isLocked(messwertObj);
        repository.delete(messwertObj);
    }
}
