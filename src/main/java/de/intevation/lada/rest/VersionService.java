/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.util.ResourceBundle;
import java.util.MissingResourceException;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.jboss.logging.Logger;

import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.Response;

/**
 * REST service returning the server version.
 * <p>
 * The services produce data in the application/json media type.
 * A typical response holds information about the action performed and the data.
 * <pre>
 * <code>
 * {
 *  "success": [boolean];
 *  "message": [string],
 *  "data":{
 *      version: [string]
 *  },
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
@Path("version")
public class VersionService extends LadaService {

    @Inject
    private Logger logger;

    /**
     * Get server Version.
     * <p>
     * Example: http://example.com/version
     *
     * @return Response object containing version.
     */
    @GET
    public Response get() {
        String version = "unknown";
        try {
            version = ResourceBundle.getBundle("lada").getString("version");
            return new Response(true, StatusCodes.OK, version);
        } catch (MissingResourceException mre) {
            logger.error(mre);
        }
        return new Response(false, StatusCodes.OK, version);
    }
}
