/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.util.HashSet;
import java.util.Set;

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

import de.intevation.lada.model.lada.Mpg;
import de.intevation.lada.model.lada.MpgMmtMp;
import de.intevation.lada.model.master.Measd;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.validation.Validator;

/**
 * REST service for MpgMmtMp objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("mpgmmtmp")
public class MpgMmtMpService extends LadaService {

    /**
     * The data repository granting read/write access.
     */
    @Inject
    private Repository repository;

    /**
     * The authorization module.
     */
    @Inject
    @AuthorizationConfig(type = AuthorizationType.HEADER)
    private Authorization authorization;

    @Inject
    private Validator<MpgMmtMp> validator;

    /**
     * Get MpgMmtMp objects.
     *
     * @param mpgId The requested objects will be filtered
     * using a URL parameter named mpgId.
     *
     * @return Response containing requested objects.
     */
    @GET
    public Response get(
        @QueryParam("mpgId") @NotNull Integer mpgId
    ) {
        QueryBuilder<MpgMmtMp> builder =
            repository.queryBuilder(MpgMmtMp.class);
        builder.and("mpgId", mpgId);
        return authorization.filter(
            repository.filter(builder.getQuery()),
            MpgMmtMp.class);
    }

    /**
     * Get a MpgMmtMp object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object containing a single MpgMmtMp.
     */
    @GET
    @Path("{id}")
    public Response getById(
        @PathParam("id") Integer id
    ) {
        return authorization.filter(
            repository.getById(MpgMmtMp.class, id),
            MpgMmtMp.class);
    }

    /**
     * Create a MpgMmtMp object.
     * @return A response object containing the created MpgMmtMp.
     */
    @POST
    public Response create(
        @Valid MpgMmtMp messprogrammmmt
    ) {
        authorization.authorize(
                messprogrammmmt,
                RequestMethod.POST,
                MpgMmtMp.class);
        validator.validate(messprogrammmmt);
        setMessgroesseObjects(messprogrammmmt);
        return authorization.filter(
            repository.create(messprogrammmmt),
            MpgMmtMp.class);
    }

    /**
     * Update an existing MpgMmtMp object.
     *
     * @return Response object containing the updated MpgMmtMp object.
     */
    @PUT
    @Path("{id}")
    public Response update(
        @PathParam("id") Integer id,
        @Valid MpgMmtMp messprogrammmmt
    ) {
        authorization.authorize(
                messprogrammmmt,
                RequestMethod.PUT,
                MpgMmtMp.class);

        validator.validate(messprogrammmmt);
        setMessgroesseObjects(messprogrammmmt);

        return authorization.filter(
            repository.update(messprogrammmmt),
            MpgMmtMp.class);
    }

    /**
     * Delete an existing MessprogrammMmt object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object.
     */
    @DELETE
    @Path("{id}")
    public Response delete(
        @PathParam("id") Integer id
    ) {
        MpgMmtMp messprogrammmmtObj = repository.getByIdPlain(
            MpgMmtMp.class, id);
        authorization.authorize(
                messprogrammmmtObj,
                RequestMethod.DELETE,
                Mpg.class);
        /* Delete the messprogrammmmt object*/
        return repository.delete(messprogrammmmtObj);
    }

    /**
     * Initialize referenced objects from given IDs.
     */
    private void setMessgroesseObjects(MpgMmtMp mm) {
        Set<Measd> mos = new HashSet<>();
        for (Integer mId: mm.getMeasds()) {
            Measd m = repository.getByIdPlain(Measd.class, mId);
            if (m != null) {
                mos.add(m);
            }
        }
        mm.setMeasdObjects(mos);
    }
}
