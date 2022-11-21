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
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.persistence.ApplyScriptBefore;
import org.jboss.arquillian.persistence.Cleanup;
import org.jboss.arquillian.persistence.DataSource;
import org.jboss.arquillian.persistence.TestExecutionPhase;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.intevation.lada.model.land.CommMeasm;
import de.intevation.lada.model.land.CommSample;
import de.intevation.lada.model.land.Mpg;
import de.intevation.lada.model.land.Measm;
import de.intevation.lada.model.land.MeasVal;
import de.intevation.lada.model.land.Ortszuordnung;
import de.intevation.lada.model.land.Sample;
import de.intevation.lada.model.land.ZusatzWert;
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
import de.intevation.lada.test.land.ZusatzwertTest;


/**
 * Class to test the Lada server 'land' services.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@RunWith(Arquillian.class)
@ApplyScriptBefore("datasets/clean_and_seed.sql")
// TODO make tests independent of test data which do not exist anymore
public class LandTest extends BaseTest {

    private static final int ID10000 = 10000;
    private static final int ID1200 = 1200;
    private static final int ID1000 = 1000;
    private static final int T1 = 1;
    private static final int T2 = 2;
    private static final int T3 = 3;
    private static final int T4 = 4;
    private static final int T5 = 5;
    private static final int T6 = 6;
    private static final int T7 = 7;
    private static final int T8 = 8;
    private static final int T9 = 9;
    private static final int T10 = 10;
    private static final int T11 = 11;
    private static final int T12 = 12;
    private static final int T13 = 13;
    private static final int T14 = 14;
    private static final int T16 = 16;
    private static final int T17 = 17;
    private static final int T18 = 18;
    private static final int T20 = 20;
    private static final int T21 = 21;
    private static final int T22 = 22;

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
        verboseLogging = false;
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
    @InSequence(T2)
    @RunAsClient
    public final void testProbe(@ArquillianResource URL baseUrl)
    throws Exception {
        probeTest.init(this.client, baseUrl, testProtocol);
        probeTest.execute();
    }

    /**
     * Tests for pkommentar operations.
     * @param baseUrl The server url used for the request.
     * @throws Exception that can occur during the test.
     */
    @Test
    @Ignore("Service payload not yet translated")
    @InSequence(T4)
    @RunAsClient
    public final void testKommentarP(@ArquillianResource URL baseUrl)
    throws Exception {
        pkommentarTest.init(this.client, baseUrl, testProtocol);
        pkommentarTest.execute();
    }

    /**
     * Tests for ortszurodnung operations.
     * @param baseUrl The server url used for the request.
     * @throws Exception that can occur during the test.
     */
    @Test
    @Ignore("Service payload not yet translated")
    @InSequence(T6)
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
    @Ignore("Service payload not yet translated")
    @InSequence(T8)
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
    @InSequence(T10)
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
    @Ignore("Service payload not yet translated")
    @InSequence(T12)
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
    @InSequence(T14)
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
    @Ignore("Service payload not yet translated")
    @InSequence(T16)
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
    @InSequence(T18)
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
    @InSequence(T18)
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
    @InSequence(T20)
    @RunAsClient
    public final void testQuery(@ArquillianResource URL baseUrl)
    throws Exception {
        queryTest.init(this.client, baseUrl, testProtocol);
        queryTest.execute();
    }

    /*------ Database operations ------*/

    /**
     * Insert a probe object into the database.
     * @throws Exception that can occur during the test.
     */
    @Test
    @InSequence(T1)
    @UsingDataSet("datasets/dbUnit_probe.json")
    @DataSource("java:jboss/lada-test")
    @Cleanup(phase = TestExecutionPhase.NONE)
    public final void prepareDatabaseProbe() throws Exception {
        Protocol protocol = new Protocol();
        protocol.setName("database");
        protocol.setType("insert probe");
        protocol.addInfo("database", "Insert Sample into database");
        testProtocol.add(protocol);
        Sample probe = em.find(Sample.class, ID1000);
        Assert.assertNotNull(probe);
        protocol.setPassed(true);
    }

    /**
     * Insert a probe kommentar into the database.
     * @throws Exception that can occur during the test.
     */
    @Test
    @InSequence(T3)
    @UsingDataSet("datasets/dbUnit_probe.json")
    @DataSource("java:jboss/lada-test")
    @Cleanup(phase = TestExecutionPhase.NONE)
    public final void prepareDatabaseKommentarP() throws Exception {
        Protocol protocol = new Protocol();
        protocol.setName("database");
        protocol.setType("insert kommentar_p");
        protocol.addInfo("database", "Insert KommentarP into database");
        testProtocol.add(protocol);
        CommSample kommentar = em.find(CommSample.class, ID1000);
        Assert.assertNotNull(kommentar);
        protocol.setPassed(true);
    }

    /**
     * Insert a ortszuordnung into the database.
     * @throws Exception that can occur during the test.
     */
    @Test
    @InSequence(T5)
    @UsingDataSet("datasets/dbUnit_probe.json")
    @DataSource("java:jboss/lada-test")
    @Cleanup(phase = TestExecutionPhase.NONE)
    public final void prepareDatabaseOrtszuordnung() throws Exception {
        Protocol protocol = new Protocol();
        protocol.setName("database");
        protocol.setType("insert ortszuordnung");
        protocol.addInfo("database", "Insert Ortszuordnung into database");
        testProtocol.add(protocol);
        Ortszuordnung ortszuordnung = em.find(Ortszuordnung.class, ID1000);
        Assert.assertNotNull(ortszuordnung);
        protocol.setPassed(true);
    }

    /**
     * Insert a zusatzwert into the database.
     * @throws Exception that can occur during the test.
     */
    @Test
    @InSequence(T7)
    @UsingDataSet("datasets/dbUnit_probe.json")
    @DataSource("java:jboss/lada-test")
    @Cleanup(phase = TestExecutionPhase.NONE)
    public final void prepareDatabaseZusatzwert() throws Exception {
        Protocol protocol = new Protocol();
        protocol.setName("database");
        protocol.setType("insert zusatzwert");
        protocol.addInfo("database", "Insert Zusatzwert into database");
        testProtocol.add(protocol);
        ZusatzWert zusatzwert = em.find(ZusatzWert.class, ID1000);
        Assert.assertNotNull(zusatzwert);
        protocol.setPassed(true);
    }

    /**
     * Insert a messung object into the database.
     * @throws Exception that can occur during the test.
     */
    @Test
    @InSequence(T9)
    @UsingDataSet("datasets/dbUnit_probe.json")
    @DataSource("java:jboss/lada-test")
    @Cleanup(phase = TestExecutionPhase.NONE)
    public final void prepareDatabaseMessung() throws Exception {
        Protocol protocol = new Protocol();
        protocol.setName("database");
        protocol.setType("insert messung");
        protocol.addInfo("database", "Insert Messung into database");
        testProtocol.add(protocol);
        Measm messung = em.find(Measm.class, ID1200);
        messung.setStatus(ID1000);
        em.merge(messung);
        Assert.assertNotNull(messung);
        protocol.setPassed(true);
    }

    /**
     * Insert a messungs kommentar into the database.
     * @throws Exception that can occur during the test.
     */
    @Test
    @InSequence(T11)
    @UsingDataSet("datasets/dbUnit_probe.json")
    @DataSource("java:jboss/lada-test")
    @Cleanup(phase = TestExecutionPhase.NONE)
    public final void prepareDatabaseKommentarM() throws Exception {
        Protocol protocol = new Protocol();
        protocol.setName("database");
        protocol.setType("insert kommentar_m");
        protocol.addInfo("database", "Insert KommentarM into database");
        testProtocol.add(protocol);
        CommMeasm kommentar = em.find(CommMeasm.class, ID1000);
        Assert.assertNotNull(kommentar);
        protocol.setPassed(true);
    }

    /**
     * Insert a messwert into the database.
     * @throws Exception that can occur during the test.
     */
    @Test
    @InSequence(T13)
    @UsingDataSet("datasets/dbUnit_probe.json")
    @DataSource("java:jboss/lada-test")
    @Cleanup(phase = TestExecutionPhase.NONE)
    public final void prepareDatabaseMesswert() throws Exception {
        Protocol protocol = new Protocol();
        protocol.setName("database");
        protocol.setType("insert messwert");
        protocol.addInfo("database", "Insert Messwert into database");
        testProtocol.add(protocol);
        MeasVal messwert = em.find(MeasVal.class, ID10000);
        Assert.assertNotNull(messwert);
        protocol.setPassed(true);
    }

    /**
     * Insert a messprogramm into the database.
     * @throws Exception that can occur during the test.
     */
    @Test
    @InSequence(T17)
    @ApplyScriptBefore("datasets/clean_and_seed.sql")
    @UsingDataSet("datasets/dbUnit_messprogramm.json")
    @DataSource("java:jboss/lada-test")
    @Cleanup(phase = TestExecutionPhase.NONE)
    public final void prepareDatabaseMessprogramm() throws Exception {
        Protocol protocol = new Protocol();
        protocol.setName("database");
        protocol.setType("insert messprogramm");
        protocol.addInfo("database", "Insert Messprogramm into database");
        testProtocol.add(protocol);
        Mpg messprogramm = em.find(Mpg.class, ID1000);
        Assert.assertNotNull(messprogramm);
        protocol.setPassed(true);
    }

    /**
     * Test probe generation from a messprogramm record.
     */
    @Test
    @UsingDataSet("datasets/dbUnit_pep_gen.json")
    @DataSource("java:jboss/lada-test")
    @InSequence(T21)
    @Cleanup(phase = TestExecutionPhase.NONE)
    public final void prepareTestPepGeneration() {
        Protocol protocol = new Protocol();
        protocol.setName("database");
        protocol.setType("insert messprogramm");
        protocol.addInfo("database", "Insert Messprogramm into database");
        testProtocol.add(protocol);
        Mpg messprogramm = em.find(Mpg.class, ID1000);
        Assert.assertNotNull(messprogramm);
        protocol.setPassed(true);
    }

    /**
     * Test probe generation from a messprogramm record via url.
     * @param baseUrl The server url used for the request.
     * @throws Exception that can occur during the test.
     */
    @Test
    @DataSource("java:jboss/lada-test")
    @InSequence(T22)
    @RunAsClient
    public final void testPepGeneration(@ArquillianResource URL baseUrl)
            throws Exception {
        pepGenerationTest.init(this.client, baseUrl, testProtocol);
        pepGenerationTest.execute();
    }
}
