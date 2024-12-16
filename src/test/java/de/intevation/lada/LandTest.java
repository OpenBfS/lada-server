/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
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

    /*------ REST service tests ------*/

    /**
     * Tests for probe operations.
     * @param baseUrl The server url used for the request.
     * @throws Exception that can occur during the test.
     */
    @Test
    @RunAsClient
    public final void testProbe() {
        probeTest.init(this.target);
        probeTest.execute();
    }

    /**
     * Tests for pkommentar operations.
     * @param baseUrl The server url used for the request.
     * @throws Exception that can occur during the test.
     */
    @Test
    @RunAsClient
    public final void testKommentarP() {
        pkommentarTest.init(this.target);
        pkommentarTest.execute();
    }

    /**
     * Tests for ortszurodnung operations.
     * @param baseUrl The server url used for the request.
     * @throws Exception that can occur during the test.
     */
    @Test
    @RunAsClient
    public final void testOrtszuordnung()
    throws Exception {
        ortszuordnungTest.init(this.target);
        ortszuordnungTest.execute();
    }

    /**
     * Tests for zusatzwert operations.
     * @param baseUrl The server url used for the request.
     * @throws Exception that can occur during the test.
     */
    @Test
    @RunAsClient
    public final void testZusatzwert()
    throws Exception {
        zusatzwertTest.init(this.target);
        zusatzwertTest.execute();
    }
    /**
     * Tests for messung operations.
     * @param baseUrl The server url used for the request.
     * @throws Exception that can occur during the test.
     */
    @Test
    @RunAsClient
    public final void testMessung()
    throws Exception {
        messungTest.init(this.target);
        messungTest.execute();
    }

    /**
     * Tests for mkommentar operations.
     * @param baseUrl The server url used for the request.
     * @throws Exception that can occur during the test.
     */
    @Test
    @RunAsClient
    public final void testKommentarM()
    throws Exception {
        mkommentarTest.init(this.target);
        mkommentarTest.execute();
    }

    /**
     * Tests for mkommentar operations.
     * @param baseUrl The server url used for the request.
     * @throws Exception that can occur during the test.
     */
    @Test
    @RunAsClient
    public final void testMesswert()
    throws Exception {
        messwertTest.init(this.target);
        messwertTest.execute();
    }

    /**
     * Tests for status operations.
     * @param baseUrl The server url used for the request.
     * @throws Exception that can occur during the test.
     */
    @Test
    @RunAsClient
    public final void testStatus()
    throws Exception {
        statusTest.init(this.target);
        statusTest.execute();
    }

    /**
     * Tests for messprogramm operations.
     * @param baseUrl The server url used for the request.
     * @throws Exception that can occur during the test.
     */
    @Test
    @RunAsClient
    public final void testMessprogramm()
    throws Exception {
        messprogrammTest.init(this.target);
        messprogrammTest.execute();
    }

    /**
     * Tests for messprogrammMmt operations.
     * @param baseUrl The server url used for the request.
     * @throws Exception that can occur during the test.
     */
    @Test
    @RunAsClient
    public final void testMessprogrammMmt()
    throws Exception {
        messprogrammMmtTest.init(this.target);
        messprogrammMmtTest.execute();
    }

    /**
     * Tests for query operations.
     * @param baseUrl The server url used for the request.
     * @throws Exception that can occur during the test.
     */
    @Test
    @RunAsClient
    public final void testQuery()
    throws Exception {
        queryTest.init(this.target);
        queryTest.execute();
    }

    /**
     * Test probe generation from a messprogramm record via url.
     * @param baseUrl The server url used for the request.
     * @throws Exception that can occur during the test.
     */
    @Test
    @RunAsClient
    public final void testPepGeneration()
            throws Exception {
        pepGenerationTest.init(this.target);
        pepGenerationTest.execute();
    }

    /**
     * Test geolocat mpg service operations.
     * @param baseUrl The server url used for the request.
     * @throws Exception that can occur during the test.
     */
    @Test
    @RunAsClient
    public final void testGeolocatMpg()
            throws Exception {
        geolocatMpgTest.init(this.target);
        geolocatMpgTest.execute();
    }

    /**
     * Test timestamp formats.
     * @param baseUrl The server url used for the request.
     * @throws Exception that can occur during the test.
     */
    @Test
    @RunAsClient
    public final void testTimestamps()
            throws Exception {
        timestampTest.init(this.target);
        timestampTest.execute();
    }
}
