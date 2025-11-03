/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import de.intevation.lada.model.lada.MpgMmtMp;
import de.intevation.lada.model.lada.MpgMmtMp_;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.rest.RequestMethod;


/**
 * REST service for MpgMmtMp objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "mpgmmtmp")
public class MpgMmtMpService
    extends LadaIntegerIdEntityEditingService<MpgMmtMp> {

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
        builder.and(MpgMmtMp_.mpgId, mpgId);
        return repository.filter(builder.getQuery());
    }

    /**
     * Get a MpgMmtMp object by id.
     *
     * @return a single MpgMmtMp.
     */
    @GET
    @Path("{id}")
    public MpgMmtMp getById() {
        return repository.getById(MpgMmtMp.class, id);
    }

    /**
     * Delete an existing MessprogrammMmt object by id.
     */
    @DELETE
    @Path("{id}")
    public void delete() {
        MpgMmtMp messprogrammmmtObj = repository.getById(
            MpgMmtMp.class, id);
        authorization.authorize(messprogrammmmtObj, RequestMethod.DELETE);
        repository.delete(messprogrammmmtObj);
    }
}
