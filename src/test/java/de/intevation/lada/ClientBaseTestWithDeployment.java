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

/**
 * Base class for Lada server tests with test methods executed
 * at client side.
 */
public abstract class ClientBaseTestWithDeployment
    extends ClientBaseTest {

    /**
     * Create deployment for client side tests.
     */
    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return createFullDeployment();
    }
}
