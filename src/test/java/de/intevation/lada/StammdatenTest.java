/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada;

import java.net.URL;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.intevation.lada.test.land.TagZuordnungTest;
import de.intevation.lada.test.stamm.DatensatzErzeugerTest;
import de.intevation.lada.test.stamm.DeskriptorenTest;
import de.intevation.lada.test.stamm.KoordinatenartTest;
import de.intevation.lada.test.stamm.MessprogrammKategorieTest;
import de.intevation.lada.test.stamm.MunicDivTest;
import de.intevation.lada.test.stamm.OrtTest;
import de.intevation.lada.test.stamm.ProbenehmerTest;
import de.intevation.lada.test.stamm.Stammdaten;
import de.intevation.lada.test.stamm.StatusMpTest;
import de.intevation.lada.test.stamm.TagTest;


/**
 * Class to test the Lada server stammdaten services.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@RunWith(Arquillian.class)
public class StammdatenTest extends BaseTest {

    private static final int ID5 = 5;
    private static final int ID9 = 9;
    private static final int ID56 = 56;
    private static final int ID101 = 101;
    private static final int ID207 = 207;
    private static final String IDA = "A";

    @PersistenceContext
    EntityManager em;

    private Stammdaten stammdatenTest;
    private DatensatzErzeugerTest datensatzerzeugerTest;
    private ProbenehmerTest probenehmerTest;
    private MessprogrammKategorieTest messprogrammkategorieTest;
    private OrtTest ortTest;
    private DeskriptorenTest deskriptorenTest;
    private KoordinatenartTest kdaTest;
    private TagTest tagTest;
    private TagZuordnungTest tagZuordnungTest;
    private MunicDivTest municDivTest;
    private StatusMpTest statusMpTest;

    public StammdatenTest() {
        stammdatenTest = new Stammdaten();
        datensatzerzeugerTest = new DatensatzErzeugerTest();
        probenehmerTest = new ProbenehmerTest();
        messprogrammkategorieTest = new MessprogrammKategorieTest();
        ortTest = new OrtTest();
        deskriptorenTest = new DeskriptorenTest();
        kdaTest = new KoordinatenartTest();
        tagTest = new TagTest();
        tagZuordnungTest = new TagZuordnungTest();
        municDivTest = new MunicDivTest();
        statusMpTest = new StatusMpTest();
        verboseLogging = false;
        testDatasetName = "datasets/dbUnit_master.xml";
    }

    /**
     * @param baseUrl The server url used for the request.
     * @throws Exception that can occur during the test.
     */
    @Test
    @RunAsClient
    public final void testDatensatzerzeuger(@ArquillianResource URL baseUrl)
    throws Exception {
        datensatzerzeugerTest.init(this.client, baseUrl);
        datensatzerzeugerTest.execute();
    }

    /**
     * Tests for probe operations.
     * @param baseUrl The server url used for the request.
     * @throws Exception that can occur during the test.
     */
    @Test
    @RunAsClient
    public final void testProbenehmer(@ArquillianResource URL baseUrl)
    throws Exception {
        probenehmerTest.init(this.client, baseUrl);
        probenehmerTest.execute();
    }

    /**
     * Tests for probe operations.
     * @param baseUrl The server url used for the request.
     * @throws Exception that can occur during the test.
     */
    @Test
    @RunAsClient
    public final void testMessprogrammKategorie(@ArquillianResource URL baseUrl)
    throws Exception {
        messprogrammkategorieTest.init(this.client, baseUrl);
        messprogrammkategorieTest.execute();
    }

    /**
     * Tests for probe operations.
     * @param baseUrl The server url used for the request.
     * @throws Exception that can occur during the test.
     */
    @Test
    @RunAsClient
    public final void testOrt(@ArquillianResource URL baseUrl)
    throws Exception {
        ortTest.init(this.client, baseUrl);
        ortTest.execute();
    }

    /**
     * Tests for datenbasis operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testRegulationAll(@ArquillianResource URL baseUrl) {
        stammdatenTest.init(this.client, baseUrl);
        stammdatenTest.getAll("regulation");
    }

    /**
     * Tests for datenbasis by id operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testRegulationById(@ArquillianResource URL baseUrl) {
        stammdatenTest.init(this.client, baseUrl);
        stammdatenTest.getById("regulation", ID9);
    }

    /**
     * Tests for messeinheit operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testMesseinheitAll(@ArquillianResource URL baseUrl) {
        stammdatenTest.init(this.client, baseUrl);
        stammdatenTest.getAll("measunit");
    }

    /**
     * Tests for messeinheit by id operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testMesseinheitById(@ArquillianResource URL baseUrl) {
        stammdatenTest.init(this.client, baseUrl);
        stammdatenTest.getById("measunit", ID207);
    }

    /**
     * Tests for messgroesse operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testMessgroesseAll(@ArquillianResource URL baseUrl) {
        stammdatenTest.init(this.client, baseUrl);
        stammdatenTest.getAll("measd");
    }

    /**
     * Tests for messgroesse by id operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testMessgroesseById(@ArquillianResource URL baseUrl) {
        stammdatenTest.init(this.client, baseUrl);
        stammdatenTest.getById("measd", ID56);
    }

    /**
     * Tests for messmethode operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testMessmethodeAll(@ArquillianResource URL baseUrl) {
        stammdatenTest.init(this.client, baseUrl);
        stammdatenTest.getAll("mmt");
    }

    /**
     * Tests for messmethode by id operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testMessmethodeById(@ArquillianResource URL baseUrl) {
        stammdatenTest.init(this.client, baseUrl);
        stammdatenTest.getById("mmt", "A3");
    }

    /**
     * Tests for messstelle operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testMessstelleAll(@ArquillianResource URL baseUrl) {
        stammdatenTest.init(this.client, baseUrl);
        stammdatenTest.getAll("measfacil");
    }

    /**
     * Tests for messstelle by id operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testMessstelleById(@ArquillianResource URL baseUrl) {
        stammdatenTest.init(this.client, baseUrl);
        stammdatenTest.getById("measfacil", "06010");
    }

    /**
     * Tests for netzbetreiber operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testNetzbetreiberAll(@ArquillianResource URL baseUrl) {
        stammdatenTest.init(this.client, baseUrl);
        stammdatenTest.getAll("network");
    }

    /**
     * Tests for netzbetreiber by id operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testNetzbetreiberById(@ArquillianResource URL baseUrl) {
        stammdatenTest.init(this.client, baseUrl);
        stammdatenTest.getById("network", "06");
    }

    /**
     * Tests for probeart operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testProbenartAll(@ArquillianResource URL baseUrl) {
        stammdatenTest.init(this.client, baseUrl);
        stammdatenTest.getAll("samplemeth");
    }

    /**
     * Tests for probeart by id operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testProbenartById(@ArquillianResource URL baseUrl) {
        stammdatenTest.init(this.client, baseUrl);
        stammdatenTest.getById("samplemeth", 1);
    }

    /**
     * Tests for probenzusatz operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testProbenzusatzAll(@ArquillianResource URL baseUrl) {
        stammdatenTest.init(this.client, baseUrl);
        stammdatenTest.getAll("samplespecif");
    }

    /**
     * Tests for probenzusatz by id operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testProbenzusatzById(@ArquillianResource URL baseUrl) {
        stammdatenTest.init(this.client, baseUrl);
        stammdatenTest.getById("samplespecif", "A74");
    }

    /**
     * Tests for koordinatenart operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testKoordinatenartAll(@ArquillianResource URL baseUrl) {
        stammdatenTest.init(this.client, baseUrl);
        stammdatenTest.getAll("spatrefsys");
    }

    /**
     * Tests for koordinatenart by id operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testKoordinatenartById(@ArquillianResource URL baseUrl) {
        stammdatenTest.init(this.client, baseUrl);
        stammdatenTest.getById("spatrefsys", ID5);
    }

    /**
     * Tests for staat operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testStaatAll(@ArquillianResource URL baseUrl) {
        stammdatenTest.init(this.client, baseUrl);
        stammdatenTest.getAll("state");
    }

    /**
     * Tests for staat by id operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testStaatById(@ArquillianResource URL baseUrl) {
        stammdatenTest.init(this.client, baseUrl);
        stammdatenTest.getById("state", 0);
    }

    /**
     * Tests for umwelt  operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testUmweltAll(@ArquillianResource URL baseUrl) {
        stammdatenTest.init(this.client, baseUrl);
        stammdatenTest.getAll("envmedium");
    }

    /**
     * Tests for umwelt by id operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testUmweltById(@ArquillianResource URL baseUrl) {
        stammdatenTest.init(this.client, baseUrl);
        stammdatenTest.getById("envmedium", "L6");
    }

    /**
     * Tests for verwaltungseinheit operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testVerwaltungseinheitAll(
        @ArquillianResource URL baseUrl
    ) {
        stammdatenTest.init(this.client, baseUrl);
        stammdatenTest.getAll("adminunit");
    }

    /**
     * Tests for verwaltungseinheit by id operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testVerwaltungseinheitById(
        @ArquillianResource URL baseUrl
    ) {
        stammdatenTest.init(this.client, baseUrl);
        stammdatenTest.getById("adminunit", "11000000");
    }

    /**
     * Tests deskriptoren service.
     * @param baseUrl The server url used for the request.
     * @throws Exception that can occur during the test.
     */
    @Test
    @RunAsClient
    public final void testDeskriptoren(@ArquillianResource URL baseUrl)
    throws Exception {
        deskriptorenTest.init(this.client, baseUrl);
        deskriptorenTest.execute();
    }

    /**
     * Tests KoordinatenartService.
     * @param baseUrl The server url used for the request.
     * @throws Exception that can occur during the test.
     */
    @Test
    @RunAsClient
    public final void testKoordinatenart(@ArquillianResource URL baseUrl)
    throws Exception {
        kdaTest.init(this.client, baseUrl);
        kdaTest.execute();
    }

    /**
     * Test Tag service.
     * @param baseUrl The server url used for the request.
     * @throws Exception that can occur during the test.
     */
    @Test
    @RunAsClient
    public final void testTag(@ArquillianResource URL baseUrl)
    throws Exception {
        tagTest.init(this.client, baseUrl);
        tagTest.execute();
    }

    /**
     * Test TagZuordnung service.
     * @param baseUrl The server url used for the request.
     * @throws Exception that can occur during the test.
     */
    @Test
    @RunAsClient
    public final void testTagZuordnung(@ArquillianResource URL baseUrl)
    throws Exception {
        tagZuordnungTest.init(this.client, baseUrl);
        tagZuordnungTest.execute();
    }

    /**
     * Tests type regulation get all operation.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testTypeRegulationAll(@ArquillianResource URL baseUrl) {
        stammdatenTest.init(this.client, baseUrl);
        stammdatenTest.getAll("typeregulation");
    }

    /**
     * Tests type regulation get by id operation.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testTypeRegulationById(@ArquillianResource URL baseUrl) {
        stammdatenTest.init(this.client, baseUrl);
        stammdatenTest.getById("typeregulation", IDA);
    }

    /**
     * Tests poi get all operation.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testPoiAll(@ArquillianResource URL baseUrl) {
        stammdatenTest.init(this.client, baseUrl);
        stammdatenTest.getAll("poi");
    }

    /**
     * Tests poi get by id operation.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testPoiById(@ArquillianResource URL baseUrl) {
        stammdatenTest.init(this.client, baseUrl);
        stammdatenTest.getById("poi", IDA);
    }

    /**
     * Tests for TargActMmtGr operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testTargActMmtGrAll(@ArquillianResource URL baseUrl) {
        stammdatenTest.init(this.client, baseUrl);
        stammdatenTest.getAll("targactmmtgr");
    }

    /**
     * Tests for TargEnvGr operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testTargEnvGrAll(@ArquillianResource URL baseUrl) {
        stammdatenTest.init(this.client, baseUrl);
        stammdatenTest.getAll("targenvgr");
    }

    /**
     * Tests EnvSpecifMp get all operation.
     * @param baseUrl The server url used for the request
     */
    @Test
    @RunAsClient
    public final void testEnvSpecifMpAll(@ArquillianResource URL baseUrl) {
        stammdatenTest.init(this.client, baseUrl);
        stammdatenTest.getAll("envspecifmp");
    }

    /**
     * Tests EnvSpecifMp get all operation.
     * @param baseUrl The server url used for the request
     */
    @Test
    @RunAsClient
    public final void testEnvSpecifMpGetById(@ArquillianResource URL baseUrl) {
        stammdatenTest.init(this.client, baseUrl);
        stammdatenTest.getById("envspecifmp", ID101);
    }

    /**
     * Test MunicDiv serivce operations.
     * @param baseUrl The server url used for the request
     */
    @Test
    @RunAsClient
    public final void testMunicDiv(@ArquillianResource URL baseUrl) {
        municDivTest.init(this.client, baseUrl);
        municDivTest.execute();
    }

    /**
     * Tests ReiAgGr get all operation.
     * @param baseUrl The server url used for the request
     */
    @Test
    @RunAsClient
    public final void testReiAgGrAll(@ArquillianResource URL baseUrl) {
        stammdatenTest.init(this.client, baseUrl);
        stammdatenTest.getAll("reiaggr");
    }

    /**
     * Tests ReiAgGR get all operation.
     * @param baseUrl The server url used for the request
     */
    @Test
    @RunAsClient
    public final void testReiAgGrById(@ArquillianResource URL baseUrl) {
        stammdatenTest.init(this.client, baseUrl);
        stammdatenTest.getById("reiaggr", ID101);
    }

    /**
     * Tests StatusMpService.
     * @param baseUrl The server url used for the request
     */
    @Test
    @RunAsClient
    public final void testStatusMp(@ArquillianResource URL baseUrl) {
        statusMpTest.init(this.client, baseUrl);
        statusMpTest.execute();
    }
}
