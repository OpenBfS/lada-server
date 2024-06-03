/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.rest;

import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.ApplicationPath;


/**
 * Activates JAX-RS and defines basic properties of the application.
 */
@ApplicationPath("/")
public class LadaApplication extends Application {
}
