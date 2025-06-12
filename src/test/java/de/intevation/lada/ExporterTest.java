/* Copyright (C) 2021 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada;

import java.io.StringReader;
import java.nio.charset.CharacterCodingException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.SyncInvoker;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.intevation.lada.data.AsyncExportService;
import de.intevation.lada.data.JsonExportService;
import de.intevation.lada.data.LafExportService;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Measm_;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.master.MeasFacil;
import de.intevation.lada.rest.AsyncLadaService.AsyncJobResponse;
import de.intevation.lada.util.data.Job;
import de.intevation.lada.util.data.Job.JobStatus;


/**
 * Test export services.
 *
 */
@RunWith(Arquillian.class)
public class ExporterTest extends BaseTest {

    private static final String ASYNC_EXPORT_URL =
        UriBuilder.fromResource(AsyncExportService.class).build() + "/";

    private final String formatCsv = "csv";
    private final String formatJson = "json";
    private final String formatLaf = "laf";

    @PersistenceContext
    EntityManager em;

    public ExporterTest() {
        this.testDatasetName = "datasets/dbUnit_query.xml";
    }

    private JsonObjectBuilder requestJsonBuilder = Json.createObjectBuilder()
        .add("exportSubData", false)
        .add("timezone", "UTC")
        .add("columns", Json.createArrayBuilder()
            .add(Json.createObjectBuilder()
                .add("columnIndex", 0)
                .add("export", true)
                .add("filterVal", "")
                .add("isFilterActive", false)
                .add("isFilterNull", false)
                .add("isFilterNegate", false)
                .add("isFilterRegex", false)
                .add("gridColMpId", 1)
                .add("queryUserId", 1))
            .add(Json.createObjectBuilder()
                .add("columnIndex", 1)
                .add("export", true)
                .add("filterVal", "")
                .add("isFilterActive", false)
                .add("isFilterNull", false)
                .add("isFilterNegate", false)
                .add("isFilterRegex", false)
                .add("gridColMpId", 2)
                .add("queryUserId", 1))
            .add(Json.createObjectBuilder()
                .add("columnIndex", 2)
                .add("export", true)
                .add("filterVal", "")
                .add("isFilterActive", false)
                .add("isFilterNull", false)
                .add("isFilterNegate", false)
                .add("isFilterRegex", false)
                .add("gridColMpId", 4)
                .add("queryUserId", 1))
            .add(Json.createObjectBuilder()
                .add("columnIndex", 3)
                .add("export", true)
                .add("filterVal", "")
                .add("isFilterActive", false)
                .add("isFilterNull", false)
                .add("isFilterNegate", false)
                .add("isFilterRegex", false)
                .add("gridColMpId", 5)
                .add("queryUserId", 1)));

    private final JsonObjectBuilder measmRequestJsonBuilder = Json
        .createObjectBuilder()
        .add("timezone", "UTC")
        .add("columns", Json.createArrayBuilder()
            .add(Json.createObjectBuilder()
                .add("columnIndex", 0)
                .add("export", true)
                .add("filterVal", "")
                .add("isFilterActive", false)
                .add("isFilterNull", false)
                .add("isFilterNegate", false)
                .add("isFilterRegex", false)
                .add("gridColMpId", 6)
                .add("queryUserId", 1)))
        .add("idField", "messungId")
        .add("idFilter", Json.createArrayBuilder().add("1200"))
        .add("exportSubData", true)
        .add("subDataColumns", Json.createArrayBuilder()
            .add("id")
            .add("measVal")
            .add("measUnitId")
            .add("measdId"));

    /**
     * Cancel asynchronous jobs in order to allow database cleanup.
     */
    @After
    public void cancelJobs() {
        target.path(ASYNC_EXPORT_URL + "cancel")
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .get();
    }

