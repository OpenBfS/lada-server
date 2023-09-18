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
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.UUID;

import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.UserTransaction;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.SyncInvoker;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
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
    private static final Integer DID9 = 9;

    @Resource UserTransaction transaction;

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
        + "TESTDATEN 0\n"
        + "MESSPROGRAMM_S 1\n"
        + "DATENBASIS \"%s\"\n"
        + "PZB_S \"%s\" 42 \"\" 5.0\n"
        + "%s"
        + "%%MESSUNG%%\n"
        + "MESSMETHODE_S \"A3\"\n"
        + "MESSWERT \"%s\" 72.177002 \"%s\" 4.4\n"
        + "%%ENDE%%\n";

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

    public ImporterTest() {
        testDatasetName = "datasets/dbUnit_import.xml";
    }

    /**
     * Identify probe objects.
     *
     * @throws Exception that can occur during the test.
     */
    @Test
    @Transactional
    public final void identifyProbeByHPNrMST() throws Exception {
        Sample probe = new Sample();
        probe.setMainSampleId("120510002");
        probe.setMeasFacilId(mstId);

        Identified found = probeIdentifier.find(probe);
        Assert.assertEquals(Identified.UPDATE, found);
    }

    /**
     * Identify probject by HP-Nr and MST.
     * @throws Exception that can occur during the test.
     */
    @Test
    @Transactional
    public final void identifyProbeByHPNrMSTNew() throws Exception {
        Sample probe = new Sample();
        probe.setMainSampleId("120510003");
        probe.setMeasFacilId(mstId);

        Identified found = probeIdentifier.find(probe);
        Assert.assertEquals(Identified.NEW, found);
    }

    /**
     * Identify probe object by external probe id.
     *
     * @throws Exception that can occur during the test.
     */
    @Test
    @Transactional
    public final void identifyProbeByExterneProbeId() throws Exception {
        Sample probe = new Sample();
        probe.setExtId("T001");

        Identified found = probeIdentifier.find(probe);
        Assert.assertEquals(Identified.UPDATE, found);
    }

    /**
     * Identify probe object by external id as new.
     * @throws Exception that can occur during test.
     */
    @Test
    @Transactional
    public final void identifyProbeByExterneProbeIdNew() throws Exception {
        Sample probe = new Sample();
        probe.setExtId("T002");

        Identified found = probeIdentifier.find(probe);
        Assert.assertEquals(Identified.NEW, found);
    }

    /**
     * Identify probe object by external id as reject.
     * @throws Exception that can occur during the test.
     */
    @Test
    @Transactional
    public final void identifyProbeByExterneProbeIdReject() throws Exception {
        Sample probe = new Sample();
        probe.setExtId("T001");
        probe.setMainSampleId("120510003");
        probe.setMeasFacilId(mstId);

        Identified found = probeIdentifier.find(probe);
        Assert.assertEquals(Identified.REJECT, found);
    }

    /**
     * Identify probe object by external id as update.
     * @throws Exception that ca occur during the test.
     */
    @Test
    @Transactional
    public final void identifyProbeByExterneProbeIdUpdate() throws Exception {
        Sample probe = new Sample();
        probe.setExtId("T001");
        probe.setMainSampleId("");
        probe.setMeasFacilId(mstId);

        Identified found = probeIdentifier.find(probe);
        Assert.assertEquals(Identified.UPDATE, found);
    }

    /**
     * Identify messung object by np nr.
     * @throws Exception that can occur during the test.
     */
    @Test
    @Transactional
    public final void identifyMessungByNpNr() throws Exception {
        Measm messung = new Measm();
        messung.setSampleId(PID1000);
        messung.setMinSampleId("06A0");

        Identified found = messungIdentifier.find(messung);
        Assert.assertEquals(Identified.UPDATE, found);
    }

    /**
     * Identify messung object by np nr. as new.
     * @throws Exception that can occur during the test.
     */
    @Test
    @Transactional
    public final void identifyMessungByNpNrNew() throws Exception {
        Measm messung = new Measm();
        messung.setSampleId(PID1000);
        messung.setMinSampleId("06A1");

        Identified found = messungIdentifier.find(messung);
        Assert.assertEquals(Identified.NEW, found);
    }

    /**
     * Identify messung object by external id.
     * @throws Exception that can occur during the test.
     */
    @Test
    @Transactional
    public final void identifyMessungByExterneMessungsId() throws Exception {
        Measm messung = new Measm();
        messung.setSampleId(PID1000);
        messung.setExtId(1);

        Identified found = messungIdentifier.find(messung);
        Assert.assertEquals(Identified.UPDATE, found);
    }

    /**
     * Identify messung object by external id as new.
     * @throws Exception that can occur during the test.
     */
    @Test
    @Transactional
    public final void identifyMessungByExterneMessungsIdNew() throws Exception {
        Measm messung = new Measm();
        messung.setSampleId(PID1000);
        messung.setExtId(2);

        Identified found = messungIdentifier.find(messung);
        Assert.assertEquals(Identified.NEW, found);
    }

    /**
     * Identify messung object by external id for reject.
     * @throws Exception that can occur during the test.
     */
    @Ignore
    @Test
    @Transactional
    //TODO: This unexpectedly returns an update instead of an reject
    public final void identifyMessungByExterneMessungsIdReject()
    throws Exception {
        Measm messung = new Measm();
        messung.setSampleId(PID1000);
        messung.setExtId(1);
        messung.setMinSampleId("06A2");

        Identified found = messungIdentifier.find(messung);
        Assert.assertEquals(Identified.REJECT, found);
    }

    /**
     * Identify messung object by external id as update.
     * @throws Exception that can occur during the test.
     */
    @Test
    @Transactional
    public final void identifyMessungByExterneMessungsIdUpdate()
    throws Exception {
        Measm messung = new Measm();
        messung.setSampleId(PID1000);
        messung.setExtId(1);
        messung.setMinSampleId("");

        Identified found = messungIdentifier.find(messung);
        Assert.assertEquals(Identified.UPDATE, found);
    }

    /**
     * Merge probe objects.
     * @throws Exception that can occur during the test.
     */
    @Test
    public final void mergeProbe() throws Exception {
        transaction.begin();
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
        transaction.commit();

        shouldMatchDataSet("datasets/dbUnit_import_merge_match.xml",
            "lada.sample", new String[]{"last_mod", "tree_mod", "mid_coll_pd"});
    }

    /**
     * Merge messung objects.
     * @throws Exception that can occur during the test
     */
    @Test
    public final void mergeMessung() throws Exception {
        transaction.begin();
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
        transaction.commit();

        shouldMatchDataSet(
            "datasets/dbUnit_import_merge_match_messung.xml",
            "lada.measm",
            new String[]{"status", "last_mod", "tree_mod"});
    }

    // TODO Record order can get mixed up here which cause the test to fail as
    //       different records get compared to each other (e.g. A74 <-> A76)
    /**
     * Merge zusatzwert objects.
     * @throws Exception that can occur during the test.
     */
    @Test
    @Ignore
    public final void mergeZusatzwert() throws Exception {
        transaction.begin();
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
        transaction.commit();

        shouldMatchDataSet(
            "datasets/dbUnit_import_merge_match_zusatzwert.xml",
            "lada.sample_specif_meas_val",
            new String[]{"id", "last_mod", "tree_mod"});
    }

    /**
     * Merge probekommentar object.
     * @throws Exception that can occur during the test
     */
    @Test
    public final void mergeProbeKommentar() throws Exception {
        transaction.begin();
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
        transaction.commit();

        shouldMatchDataSet(
            "datasets/dbUnit_import_merge_match_kommentar.xml",
            "lada.comm_sample",
            new String[]{"id"});
    }

    /**
     * Merge messungkommentar object.
     * @throws Exception that can occur during the test.
     */
    @Test
    public final void mergeMessungKommentar() throws Exception {
        transaction.begin();
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
        transaction.commit();

        shouldMatchDataSet(
            "datasets/dbUnit_import_merge_match_kommentarm.xml",
            "lada.comm_measm",
            new String[]{"id"});
    }

    /**
     * Merge messwert objects.
     * @throws Exception that can occur during the test.
     */
    @Test
    public final void mergeMesswerte() throws Exception {
        transaction.begin();
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
        transaction.commit();

        shouldMatchDataSet(
            "datasets/dbUnit_import_merge_match_messwert.xml",
            "lada.meas_val",
            new String[]{"id", "last_mod", "tree_mod"});
    }

    /**
     * Test synchronous import of a Sample object.
     */
    @Test
    @RunAsClient
    public final void testImportProbe(
        @ArquillianResource URL baseUrl
    ) {
        final String laf = String.format(
            lafTemplate, randomProbeId(),
            regulation, sampleSpecifId, "", measd, measUnit);

        /* Request synchronous import */
        Response importResponse = client.target(
            baseUrl + "data/import/laf")
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .header("X-LADA-MST", mstId)
            .post(Entity.entity(laf, MediaType.TEXT_PLAIN));
        JsonObject importResponseObject = parseResponse(importResponse);

        /* Check if a Sample object has been imported */
        assertContains(importResponseObject, dataKey);
        JsonObject data = importResponseObject.getJsonObject(dataKey);

        final String probeIdsKey = "probeIds";
        assertContains(data, probeIdsKey);
        Assert.assertEquals(1,
            data.getJsonArray(probeIdsKey).size());
    }

    /**
     * Test successful asynchronous import of a Sample object.
     */
    @Test
    @RunAsClient
    public final void testAsyncImportProbeSuccess(
        @ArquillianResource URL baseUrl
    ) throws InterruptedException, CharacterCodingException {
        final String lafSampleId = randomProbeId();
        final String laf = String.format(
            lafTemplate, lafSampleId,
            regulation, sampleSpecifId, "", measd, measUnit);
        testAsyncImportProbe(baseUrl, laf, lafSampleId, true);
    }

    /**
     * Test import with lowercase LAF keywords.
     */
    @Test
    @RunAsClient
    public final void testImportLowercaseKeywords(
        @ArquillianResource URL baseUrl
    ) throws InterruptedException, CharacterCodingException {
        final String lafSampleId = randomProbeId();
        final String lowerCaseLAF = String.format(
            lafTemplate, lafSampleId, regulation, sampleSpecifId,
            "", measd, measUnit).lines().map(line -> {
                    if (line.matches("^\\w+ .*")) {
                        String[] words = line.split(" ");
                        words[0] = words[0].toLowerCase();
                        return String.join(" ", words);
                    }
                    return line;
                }).collect(Collectors.joining("\n"));
        testAsyncImportProbe(baseUrl, lowerCaseLAF, lafSampleId, true);
    }

    /**
     * Test unsuccessful asynchronous import of a Probe object.
     */
    @Test
    @RunAsClient
    public final void testAsyncImportProbeNoSuccess(
        @ArquillianResource URL baseUrl
    ) throws InterruptedException, CharacterCodingException {
        testAsyncImportProbe(baseUrl, "no valid LAF", "", false);
    }

    /**
     * Test message localization in asynchronous import.
     */
    @Test
    @RunAsClient
    public final void testAsyncImportProbeI18n(
        @ArquillianResource URL baseUrl
    ) throws InterruptedException, CharacterCodingException {
        final String lafSampleId = randomProbeId();
        final String noOprModeLAF = String.format(
            lafTemplate, lafSampleId, regulation, sampleSpecifId,
            "", measd, measUnit).lines().filter(
                line -> !line.startsWith("MESSPROGRAMM")).collect(
                    Collectors.joining("\n"));

        Map<Locale, String> msgs = Map.of(
            Locale.GERMAN, "darf nicht null sein",
            Locale.US, "must not be null");
        final String errorsKey = "errors";
        for (Locale locale: msgs.keySet()) {
            JsonObject report = testAsyncImportProbe(
                baseUrl, locale, noOprModeLAF, "", false);
            assertContains(report, errorsKey);
            JsonArray errors = report.getJsonObject(errorsKey)
                .getJsonArray(lafSampleId);
            LOG.debug(errors);
            JsonObject expectedError = Json.createObjectBuilder()
                .add("key", "validation#probe")
                .add("value", "oprModeId")
                .add("code", msgs.get(locale)).build();
            Assert.assertTrue(
                "Missing error: " + expectedError.toString(),
                errors.contains(expectedError));
        }
    }

    /**
     * Test asynchronous import of a Sample object with attribute conversion.
     */
    @Test
    @RunAsClient
    public final void testAsyncImportProbeImportConfConvert(
        @ArquillianResource URL baseUrl
    ) throws InterruptedException, CharacterCodingException {
        final String lafSampleId = randomProbeId();
        testAsyncImportProbe(
            baseUrl,
            String.format(
                lafTemplate, lafSampleId, "conv", sampleSpecifId,
                "", measd, measUnit),
            lafSampleId,
            true);
    }

    /**
     * Test asynchronous import with attribute transformation in MeasVal.
     */
    @Test
    @RunAsClient
    public final void testAsyncImportMeasValImportConfTransform(
        @ArquillianResource URL baseUrl
    ) throws InterruptedException, CharacterCodingException {
        final String lafSampleId = randomProbeId();
        testAsyncImportProbe(
            baseUrl,
            String.format(
                lafTemplate, lafSampleId, "conv", sampleSpecifId,
                "", "H 3", measUnit),
            lafSampleId,
            true);
    }

    /**
     * Test asynchronous import with attribute conversion
     * in SampleSpecifMeasVal.
     */
    @Test
    @RunAsClient
    @Ignore
    public final void testAsyncImportSampleSpecifMeasValImportConfTransform(
        @ArquillianResource URL baseUrl
    ) throws InterruptedException, CharacterCodingException {
        final String lafSampleId = randomProbeId();
        testAsyncImportProbe(
            baseUrl,
            String.format(
                lafTemplate, lafSampleId, "conv", "XX",
                "", measd, measUnit),
            lafSampleId,
            true);
    }

    /**
     * Test "Zeitbasis" handling in LAF8 import.
     */
    @Test
    @RunAsClient
    public final void testZeitbasis(
        @ArquillianResource URL baseUrl
    ) throws InterruptedException, CharacterCodingException {
        testZeitbasis(baseUrl, "ZEITBASIS", "\"MESZ\"", false);
        testZeitbasis(baseUrl, "ZEITBASIS", "\"INVALID\"", true);
        testZeitbasis(baseUrl, "ZEITBASIS_S", "1", false);
        testZeitbasis(baseUrl, "ZEITBASIS_S", "0", true);

        // Use default from import_conf
        testZeitbasis(baseUrl, "", "", false);
    }

    private void testZeitbasis(
        URL baseUrl,
        String lafKey,
        String value,
        boolean expectWarning
    ) throws InterruptedException, CharacterCodingException {
        final String lafSampleId = randomProbeId();
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

        JsonArray warnings = testAsyncImportProbe(
            baseUrl, lafZb, lafSampleId, true)
            .getJsonObject("warnings").getJsonArray(lafSampleId);
        LOG.trace(warnings);
        final String keyKey = "key";
        if (expectWarning) {
            JsonObject expectedWarning = Json.createObjectBuilder()
                .add(keyKey, lafKey)
                .add("value", value.replace("\"", ""))
                .add("code", String.valueOf(StatusCodes.IMP_INVALID_VALUE))
                .build();
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
        String lafSampleId,
        boolean expectSuccess
    ) throws InterruptedException, CharacterCodingException {
        return testAsyncImportProbe(
            baseUrl, Locale.US, lafData, lafSampleId, expectSuccess);
    }

    private JsonObject testAsyncImportProbe(
        URL baseUrl,
        Locale locale,
        String lafData,
        String lafSampleId,
        boolean expectSuccess
    ) throws InterruptedException, CharacterCodingException {
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
            .acceptLanguage(locale)
            .post(Entity.entity(requestJson.toString(),
                    MediaType.APPLICATION_JSON));
        JsonObject importCreatedObject = parseSimpleResponse(importCreated);

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
            importStatusObject = parseSimpleResponse(statusRequest.get());

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
        JsonObject report = parseSimpleResponse(reportResponse);

        assertContains(report, fileName);
        JsonObject fileReport = report.getJsonObject(fileName);

        final String successKey = "success";
        assertContains(fileReport, successKey);
        boolean success = fileReport.getBoolean(successKey);
        final String sampleIdsKey = "probeIds";
        assertContains(fileReport, sampleIdsKey);
        if (!expectSuccess) {
            Assert.assertFalse(
                "Unexpectedly successful import: " + fileReport, success);
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
            importedSampleResponse).getJsonObject(dataKey);
        Assert.assertEquals(lafSampleId, importedSample.getString("extId"));
        Assert.assertEquals(mstId, importedSample.getString("measFacilId"));
        Assert.assertEquals(1, importedSample.getInt("regulationId"));

        Response importedSampleSpecifMeasValResponse = client.target(
            baseUrl + "rest/samplespecifmeasval?sampleId=" + sampleId)
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .get();
        JsonArray importedSampleSpecifMeasVals =
            parseResponse(importedSampleSpecifMeasValResponse)
            .getJsonArray(dataKey);
        Assert.assertEquals(1, importedSampleSpecifMeasVals.size());
        JsonObject importedSampleSpecifMeasVal =
            importedSampleSpecifMeasVals.getJsonObject(0);
        Assert.assertEquals(
            sampleSpecifId,
            importedSampleSpecifMeasVal.getString("sampleSpecifId"));

        Response importedMeasmResponse = client.target(
            baseUrl + "rest/measm?sampleId=" + sampleId)
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .get();
        final int measmId = parseResponse(importedMeasmResponse)
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
            parseResponse(importedMeasValResponse)
            .getJsonArray(dataKey)
            .getJsonObject(0);
        Assert.assertEquals(1, importedMeasVal.getInt("measdId"));
        Assert.assertEquals(1, importedMeasVal.getInt("measUnitId"));

        return fileReport;
    }

    private String randomProbeId() {
        final int probeIdLength = 16;
        return UUID.randomUUID().toString().substring(0, probeIdLength);
   }
}
