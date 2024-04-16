/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import de.intevation.lada.i18n.I18n;


/**
 * REST service returning the server version.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("version")
public class VersionService extends LadaService {

    @Inject
    I18n i18n;

    /**
     * Get server Version.
     * <p>
     * Example: http://example.com/version
     *
     * @return version.
     */
    @GET
    public String get() {
        return i18n.getString("version");
    }
}