    /**
     * Test JSON export of a Sample identified by ID.
     */
    @Test
    @RunAsClient
    public final void testJsonExportProbeById()
        throws InterruptedException, CharacterCodingException {
        final int probeId = 1000;
        JsonArray result = target
            .path(UriBuilder.fromResource(JsonExportService.class)
                .path(JsonExportService.class, "downloadSamples")
                .build().toString())
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .post(Entity.entity(List.of(probeId), MediaType.APPLICATION_JSON),
                JsonArray.class);
        Assert.assertEquals("Unexpected JSON content", 1, result.size());
        assertJsonExportAsExpected(probeId, result, false);
    }

    /**
     * Test JSON export of a Sample identified by ID.
     */
    @Test
    @RunAsClient
    public final void testJsonExportMeasmById()
        throws InterruptedException, CharacterCodingException {
        final int measmId = 1200;
        JsonArray result = target
            .path(UriBuilder.fromResource(JsonExportService.class)
                .path(JsonExportService.class, "downloadMeasms")
                .build().toString())
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .post(Entity.entity(List.of(measmId), MediaType.APPLICATION_JSON),
                JsonArray.class);
        Assert.assertEquals("Unexpected JSON content", 1, result.size());
        assertJsonExportAsExpected(measmId, result, true);
    }

    private void assertJsonExportAsExpected(
        int id, JsonArray result, boolean isMeasmExport
    ) {
        Integer sampleId = isMeasmExport
            ? BaseTest.filterJsonArrayById(
                BaseTest.readXmlResource(this.testDatasetName, Measm.class),
                id).getInt(Measm_.SAMPLE_ID)
            : id;

        // Returned JSON represents expected sample
        JsonObject expectedSample = BaseTest.filterJsonArrayById(
            BaseTest.readXmlResource(this.testDatasetName, Sample.class),
            sampleId);
        JsonObject jsonSample = result.getJsonObject(0);
        BaseTest.verify(expectedSample, jsonSample);

        // Check for additional attributes expected in JSON export
        Map<String, String> extraAttrs = Map.ofEntries(
            Map.entry("sampleMethExtId", "E"),
            Map.entry("regulation", "nine"),
            Map.entry("envMediumName", "Spurenmessung Luft"),
            Map.entry("oprModeName", "Normal-/Routinebetrieb"),
            Map.entry("mpgCategExtId", "MC1"),
            Map.entry("mpgCategName", "mpgCateg1"),
            Map.entry("samplerExtId", "prn"),
            Map.entry("samplerDescr", "test"),
            Map.entry("samplerNetworkId", "06"),
            Map.entry("samplerShortText", "test"),
            Map.entry("reiAgGrDescr", "REI Ag Gr"),
            Map.entry("reiAgGrName", "reiAgGr")
        );
        for (String key : extraAttrs.keySet()) {
            assertContains(jsonSample, key);
            Assert.assertEquals(extraAttrs.get(key), jsonSample.getString(key));
        }

        final String[] measFacilAttrs = { "measFacil", "apprLab" };
        JsonObject measFacil = BaseTest.readXmlResource(
            this.testDatasetName, MeasFacil.class).getJsonObject(0);
        for (String key : measFacilAttrs) {
            assertContains(jsonSample, key);
            BaseTest.verify(measFacil, jsonSample.getJsonObject(key));
        }

        final String measFacilName = measFacil.getString("name");
        assertHasAssociated(jsonSample, "commSamples", 1, Map.of(
                measFacilAttrs[0], measFacilName));

        assertHasAssociated(jsonSample, "sampleSpecifMeasVals", 1, Map.of(
                "sampleSpecifName", "Volumenstrom",
                "unit", "Sv"));

        final String measmsKey = "measms";
        final int measmId = isMeasmExport ? id : 1200;
        // Sample export contains all measms, Measm export only the selected one
        final int measmsSize = isMeasmExport ? 1 : 2;
        assertHasAssociated(jsonSample, measmsKey, measmsSize, measmId, Map.of(
                "mmt", "mmt A3"));
        JsonObject measm = BaseTest.filterJsonArrayById(
            jsonSample.getJsonArray(measmsKey), measmId);
        final int measValId = 1000;
        assertHasAssociated(measm, "measVals", 2, measValId, Map.of(
                "unit", "Sv",
                "measd", "test"));
        assertHasAssociated(measm, "commMeasms", 1, Map.of(
                measFacilAttrs[0], measFacilName));
        assertHasAssociated(measm, "statusProtocol", 1, Map.of(
                "statusLev", "MST",
                "statusVal", "nicht vergeben",
                measFacilAttrs[0], measFacilName));

        final String envDescripKey = "envDescrip";
        assertContains(jsonSample, envDescripKey);
        JsonObject envDescrip = jsonSample.getJsonObject(envDescripKey);
        final String s0Key = "S0";
        assertContains(envDescrip, s0Key);
        Assert.assertEquals("test", envDescrip.getString(s0Key));

        final String geolocatsKey = "geolocat";
        assertHasAssociated(jsonSample, geolocatsKey, 1, Map.of());
        JsonObject geolocat = jsonSample.getJsonArray(geolocatsKey)
            .getJsonObject(0);
        final String siteKey = "site";
        assertContains(geolocat, siteKey);
        JsonObject site = geolocat.getJsonObject(siteKey);
        Map<String, String> siteAttrs = Map.of(
            "adminUnit", "Berlin",
            "state", "Deutschland"
        );
        for (String key : siteAttrs.keySet()) {
            assertContains(site, key);
            Assert.assertEquals(siteAttrs.get(key), site.getString(key));
        }
    }

