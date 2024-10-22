/* Copyright (C) 2021 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada;

import java.io.StringReader;
import java.net.URL;
import java.nio.charset.CharacterCodingException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.SyncInvoker;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.jboss.logging.Logger;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.intevation.lada.util.data.Job;


/**
 * Test export services.
 *
 */
@RunWith(Arquillian.class)
public class ExporterTest extends BaseTest {

    private static Logger logger = Logger.getLogger(ExporterTest.class);

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
     * Test asynchronous CSV export of a Sample object.
     */
    @Test
    @RunAsClient
    public final void testCsvExportProbe(
        @ArquillianResource URL baseUrl
    ) throws InterruptedException, CharacterCodingException {
        /* Request asynchronous export */
        JsonObject requestJson = requestJsonBuilder
            .add("idField", JsonValue.NULL)
            .build();

        assertHasLinesInAnyOrder(
            runExportTest(baseUrl, formatCsv, requestJson),
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
    public final void testCsvExportFieldSeparator(
        @ArquillianResource URL baseUrl
    ) throws InterruptedException, CharacterCodingException {
        /* Request asynchronous export */
        JsonObject requestJson = requestJsonBuilder
            .add("idField", JsonValue.NULL)
            .add("fieldSeparator", ";")
            .build();

        assertHasLinesInAnyOrder(
            runExportTest(baseUrl, formatCsv, requestJson),
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
    public final void testCsvExportRowDelimiter(
        @ArquillianResource URL baseUrl
    ) throws InterruptedException, CharacterCodingException {
        final String rowDelimiter = "\n";
        JsonObject requestJson = requestJsonBuilder
            .add("idField", JsonValue.NULL)
            .add("rowDelimiter", rowDelimiter)
            .build();

        assertHasLinesInAnyOrder(
            runExportTest(baseUrl, formatCsv, requestJson),
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
    public final void testCsvExportQuote(
        @ArquillianResource URL baseUrl
    ) throws InterruptedException, CharacterCodingException {
        /* Request asynchronous export */
        JsonObject requestJson = requestJsonBuilder
            .add("idField", JsonValue.NULL)
            .add("quote", "'")
            .build();

        assertHasLinesInAnyOrder(
            runExportTest(baseUrl, formatCsv, requestJson),
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
    public final void testCsvExportDecimalSeparator(
        @ArquillianResource URL baseUrl
    ) throws InterruptedException, CharacterCodingException {
        assertHasLinesInAnyOrder(
            runExportTest(baseUrl, formatCsv, measmRequestJsonBuilder
                .add("decimalSeparator", ",").build()),
            "\r\n",
            "messungId,id,measVal,measUnitId,measdId",
            "1200,1000,\"1,1E00\",Sv,test",
            "1200,1001,,Sv,test");
    }

    /**
     * Test asynchronous CSV export of a Sample identified by ID.
     */
    @Test
    @RunAsClient
    public final void testCsvExportProbeById(
        @ArquillianResource URL baseUrl
    ) throws InterruptedException, CharacterCodingException {
        JsonObject requestJson = requestJsonBuilder
            .add("idField", "main_sample_id")
            .add("idFilter", Json.createArrayBuilder().add("120510002"))
            .build();

        assertHasLinesInAnyOrder(
            runExportTest(baseUrl, formatCsv, requestJson),
            "\r\n",
            "hauptprobenNr,umwId,isTest,probeId",
            "120510002,L6,No,1000");
    }

    /**
     * Test asynchronous CSV export of Sample objects including measms.
     */
    @Test
    @RunAsClient
    public final void testCsvExportProbeSubData(
        @ArquillianResource URL baseUrl
    ) throws InterruptedException, CharacterCodingException {
        JsonObject requestJson = requestJsonBuilder
            .add("idField", "probeId")
            .add("exportSubData", true)
            .add("subDataColumns", Json.createArrayBuilder()
                .add("extId")
                .add("messwerteCount"))
            .build();

        assertHasLinesInAnyOrder(
            runExportTest(baseUrl, formatCsv, requestJson),
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
    public final void testCsvExportMeasmSubData(
        @ArquillianResource URL baseUrl
    ) throws InterruptedException, CharacterCodingException {
        assertHasLinesInAnyOrder(
            runExportTest(
                baseUrl, formatCsv, measmRequestJsonBuilder.build()),
            "\r\n",
            "messungId,id,measVal,measUnitId,measdId",
            "1200,1000,1.1E00,Sv,test",
            "1200,1001,,Sv,test");
    }

    /**
     * Test asynchronous JSON export of a Sample identified by ID.
     */
    @Test
    @RunAsClient
    public final void testJsonExportProbeById(
        @ArquillianResource URL baseUrl
    ) throws InterruptedException, CharacterCodingException {
        /* Request asynchronous export */
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
            runJSONExportTest(baseUrl, requestJson));
    }

    /**
     * Test asynchronous JSON export of a Sample object with measms.
     */
    @Test
    @RunAsClient
    public final void testJsonExportProbeSubData(
        @ArquillianResource URL baseUrl
    ) throws InterruptedException, CharacterCodingException {
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
            runJSONExportTest(baseUrl, requestJson));
    }

    /**
     * Test asynchronous JSON export of a Measm object with measVals.
     */
    @Test
    @RunAsClient
    public final void testJsonExportMeasmSubData(
        @ArquillianResource URL baseUrl
    ) throws InterruptedException, CharacterCodingException {
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
                    + "\"measdId\":\"test\","
                    + "\"measVal\":null,"
                    + "\"id\":1001}]}}")).readObject(),
            runJSONExportTest(baseUrl, measmRequestJsonBuilder.build()));
    }

    /**
     * Test asynchronous LAF export of a Sample identified by ID.
     */
    @Test
    @RunAsClient
    public final void testLafExportProbeById(
        @ArquillianResource URL baseUrl
    ) throws InterruptedException, CharacterCodingException {
        /* Request asynchronous export */
        final int probeId = 1000;
        JsonObject requestJson = requestJsonBuilder
            .add("proben", Json.createArrayBuilder().add(probeId))
            .build();

        String result = runExportTest(baseUrl, formatLaf, requestJson);
        Assert.assertTrue(
            "Unexpected LAF content",
            result.startsWith("%PROBE%") && result.endsWith("%ENDE%"));
    }

    /**
     * Test asynchronous export of an empty query result.
     */
    @Test
    @RunAsClient
    public final void testQueryExportEmpty(
        @ArquillianResource URL baseUrl
    ) throws InterruptedException, CharacterCodingException {
        /* Request asynchronous export */
        JsonObject requestJson = requestJsonBuilder
            .add("idField", "main_sample_id")
            .add("idFilter", Json.createArrayBuilder().add("nonexistent"))
            .build();

        String csvResult = runExportTest(
            baseUrl, formatCsv, requestJson);
        Assert.assertEquals(
            "Unexpected CSV content",
            "hauptprobenNr,umwId,isTest,probeId\r\n",
            csvResult);

        Assert.assertEquals(
            "Unexpected JSON content",
            JsonValue.EMPTY_JSON_OBJECT,
            runJSONExportTest(baseUrl, requestJson));
    }

    /**
     * Test failing asynchronous export with invalid request payload.
     */
    @Test
    @RunAsClient
    public final void testAsyncExportFailure(
        @ArquillianResource URL baseUrl
    ) throws InterruptedException, CharacterCodingException {
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
                exportRequest(baseUrl, formatLaf, lafJson),
                Response.Status.BAD_REQUEST)
                .asJsonObject(),
            samplePath, sampleMessage);
    }

    /**
     * Test failing asynchronous export with invalid encoding.
     */
    @Test
    @RunAsClient
    public final void testAsyncExportInvalidCharset(
        @ArquillianResource URL baseUrl
    ) {
        JsonObject jsonExportJson = Json.createObjectBuilder()
            .add("encoding", "invalidEncoding")
            .build();
        parseResponse(
            exportRequest(baseUrl, formatJson, jsonExportJson),
            Response.Status.BAD_REQUEST);
    }

    private Response exportRequest(
            URL baseUrl, String format, JsonObject requestJson) {
        Response response = client.target(
            baseUrl + "data/asyncexport/" + format)
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .header("Accept", MediaType.APPLICATION_JSON)
            .post(Entity.entity(requestJson.toString(),
                    MediaType.APPLICATION_JSON));
        return response;
    }

    private String startExport(
        URL baseUrl,
        String format,
        JsonObject requestJson,
        Job.Status expectedStatus
    ) throws InterruptedException {
        Response exportCreated = exportRequest(baseUrl, format, requestJson);
        JsonObject exportCreatedObject =
            parseResponse(exportCreated).asJsonObject();

        final String jobIdKey = "jobId";
        assertContains(exportCreatedObject, jobIdKey);
        String jobId = exportCreatedObject.getString(jobIdKey);

        /* Request status of asynchronous export */
        SyncInvoker statusRequest = client.target(
            baseUrl + "data/asyncexport/status/" + jobId)
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles);
        JsonObject exportStatusObject = Json.createObjectBuilder().build();
        boolean done = false;
        final Instant waitUntil = Instant.now().plus(Duration.ofMinutes(1));
        final int waitASecond = 1000;
        do {
            exportStatusObject =
                parseResponse(statusRequest.get()).asJsonObject();

            final String doneKey = "done";
            assertContains(exportStatusObject, doneKey);
            done = exportStatusObject.getBoolean(doneKey);

            Assert.assertTrue(
                "Export not done within one minute",
                waitUntil.isAfter(Instant.now()));
            Thread.sleep(waitASecond);
        } while (!done);

        final String statusKey = "status";
        assertContains(exportStatusObject, statusKey);
        Assert.assertEquals(
            expectedStatus.name().toLowerCase(),
            exportStatusObject.getString(statusKey));

        return jobId;
    }

    private JsonObject runJSONExportTest(
        URL baseUrl, JsonObject requestJson
    ) throws InterruptedException {
        return Json.createReader(new StringReader(
                runExportTest(baseUrl, formatJson, requestJson))).readObject();
    }

    private String runExportTest(
        URL baseUrl, String format, JsonObject requestJson
    ) throws InterruptedException {
        String jobId = startExport(
            baseUrl, format, requestJson, Job.Status.FINISHED);

        /* Request export result */
        Response download = client.target(
            baseUrl + "data/asyncexport/download/" + jobId)
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
