/* Copyright (C) 2026 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada;

import java.io.File;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

import de.intevation.lada.util.auth.Authentication;
import de.intevation.lada.util.auth.TestAuthentication;

/**
 * Base class for Lada server tests with test methods executed
 * in container.
 */
public abstract class ContainerBaseTest extends BaseTest {

    /**
     * Create deployment for tests run in container.
     *
     * Replace {@link jakarta.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism}
     * implementation to enable HTTP requests actually running the tests.
     * Request authentication is not needed, because no actual client requests
     * are tested.
     *
     * Also adds additional resources and dependencies needed for
     * in-container tests.
     */
    @Deployment
    public static WebArchive createDeployment() {
        WebArchive archive = createFullDeployment()
            .deleteClass(Authentication.class)
            .addClass(TestAuthentication.class)
            .addAsResource(DATASETS_DIR, DATASETS_DIR);
        //Add additional test dependencies
        addWithDependencies("org.postgresql:postgresql", archive);
        addWithDependencies("net.postgis:postgis-jdbc", archive);
        addWithDependencies("org.dbunit:dbunit", archive);
        addWithDependencies(
            "org.jboss.arquillian.extension:arquillian-transaction-api",
            archive);
        addWithDependencies(
            "org.jboss.arquillian.extension:arquillian-transaction-jta",
            archive);
        addWithDependencies("org.eclipse.parsson:parsson", archive);
        return archive;
    }

    /**
     * Add a dependency to the given webarchive.
     *
     * @param coordinate
     * @param archive
     */
    private static void addWithDependencies(
        String coordinate, WebArchive archive
    ) {
        File[] files = Maven.resolver().loadPomFromFile("pom.xml")
            .resolve(coordinate).withTransitivity().asFile();
        for (File f : files) {
            archive.addAsLibrary(f);
        }
    }
}