    private void assertHasAssociated(
        JsonObject jsonSample,
        String associationKey,
        int size,
        Map<String, String> expectedAttrs
    ) {
        assertHasAssociated(
            jsonSample, associationKey, size, null, expectedAttrs);
    }

    private void assertHasAssociated(
        JsonObject jsonObject,
        String associationKey,
        int size,
        Integer id,
        Map<String, String> expectedAttrs
    ) {
        assertContains(jsonObject, associationKey);
        JsonArray associations = jsonObject.getJsonArray(associationKey);
        Assert.assertEquals(size, associations.size());
        JsonObject associated = id == null
            ? associations.getJsonObject(0)
            : BaseTest.filterJsonArrayById(associations, id);
        for (String key : expectedAttrs.keySet()) {
            assertContains(associated, key);
            Assert.assertEquals(
                expectedAttrs.get(key), associated.getString(key));
        }
    }

    /**
     * Test LAF export of a Sample identified by ID.
     */
    @Test
    @RunAsClient
    public final void testLafExportProbeById()
        throws InterruptedException, CharacterCodingException {
        final int probeId = 1000;
        JsonObject requestJson = requestJsonBuilder
            .add("proben", Json.createArrayBuilder().add(probeId))
            .build();

        String result = target
            .path(UriBuilder.fromResource(LafExportService.class)
                .path(LafExportService.class, "download").build().toString())
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .post(Entity.entity(requestJson, MediaType.APPLICATION_JSON),
                String.class);
        Assert.assertTrue(
            "Unexpected LAF content",
            result.startsWith("%PROBE%") && result.endsWith("%ENDE%"));
    }

    /**
     * Test asynchronous CSV export of a Sample object.
     */
    @Test
    @RunAsClient
    public final void testCsvExportProbe()
        throws InterruptedException, CharacterCodingException {
        /* Request asynchronous export */
        JsonObject requestJson = requestJsonBuilder
            .add("idField", JsonValue.NULL)
            .build();

        assertHasLinesInAnyOrder(
            runExportTest(formatCsv, requestJson),
            "\r\n",
            "hauptprobenNr,umwId,isTest,probeId",
            "120510002,L6,No,1000",
            "\"12051,0001\",L6,Yes,1001");
    }

    /**
     * Test asynchronous CSV export using CSV options.
     */
    @Test
    @RunAsClient
    public final void testCsvExportFieldSeparator()
        throws InterruptedException, CharacterCodingException {
        /* Request asynchronous export */
        JsonObject requestJson = requestJsonBuilder
            .add("idField", JsonValue.NULL)
            .add("fieldSeparator", ";")
            .build();

        assertHasLinesInAnyOrder(
            runExportTest(formatCsv, requestJson),
            "\r\n",
            "hauptprobenNr;umwId;isTest;probeId",
            "120510002;L6;No;1000",
            "12051,0001;L6;Yes;1001");
    }

