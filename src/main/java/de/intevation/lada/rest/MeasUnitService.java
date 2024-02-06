/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.model.master.MeasUnit;
import de.intevation.lada.model.master.UnitConvers;

/**
 * REST service for MeasUnit objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("measunit")
public class MeasUnitService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    /**
     * Get MeasUnit objects.
     *
     * The requested Objects can be filtered using two URL parameters:
     * @param measUnitId
     * @param secMeasUnitId
     * If these parameters are used, the filter only returns records that are
     * convertable into one of these units.
     * Records, convertable into the primary MeasUnit (measUnitId) will have the
     * attribute 'primary' set to true.
     * Records convertable into the secondary MeasUnit (secMeasUnitId) will have
     * the attribute 'primary' set to false.
     *
     * @return Response containing requested objects.
     */
    @GET
    public Response get(
        @QueryParam("measUnitId") Integer measUnitId,
        @QueryParam("secMeasUnitId") Integer secMeasUnitId
    ) {
        if (measUnitId == null) {
            return repository.getAll(MeasUnit.class);
        }

        MeasUnit meh = repository.getByIdPlain(MeasUnit.class, measUnitId);
        MeasUnit secMeh = null;
        if (secMeasUnitId != null) {
            secMeh = repository.getByIdPlain(MeasUnit.class, secMeasUnitId);
        }
        List<MeasUnit> einheits =
            new ArrayList<MeasUnit>(
                meh.getUnitConversTo().size());
        meh.setPrimary(true);
        einheits.add(meh);
        if (secMeh != null) {
            secMeh.setPrimary(false);
            einheits.add(secMeh);
        }
        for (UnitConvers umrechnung
            : meh.getUnitConversTo()
        ) {
            MeasUnit einheit = umrechnung.getFromUnit();
            einheit.setPrimary(true);
            einheits.add(einheit);
        }
        if (secMeh != null) {
            secMeh.getUnitConversTo().forEach(umrechnung -> {
                MeasUnit einheit = umrechnung.getFromUnit();
                //If unit was not already added
                if (!einheits.contains(einheit)) {
                    //Add as secondary unit
                    einheit.setPrimary(false);
                    einheits.add(einheit);
                }
            });
        }
        return new Response(true, StatusCodes.OK, einheits);
    }

    /**
     * Get a single MeasUnit object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object containing a single MeasUnit.
     */
    @GET
    @Path("{id}")
    public Response getById(
        @PathParam("id") Integer id
    ) {
        return repository.getById(MeasUnit.class, id);
    }
}
