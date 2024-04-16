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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import de.intevation.lada.util.data.KdaUtil;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.model.master.SpatRefSys;
import de.intevation.lada.validation.constraints.SupportedSpatRefSysId;
import de.intevation.lada.validation.constraints.ValidCoordinates;


/**
 * REST service for SpatRefSys objects.
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("spatrefsys")
public class SpatRefSysService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    /**
     * Expected format for the payload in POST request to recalculate().
     * @param from SpatRefSysId to transform from
     * @param to SpatRefSysId to transform to
     * @param x X coordinate
     * @param y Y coordinate
     */
    @ValidCoordinates
    public record PostData(
        @NotNull
        @SupportedSpatRefSysId
        Integer from,

        @NotNull
        @SupportedSpatRefSysId
        Integer to,

        @NotBlank
        String x,

        @NotBlank
        String y
    ) { }

    /**
     * Get all SpatRefSys objects.
     * @return all SpatRefSys objects.
     */
    @GET
    public List<SpatRefSys> get() {
        return repository.getAll(SpatRefSys.class);
    }

    /**
     * Get a single SpatRefSys object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return a single SpatRefSys.
     */
    @GET
    @Path("{id}")
    public SpatRefSys getById(
        @PathParam("id") Integer id
    ) {
        return repository.getById(SpatRefSys.class, id);
    }

    @POST
    public KdaUtil.Result recalculate(
        @Valid PostData object
    ) {
        return new KdaUtil().transform(
            KdaUtil.KDAS.get(object.from),
            KdaUtil.KDAS.get(object.to),
            object.x,
            object.y);
    }
}
