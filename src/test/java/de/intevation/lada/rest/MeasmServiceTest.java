/* Copyright (C) 2026 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import org.jboss.arquillian.container.test.api.BeforeDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.intevation.lada.ClientBaseTest;
import de.intevation.lada.test.ServiceTest;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.UriBuilder;

/**
 * Client mode test for running the instrumented methods in
 * {@link MeasmTestService}.
 */
@RunWith(Arquillian.class)
public class MeasmServiceTest extends ClientBaseTest {

    private static class MeasmTest extends ServiceTest {

        private static final String TEST_URL = UriBuilder
            .fromResource(MeasmTestService.class).build().toString();

        private JsonObject measm;

        @Override
        public void init(WebTarget t) {
            super.init(t);
            this.measm = readJsonResource("/datasets/messung.json");
        }

        void execute() {
            create(TEST_URL, this.measm);
        }
    }

    public MeasmServiceTest() {
        testDatasetName = "datasets/dbUnit_lada.xml";
    }

    @BeforeDeployment
    public static WebArchive adaptDeployment(WebArchive archive) {
        return archive.addClasses(MeasmTestService.class);
    }

    @Test
    public void createMeasm() {
        MeasmTest measmTest = new MeasmTest();
        measmTest.init(this.target);
        measmTest.execute();
    }
}
