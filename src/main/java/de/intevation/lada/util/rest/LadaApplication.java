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

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;


/**
 * Activates JAX-RS and defines basic properties of the application.
 */
@ApplicationPath("/")
@OpenAPIDefinition(info = @Info(
        title = "LADA services",
        version = "Please query version service",
        description = "REST, import and export services for IMIS LADA"))
public class LadaApplication extends Application {
}