    /**
     * Test asynchronous CSV export using CSV options.
     */
    @Test
    @RunAsClient
    public final void testCsvExportRowDelimiter()
        throws InterruptedException, CharacterCodingException {
        final String rowDelimiter = "\n";
        JsonObject requestJson = requestJsonBuilder
            .add("idField", JsonValue.NULL)
            .add("rowDelimiter", rowDelimiter)
            .build();

        assertHasLinesInAnyOrder(
            runExportTest(formatCsv, requestJson),
            rowDelimiter,
            "hauptprobenNr,umwId,isTest,probeId",
            "120510002,L6,No,1000",
            "\"12051,0001\",L6,Yes,1001");
    }

    /**
     * Test asynchronous CSV export using CSV options.
     */
    @Test
    @RunAsClient
    public final void testCsvExportQuote()
        throws InterruptedException, CharacterCodingException {
        /* Request asynchronous export */
        JsonObject requestJson = requestJsonBuilder
            .add("idField", JsonValue.NULL)
            .add("quote", "'")
            .build();

        assertHasLinesInAnyOrder(
            runExportTest(formatCsv, requestJson),
            "\r\n",
            "hauptprobenNr,umwId,isTest,probeId",
            "120510002,L6,No,1000",
            "'12051,0001',L6,Yes,1001");
    }

    /**
     * Test asynchronous CSV export using CSV options.
     */
    @Test
    @RunAsClient
    public final void testCsvExportDecimalSeparator()
        throws InterruptedException, CharacterCodingException {
        assertHasLinesInAnyOrder(
            runExportTest(formatCsv, measmRequestJsonBuilder
                .add("decimalSeparator", ",").build()),
            "\r\n",
            "messungId,id,measVal,measUnitId,measdId",
            "1200,1000,\"1,1E00\",Sv,test",
            "1200,1001,,Sv,test1");
    }

    /**
     * Test asynchronous CSV export of a Sample identified by ID.
     */
    @Test
    @RunAsClient
    public final void testCsvExportProbeById()
        throws InterruptedException, CharacterCodingException {
        JsonObject requestJson = requestJsonBuilder
            .add("idField", "main_sample_id")
            .add("idFilter", Json.createArrayBuilder().add("120510002"))
            .build();

        assertHasLinesInAnyOrder(
            runExportTest(formatCsv, requestJson),
            "\r\n",
            "hauptprobenNr,umwId,isTest,probeId",
            "120510002,L6,No,1000");
    }

    /**
     * Test asynchronous CSV export of Sample objects including measms.
     */
    @Test
    @RunAsClient
    public final void testCsvExportProbeSubData()
        throws InterruptedException, CharacterCodingException {
        JsonObject requestJson = requestJsonBuilder
            .add("idField", "probeId")
            .add("exportSubData", true)
            .add("subDataColumns", Json.createArrayBuilder()
                .add("extId")
                .add("messwerteCount"))
            .build();

        assertHasLinesInAnyOrder(
            runExportTest(formatCsv, requestJson),
            "\r\n",
            "hauptprobenNr,umwId,isTest,probeId,extId,messwerteCount",
            "120510002,L6,No,1000,453,2",
            "120510002,L6,No,1000,454,0",
            "\"12051,0001\",L6,Yes,1001,,");
    }

    /**
     * Test asynchronous CSV export of Measm objects including measVals.
     */
    @Test
    @RunAsClient
    public final void testCsvExportMeasmSubData()
        throws InterruptedException, CharacterCodingException {
        assertHasLinesInAnyOrder(
            runExportTest(
                formatCsv, measmRequestJsonBuilder.build()),
            "\r\n",
            "messungId,id,measVal,measUnitId,measdId",
            "1200,1000,1.1E00,Sv,test",
            "1200,1001,,Sv,test1");
    }

