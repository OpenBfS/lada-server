/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada;

import java.net.URL;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.logging.Logger;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.intevation.lada.test.land.GeolocatMpgTest;
import de.intevation.lada.test.land.KommentarMTest;
import de.intevation.lada.test.land.KommentarPTest;
import de.intevation.lada.test.land.MessprogrammTest;
import de.intevation.lada.test.land.MessprogrammMmtTest;
import de.intevation.lada.test.land.MessungTest;
import de.intevation.lada.test.land.MesswertTest;
import de.intevation.lada.test.land.OrtszuordnungTest;
import de.intevation.lada.test.land.PepGenerationTest;
import de.intevation.lada.test.land.ProbeTest;
import de.intevation.lada.test.land.QueryTest;
import de.intevation.lada.test.land.StatusTest;
import de.intevation.lada.test.land.TimestampTest;
import de.intevation.lada.test.land.ZusatzwertTest;


/**
 * Class to test the Lada server 'land' services.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@RunWith(Arquillian.class)
public class LandTest extends BaseTest {

    private static final int ID10000 = 10000;
    private static final int ID1200 = 1200;
    private static final int ID1000 = 1000;

    private static Logger logger = Logger.getLogger(StammdatenTest.class);

    @PersistenceContext
    EntityManager em;

    private ProbeTest probeTest;
    private MessungTest messungTest;
    private KommentarMTest mkommentarTest;
    private KommentarPTest pkommentarTest;
    private OrtszuordnungTest ortszuordnungTest;
    private ZusatzwertTest zusatzwertTest;
    private MesswertTest messwertTest;
    private StatusTest statusTest;
    private MessprogrammTest messprogrammTest;
    private MessprogrammMmtTest messprogrammMmtTest;
    private QueryTest queryTest;
    private PepGenerationTest pepGenerationTest;
    private GeolocatMpgTest geolocatMpgTest;
    private TimestampTest timestampTest;

    public LandTest() {
        probeTest = new ProbeTest();
        messungTest = new MessungTest();
        mkommentarTest = new KommentarMTest();
        pkommentarTest = new KommentarPTest();
        ortszuordnungTest = new OrtszuordnungTest();
        zusatzwertTest = new ZusatzwertTest();
        messwertTest = new MesswertTest();
        statusTest = new StatusTest();
        messprogrammTest = new MessprogrammTest();
        messprogrammMmtTest = new MessprogrammMmtTest();
        queryTest = new QueryTest();
        pepGenerationTest = new PepGenerationTest();
        geolocatMpgTest = new GeolocatMpgTest();
        timestampTest = new TimestampTest();
        verboseLogging = false;

        testDatasetName = "datasets/dbUnit_lada.xml";
    }

    /**
     * Output  for current test run.
     */
    @BeforeClass
    public static void beforeTests() {
        logger.info("---------- Testing Lada Land Services ----------");
    }

    /*------ REST service tests ------*/

    /**
     * Tests for probe operations.
     * @param baseUrl The server url used for the request.
     * @throws Exception that can occur during the test.
     */
    @Test
    @RunAsClient
    public final void testProbe(@ArquillianResource URL baseUrl) {
        probeTest.init(this.client, baseUrl, testProtocol);
        probeTest.execute();
    }

    /**
     * Tests for pkommentar operations.
     * @param baseUrl The server url used for the request.
     * @throws Exception that can occur during the test.
     */
    @Test
    @RunAsClient
    public final void testKommentarP(@ArquillianResource URL baseUrl) {
        pkommentarTest.init(this.client, baseUrl, testProtocol);
        pkommentarTest.execute();
    }

    /**
     * Tests for ortszurodnung operations.
     * @param baseUrl The server url used for the request.
     * @throws Exception that can occur during the test.
     */
    @Test
    @RunAsClient
    public final void testOrtszuordnung(@ArquillianResource URL baseUrl)
    throws Exception {
        ortszuordnungTest.init(this.client, baseUrl, testProtocol);
        ortszuordnungTest.execute();
    }

    /**
     * Tests for zusatzwert operations.
     * @param baseUrl The server url used for the request.
     * @throws Exception that can occur during the test.
     */
    @Test
    @RunAsClient
    public final void testZusatzwert(@ArquillianResource URL baseUrl)
    throws Exception {
        zusatzwertTest.init(this.client, baseUrl, testProtocol);
        zusatzwertTest.execute();
    }
    /**
     * Tests for messung operations.
     * @param baseUrl The server url used for the request.
     * @throws Exception that can occur during the test.
     */
    @Test
    @RunAsClient
    public final void testMessung(@ArquillianResource URL baseUrl)
    throws Exception {
        messungTest.init(this.client, baseUrl, testProtocol);
        messungTest.execute();
    }

    /**
     * Tests for mkommentar operations.
     * @param baseUrl The server url used for the request.
     * @throws Exception that can occur during the test.
     */
    @Test
    @RunAsClient
    public final void testKommentarM(@ArquillianResource URL baseUrl)
    throws Exception {
        mkommentarTest.init(this.client, baseUrl, testProtocol);
        mkommentarTest.execute();
    }

    /**
     * Tests for mkommentar operations.
     * @param baseUrl The server url used for the request.
     * @throws Exception that can occur during the test.
     */
    @Test
    @RunAsClient
    public final void testMesswert(@ArquillianResource URL baseUrl)
    throws Exception {
        messwertTest.init(this.client, baseUrl, testProtocol);
        messwertTest.execute();
    }

    /**
     * Tests for status operations.
     * @param baseUrl The server url used for the request.
     * @throws Exception that can occur during the test.
     */
    @Test
    @RunAsClient
    public final void testStatus(@ArquillianResource URL baseUrl)
    throws Exception {
        statusTest.init(this.client, baseUrl, testProtocol);
        statusTest.execute();
    }

    /**
     * Tests for messprogramm operations.
     * @param baseUrl The server url used for the request.
     * @throws Exception that can occur during the test.
     */
    @Test
    @RunAsClient
    public final void testMessprogramm(@ArquillianResource URL baseUrl)
    throws Exception {
        messprogrammTest.init(this.client, baseUrl, testProtocol);
        messprogrammTest.execute();
    }

    /**
     * Tests for messprogrammMmt operations.
     * @param baseUrl The server url used for the request.
     * @throws Exception that can occur during the test.
     */
    @Test
    @RunAsClient
    public final void testMessprogrammMmt(@ArquillianResource URL baseUrl)
    throws Exception {
        messprogrammMmtTest.init(this.client, baseUrl, testProtocol);
        messprogrammMmtTest.execute();
    }

    /**
     * Tests for query operations.
     * @param baseUrl The server url used for the request.
     * @throws Exception that can occur during the test.
     */
    @Test
    @RunAsClient
    public final void testQuery(@ArquillianResource URL baseUrl)
    throws Exception {
        queryTest.init(this.client, baseUrl, testProtocol);
        queryTest.execute();
    }

    /**
     * Test probe generation from a messprogramm record via url.
     * @param baseUrl The server url used for the request.
     * @throws Exception that can occur during the test.
     */
    @Test
    @RunAsClient
    public final void testPepGeneration(@ArquillianResource URL baseUrl)
            throws Exception {
        pepGenerationTest.init(this.client, baseUrl, testProtocol);
        pepGenerationTest.execute();
    }

    /**
     * Test geolocat mpg service operations.
     * @param baseUrl The server url used for the request.
     * @throws Exception that can occur during the test.
     */
    @Test
    @RunAsClient
    public final void testGeolocatMpg(@ArquillianResource URL baseUrl)
            throws Exception {
        geolocatMpgTest.init(this.client, baseUrl, testProtocol);
        geolocatMpgTest.execute();
    }

    /**
     * Test timestamp formats.
     * @param baseUrl The server url used for the request.
     * @throws Exception that can occur during the test.
     */
    @Test
    @RunAsClient
    public final void testTimestamps(@ArquillianResource URL baseUrl)
            throws Exception {
        timestampTest.init(this.client, baseUrl, testProtocol);
        timestampTest.execute();
    }
}
