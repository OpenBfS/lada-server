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
    public final void testDatensatzerzeuger()
    throws Exception {
        datensatzerzeugerTest.init(this.target);
        datensatzerzeugerTest.execute();
    }

    /**
     * Tests for probe operations.
     * @param baseUrl The server url used for the request.
     * @throws Exception that can occur during the test.
     */
    @Test
    @RunAsClient
    public final void testProbenehmer()
    throws Exception {
        probenehmerTest.init(this.target);
        probenehmerTest.execute();
    }

    /**
     * Tests for probe operations.
     * @param baseUrl The server url used for the request.
     * @throws Exception that can occur during the test.
     */
    @Test
    @RunAsClient
    public final void testMessprogrammKategorie()
    throws Exception {
        messprogrammkategorieTest.init(this.target);
        messprogrammkategorieTest.execute();
    }

    /**
     * Tests for probe operations.
     * @param baseUrl The server url used for the request.
     * @throws Exception that can occur during the test.
     */
    @Test
    @RunAsClient
    public final void testOrt()
    throws Exception {
        ortTest.init(this.target);
        ortTest.execute();
    }

    /**
     * Tests for datenbasis operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testRegulationAll() {
        stammdatenTest.init(this.target);
        stammdatenTest.getAll("regulation");
    }

    /**
     * Tests for datenbasis by id operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testRegulationById() {
        stammdatenTest.init(this.target);
        stammdatenTest.getById("regulation", ID9);
    }

    /**
     * Tests for messeinheit operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testMesseinheitAll() {
        stammdatenTest.init(this.target);
        stammdatenTest.getAll("measunit");
    }

    /**
     * Tests for messeinheit by id operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testMesseinheitById() {
        stammdatenTest.init(this.target);
        stammdatenTest.getById("measunit", ID207);
    }

    /**
     * Tests for messgroesse operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testMessgroesseAll() {
        stammdatenTest.init(this.target);
        stammdatenTest.getAll("measd");
    }

    /**
     * Tests for messgroesse by id operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testMessgroesseById() {
        stammdatenTest.init(this.target);
        stammdatenTest.getById("measd", ID56);
    }

    /**
     * Tests for messmethode operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testMessmethodeAll() {
        stammdatenTest.init(this.target);
        stammdatenTest.getAll("mmt");
    }

    /**
     * Tests for messmethode by id operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testMessmethodeById() {
        stammdatenTest.init(this.target);
        stammdatenTest.getById("mmt", "A3");
    }

    /**
     * Tests for messstelle operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testMessstelleAll() {
        stammdatenTest.init(this.target);
        stammdatenTest.getAll("measfacil");
    }

    /**
     * Tests for messstelle by id operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testMessstelleById() {
        stammdatenTest.init(this.target);
        stammdatenTest.getById("measfacil", "06010");
    }

    /**
     * Tests for netzbetreiber operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testNetzbetreiberAll() {
        stammdatenTest.init(this.target);
        stammdatenTest.getAll("network");
    }

    /**
     * Tests for netzbetreiber by id operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testNetzbetreiberById() {
        stammdatenTest.init(this.target);
        stammdatenTest.getById("network", "06");
    }

    /**
     * Tests for probeart operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testProbenartAll() {
        stammdatenTest.init(this.target);
        stammdatenTest.getAll("samplemeth");
    }

    /**
     * Tests for probeart by id operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testProbenartById() {
        stammdatenTest.init(this.target);
        stammdatenTest.getById("samplemeth", 1);
    }

    /**
     * Tests for probenzusatz operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testProbenzusatzAll() {
        stammdatenTest.init(this.target);
        stammdatenTest.getAll("samplespecif");
    }

    /**
     * Tests for probenzusatz by id operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testProbenzusatzById() {
        stammdatenTest.init(this.target);
        stammdatenTest.getById("samplespecif", "A74");
    }

    /**
     * Tests for koordinatenart operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testKoordinatenartAll() {
        stammdatenTest.init(this.target);
        stammdatenTest.getAll("spatrefsys");
    }

    /**
     * Tests for koordinatenart by id operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testKoordinatenartById() {
        stammdatenTest.init(this.target);
        stammdatenTest.getById("spatrefsys", ID5);
    }

    /**
     * Tests for staat operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testStaatAll() {
        stammdatenTest.init(this.target);
        stammdatenTest.getAll("state");
    }

    /**
     * Tests for staat by id operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testStaatById() {
        stammdatenTest.init(this.target);
        stammdatenTest.getById("state", 0);
    }

    /**
     * Tests for umwelt  operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testUmweltAll() {
        stammdatenTest.init(this.target);
        stammdatenTest.getAll("envmedium");
    }

    /**
     * Tests for umwelt by id operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testUmweltById() {
        stammdatenTest.init(this.target);
        stammdatenTest.getById("envmedium", "L6");
    }

    /**
     * Tests for verwaltungseinheit operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testVerwaltungseinheitAll() {
        stammdatenTest.init(this.target);
        stammdatenTest.getAll("adminunit");
    }

    /**
     * Tests for verwaltungseinheit by id operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testVerwaltungseinheitById() {
        stammdatenTest.init(this.target);
        stammdatenTest.getById("adminunit", "11000000");
    }

    /**
     * Tests deskriptoren service.
     * @param baseUrl The server url used for the request.
     * @throws Exception that can occur during the test.
     */
    @Test
    @RunAsClient
    public final void testDeskriptoren()
    throws Exception {
        deskriptorenTest.init(this.target);
        deskriptorenTest.execute();
    }

    /**
     * Tests KoordinatenartService.
     * @param baseUrl The server url used for the request.
     * @throws Exception that can occur during the test.
     */
    @Test
    @RunAsClient
    public final void testKoordinatenart()
    throws Exception {
        kdaTest.init(this.target);
        kdaTest.execute();
    }

    /**
     * Test Tag service.
     * @param baseUrl The server url used for the request.
     * @throws Exception that can occur during the test.
     */
    @Test
    @RunAsClient
    public final void testTag()
    throws Exception {
        tagTest.init(this.target);
        tagTest.execute();
    }

    /**
     * Test TagZuordnung service.
     * @param baseUrl The server url used for the request.
     * @throws Exception that can occur during the test.
     */
    @Test
    @RunAsClient
    public final void testTagZuordnung()
    throws Exception {
        tagZuordnungTest.init(this.target);
        tagZuordnungTest.execute();
    }

    /**
     * Tests type regulation get all operation.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testTypeRegulationAll() {
        stammdatenTest.init(this.target);
        stammdatenTest.getAll("typeregulation");
    }

    /**
     * Tests type regulation get by id operation.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testTypeRegulationById() {
        stammdatenTest.init(this.target);
        stammdatenTest.getById("typeregulation", IDA);
    }

    /**
     * Tests poi get all operation.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testPoiAll() {
        stammdatenTest.init(this.target);
        stammdatenTest.getAll("poi");
    }

    /**
     * Tests poi get by id operation.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testPoiById() {
        stammdatenTest.init(this.target);
        stammdatenTest.getById("poi", IDA);
    }

    /**
     * Tests for TargActMmtGr operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testTargActMmtGrAll() {
        stammdatenTest.init(this.target);
        stammdatenTest.getAll("targactmmtgr");
    }

    /**
     * Tests for TargEnvGr operations.
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testTargEnvGrAll() {
        stammdatenTest.init(this.target);
        stammdatenTest.getAll("targenvgr");
    }

    /**
     * Tests EnvSpecifMp get all operation.
     * @param baseUrl The server url used for the request
     */
    @Test
    @RunAsClient
    public final void testEnvSpecifMpAll() {
        stammdatenTest.init(this.target);
        stammdatenTest.getAll("envspecifmp");
    }

    /**
     * Tests EnvSpecifMp get all operation.
     * @param baseUrl The server url used for the request
     */
    @Test
    @RunAsClient
    public final void testEnvSpecifMpGetById() {
        stammdatenTest.init(this.target);
        stammdatenTest.getById("envspecifmp", ID101);
    }

    /**
     * Test MunicDiv serivce operations.
     * @param baseUrl The server url used for the request
     */
    @Test
    @RunAsClient
    public final void testMunicDiv() {
        municDivTest.init(this.target);
        municDivTest.execute();
    }

    /**
     * Tests ReiAgGr get all operation.
     * @param baseUrl The server url used for the request
     */
    @Test
    @RunAsClient
    public final void testReiAgGrAll() {
        stammdatenTest.init(this.target);
        stammdatenTest.getAll("reiaggr");
    }

    /**
     * Tests ReiAgGR get all operation.
     * @param baseUrl The server url used for the request
     */
    @Test
    @RunAsClient
    public final void testReiAgGrById() {
        stammdatenTest.init(this.target);
        stammdatenTest.getById("reiaggr", ID101);
    }

    /**
     * Tests StatusMpService.
     * @param baseUrl The server url used for the request
     */
    @Test
    @RunAsClient
    public final void testStatusMp() {
        statusMpTest.init(this.target);
        statusMpTest.execute();
    }
}