    /**
     * Test asynchronous JSON export of a Sample identified by ID.
     */
    @Test
    @RunAsClient
    public final void testAsyncJsonExportProbeById()
        throws InterruptedException, CharacterCodingException {
        JsonObject requestJson = requestJsonBuilder
            .add("idField", "main_sample_id")
            .add("idFilter", Json.createArrayBuilder().add("120510002"))
            .build();

        Assert.assertEquals(
            "Unexpected JSON content",
            Json.createReader(new StringReader("{\"120510002\":"
                    + "{\"main_sample_id\":\"120510002\","
                    + "\"env_medium_id\":\"L6\","
                    + "\"is_test\":\"false\","
                    + "\"probeId\":1000}}")).readObject(),
            runJSONExportTest(requestJson));
    }

    /**
     * Test asynchronous JSON export of a Sample object with measms.
     */
    @Test
    @RunAsClient
    public final void testJsonExportProbeSubData()
        throws InterruptedException, CharacterCodingException {
        /* Request asynchronous export */
        JsonObject requestJson = requestJsonBuilder
            .add("idField", "probeId")
            .add("idFilter", Json.createArrayBuilder().add("1000"))
            .add("exportSubData", true)
            .add("subDataColumns", Json.createArrayBuilder()
                .add("extId")
                .add("messwerteCount"))
            .build();

        Assert.assertEquals(
            "Unexpected JSON content",
            Json.createReader(new StringReader(
                    "{\"1000\":"
                    + "{\"main_sample_id\":\"120510002\","
                    + "\"env_medium_id\":\"L6\","
                    + "\"is_test\":\"false\","
                    + "\"probeId\":1000,"
                    + "\"Messungen\":[{\"messwerteCount\":2,\"extId\":453},"
                    + "{\"messwerteCount\":0,\"extId\":454}]}}")).readObject(),
            runJSONExportTest(requestJson));
    }

    /**
     * Test asynchronous JSON export of a Measm object with measVals.
     */
    @Test
    @RunAsClient
    public final void testJsonExportMeasmSubData()
        throws InterruptedException, CharacterCodingException {
        Assert.assertEquals(
            "Unexpected JSON content",
            Json.createReader(new StringReader("{\"1200\":"
                    + "{\"messungId\":1200,"
                    + "\"messwerte\":[{"
                    + "\"measUnitId\":\"Sv\","
                    + "\"measdId\":\"test\","
                    + "\"measVal\":1.1,"
                    + "\"id\":1000},{"
                    + "\"measUnitId\":\"Sv\","
                    + "\"measdId\":\"test1\","
                    + "\"measVal\":null,"
                    + "\"id\":1001}]}}")).readObject(),
            runJSONExportTest(measmRequestJsonBuilder.build()));
    }

    /**
     * Test asynchronous LAF export of a Sample identified by ID.
     */
    @Test
    @RunAsClient
    public final void testAsyncLafExportProbeById()
        throws InterruptedException, CharacterCodingException {
        final int probeId = 1000;
        JsonObject requestJson = requestJsonBuilder
            .add("proben", Json.createArrayBuilder().add(probeId))
            .build();

        String result = runExportTest(formatLaf, requestJson);
        Assert.assertTrue(
            "Unexpected LAF content",
            result.startsWith("%PROBE%") && result.endsWith("%ENDE%"));
    }

    /**
     * Test asynchronous export of an empty query result.
     */
    @Test
    @RunAsClient
    public final void testQueryExportEmpty()
        throws InterruptedException, CharacterCodingException {
        /* Request asynchronous export */
        JsonObject requestJson = requestJsonBuilder
            .add("idField", "main_sample_id")
            .add("idFilter", Json.createArrayBuilder().add("nonexistent"))
            .build();

        String csvResult = runExportTest(
            formatCsv, requestJson);
        Assert.assertEquals(
            "Unexpected CSV content",
            "hauptprobenNr,umwId,isTest,probeId\r\n",
            csvResult);

        Assert.assertEquals(
            "Unexpected JSON content",
            JsonValue.EMPTY_JSON_OBJECT,
            runJSONExportTest(requestJson));
    }

