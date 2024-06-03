/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.util.HashSet;
import java.util.List;
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

/**
 * REST service for MpgMmtMp objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "mpgmmtmp")
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

    /**
     * Get MpgMmtMp objects.
     *
     * @param mpgId The requested objects will be filtered
     * using a URL parameter named mpgId.
     *
     * @return requested objects.
     */
    @GET
    public List<MpgMmtMp> get(
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
     * @return a single MpgMmtMp.
     */
    @GET
    @Path("{id}")
    public MpgMmtMp getById(
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
    public MpgMmtMp create(
        @Valid MpgMmtMp messprogrammmmt
    ) {
        authorization.authorize(
                messprogrammmmt,
                RequestMethod.POST,
                MpgMmtMp.class);
        setMessgroesseObjects(messprogrammmmt);
        return authorization.filter(
            repository.create(messprogrammmmt),
            MpgMmtMp.class);
    }

    /**
     * Update an existing MpgMmtMp object.
     *
     * @return the updated MpgMmtMp object.
     */
    @PUT
    @Path("{id}")
    public MpgMmtMp update(
        @PathParam("id") Integer id,
        @Valid MpgMmtMp messprogrammmmt
    ) {
        authorization.authorize(
                messprogrammmmt,
                RequestMethod.PUT,
                MpgMmtMp.class);

        setMessgroesseObjects(messprogrammmmt);

        return authorization.filter(
            repository.update(messprogrammmmt),
            MpgMmtMp.class);
    }

    /**
     * Delete an existing MessprogrammMmt object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     */
    @DELETE
    @Path("{id}")
    public void delete(
        @PathParam("id") Integer id
    ) {
        MpgMmtMp messprogrammmmtObj = repository.getById(
            MpgMmtMp.class, id);
        authorization.authorize(
                messprogrammmmtObj,
                RequestMethod.DELETE,
                Mpg.class);
        repository.delete(messprogrammmmtObj);
    }

    /**
     * Initialize referenced objects from given IDs.
     */
    private void setMessgroesseObjects(MpgMmtMp mm) {
        Set<Measd> mos = new HashSet<>();
        for (Integer mId: mm.getMeasds()) {
            Measd m = repository.getById(Measd.class, mId);
            if (m != null) {
                mos.add(m);
            }
        }
        mm.setMeasdObjects(mos);
    }
}
