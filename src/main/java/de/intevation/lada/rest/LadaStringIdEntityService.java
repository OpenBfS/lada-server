/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import jakarta.ws.rs.PathParam;


/**
 * Base class for services with String-typed resource identifier
 * provided in URL paths.
 */
abstract class LadaStringIdEntityService extends LadaEntityService<String> {

    @PathParam("id")
    protected String id;

    @Override
    String getPathId() {
        return this.id;
    }
}
