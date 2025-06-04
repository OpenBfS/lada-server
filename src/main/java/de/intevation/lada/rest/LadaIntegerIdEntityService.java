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
 * Base class for services with Integer-typed resource identifier
 * provided in URL paths.
 */
abstract class LadaIntegerIdEntityService extends LadaEntityService<Integer> {

    @PathParam("id")
    protected Integer id;

    @Override
    Integer getPathId() {
        return this.id;
    }
}
