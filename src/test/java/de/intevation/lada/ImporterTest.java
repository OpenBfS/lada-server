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
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.UUID;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.SyncInvoker;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.logging.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.intevation.lada.rest.AsyncLadaService.AsyncJobResponse;
import de.intevation.lada.data.requests.Laf8ImportParameters;
import de.intevation.lada.data.requests.Laf9ImportParameters;
import de.intevation.lada.data.requests.LafImportParameters;
import de.intevation.lada.model.lada.MeasVal;
import de.intevation.lada.model.lada.MeasVal_;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Measm_;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.lada.Sample_;
import de.intevation.lada.model.lada.SampleSpecifMeasVal;
import de.intevation.lada.model.lada.SampleSpecifMeasVal_;
import de.intevation.lada.util.data.Job;
import de.intevation.lada.util.data.Job.JobStatus;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.JSONBConfig;


/**
 * Class to test the Lada-Importer.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@RunWith(Arquillian.class)
public class ImporterTest extends BaseTest {

    private static final Logger LOG = Logger.getLogger(ImporterTest.class);

    private static final String ASYNC_IMPORT_URL = "data/import/async/";

    public static final String SAMPLE_IDS_KEY = "sampleIds";
    private static final String ERRORS_KEY = "errors";
    private static final String REPORT_ITEM_KEY_KEY = "key";
    private static final String REPORT_ITEM_VALUE_KEY = "value";
    private static final String REPORT_ITEM_CODE_KEY = "code";

    private static final Jsonb JSONB_SPARSE = JsonbBuilder.create(
        JSONBConfig.JSONB_CONFIG.withNullValues(false));

    private final String existingExtId = "T001";
    private final String existingMainSampleId = "120510002";
    private final String mstId = "06010";
    private final String regulation = "test";
    private final String sampleSpecifId = "A1";
    private final String mmtId = "A3";
    private final String measd = "H-3";
    private final String measUnit = "Bq/kgFM";
    private final String laf8Template = "%%PROBE%%\n"
        + "UEBERTRAGUNGSFORMAT 7\n"
        + "VERSION \"0084\"\n"
        + "HAUPTPROBENNUMMER \"%s\"\n"
        + "PROBENART \"E\"\n"
        + "TESTDATEN 0\n"
        + "MESSPROGRAMM_S 1\n"
        + "DATENBASIS \"%s\"\n"
        + "PZB_S \"%s\" 42 \"\" 5.0\n"
        + "PROBENAHME_DATUM_UHRZEIT_A 20120510 0900\n"
        + "DESKRIPTOREN \"010100000000000000000000\"\n"
        + "%s"
        + "%%MESSUNG%%\n"
        + "MESSMETHODE_S \"" + mmtId + "\"\n"
        + "MESSWERT \"%s\" 72.177002 \"%s\" 4.4\n"
        + "%s"
        + "%%ENDE%%\n";

    private Map<String, JsonValue> expectedAttrs = Map.of(
        Sample_.IS_TEST, JsonValue.FALSE,
        Sample_.MEAS_FACIL_ID, Json.createValue(mstId),
        Sample_.REGULATION_ID, Json.createValue(1),
        Sample_.SAMPLE_SPECIF_MEAS_VALS, Json.createArrayBuilder()
        .add(Json.createObjectBuilder()
            .add(SampleSpecifMeasVal_.SAMPLE_SPECIF_ID, sampleSpecifId))
        .build(),
        Sample_.MEASMS, Json.createArrayBuilder()
        .add(Json.createObjectBuilder()
            .add(Measm_.MMT_ID, mmtId)
            .add(Measm_.MEAS_VALS, Json.createArrayBuilder()
                .add(Json.createObjectBuilder()
                    .add(MeasVal_.MEASD_ID, 1)
                    .add(MeasVal_.MEAS_UNIT_ID, 1))))
        .build()
    );

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
     * Test synchronous import of a Sample object.
     */
    @Test
    @RunAsClient
    public final void testImportProbe() {
        final String laf = String.format(
            laf8Template, randomProbeId(),
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
     * Test successful asynchronous LAF8 import of a Sample object.
     */
    @Test
    @RunAsClient
    public final void testAsyncLaf8ImportSuccess()
        throws InterruptedException, CharacterCodingException {
        final String lafSampleId = randomProbeId();
        final String laf = String.format(
            laf8Template, lafSampleId,
            regulation, sampleSpecifId, "", measd, measUnit, "");
        Map<String, JsonValue> verify = new HashMap<>(expectedAttrs);
        verify.put(Sample_.MAIN_SAMPLE_ID, Json.createValue(lafSampleId));
        JsonObject report = testAsyncLaf8Import(laf, lafSampleId, true, verify);
        JsonObject expectedWarning = Json.createObjectBuilder()
            .add(REPORT_ITEM_KEY_KEY, "validation#probe")
            .add(REPORT_ITEM_VALUE_KEY, "geolocats")
            .add(REPORT_ITEM_CODE_KEY, "A sampling location must be provided")
            .build();
        MatcherAssert.assertThat(
            report.getJsonObject("warnings").getJsonArray(lafSampleId),
            CoreMatchers.hasItem(expectedWarning));
    }

    /**
     * Test successful asynchronous LAF9 import of a Sample object.
     */
    @Test
    @RunAsClient
    public final void testAsyncLaf9ImportSuccess()
        throws InterruptedException, CharacterCodingException {
        final String lafSampleId = randomProbeId();
        Sample laf = prepareLaf9Data();
        laf.setMainSampleId(lafSampleId);
        JsonObject verify = JSONBConfig.JSONB.fromJson(
            JSONB_SPARSE.toJson(laf), JsonObject.class);
        testAsyncLaf9Import(laf, lafSampleId, true, verify);
        // TODO: Check for expected warning
        // JsonObject expectedWarning = Json.createObjectBuilder()
        //     .add(REPORT_ITEM_KEY_KEY, "validation#probe")
        //     .add(REPORT_ITEM_VALUE_KEY, "geolocats")
        //     .add(REPORT_ITEM_CODE_KEY, "A sampling location must be provided")
        //     .build();
        // MatcherAssert.assertThat(
        //     report.getJsonObject("warnings").getJsonArray(lafSampleId),
        //     CoreMatchers.hasItem(expectedWarning));
    }

    /**
     * Test successful asynchronous LAF8 update import of a Sample object.
     */
    @Test
    @RunAsClient
    public final void testAsyncLaf8UpdateImport()
        throws InterruptedException, CharacterCodingException {
        final String laf = String.format(
            laf8Template, existingMainSampleId,
            "test2", sampleSpecifId, "", measd, measUnit, "");

        Map<String, JsonValue> verify = new HashMap<>(expectedAttrs);
        verify.put(
            Sample_.MAIN_SAMPLE_ID, Json.createValue(existingMainSampleId));
        verify.put(Sample_.REGULATION_ID, Json.createValue(2));

        testAsyncLaf8Import(laf, existingMainSampleId, true, verify);
    }

    /**
     * Test successful asynchronous LAF9 update import of a Sample object.
     */
    @Test
    @RunAsClient
    public final void testAsyncLaf9UpdateImport()
        throws InterruptedException, CharacterCodingException {
        Sample laf = prepareLaf9Data();
        laf.setMainSampleId(existingMainSampleId);
        final int updateRegId = 2;
        laf.setRegulationId(updateRegId);

        Map<String, JsonValue> verify = new HashMap<>(expectedAttrs);
        verify.put(
            Sample_.MAIN_SAMPLE_ID, Json.createValue(existingMainSampleId));
        verify.put(Sample_.REGULATION_ID, Json.createValue(updateRegId));

        testAsyncLaf9Import(laf, existingMainSampleId, true, true, verify);
    }

    /**
     * Test successful asynchronous LAF9 update import of Measm objects.
     */
    @Test
    @RunAsClient
    public final void testAsyncLaf9UpdateMeasmImport()
        throws InterruptedException, CharacterCodingException {
        Sample laf = new Sample();
        laf.setExtId(existingExtId);

        final int existingMeasmExtId = 453;
        Measm measmUpdate = new Measm();
        measmUpdate.setExtId(existingMeasmExtId);
        final String minSampleId = "test";
        measmUpdate.setMinSampleId(minSampleId);

        Measm measmNew = new Measm();
        measmNew.setMmtId("I3");

        laf.setMeasms(List.of(measmUpdate, measmNew));

        JsonObject verify = JSONBConfig.JSONB.fromJson(
            JSONB_SPARSE.toJson(laf).toString(), JsonObject.class);
        testAsyncLaf9Import(laf, existingMainSampleId, true, true, verify);
    }

    /**
     * Test failing sample identification with LAF8.
     */
    @Test
    @RunAsClient
    public final void testAsyncLaf8IdentifyFail()
        throws InterruptedException, CharacterCodingException {
        final String newMainSampleId = "XXX";
        final String laf = String.format(
            laf8Template, newMainSampleId,
            "test", sampleSpecifId,
            "PROBE_ID \"" + existingExtId + "\"\n", measd, measUnit, "");

        JsonObject report = testAsyncLaf8Import(laf, newMainSampleId, false);
        JsonObject expectedError = Json.createObjectBuilder()
            .add(REPORT_ITEM_KEY_KEY, "duplicate")
            .add(REPORT_ITEM_VALUE_KEY, "")
            .add(REPORT_ITEM_CODE_KEY, String.valueOf(StatusCodes.IMP_PRESENT))
            .build();
        MatcherAssert.assertThat(
            report.getJsonObject(ERRORS_KEY).getJsonArray(existingExtId),
            CoreMatchers.hasItem(expectedError));
    }

    /**
     * Test failing sample identification with LAF9.
     */
    @Test
    @RunAsClient
    public final void testAsyncLaf9IdentifyFail()
        throws InterruptedException, CharacterCodingException {
        Sample laf = prepareLaf9Data();
        laf.setExtId(existingExtId);
        final String newMainSampleId = "XXX";
        laf.setMainSampleId("XXX");

        JsonObject report = testAsyncLaf9Import(
            laf, newMainSampleId, false, expectedAttrs);
        JsonObject expectedError = Json.createObjectBuilder()
            .add(REPORT_ITEM_KEY_KEY, Sample_.EXT_ID)
            .add(REPORT_ITEM_VALUE_KEY, existingExtId)
            .add(REPORT_ITEM_CODE_KEY,
                String.valueOf(StatusCodes.IMP_INVALID_VALUE))
            .build();
        MatcherAssert.assertThat(
            report.getJsonObject(ERRORS_KEY).getJsonArray(existingExtId),
            CoreMatchers.hasItem(expectedError));
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
            laf8Template, lafSampleId, regulation, sampleSpecifId,
            "", measd, measUnit, "").lines().map(line -> {
                    if (line.matches("^\\w+ .*")) {
                        String[] words = line.split(" ");
                        words[0] = words[0].toLowerCase();
                        return String.join(" ", words);
                    }
                    return line;
                }).collect(Collectors.joining("\n"));
        testAsyncLaf8Import(lowerCaseLAF, lafSampleId, true);
    }

    /**
     * Test asynchronous LAF8 import with invalid input.
     */
    @Test
    @RunAsClient
    public final void testAsyncLaf8ImportNoSuccess()
        throws InterruptedException, CharacterCodingException {
        testAsyncLaf8Import("no valid LAF", "", false);
    }

    /**
     * Test asynchronous LAF9 import with invalid input.
     */
    @Test
    @RunAsClient
    public final void testAsyncLaf9ImportNoSuccess()
        throws InterruptedException, CharacterCodingException {
        // Only Arrays of non-null objects should be accepted as list of samples
        final JsonValue jsonString = Json.createValue("test");
        final JsonValue jsonNumber = Json.createValue(1);
        List<JsonValue> invalidLists = List.of(
            JsonValue.NULL,
            jsonString,
            jsonNumber,
            JsonValue.TRUE,
            JsonValue.EMPTY_JSON_OBJECT,
            Json.createArrayBuilder().add(JsonValue.NULL).build()
            /* JSON binding accepts any JSON value as object. See
               https://github.com/eclipse-ee4j/yasson/issues/672
            Json.createArrayBuilder().add(jsonString).build(),
            Json.createArrayBuilder().add(jsonNumber).build(),
            Json.createArrayBuilder().add(JsonValue.TRUE).build(),
            Json.createArrayBuilder().add(JsonValue.EMPTY_JSON_ARRAY).build()
            */
            );
        for (JsonValue invalidList : invalidLists) {
            JsonValue payload = Json.createObjectBuilder()
                .add("files", Json.createObjectBuilder()
                    .add("invalidFile", invalidList))
                .add("measFacilId", mstId)
                .build();
            parseResponse(target
                .path(ASYNC_IMPORT_URL)
                .path("laf9")
                .request()
                .header("X-SHIB-user", BaseTest.testUser)
                .header("X-SHIB-roles", BaseTest.testRoles)
                .post(Entity.entity(payload, MediaType.APPLICATION_JSON)),
                Response.Status.BAD_REQUEST);
        }
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
            laf8Template, lafSampleId, regulation, sampleSpecifId,
            "", measd, measUnit, "").lines().filter(
                line -> !line.startsWith("MESSPROGRAMM")).collect(
                    Collectors.joining("\n"));

        Map<Locale, String> msgs = Map.of(
            Locale.GERMAN, "Wert nicht gesetzt",
            Locale.US, "No value provided");
        for (Locale locale: msgs.keySet()) {
            JsonObject report = testAsyncLaf8Import(
                locale, noOprModeLAF, "", false);
            assertContains(report, ERRORS_KEY);
            JsonArray errors = report.getJsonObject(ERRORS_KEY)
                .getJsonArray(lafSampleId);
            JsonObject expectedError = Json.createObjectBuilder()
                .add(REPORT_ITEM_KEY_KEY, "validation#probe")
                .add(REPORT_ITEM_VALUE_KEY, "oprModeId")
                .add(REPORT_ITEM_CODE_KEY, msgs.get(locale)).build();
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
        testAsyncLaf8Import(
            String.format(
                laf8Template, lafSampleId, "conv", sampleSpecifId,
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
        testAsyncLaf8Import(
            String.format(
                laf8Template, lafSampleId, "conv", sampleSpecifId,
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
        testAsyncLaf8Import(
            String.format(
                laf8Template, lafSampleId, "conv", "XX",
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
                laf8Template, lafSampleId,
                regulation, sampleSpecifId,
                "P_KOORDINATEN_S 04 \"7.1\" \"50.4\"\n",
                measd, measUnit, ""),
            lafSampleId,
            true);
    }

    /**
     * Test asynchronous import updating sampling location.
     */
    @Test
    @RunAsClient
    public final void testAsyncImportUpdateGeolocatE()
        throws InterruptedException, CharacterCodingException {
        final String lafSampleId = existingMainSampleId;
        testAsyncImportProbeNoWarnings(
            String.format(
                laf8Template, lafSampleId,
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
                laf8Template, lafSampleId,
                regulation, sampleSpecifId,
                "P_KOORDINATEN_S 04 \"7.1\" \"50.4\"\n",
                measd, measUnit,
                "BEARBEITUNGSSTATUS 1000\n"),
            lafSampleId,
            true);
    }

    /**
     * Test creating new sample with forbidden extId.
     */
    @Test
    @RunAsClient
    public final void asyncImportForbiddenExtId()
        throws InterruptedException, CharacterCodingException {
        final String lafSampleId = "ZDB123456789012Y";
        testAsyncLaf8Import(
            String.format(
                laf8Template, lafSampleId,
                regulation, sampleSpecifId,
                "PROBE_ID \"" + lafSampleId + "\"\nP_KOORDINATEN_S 04 \"7.1\" \"50.4\"\n",
                measd, measUnit,
                "BEARBEITUNGSSTATUS 1000\n"),
            lafSampleId,
            false);
    }

    /**
     * Ensure setting status with warning in measVal fails.
     */
    @Test
    @RunAsClient
    public final void asyncImportStatusWarningMeasVal()
        throws InterruptedException, CharacterCodingException {
        final String lafSampleId = randomProbeId();
        testAsyncLaf8Import(
            String.format(
                laf8Template, lafSampleId,
                regulation, sampleSpecifId,
                "P_KOORDINATEN_S 04 \"7.1\" \"50.4\"\n",
                // Measurand does not match measuring method
                "Mangan", measUnit,
                "BEARBEITUNGSSTATUS 1000\n"),
            lafSampleId,
            false);
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
            laf8Template,
            lafSampleId,
            regulation,
            sampleSpecifId,
            lafKey + " " + value + "\n",
            measd,
            measUnit,
            "");
        LOG.trace(lafZb);

        JsonArray warnings = testAsyncLaf8Import(lafZb, lafSampleId, true)
            .getJsonObject("warnings").getJsonArray(lafSampleId);
        LOG.trace(warnings);
        if (expectWarning) {
            JsonObject expectedWarning = Json.createObjectBuilder()
                .add(REPORT_ITEM_KEY_KEY, lafKey)
                .add(REPORT_ITEM_VALUE_KEY, value.replace("\"", ""))
                .add(REPORT_ITEM_CODE_KEY, String.valueOf(
                        StatusCodes.IMP_INVALID_VALUE))
                .build();
            Assert.assertFalse(
                "Missing warning: " + expectedWarning.toString(),
                !warnings.contains(expectedWarning));
        } else {
            for (JsonValue warningVal: warnings) {
                JsonObject warning = (JsonObject) warningVal;
                Assert.assertFalse(
                    "Unexpected warning: " + warning.toString(),
                    warning.getString(REPORT_ITEM_KEY_KEY)
                    .startsWith("ZEITBASIS"));
            }
        }
    }

    private JsonObject testAsyncImportProbeNoWarnings(
        String lafData,
        String lafSampleId,
        boolean expectSuccess
    ) throws InterruptedException, CharacterCodingException {
        JsonObject response = testAsyncLaf8Import(
            lafData, lafSampleId, expectSuccess);
        final String warningsKey = "warnings";
        JsonObject warnings = response.getJsonObject(warningsKey);
        if (!warnings.isEmpty()) {
            Assert.fail("Unexpected warnings: "
                + warnings.getJsonArray(lafSampleId));
        }
        return response;
    }

    private JsonObject testAsyncLaf8Import(
        String lafData,
        String lafSampleId,
        boolean expectSuccess
    ) throws InterruptedException, CharacterCodingException {
        return testAsyncLaf8Import(
            Locale.US, lafData, lafSampleId, expectSuccess, expectedAttrs);
    }

    private JsonObject testAsyncLaf8Import(
        String lafData,
        String lafSampleId,
        boolean expectSuccess,
        Map<String, JsonValue> verify
    ) throws InterruptedException, CharacterCodingException {
        return testAsyncLaf8Import(
            Locale.US, lafData, lafSampleId, expectSuccess, verify);
    }

    private JsonObject testAsyncLaf8Import(
        Locale locale,
        String lafData,
        String lafSampleId,
        boolean expectSuccess
    ) throws InterruptedException, CharacterCodingException {
        return testAsyncLaf8Import(
            locale, lafData, lafSampleId, expectSuccess, expectedAttrs);
    }

    private JsonObject testAsyncLaf8Import(
        Locale locale,
        String lafData,
        String lafSampleId,
        boolean expectSuccess,
        Map<String, JsonValue> verify
    ) throws InterruptedException, CharacterCodingException {
        final String fileName = "test.laf";

        /* Request asynchronous import */
        var requestJson = new Laf8ImportParameters();
        requestJson.setEncoding(StandardCharsets.UTF_8);
        requestJson.setMeasFacilId(mstId);
        requestJson.setFiles(Map.of(fileName, Base64.getEncoder()
                .encodeToString(lafData.getBytes(StandardCharsets.UTF_8))));

        JsonObject fileReport = runAsyncImport(
            target, "laf", locale, requestJson, expectSuccess)
            .getJsonObject(fileName);
        if (!expectSuccess) {
            return fileReport;
        }
        return checkImportedData(lafSampleId, fileReport, verify);
    }

    private JsonObject testAsyncLaf9Import(
        Sample lafData,
        String lafSampleId,
        boolean expectSuccess,
        Map<String, JsonValue> verify
    ) throws InterruptedException, CharacterCodingException {
        return testAsyncLaf9Import(
            lafData, lafSampleId, expectSuccess, false, verify);
    }

    private JsonObject testAsyncLaf9Import(
        Sample lafData,
        String lafSampleId,
        boolean expectSuccess,
        boolean sparse,
        Map<String, JsonValue> verify
    ) throws InterruptedException, CharacterCodingException {
        final String fileName = "test.json";

        Jsonb jsonb = sparse ? JSONB_SPARSE : JSONBConfig.JSONB;

        /* Request asynchronous import */
        var requestJson = new Laf9ImportParameters();
        requestJson.setMeasFacilId(mstId);
        requestJson.setFiles(Map.of(
                fileName,
                List.of(jsonb.fromJson(
                        jsonb.toJson(lafData), JsonObject.class))));

        JsonObject fileReport = runAsyncImport(
            target, "laf9", Locale.getDefault(), requestJson, expectSuccess)
            .getJsonObject(fileName);
        if (!expectSuccess) {
            return fileReport;
        }
        return checkImportedData(lafSampleId, fileReport, verify);
    }

    private JsonObject checkImportedData(
        String lafSampleId,
        JsonObject fileReport,
        Map<String, JsonValue> verify
    ) {
        // Test if data correctly entered database
        final int sampleId = fileReport.getJsonArray(SAMPLE_IDS_KEY)
            .getJsonNumber(0).intValue();
        Response importedSampleResponse = target
            .path("rest/sample/" + sampleId)
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .get();
        JsonObject importedSample =
            parseResponse(importedSampleResponse, JsonObject.class);
        BaseTest.verify(verify, importedSample, "owner");

        return fileReport;
    }

    private String randomProbeId() {
        final int probeIdLength = 16;
        return UUID.randomUUID().toString().substring(0, probeIdLength);
   }

    public static JsonObject runAsyncImport(
        WebTarget target,
        String path,
        Locale locale,
        LafImportParameters<?> requestJson,
        boolean expectSuccess
    ) throws InterruptedException {
        Response importCreated = target.path(ASYNC_IMPORT_URL)
            .path(path)
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

        for (String fileName: requestJson.getFiles().keySet()) {
            assertContains(report, fileName);
            JsonObject fileReport = report.getJsonObject(fileName);

            final String successKey = "success";
            assertContains(fileReport, successKey);
            boolean success = fileReport.getBoolean(successKey);
            assertContains(fileReport, SAMPLE_IDS_KEY);
            if (!expectSuccess) {
                Assert.assertFalse(
                    "Unexpectedly successful import: " + fileReport, success);
                return report;
            }
            Assert.assertTrue(
                "Unsuccessful import: " + fileReport, success);
        }
        return report;
    }

    private Sample prepareLaf9Data() {
        Sample laf9Template = new Sample();
        laf9Template.setMeasFacilId(mstId);
        laf9Template.setApprLabId(mstId);
        laf9Template.setOprModeId(1);
        laf9Template.setRegulationId(1);
        laf9Template.setSampleMethId(1);
        laf9Template.setIsTest(false);

        SampleSpecifMeasVal sampleSpecif = new SampleSpecifMeasVal();
        sampleSpecif.setSampleSpecifId(sampleSpecifId);
        laf9Template.setSampleSpecifMeasVals(Set.of(sampleSpecif));

        Measm measm = new Measm();
        measm.setMmtId(mmtId);
        MeasVal measVal = new MeasVal();
        measVal.setMeasdId(1);
        measVal.setMeasUnitId(1);
        measm.setMeasVals(Set.of(measVal));
        laf9Template.setMeasms(List.of(measm));

        return laf9Template;
    }
}
