/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada;

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
import jakarta.transaction.UserTransaction;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.SyncInvoker;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.logging.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.intevation.lada.rest.AsyncLadaService.AsyncJobResponse;
import de.intevation.lada.data.requests.LafImportParameters;
import de.intevation.lada.importer.Identified;
import de.intevation.lada.importer.Identifier;
import de.intevation.lada.importer.ObjectMerger;
import de.intevation.lada.model.lada.MeasVal;
import de.intevation.lada.model.lada.MeasVal_;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.lada.SampleSpecifMeasVal;
import de.intevation.lada.util.data.Job;
import de.intevation.lada.util.data.Job.JobStatus;
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

    private static final String ASYNC_IMPORT_URL = "data/import/async/";

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
        + "PROBENAHME_DATUM_UHRZEIT_A 20120510 0900\n"
        + "DESKRIPTOREN \"010100000000000000000000\"\n"
        + "%s"
        + "%%MESSUNG%%\n"
        + "MESSMETHODE_S \"A3\"\n"
        + "MESSWERT \"%s\" 72.177002 \"%s\" 4.4\n"
        + "%s"
        + "%%ENDE%%\n";

    @Inject
    Identifier<Sample> probeIdentifier;

    @Inject
    Identifier<Measm> messungIdentifier;

    @Inject
    Repository repository;

    @Inject
    ObjectMerger merger;

    public ImporterTest() {
        testDatasetName = "datasets/dbUnit_import.xml";
    }

    /**
     * Cancel asynchronous jobs in order to allow database cleanup.
     */
    @After
    public void cancelJobs() {
        target.path(ASYNC_IMPORT_URL + "cancel")
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .get();
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
        Sample dbProbe = repository.getById(Sample.class, PID1000);
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
            repository.getById(Measm.class, MID1200);
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
        Sample probe = repository.getById(Sample.class, PID1000);
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
     * Merge messwert objects.
     * @throws Exception that can occur during the test.
     */
    @Test
    public final void mergeMesswerte() throws Exception {
        transaction.begin();
        Measm messung =
            repository.getById(Measm.class, MID1200);
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
        builder.and(MeasVal_.measmId, messung.getId());
        List<MeasVal> dbWerte =
            repository.filter(builder.getQuery());
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
    public final void testImportProbe() {
        final String laf = String.format(
            lafTemplate, randomProbeId(),
            regulation, sampleSpecifId, "", measd, measUnit, "");

        /* Request synchronous import */
        Response importResponse = target
            .path("data/import/laf")
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .header("X-LADA-MST", mstId)
            .post(Entity.entity(laf, MediaType.TEXT_PLAIN));
        JsonObject importResponseObject =
            parseResponse(importResponse).asJsonObject();

        /* Check if a Sample object has been imported */
        final String probeIdsKey = "probeIds";
        assertContains(importResponseObject, probeIdsKey);
        Assert.assertEquals(1,
            importResponseObject.getJsonArray(probeIdsKey).size());
    }

    /**
     * Test successful asynchronous import of a Sample object.
     */
    @Test
    @RunAsClient
    public final void testAsyncImportProbeSuccess()
        throws InterruptedException, CharacterCodingException {
        final String lafSampleId = randomProbeId();
        final String laf = String.format(
            lafTemplate, lafSampleId,
            regulation, sampleSpecifId, "", measd, measUnit, "");
        JsonObject report = testAsyncImportProbe(laf, lafSampleId, true);
        JsonObject expectedWarning = Json.createObjectBuilder()
            .add("key", "validation#probe")
            .add("value", "geolocats")
            .add("code", "A sampling location must be provided")
            .build();
        MatcherAssert.assertThat(
            report.getJsonObject("warnings").getJsonArray(lafSampleId),
            CoreMatchers.hasItem(expectedWarning));
    }

    /**
     * Test import with lowercase LAF keywords.
     */
    @Test
    @RunAsClient
    public final void testImportLowercaseKeywords()
        throws InterruptedException, CharacterCodingException {
        final String lafSampleId = randomProbeId();
        final String lowerCaseLAF = String.format(
            lafTemplate, lafSampleId, regulation, sampleSpecifId,
            "", measd, measUnit, "").lines().map(line -> {
                    if (line.matches("^\\w+ .*")) {
                        String[] words = line.split(" ");
                        words[0] = words[0].toLowerCase();
                        return String.join(" ", words);
                    }
                    return line;
                }).collect(Collectors.joining("\n"));
        testAsyncImportProbe(lowerCaseLAF, lafSampleId, true);
    }

    /**
     * Test unsuccessful asynchronous import of a Probe object.
     */
    @Test
    @RunAsClient
    public final void testAsyncImportProbeNoSuccess()
        throws InterruptedException, CharacterCodingException {
        testAsyncImportProbe("no valid LAF", "", false);
    }

    /**
     * Test message localization in asynchronous import.
     */
    @Test
    @RunAsClient
    public final void testAsyncImportProbeI18n()
        throws InterruptedException, CharacterCodingException {
        final String lafSampleId = randomProbeId();
        final String noOprModeLAF = String.format(
            lafTemplate, lafSampleId, regulation, sampleSpecifId,
            "", measd, measUnit, "").lines().filter(
                line -> !line.startsWith("MESSPROGRAMM")).collect(
                    Collectors.joining("\n"));

        Map<Locale, String> msgs = Map.of(
            Locale.GERMAN, "Wert nicht gesetzt",
            Locale.US, "No value provided");
        final String errorsKey = "errors";
        for (Locale locale: msgs.keySet()) {
            JsonObject report = testAsyncImportProbe(
                locale, noOprModeLAF, "", false);
            assertContains(report, errorsKey);
            JsonArray errors = report.getJsonObject(errorsKey)
                .getJsonArray(lafSampleId);
            JsonObject expectedError = Json.createObjectBuilder()
                .add("key", "validation#probe")
                .add("value", "oprModeId")
                .add("code", msgs.get(locale)).build();
            Assert.assertTrue(
                "Missing error " + expectedError.toString()
                + " in " + errors.toString(),
                errors.contains(expectedError));
        }
    }

    /**
     * Test asynchronous import of a Sample object with attribute conversion.
     */
    @Test
    @RunAsClient
    public final void testAsyncImportProbeImportConfConvert()
        throws InterruptedException, CharacterCodingException {
        final String lafSampleId = randomProbeId();
        testAsyncImportProbe(
            String.format(
                lafTemplate, lafSampleId, "conv", sampleSpecifId,
                "", measd, measUnit, ""),
            lafSampleId,
            true);
    }

    /**
     * Test asynchronous import with attribute transformation in MeasVal.
     */
    @Test
    @RunAsClient
    public final void testAsyncImportMeasValImportConfTransform()
        throws InterruptedException, CharacterCodingException {
        final String lafSampleId = randomProbeId();
        testAsyncImportProbe(
            String.format(
                lafTemplate, lafSampleId, "conv", sampleSpecifId,
                "", "H 3", measUnit, ""),
            lafSampleId,
            true);
    }

    /**
     * Test asynchronous import with attribute conversion
     * in SampleSpecifMeasVal.
     */
    @Test
    @RunAsClient
    public final void testAsyncImportSampleSpecifMeasValImportConfTransform()
        throws InterruptedException, CharacterCodingException {
        final String lafSampleId = randomProbeId();
        testAsyncImportProbe(
            String.format(
                lafTemplate, lafSampleId, "conv", "XX",
                "", measd, measUnit, ""),
            lafSampleId,
            true);
    }

    /**
     * Test asynchronous import including sampling location.
     * Ensure that no false warning about missing sampling location occurs.
     */
    @Test
    @RunAsClient
    public final void testAsyncImportSampleGeolocatE()
        throws InterruptedException, CharacterCodingException {
        final String lafSampleId = randomProbeId();
        testAsyncImportProbeNoWarnings(
            String.format(
                lafTemplate, lafSampleId,
                regulation, sampleSpecifId,
                "P_KOORDINATEN_S 04 \"7.1\" \"50.4\"\n",
                measd, measUnit, ""),
            lafSampleId,
            true);
    }

    /**
     * Test asynchronous import including status.
     */
    @Test
    @RunAsClient
    public final void testAsyncImportStatus()
        throws InterruptedException, CharacterCodingException {
        final String lafSampleId = randomProbeId();
        testAsyncImportProbeNoWarnings(
            String.format(
                lafTemplate, lafSampleId,
                regulation, sampleSpecifId,
                "P_KOORDINATEN_S 04 \"7.1\" \"50.4\"\n",
                measd, measUnit,
                "BEARBEITUNGSSTATUS 1000\n"),
            lafSampleId,
            true);
    }

    /**
     * Test "Zeitbasis" handling in LAF8 import.
     */
    @Test
    @RunAsClient
    public final void testZeitbasis()
        throws InterruptedException, CharacterCodingException {
        testZeitbasis("ZEITBASIS", "\"MESZ\"", false);
        testZeitbasis("ZEITBASIS", "\"INVALID\"", true);
        testZeitbasis("ZEITBASIS_S", "1", false);
        testZeitbasis("ZEITBASIS_S", "0", true);

        // Use default from import_conf
        testZeitbasis("", "", false);
    }

    private void testZeitbasis(
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
            measUnit,
            "");
        LOG.trace(lafZb);

        JsonArray warnings = testAsyncImportProbe(lafZb, lafSampleId, true)
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

    private JsonObject testAsyncImportProbeNoWarnings(
        String lafData,
        String lafSampleId,
        boolean expectSuccess
    ) throws InterruptedException, CharacterCodingException {
        JsonObject response = testAsyncImportProbe(
            lafData, lafSampleId, expectSuccess);
        final String warningsKey = "warnings";
        if (response.containsKey(warningsKey)) {
            Assert.fail("Unexpected warnings: "
                + response.getJsonObject(warningsKey)
                .getJsonArray(lafSampleId));
        }
        return response;
    }

    private JsonObject testAsyncImportProbe(
        String lafData,
        String lafSampleId,
        boolean expectSuccess
    ) throws InterruptedException, CharacterCodingException {
        return testAsyncImportProbe(
            Locale.US, lafData, lafSampleId, expectSuccess);
    }

    private JsonObject testAsyncImportProbe(
        Locale locale,
        String lafData,
        String lafSampleId,
        boolean expectSuccess
    ) throws InterruptedException, CharacterCodingException {
        final String fileName = "test.laf";

        /* Request asynchronous import */
        var requestJson = new LafImportParameters();
        requestJson.setEncoding(StandardCharsets.UTF_8);
        requestJson.setMeasFacilId(mstId);
        requestJson.setFiles(Map.of(fileName, Base64.getEncoder()
                .encodeToString(lafData.getBytes(StandardCharsets.UTF_8))));

        Response importCreated = target.path(ASYNC_IMPORT_URL + "laf")
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .acceptLanguage(locale)
            .post(Entity.entity(requestJson, MediaType.APPLICATION_JSON));
        String jobId = parseResponse(importCreated, AsyncJobResponse.class)
            .getJobId();

        /* Request status of asynchronous import */
        SyncInvoker statusRequest = target
            .path(ASYNC_IMPORT_URL + "status/" + jobId)
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles);
        JobStatus importStatusObject;
        boolean done = false;
        final Instant waitUntil = Instant.now().plus(Duration.ofMinutes(1));
        final int waitASecond = 1000;
        do {
            Response response = statusRequest.get();
            importStatusObject =
                parseResponse(response, JobStatus.class);
            done = importStatusObject.isDone();
            Assert.assertTrue(
                "Import not done within one minute",
                waitUntil.isAfter(Instant.now()));
            Thread.sleep(waitASecond);
        } while (!done);

        Assert.assertEquals(
            Job.Status.FINISHED.name(),
            importStatusObject.getStatus().name());

        /* Fetch import result report */
        Response reportResponse = target
            .path(ASYNC_IMPORT_URL + "result/" + jobId)
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .get();
        JsonObject report = parseResponse(reportResponse).asJsonObject();

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
        Response importedSampleResponse = target
            .path("rest/sample/" + sampleId)
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .get();
        Sample importedSample =
            parseResponse(importedSampleResponse, Sample.class);
        Assert.assertEquals(lafSampleId, importedSample.getExtId());
        Assert.assertEquals(mstId, importedSample.getMeasFacilId());
        Assert.assertEquals(1, (int) importedSample.getRegulationId());

        Response importedSampleSpecifMeasValResponse = target
            .path("rest/samplespecifmeasval")
            .queryParam("sampleId", sampleId)
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .get();
        List<SampleSpecifMeasVal> importedSampleSpecifMeasVals =
            parseResponse(importedSampleSpecifMeasValResponse,
                new GenericType<List<SampleSpecifMeasVal>>() { });
        Assert.assertEquals(1, importedSampleSpecifMeasVals.size());
        Assert.assertEquals(
            sampleSpecifId,
            importedSampleSpecifMeasVals.get(0).getSampleSpecifId());

        Response importedMeasmResponse = target
            .path("rest/measm")
            .queryParam("sampleId", sampleId)
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .get();
        final int measmId = parseResponse(
            importedMeasmResponse, new GenericType<List<Measm>>() { })
            .get(0).getId();

        Response importedMeasValResponse = target
            .path("rest/measval")
            .queryParam("measmId", measmId)
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .get();
        MeasVal importedMeasVal = parseResponse(
            importedMeasValResponse, new GenericType<List<MeasVal>>() { })
            .get(0);
        Assert.assertEquals(1, (int) importedMeasVal.getMeasdId());
        Assert.assertEquals(1, (int) importedMeasVal.getMeasUnitId());

        return fileReport;
    }

    private String randomProbeId() {
        final int probeIdLength = 16;
        return UUID.randomUUID().toString().substring(0, probeIdLength);
   }
}
