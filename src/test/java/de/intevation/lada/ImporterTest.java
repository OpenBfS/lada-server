/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada;

import java.net.URL;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.SyncInvoker;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.persistence.ApplyScriptBefore;
import org.jboss.arquillian.persistence.Cleanup;
import org.jboss.arquillian.persistence.CleanupStrategy;
import org.jboss.arquillian.persistence.ShouldMatchDataSet;
import org.jboss.arquillian.persistence.TestExecutionPhase;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.logging.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.intevation.lada.importer.Identified;
import de.intevation.lada.importer.Identifier;
import de.intevation.lada.importer.IdentifierConfig;
import de.intevation.lada.importer.ObjectMerger;
import de.intevation.lada.model.lada.CommMeasm;
import de.intevation.lada.model.lada.CommSample;
import de.intevation.lada.model.lada.MeasVal;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.lada.SampleSpecifMeasVal;
import de.intevation.lada.util.data.Job;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;


/**
 * Class to test the Lada-Importer.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@RunWith(Arquillian.class)
@ApplyScriptBefore("datasets/clean_and_seed.sql")
public class ImporterTest extends BaseTest {

    private static final Logger LOG = Logger.getLogger(ImporterTest.class);

    private static final double MESS15D = 1.5d;
    private static final int MGID56 = 56;
    private static final int MEHID207 = 207;
    private static final double MESS18D = 1.8d;
    private static final float MESSFEHLER02F = 0.2f;
    private static final float MESSFEHLER12F = 1.2f;
    private static final int MDAUER1000 = 1000;
    private static final int MID1200 = 1200;
    private static final int PNID = 726;
    private static final int MPRID1000 = 1000;
    private static final int PID1000 = 1000;
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
    private static final int T15 = 15;
    private static final int T16 = 16;
    private static final int T17 = 17;
    private static final Integer DID9 = 9;

    private final String lafSampleId = "XXX";
    private final String mstId = "06010";
    private final String regulation = "test";
    private final String sampleSpecifId = "A1";
    private final String measd = "H-3";
    private final String measUnit = "Bq/kgFM";
    private final String lafTemplate = "%%PROBE%%\n"
        + "UEBERTRAGUNGSFORMAT 7\n"
        + "VERSION \"0084\"\n"
        + "PROBE_ID \"%s\"\n"
        + "PROBENART \"E\"\n"
        + "MESSPROGRAMM_S 1\n"
        + "DATENBASIS \"%s\"\n"
        + "PZB_S \"%s\" 42 \"\" 5.0\n"
        + "%s"
        + "%%MESSUNG%%\n"
        + "MESSMETHODE_S \"A3\"\n"
        + "MESSWERT \"%s\" 72.177002 \"%s\" 4.4\n"
        + "%%ENDE%%\n";
    private final String laf = String.format(
        lafTemplate, lafSampleId, regulation, sampleSpecifId,
        "", measd, measUnit);

    final String dataKey = "data";

    @PersistenceContext
    EntityManager em;

    @Inject
    @IdentifierConfig(type = "Sample")
    Identifier probeIdentifier;

    @Inject
    @IdentifierConfig(type = "Messung")
    Identifier messungIdentifier;

    @Inject
    Repository repository;

    @Inject
    ObjectMerger merger;

    /**
     * Identify probe objects.
     *
     * @throws Exception that can occur during the test.
     */
    @Test
    @InSequence(0)
    @UsingDataSet("datasets/dbUnit_probe_import.json")
    @Cleanup(phase = TestExecutionPhase.AFTER,
        strategy = CleanupStrategy.USED_TABLES_ONLY)
    public final void identifyProbeByHPNrMST() throws Exception {
        Protocol protocol = new Protocol();
        protocol.setName("import");
        protocol.setType("identify probe");
        protocol.addInfo(
            "import",
            "Compare and find Sample by HP-Nr. and MST, Update");

        Sample probe = new Sample();
        probe.setMainSampleId("120510002");
        probe.setMeasFacilId(mstId);

        Identified found = probeIdentifier.find(probe);
        Assert.assertEquals(Identified.UPDATE, found);
        protocol.setPassed(true);
        testProtocol.add(protocol);
    }

    /**
     * Identify probject by HP-Nr and MST.
     * @throws Exception that can occur during the test.
     */
    @Test
    @InSequence(T1)
    @UsingDataSet("datasets/dbUnit_probe_import.json")
    @Cleanup(phase = TestExecutionPhase.AFTER,
        strategy = CleanupStrategy.USED_TABLES_ONLY)
    public final void identifyProbeByHPNrMSTNew() throws Exception {
        Protocol protocol = new Protocol();
        protocol.setName("import");
        protocol.setType("identify probe");
        protocol.addInfo(
            "import",
            "Compare and find Sample by HP-Nr. and MST, New");

        Sample probe = new Sample();
        probe.setMainSampleId("120510003");
        probe.setMeasFacilId(mstId);

        Identified found = probeIdentifier.find(probe);
        Assert.assertEquals(Identified.NEW, found);
        protocol.setPassed(true);
        testProtocol.add(protocol);
    }

    /**
     * Identify probe object by external probe id.
     *
     * @throws Exception that can occur during the test.
     */
    @Test
    @InSequence(T2)
    @UsingDataSet("datasets/dbUnit_probe_import.json")
    @Cleanup(phase = TestExecutionPhase.AFTER,
        strategy = CleanupStrategy.USED_TABLES_ONLY)
    public final void identifyProbeByExterneProbeId() throws Exception {
        Protocol protocol = new Protocol();
        protocol.setName("import");
        protocol.setType("identify probe");
        protocol.addInfo(
            "import",
            "Compare and find Sample by extId, Update");

        Sample probe = new Sample();
        probe.setExtId("T001");

        Identified found = probeIdentifier.find(probe);
        Assert.assertEquals(Identified.UPDATE, found);
        protocol.setPassed(true);
        testProtocol.add(protocol);
    }

    /**
     * Identify probe object by external id as new.
     * @throws Exception that can occur during test.
     */
    @Test
    @InSequence(T3)
    @UsingDataSet("datasets/dbUnit_probe_import.json")
    @Cleanup(phase = TestExecutionPhase.AFTER,
        strategy = CleanupStrategy.USED_TABLES_ONLY)
    public final void identifyProbeByExterneProbeIdNew() throws Exception {
        Protocol protocol = new Protocol();
        protocol.setName("import");
        protocol.setType("identify probe");
        protocol.addInfo(
            "import",
            "Compare and find Sample by extId, New");

        Sample probe = new Sample();
        probe.setExtId("T002");

        Identified found = probeIdentifier.find(probe);
        Assert.assertEquals(Identified.NEW, found);
        protocol.setPassed(true);
        testProtocol.add(protocol);
    }

    /**
     * Identify probe object by external id as reject.
     * @throws Exception that can occur during the test.
     */
    @Test
    @InSequence(T4)
    @UsingDataSet("datasets/dbUnit_probe_import.json")
    @Cleanup(phase = TestExecutionPhase.AFTER,
        strategy = CleanupStrategy.USED_TABLES_ONLY)
    public final void identifyProbeByExterneProbeIdReject() throws Exception {
        Protocol protocol = new Protocol();
        protocol.setName("import");
        protocol.setType("identify probe");
        protocol.addInfo(
            "import",
            "Compare and find Sample by extId, Reject");

        Sample probe = new Sample();
        probe.setExtId("T001");
        probe.setMainSampleId("120510003");
        probe.setMeasFacilId(mstId);

        Identified found = probeIdentifier.find(probe);
        Assert.assertEquals(Identified.REJECT, found);
        protocol.setPassed(true);
        testProtocol.add(protocol);
    }

    /**
     * Identify probe object by external id as update.
     * @throws Exception that ca occur during the test.
     */
    @Test
    @InSequence(T5)
    @UsingDataSet("datasets/dbUnit_probe_import.json")
    @Cleanup(phase = TestExecutionPhase.AFTER,
        strategy = CleanupStrategy.USED_TABLES_ONLY)
    public final void identifyProbeByExterneProbeIdUpdate() throws Exception {
        Protocol protocol = new Protocol();
        protocol.setName("import");
        protocol.setType("identify probe");
        protocol.addInfo(
            "import",
            "Compare and find Sample by extId, Update");

        Sample probe = new Sample();
        probe.setExtId("T001");
        probe.setMainSampleId("");
        probe.setMeasFacilId(mstId);

        Identified found = probeIdentifier.find(probe);
        Assert.assertEquals(Identified.UPDATE, found);
        protocol.setPassed(true);
        testProtocol.add(protocol);
    }

    /**
     * Identify messung object by np nr.
     * @throws Exception that can occur during the test.
     */
    @Test
    @InSequence(T6)
    @UsingDataSet("datasets/dbUnit_messung_import.json")
    @Cleanup(phase = TestExecutionPhase.AFTER,
        strategy = CleanupStrategy.USED_TABLES_ONLY)
    public final void identifyMessungByNpNr() throws Exception {
        Protocol protocol = new Protocol();
        protocol.setName("import");
        protocol.setType("identify messung");
        protocol.addInfo(
            "import",
            "Compare and find Messung by NP-Nr., Update");

        Measm messung = new Measm();
        messung.setSampleId(PID1000);
        messung.setMinSampleId("06A0");

        Identified found = messungIdentifier.find(messung);
        Assert.assertEquals(Identified.UPDATE, found);
        protocol.setPassed(true);
        testProtocol.add(protocol);
    }

    /**
     * Identify messung object by np nr. as new.
     * @throws Exception that can occur during the test.
     */
    @Test
    @InSequence(T7)
    @UsingDataSet("datasets/dbUnit_messung_import.json")
    @Cleanup(phase = TestExecutionPhase.AFTER,
        strategy = CleanupStrategy.USED_TABLES_ONLY)
    public final void identifyMessungByNpNrNew() throws Exception {
        Protocol protocol = new Protocol();
        protocol.setName("import");
        protocol.setType("identify messung");
        protocol.addInfo("import", "Compare and find Messung by NP-Nr., New");

        Measm messung = new Measm();
        messung.setSampleId(PID1000);
        messung.setMinSampleId("06A1");

        Identified found = messungIdentifier.find(messung);
        Assert.assertEquals(Identified.NEW, found);
        protocol.setPassed(true);
        testProtocol.add(protocol);
    }

    /**
     * Identify messung object by external id.
     * @throws Exception that can occur during the test.
     */
    @Test
    @InSequence(T8)
    @UsingDataSet("datasets/dbUnit_messung_import.json")
    @Cleanup(phase = TestExecutionPhase.AFTER,
        strategy = CleanupStrategy.USED_TABLES_ONLY)
    public final void identifyMessungByExterneMessungsId() throws Exception {
        Protocol protocol = new Protocol();
        protocol.setName("import");
        protocol.setType("identify messung");
        protocol.addInfo(
            "import",
            "Compare and find Messung by externeMessungsId, Update");

        Measm messung = new Measm();
        messung.setSampleId(PID1000);
        messung.setExtId(1);

        Identified found = messungIdentifier.find(messung);
        Assert.assertEquals(Identified.UPDATE, found);
        protocol.setPassed(true);
        testProtocol.add(protocol);
    }

    /**
     * Identify messung object by external id as new.
     * @throws Exception that can occur during the test.
     */
    @Test
    @InSequence(T9)
    @UsingDataSet("datasets/dbUnit_messung_import.json")
    @Cleanup(phase = TestExecutionPhase.AFTER,
        strategy = CleanupStrategy.USED_TABLES_ONLY)
    public final void identifyMessungByExterneMessungsIdNew() throws Exception {
        Protocol protocol = new Protocol();
        protocol.setName("import");
        protocol.setType("identify messung");
        protocol.addInfo(
            "import",
            "Compare and find Messung by externeMessungsId, New");

        Measm messung = new Measm();
        messung.setSampleId(PID1000);
        messung.setExtId(2);

        Identified found = messungIdentifier.find(messung);
        Assert.assertEquals(Identified.NEW, found);
        protocol.setPassed(true);
        testProtocol.add(protocol);
    }

    /**
     * Identify messung object by external id for reject.
     * @throws Exception that can occur during the test.
     */
    @Test
    @Ignore
    @InSequence(T10)
    @Cleanup(phase = TestExecutionPhase.AFTER,
        strategy = CleanupStrategy.USED_TABLES_ONLY)
    public final void identifyMessungByExterneMessungsIdReject()
    throws Exception {
        Protocol protocol = new Protocol();
        protocol.setName("import");
        protocol.setType("identify messung");
        protocol.addInfo(
            "import",
            "Compare and find Messung by externeMessungsId, Reject");

        Measm messung = new Measm();
        messung.setSampleId(PID1000);
        messung.setExtId(1);
        messung.setMinSampleId("06A2");

        Identified found = messungIdentifier.find(messung);
        Assert.assertEquals(Identified.REJECT, found);
        protocol.setPassed(true);
        testProtocol.add(protocol);
    }

    /**
     * Identify messung object by external id as update.
     * @throws Exception that can occur during the test.
     */
    @Test
    @InSequence(T11)
    @UsingDataSet("datasets/dbUnit_messung_import.json")
    @Cleanup(phase = TestExecutionPhase.AFTER,
        strategy = CleanupStrategy.USED_TABLES_ONLY)
    public final void identifyMessungByExterneMessungsIdUpdate()
    throws Exception {
        Protocol protocol = new Protocol();
        protocol.setName("import");
        protocol.setType("identify messung");
        protocol.addInfo(
            "import",
            "Compare and find Messung by externeMessungsId, Update");

        Measm messung = new Measm();
        messung.setSampleId(PID1000);
        messung.setExtId(1);
        messung.setMinSampleId("");

        Identified found = messungIdentifier.find(messung);
        Assert.assertEquals(Identified.UPDATE, found);
        protocol.setPassed(true);
        testProtocol.add(protocol);
    }

    /**
     * Merge probe objects.
     * @throws Exception that can occur during the test.
     */
    @Test
    @InSequence(T12)
    @UsingDataSet("datasets/dbUnit_import_merge.json")
    @ShouldMatchDataSet(value = "datasets/dbUnit_import_merge_match.json",
        excludeColumns = {"last_mod", "tree_mod"})
    @Cleanup(phase = TestExecutionPhase.AFTER,
        strategy = CleanupStrategy.USED_TABLES_ONLY)
    public final void mergeProbe() throws Exception {
        Protocol protocol = new Protocol();
        protocol.setName("import");
        protocol.setType("merge probe");
        protocol.addInfo("import", "Merge objects");

        Sample probe = new Sample();
        probe.setExtId("T001");
        probe.setMainSampleId("120510002");
        probe.setMeasFacilId(mstId);
        probe.setOprModeId(1);
        probe.setRegulationId(DID9);
        probe.setEnvDescripName(
            "Trinkwasser Zentralversorgung Oberflächenwasser aufbereitet");
        probe.setEnvDescripDisplay("D: 59 04 01 00 05 05 01 02 00 00 00 00");
        probe.setMpgId(MPRID1000);
        probe.setSamplerId(PNID);
        probe.setIsTest(false);
        probe.setApprLabId(mstId);
        probe.setSampleMethId(2);
        probe.setEnvMediumId("A6");
        probe.setSchedStartDate(Timestamp.valueOf("2013-05-01 16:00:00"));
        probe.setSchedEndDate(Timestamp.valueOf("2013-05-05 16:00:00"));
        probe.setSampleStartDate(Timestamp.valueOf("2012-05-03 13:07:00"));
        Sample dbProbe = repository.getByIdPlain(Sample.class, PID1000);
        merger.merge(dbProbe, probe);

        protocol.setPassed(true);
        testProtocol.add(protocol);
    }

    /**
     * Merge messung objects.
     * @throws Exception that can occur during the test
     */
    @Test
    @InSequence(T13)
    @UsingDataSet("datasets/dbUnit_import_merge.json")
    @ShouldMatchDataSet(
        value = "datasets/dbUnit_import_merge_match_messung.json",
        excludeColumns = {"last_mod", "tree_mod"})
    @Cleanup(phase = TestExecutionPhase.AFTER,
        strategy = CleanupStrategy.USED_TABLES_ONLY)
    public final void mergeMessung() throws Exception {
        Protocol protocol = new Protocol();
        protocol.setName("import");
        protocol.setType("merge messung");
        protocol.addInfo("import", "Merge objects");

        Measm messung = new Measm();
        messung.setMinSampleId("06A0");
        messung.setIsScheduled(true);
        messung.setIsCompleted(false);
        messung.setMeasPd(MDAUER1000);
        messung.setMmtId("A3");
        messung.setMeasmStartDate(Timestamp.valueOf("2012-05-06 14:00:00"));
        Measm dbMessung =
            repository.getByIdPlain(Measm.class, MID1200);
        merger.mergeMessung(dbMessung, messung);

        protocol.setPassed(true);
        testProtocol.add(protocol);
    }

    // TODO Record order can get mixed up here which cause the test to fail as
    //       different records get compared to each other (e.g. A74 <-> A76)
    /**
     * Merge zusatzwert objects.
     * @throws Exception that can occur during the test.
     */
    @Test
    @Ignore
    @InSequence(T14)
    @UsingDataSet("datasets/dbUnit_import_merge.json")
    @ShouldMatchDataSet(
        value = "datasets/dbUnit_import_merge_match_zusatzwert.json",
        excludeColumns = {"id", "letzte_aenderung", "tree_modified"})
    @Cleanup(phase = TestExecutionPhase.AFTER,
        strategy = CleanupStrategy.USED_TABLES_ONLY)
    public final void mergeZusatzwert() throws Exception {
        Protocol protocol = new Protocol();
        protocol.setName("import");
        protocol.setType("merge zusatzwert");
        protocol.addInfo("import", "Merge objects");

        Sample probe = repository.getByIdPlain(Sample.class, PID1000);
        List<SampleSpecifMeasVal> zusatzwerte = new ArrayList<SampleSpecifMeasVal>();
        SampleSpecifMeasVal wert1 = new SampleSpecifMeasVal();
        wert1.setSampleId(PID1000);
        wert1.setError(MESSFEHLER12F);
        wert1.setSmallerThan("<");
        wert1.setSampleSpecifId("A74");

        SampleSpecifMeasVal wert2 = new SampleSpecifMeasVal();
        wert2.setSampleId(PID1000);
        wert2.setError(MESSFEHLER02F);
        wert2.setMeasVal(MESS18D);
        wert1.setSmallerThan(null);
        wert2.setSampleSpecifId("A75");

        SampleSpecifMeasVal wert3 = new SampleSpecifMeasVal();
        wert3.setSampleId(PID1000);
        wert3.setError(MESSFEHLER02F);
        wert3.setMeasVal(MESS18D);
        wert1.setSmallerThan(null);
        wert3.setSampleSpecifId("A76");

        zusatzwerte.add(wert1);
        zusatzwerte.add(wert2);
        zusatzwerte.add(wert3);
        merger.mergeZusatzwerte(probe, zusatzwerte);

        protocol.setPassed(true);
        testProtocol.add(protocol);
    }

    /**
     * Merge probekommentar object.
     * @throws Exception that can occur during the test
     */
    @Test
    @InSequence(T15)
    @UsingDataSet("datasets/dbUnit_import_merge.json")
    @ShouldMatchDataSet(
        value = "datasets/dbUnit_import_merge_match_kommentar.json",
        excludeColumns = {"id"})
    @Cleanup(phase = TestExecutionPhase.AFTER,
        strategy = CleanupStrategy.USED_TABLES_ONLY)
    public final void mergeProbeKommentar() throws Exception {
        Protocol protocol = new Protocol();
        protocol.setName("import");
        protocol.setType("merge probe kommentar");
        protocol.addInfo("import", "Merge objects");

        Sample probe = repository.getByIdPlain(Sample.class, PID1000);
        List<CommSample> kommentare = new ArrayList<CommSample>();
        CommSample komm1 = new CommSample();
        komm1.setSampleId(PID1000);
        komm1.setDate(Timestamp.valueOf("2012-05-08 12:00:00"));
        komm1.setMeasFacilId(mstId);
        komm1.setText("Testtext2");

        CommSample komm2 = new CommSample();
        komm2.setSampleId(PID1000);
        komm2.setDate(Timestamp.valueOf("2012-04-08 12:00:00"));
        komm2.setMeasFacilId(mstId);
        komm2.setText("Testtext3");

        kommentare.add(komm1);
        kommentare.add(komm2);

        merger.mergeKommentare(probe, kommentare);
        Assert.assertEquals(2, kommentare.size());

        protocol.setPassed(true);
        testProtocol.add(protocol);
    }

    /**
     * Merge messungkommentar object.
     * @throws Exception that can occur during the test.
     */
    @Test
    @InSequence(T16)
    @UsingDataSet("datasets/dbUnit_import_merge.json")
    @ShouldMatchDataSet(
        value = "datasets/dbUnit_import_merge_match_kommentarm.json",
        excludeColumns = {"id"})
    @Cleanup(phase = TestExecutionPhase.AFTER,
        strategy = CleanupStrategy.USED_TABLES_ONLY)
    public final void mergeMessungKommentar() throws Exception {
        Protocol protocol = new Protocol();
        protocol.setName("import");
        protocol.setType("merge messung kommentar");
        protocol.addInfo("import", "Merge objects");

        Measm messung =
            repository.getByIdPlain(Measm.class, MID1200);
        List<CommMeasm> kommentare = new ArrayList<CommMeasm>();
        CommMeasm komm1 = new CommMeasm();
        komm1.setMeasmId(MID1200);
        komm1.setDate(Timestamp.valueOf("2012-05-08 12:00:00"));
        komm1.setMeasFacilId(mstId);
        komm1.setText("Testtext2");

        CommMeasm komm2 = new CommMeasm();
        komm2.setMeasmId(MID1200);
        komm2.setDate(Timestamp.valueOf("2012-03-08 12:00:00"));
        komm2.setMeasFacilId(mstId);
        komm2.setText("Testtext3");

        kommentare.add(komm1);
        kommentare.add(komm2);

        merger.mergeMessungKommentare(messung, kommentare);
        Assert.assertEquals(2, kommentare.size());

        protocol.setPassed(true);
        testProtocol.add(protocol);
    }

    /**
     * Merge messwert objects.
     * @throws Exception that can occur during the test.
     */
    @Test
    @InSequence(T17)
    @UsingDataSet("datasets/dbUnit_import_merge.json")
    @ShouldMatchDataSet(
        value = "datasets/dbUnit_import_merge_match_messwert.json",
        excludeColumns = {"id"})
    @Cleanup(phase = TestExecutionPhase.AFTER,
        strategy = CleanupStrategy.USED_TABLES_ONLY)
    public final void mergeMesswerte() throws Exception {
        Protocol protocol = new Protocol();
        protocol.setName("import");
        protocol.setType("merge messwerte");
        protocol.addInfo("import", "Merge objects");

        Measm messung =
            repository.getByIdPlain(Measm.class, MID1200);
        List<MeasVal> messwerte = new ArrayList<MeasVal>();
        MeasVal wert1 = new MeasVal();
        wert1.setMeasmId(MID1200);
        wert1.setMeasUnitId(MEHID207);
        wert1.setMeasdId(MGID56);
        wert1.setMeasVal(MESS15D);
        messwerte.add(wert1);

        merger.mergeMesswerte(messung, messwerte);
        QueryBuilder<MeasVal> builder =
            repository.queryBuilder(MeasVal.class);
        builder.and("measmId", messung.getId());
        List<MeasVal> dbWerte =
            repository.filterPlain(builder.getQuery());
        Assert.assertEquals(1, dbWerte.size());

        protocol.setPassed(true);
        testProtocol.add(protocol);
    }

    /**
     * Preliminary data for testing LAF 8 import.
     * @throws Exception that can occur during the test.
     */
    @Test
    @InSequence(18)
    @UsingDataSet("datasets/dbUnit_import_conf.json")
    @Cleanup(phase = TestExecutionPhase.NONE)
    public final void prepareDatabaseImport() throws Exception {
        // Nothing to do.
    }

    /**
     * Test synchronous import of a Sample object.
     */
    @Test
    @InSequence(19)
    @RunAsClient
    public final void testImportProbe(
        @ArquillianResource URL baseUrl
    ) {
        Protocol prot = new Protocol();
        prot.setName("syncimport service");
        prot.setType("laf");
        prot.setPassed(false);
        testProtocol.add(prot);

        /* Request synchronous import */
        Response importResponse = client.target(
            baseUrl + "data/import/laf")
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .header("X-LADA-MST", mstId)
            .post(Entity.entity(laf, MediaType.TEXT_PLAIN));
        JsonObject importResponseObject = parseResponse(importResponse, prot);

        /* Check if a Sample object has been imported */
        assertContains(importResponseObject, dataKey);
        JsonObject data = importResponseObject.getJsonObject(dataKey);

        final String probeIdsKey = "probeIds";
        assertContains(data, probeIdsKey);
        Assert.assertEquals(1,
            data.getJsonArray(probeIdsKey).size());

        prot.setPassed(true);
    }

    /**
     * Test successful asynchronous import of a Sample object.
     */
    @Test
    @InSequence(19)
    @RunAsClient
    public final void testAsyncImportProbeSuccess(
        @ArquillianResource URL baseUrl
    ) throws InterruptedException, CharacterCodingException {
        Protocol prot = new Protocol();
        prot.setName("asyncimport service successful");
        testAsyncImportProbe(baseUrl, laf, true, prot);
    }

    /**
     * Test unsuccessful asynchronous import of a Probe object.
     */
    @Test
    @InSequence(19)
    @RunAsClient
    public final void testAsyncImportProbeNoSuccess(
        @ArquillianResource URL baseUrl
    ) throws InterruptedException, CharacterCodingException {
        Protocol prot = new Protocol();
        prot.setName("asyncimport service unsuccessful");
        testAsyncImportProbe(baseUrl, "no valid LAF", false, prot);
    }

    /**
     * Test asynchronous import of a Sample object with attribute conversion.
     */
    @Test
    @InSequence(19)
    @RunAsClient
    public final void testAsyncImportProbeImportConfConvert(
        @ArquillianResource URL baseUrl
    ) throws InterruptedException, CharacterCodingException {
        Protocol prot = new Protocol();
        prot.setName("asyncimport service import config");
        testAsyncImportProbe(
            baseUrl,
            String.format(
                lafTemplate, lafSampleId, "conv", sampleSpecifId,
                "", measd, measUnit),
            true,
            prot);
    }

    /**
     * Test asynchronous import with attribute transformation in MeasVal.
     */
    @Test
    @InSequence(19)
    @RunAsClient
    public final void testAsyncImportMeasValImportConfTransform(
        @ArquillianResource URL baseUrl
    ) throws InterruptedException, CharacterCodingException {
        Protocol prot = new Protocol();
        prot.setName("asyncimport service import config");
        testAsyncImportProbe(
            baseUrl,
            String.format(
                lafTemplate, lafSampleId, "conv", sampleSpecifId,
                "", "H 3", measUnit),
            true,
            prot);
    }

    /**
     * Test asynchronous import with attribute conversion
     * in SampleSpecifMeasVal.
     */
    @Test
    @InSequence(19)
    @RunAsClient
    public final void testAsyncImportSampleSpecifMeasValImportConfTransform(
        @ArquillianResource URL baseUrl
    ) throws InterruptedException, CharacterCodingException {
        Protocol prot = new Protocol();
        prot.setName("asyncimport service import config");
        testAsyncImportProbe(
            baseUrl,
            String.format(
                lafTemplate, lafSampleId, "conv", "XX",
                "", measd, measUnit),
            true,
            prot);
    }

    /**
     * Test "Zeitbasis" handling in LAF8 import.
     */
    @Test
    @InSequence(19)
    @RunAsClient
    public final void testZeitbasis(
        @ArquillianResource URL baseUrl
    ) throws InterruptedException, CharacterCodingException {
        Protocol prot = new Protocol();
        prot.setName("asyncimport zeitbasis");

        testZeitbasis(baseUrl, "ZEITBASIS", "\"MESZ\"", false, prot);
        testZeitbasis(baseUrl, "ZEITBASIS", "\"INVALID\"", true, prot);
        testZeitbasis(baseUrl, "ZEITBASIS_S", "1", false, prot);
        testZeitbasis(baseUrl, "ZEITBASIS_S", "0", true, prot);

        // Use default from import_conf
        testZeitbasis(baseUrl, "", "", false, prot);
    }

    private void testZeitbasis(
        URL baseUrl,
        String lafKey,
        String value,
        boolean expectWarning,
        Protocol prot
    ) throws InterruptedException, CharacterCodingException {
        // Add "ZEITBASIS" attribute to LAF string
        String lafZb = String.format(
            lafTemplate,
            lafSampleId,
            regulation,
            sampleSpecifId,
            lafKey + " " + value + "\n",
            measd,
            measUnit);
        LOG.trace(lafZb);

        JsonArray warnings = testAsyncImportProbe(baseUrl, lafZb, true, prot)
            .getJsonObject("warnings").getJsonArray(lafSampleId);
        LOG.trace(warnings);
        final String keyKey = "key";
        if (expectWarning) {
            JsonObject expectedWarning = Json.createObjectBuilder()
                .add(keyKey, lafKey)
                .add("value", value.replace("\"", ""))
                .add("code", StatusCodes.IMP_INVALID_VALUE).build();
            Assert.assertFalse(
                "Missing warning: " + expectedWarning.toString(),
                !warnings.contains(expectedWarning));
        } else {
            for (JsonValue warningVal: warnings) {
                JsonObject warning = (JsonObject) warningVal;
                Assert.assertFalse(
                    "Unexpected warning: " + warning.toString(),
                    warning.getString(keyKey).startsWith("ZEITBASIS"));
            }
        }
    }

    private JsonObject testAsyncImportProbe(
        URL baseUrl,
        String lafData,
        boolean expectSuccess,
        Protocol prot
    ) throws InterruptedException, CharacterCodingException {
        prot.setType("laf");
        prot.setPassed(false);
        testProtocol.add(prot);

        final String asyncImportUrl = baseUrl + "data/import/async/";
        final String fileName = "test.laf";

        /* Request asynchronous import */
        JsonObject requestJson = Json.createObjectBuilder()
            .add("encoding", "utf-8")
            .add("files", Json.createObjectBuilder()
                .add(fileName, Base64.getEncoder().encodeToString(
                        lafData.getBytes(StandardCharsets.UTF_8))))
            .build();

        Response importCreated = client.target(asyncImportUrl + "laf")
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .header("X-LADA-MST", mstId)
            .post(Entity.entity(requestJson.toString(),
                    MediaType.APPLICATION_JSON));
        JsonObject importCreatedObject = parseSimpleResponse(
            importCreated, prot);

        final String refIdKey = "refId";
        assertContains(importCreatedObject, refIdKey);
        String refId = importCreatedObject.getString(refIdKey);

        /* Request status of asynchronous import */
        SyncInvoker statusRequest = client.target(
            asyncImportUrl + "status/" + refId)
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles);
        JsonObject importStatusObject = Json.createObjectBuilder().build();
        boolean done = false;
        final Instant waitUntil = Instant.now().plus(Duration.ofMinutes(1));
        final int waitASecond = 1000;
        do {
            importStatusObject = parseSimpleResponse(statusRequest.get(), prot);

            final String doneKey = "done";
            assertContains(importStatusObject, doneKey);
            done = importStatusObject.getBoolean(doneKey);

            Assert.assertTrue(
                "Import not done within one minute",
                waitUntil.isAfter(Instant.now()));
            Thread.sleep(waitASecond);
        } while (!done);

        final String statusKey = "status";
        assertContains(importStatusObject, statusKey);
        Assert.assertEquals(
            Job.Status.FINISHED.name().toLowerCase(),
            importStatusObject.getString(statusKey));

        /* Fetch import result report */
        Response reportResponse = client.target(
            asyncImportUrl + "result/" + refId)
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .get();
        JsonObject report = parseSimpleResponse(reportResponse, prot);

        assertContains(report, fileName);
        JsonObject fileReport = report.getJsonObject(fileName);
        LOG.debug(fileReport);

        final String successKey = "success";
        assertContains(fileReport, successKey);
        boolean success = fileReport.getBoolean(successKey);
        final String sampleIdsKey = "probeIds";
        assertContains(fileReport, sampleIdsKey);
        if (!expectSuccess) {
            Assert.assertFalse(
                "Unexpectedly successful import: " + fileReport, success);
            prot.setPassed(true);
            return fileReport;
        }
        Assert.assertTrue(
            "Unsuccessful import: " + fileReport, success);

        // Test if data correctly entered database
        final int sampleId = fileReport.getJsonArray(sampleIdsKey)
            .getJsonNumber(0).intValue();
        Response importedSampleResponse = client.target(
            baseUrl + "rest/sample/" + sampleId)
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .get();
        JsonObject importedSample = parseResponse(
            importedSampleResponse, prot).getJsonObject(dataKey);
        Assert.assertEquals(lafSampleId, importedSample.getString("extId"));
        Assert.assertEquals(mstId, importedSample.getString("measFacilId"));
        Assert.assertEquals(1, importedSample.getInt("regulationId"));

        Response importedSampleSpecifMeasValResponse = client.target(
            baseUrl + "rest/samplespecifmeasval?sampleId=" + sampleId)
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .get();
        JsonObject importedSampleSpecifMeasVal =
            parseResponse(importedSampleSpecifMeasValResponse, prot)
            .getJsonArray(dataKey)
            .getJsonObject(0);
        Assert.assertEquals(
            sampleSpecifId,
            importedSampleSpecifMeasVal.getString("sampleSpecifId"));

        Response importedMeasmResponse = client.target(
            baseUrl + "rest/measm?sampleId=" + sampleId)
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .get();
        final int measmId = parseResponse(importedMeasmResponse, prot)
            .getJsonArray(dataKey)
            .getJsonObject(0)
            .getInt("id");

        Response importedMeasValResponse = client.target(
            baseUrl + "rest/measval?measmId=" + measmId)
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .get();
        JsonObject importedMeasVal =
            parseResponse(importedMeasValResponse, prot)
            .getJsonArray(dataKey)
            .getJsonObject(0);
        Assert.assertEquals(1, importedMeasVal.getInt("measdId"));
        Assert.assertEquals(1, importedMeasVal.getInt("measUnitId"));

        prot.setPassed(true);
        return fileReport;
    }
}
