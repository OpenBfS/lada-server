/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest.stamm;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import de.intevation.lada.model.stammdaten.MassEinheitUmrechnung;
import de.intevation.lada.model.stammdaten.MessEinheit;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.rest.LadaService;

/**
 * REST service for MessEinheit objects.
 * <p>
 * The services produce data in the application/json media type.
 * A typical response holds information about the action performed and the data.
 * <pre>
 * <code>
 * {
 *  "success": [boolean];
 *  "message": [string],
 *  "data":[{
 *      "id": [number],
 *      "beschreibung": [string],
 *      "einheit": [string],
 *      "eudfMesseinheitId": [string],
 *      "umrechnungsFaktorEudf": [number]
 *  }],
 *  "errors": [object],
 *  "warnings": [object],
 *  "readonly": [boolean],
 *  "totalCount": [number]
 * }
 * </code>
 * </pre>
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("rest/messeinheit")
public class MesseinheitService extends LadaService {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    /**
     * Get MessEinheit objects.
     *
     * The requested Objects can be filtered using two URL parameters:
     * @param mehId
     * @param secMehId
     * If these parameters are used, the filter only returns records that are
     * convertable into one of these units.
     * Records, convertable into the primary messeinheit (mehId) will have the
     * attribute 'primary' set to true.
     * Records convertable into the secondary messeinheit (secMehId) will have
     * the attribute 'primary' set to false.
     *
     * @return Response containing requested objects.
     */
    @GET
    @Path("/")
    public Response get(
        @QueryParam("mehId") Integer mehId,
        @QueryParam("secMehId") Integer secMehId
    ) {
        if (mehId == null) {
            return repository.getAll(MessEinheit.class);
        }

        MessEinheit meh = repository.getByIdPlain(MessEinheit.class, mehId);
        MessEinheit secMeh = null;
        if (secMehId != null) {
            secMeh = repository.getByIdPlain(MessEinheit.class, secMehId);
        }
        List<MessEinheit> einheits =
            new ArrayList<MessEinheit>(
                meh.getMassEinheitUmrechnungZus().size());
        meh.setPrimary(true);
        einheits.add(meh);
        if (secMeh != null) {
            secMeh.setPrimary(false);
            einheits.add(secMeh);
        }
        for (MassEinheitUmrechnung umrechnung
            : meh.getMassEinheitUmrechnungZus()
        ) {
            MessEinheit einheit = umrechnung.getMehVon();
            einheit.setPrimary(true);
            einheits.add(einheit);
        }
        if (secMeh != null) {
            secMeh.getMassEinheitUmrechnungZus().forEach(umrechnung -> {
                MessEinheit einheit = umrechnung.getMehVon();
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
     * Get a single MessEinheit object by id.
     * <p>
     * The id is appended to the URL as a path parameter.
     * <p>
     * Example: http://example.com/messeinheit/{id}
     *
     * @return Response object containing a single MessEinheit.
     */
    @GET
    @Path("/{id}")
    public Response getById(
        @PathParam("id") String id
    ) {
        return repository.getById(MessEinheit.class, Integer.valueOf(id));
    }
}
