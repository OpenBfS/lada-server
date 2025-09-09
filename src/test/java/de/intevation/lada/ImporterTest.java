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
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.UUID;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.SyncInvoker;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.logging.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.intevation.lada.rest.TagService;
import de.intevation.lada.rest.AsyncLadaService.AsyncJobResponse;
import de.intevation.lada.data.AsyncImportService;
import de.intevation.lada.data.requests.LafImportParameters;
import de.intevation.lada.model.lada.MeasVal;
import de.intevation.lada.model.lada.MeasVal_;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Measm_;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.lada.SampleSpecifMeasVal;
import de.intevation.lada.model.master.Tag;
import de.intevation.lada.util.data.Job;
import de.intevation.lada.util.data.Job.JobStatus;
import de.intevation.lada.util.data.StatusCodes;


/**
 * Class to test the Lada-Importer.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@RunWith(Arquillian.class)
public class ImporterTest extends ClientBaseTest {

    private static final Logger LOG = Logger.getLogger(ImporterTest.class);

    private static final String ASYNC_IMPORT_URL = UriBuilder
        .fromResource(AsyncImportService.class).build().getPath() + "/";

    private static final int GENERATED_EXPIRATION_TIME = 584;

    private final String sampleIdsKey = "sampleIds";

    private final String existingMainSampleId = "120510002";
    private final String mstId = "06010";
    private final String regulation = "test";
    private final String sampleSpecifId = "A1";
    private final String minSampleId = "test";
    private final String measd = "H-3";
    private final String measUnit = "Bq/kgFM";
    private final String lafTemplate = "%%PROBE%%\n"
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
        + "NEBENPROBENNUMMER \"" + minSampleId + "\"\n"
        + "MESSMETHODE_S \"A3\"\n"
        + "MESSWERT \"%s\" 0 \"%s\" 4.4\n"
        + "%s"
        + "%%ENDE%%\n";

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
        assertContains(importResponseObject, sampleIdsKey);
        Assert.assertEquals(1,
            importResponseObject.getJsonArray(sampleIdsKey).size());
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

        JsonObject expectedNotification = Json.createObjectBuilder()
            .add("key", "validation#messwert")
            .add("value", MeasVal_.MEAS_VAL)
            .add("code", "must be greater than 0")
            .build();
        MatcherAssert.assertThat(
            report.getJsonObject("notifications").getJsonArray(lafSampleId),
            CoreMatchers.hasItem(expectedNotification));
    }

    /**
     * Assert that generated Tag has expected validity.
     */
    @Test
    @RunAsClient
    public final void tagValidity()
        throws InterruptedException, CharacterCodingException {
        final String lafSampleId = randomProbeId();
        final String laf = String.format(
            lafTemplate, lafSampleId,
            regulation, sampleSpecifId, "", measd, measUnit, "");
        JsonObject fileReport = testAsyncImportProbe(laf, lafSampleId, true);
        final int sampleId = fileReport.getJsonArray(sampleIdsKey)
            .getJsonNumber(0).intValue();

        // Assert that generated Tag has expected validity
        List<Tag> tags = target
            .path(UriBuilder.fromResource(TagService.class).build().getPath())
            .queryParam("sampleId", sampleId)
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .get(new GenericType<List<Tag>>() { });
        final String tagName = fileReport.getString("tag");
        MatcherAssert.assertThat(tags.stream().map(Tag::getName).toList(),
            CoreMatchers.hasItem(tagName));
        Tag tag = tags.stream().filter(t -> tagName.equals(t.getName()))
            .findFirst().get();
        Assert.assertEquals(GENERATED_EXPIRATION_TIME,
            /* +1 because until calculates complete units and the tag has
               been generated a few moments ago */
            1 + Instant.now().until(
                tag.getValUntil().toInstant(), ChronoUnit.DAYS));
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
     * Test asynchronous import updating sampling location.
     */
    @Test
    @RunAsClient
    public final void testAsyncImportUpdateGeolocatE()
        throws InterruptedException, CharacterCodingException {
        final String lafSampleId = existingMainSampleId;
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
     * Ensure measms are validated on creation.
     */
    @Test
    @RunAsClient
    public final void measmsValidatedOnCreate()
        throws InterruptedException, CharacterCodingException {
        final String lafSampleId = existingMainSampleId;
        JsonObject report = testAsyncImportProbe(
            String.format(
                lafTemplate, lafSampleId,
                regulation, sampleSpecifId, "",
                // New measm with extId should not be possible
                measd, measUnit,
                "MESSUNGS_ID 11\n"),
            lafSampleId,
            false);
        JsonObject expectedErr = Json.createObjectBuilder()
            .add("key", "validation#messung")
            .add("value", Measm_.EXT_ID + "#" + minSampleId)
            .add("code", "Field can only be set by System")
            .build();
        MatcherAssert.assertThat(
            report.getJsonObject("errors").getJsonArray(lafSampleId),
            CoreMatchers.hasItem(expectedErr));
    }

    /**
     * Ensure measms are validated on update.
     */
    @Test
    @RunAsClient
    public final void measmsValidatedOnUpdate()
        throws InterruptedException, CharacterCodingException {
        final String lafSampleId = existingMainSampleId;
        JsonObject report = testAsyncImportProbe(
            String.format(
                lafTemplate, lafSampleId,
                regulation, sampleSpecifId, "",
                // New Measm with minSampleId
                measd, measUnit,
                // Give existing Measm the same minSampleId
                "%MESSUNG%\n"
                + "MESSUNGS_ID 1\n"
                + "NEBENPROBENNUMMER \"" + minSampleId + "\"\n"),
            lafSampleId,
            false);
        JsonObject expectedErr = Json.createObjectBuilder()
            .add("key", "validation#messung")
            .add("value", Measm_.MIN_SAMPLE_ID + "#" + minSampleId)
            .add("code",
                "Non-unique value combination for [minSampleId, sampleId]")
            .build();
        MatcherAssert.assertThat(
            report.getJsonObject("errors").getJsonArray(lafSampleId),
            CoreMatchers.hasItem(expectedErr));
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
     * Test creating new sample with forbidden extId.
     */
    @Test
    @RunAsClient
    public final void asyncImportForbiddenExtId()
        throws InterruptedException, CharacterCodingException {
        final String lafSampleId = "ZDB123456789012Y";
        testAsyncImportProbe(
            String.format(
                lafTemplate, lafSampleId,
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
        testAsyncImportProbe(
            String.format(
                lafTemplate, lafSampleId,
                regulation, sampleSpecifId,
                "P_KOORDINATEN_S 04 \"7.1\" \"50.4\"\n",
                // Measurand does not match measuring method
                "Mangan", measUnit,
                "BEARBEITUNGSSTATUS 1000\n"),
            lafSampleId,
            false);
    }

    /**
     * Test import MeasVal using "MESSWERT_S".
     */
    @Test
    @RunAsClient
    public final void asyncImportMeasValS()
        throws InterruptedException, CharacterCodingException {
        final String lafSampleId = randomProbeId();
        testAsyncImportProbe(
            String.format(
                lafTemplate, lafSampleId,
                regulation, sampleSpecifId, "",
                measd, measUnit,
                "MESSWERT_S 1 42.0 1 0.42\n"),
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
        JsonObject warnings = response.getJsonObject(warningsKey);
        if (!warnings.isEmpty()) {
            Assert.fail("Unexpected warnings: "
                + warnings.getJsonArray(lafSampleId));
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
        Assert.assertEquals(lafSampleId, importedSample.getMainSampleId());
        Assert.assertFalse(importedSample.getIsTest());
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
            .stream().filter(m -> minSampleId.equals(m.getMinSampleId()))
            .findFirst().get().getId();

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
        Assert.assertEquals(measd, importedMeasVal.getMeasdId());
        Assert.assertEquals(1, (int) importedMeasVal.getMeasUnitId());

        return fileReport;
    }

    private String randomProbeId() {
        final int probeIdLength = 16;
        return UUID.randomUUID().toString().substring(0, probeIdLength);
   }
}
