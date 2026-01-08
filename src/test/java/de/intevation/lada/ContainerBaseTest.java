/* Copyright (C) 2026 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import de.intevation.lada.util.auth.Authentication;

/**
 * Base class for Lada server tests with test methods executed
 * in container.
 */
public class ContainerBaseTest extends BaseTest {

    /**
     * Create deployment for tests run in container.
     *
     * Drop {@link jakarta.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism}
     * implementation from deployment to enable HTTP requests
     * actually running the tests. Authentication is not needed, because
     * no actual client requests are tested.
     */
    @Deployment
    public static WebArchive createDeployment() {
        return createFullDeployment()
            .deleteClass(Authentication.class);
    }
}