    /**
     * Test failing asynchronous export with invalid request payload.
     */
    @Test
    @RunAsClient
    public final void testAsyncExportFailure()
        throws InterruptedException, CharacterCodingException {
        /* Test values */
        String sampleValue = "99999";
        String samplePath = "createLafExportJob.arg0.proben[0].<list element>";
        String sampleMessage = String.format(
            "'%s' is no valid primary key", sampleValue);

        /* Request asynchronous export */
        JsonObject lafJson = Json.createObjectBuilder()
            .add("proben", Json.createArrayBuilder().add(sampleValue))
            .build();

        assertJsonContainsValidationMessage(
            parseResponse(
                exportRequest(formatLaf, lafJson),
                Response.Status.BAD_REQUEST)
                .asJsonObject(),
            samplePath, sampleMessage);
    }

    /**
     * Test failing asynchronous export with invalid encoding.
     */
    @Test
    @RunAsClient
    public final void testAsyncExportInvalidCharset() {
        JsonObject jsonExportJson = Json.createObjectBuilder()
            .add("encoding", "invalidEncoding")
            .build();
        parseResponse(
            exportRequest(formatJson, jsonExportJson),
            Response.Status.BAD_REQUEST);
    }

    private Response exportRequest(
            String format, JsonObject requestJson) {
        Response response = target
            .path(ASYNC_EXPORT_URL + format)
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .header("Accept", MediaType.APPLICATION_JSON)
            .post(Entity.entity(requestJson.toString(),
                    MediaType.APPLICATION_JSON));
        return response;
    }

    private String startExport(
        String format,
        JsonObject requestJson,
        Job.Status expectedStatus
    ) throws InterruptedException {
        Response exportCreated = exportRequest(format, requestJson);
        AsyncJobResponse asyncJobResponse =
            parseResponse(exportCreated, AsyncJobResponse.class);
        String jobId = asyncJobResponse
            .getJobId();

        /* Request status of asynchronous export */
        SyncInvoker statusRequest = target
            .path(ASYNC_EXPORT_URL + "status/" + jobId)
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles);
        boolean done = false;
        final Instant waitUntil = Instant.now().plus(Duration.ofMinutes(1));
        final int waitASecond = 1000;
        JobStatus exportStatusObject;
        do {
            Response response = statusRequest.get();
            exportStatusObject = parseResponse(response, JobStatus.class);
            done = exportStatusObject.isDone();

            Assert.assertTrue(
                "Export not done within one minute",
                waitUntil.isAfter(Instant.now()));
            Thread.sleep(waitASecond);
        } while (!done);

        Assert.assertEquals(
            expectedStatus.name(),
            exportStatusObject.getStatus().name());

        return jobId;
    }

    private JsonObject runJSONExportTest(
        JsonObject requestJson
    ) throws InterruptedException {
        return Json.createReader(new StringReader(
                runExportTest(formatJson, requestJson))).readObject();
    }

    private String runExportTest(
        String format, JsonObject requestJson
    ) throws InterruptedException {
        String jobId = startExport(
            format, requestJson, Job.Status.FINISHED);

        /* Request export result */
        Response download = target
            .path(ASYNC_EXPORT_URL + "download/" + jobId)
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .get();
        Assert.assertEquals(
            "Unexpected response status code",
            Response.Status.OK.getStatusCode(),
            download.getStatus());

        return download.readEntity(String.class);
    }

    private void assertHasLinesInAnyOrder(
        String csv, String recordSep, String header, String... line
    ) {
        List<String> resultLines = Arrays.asList(csv.split(recordSep));

        // Assert that header matches
        Assert.assertEquals(
            "Unexpected CSV header", header, resultLines.get(0));

        // Assert that expected lines exist
        MatcherAssert.assertThat(resultLines, CoreMatchers.hasItems(line));

        // Assert that result does not have extraneous lines
        Assert.assertEquals(
            "Contains extraneous lines", line.length + 1, resultLines.size());
    }
}
