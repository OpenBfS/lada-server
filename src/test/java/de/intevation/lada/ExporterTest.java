/* Copyright (C) 2021 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada;

import java.net.URL;
import java.nio.charset.CharacterCodingException;
import java.time.Duration;
import java.time.Instant;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.SyncInvoker;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.logging.Logger;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.persistence.ApplyScriptBefore;
import org.jboss.arquillian.persistence.Cleanup;
import org.jboss.arquillian.persistence.TestExecutionPhase;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.intevation.lada.model.master.BaseQuery;
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
                .add("gridColMpId", 1))
            .add(Json.createObjectBuilder()
                .add("columnIndex", 1)
                .add("export", true)
                .add("filterVal", "")
                .add("isFilterActive", false)
                .add("isFilterNull", false)
                .add("isFilterNegate", false)
                .add("isFilterRegex", false)
                .add("gridColMpId", 2))
            .add(Json.createObjectBuilder()
                .add("columnIndex", 2)
                .add("export", true)
                .add("filterVal", "")
                .add("isFilterActive", false)
                .add("isFilterNull", false)
                .add("isFilterNegate", false)
                .add("isFilterRegex", false)
                .add("gridColMpId", 4)));

    private final JsonObject measmRequestJson = Json.createObjectBuilder()
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
                .add("gridColMpId", 5)))
        .add("idField", "messungId")
        .add("idFilter", Json.createArrayBuilder().add("1200"))
        .add("exportSubData", true)
        .add("subDataColumns", Json.createArrayBuilder().add("id"))
        .build();

    /**
     * Prepare data for export of a Sample object.
     */
    @Ignore
    @Test
    @InSequence(1)
    @ApplyScriptBefore("datasets/clean_and_seed.sql")
    @UsingDataSet("datasets/dbUnit_probe_query.json")
    @Cleanup(phase = TestExecutionPhase.NONE)
    public final void prepareExportProbe() {
        Protocol protocol = new Protocol();
        protocol.setName("database");
        protocol.setType("insert query data");
        protocol.addInfo("database", "Insert query data into database");
        testProtocol.add(protocol);
        // Just check one of the inserted objects:
        BaseQuery query = em.find(BaseQuery.class, 1);
        Assert.assertNotNull(query);
        protocol.setPassed(true);
    }

    /**
     * Test asynchronous CSV export of a Sample object.
     */
    @Ignore
    @Test
    @InSequence(2)
    @RunAsClient
    public final void testCsvExportProbe(
        @ArquillianResource URL baseUrl
    ) throws InterruptedException, CharacterCodingException {
        Protocol prot = new Protocol();
        prot.setName("asyncexport service");
        prot.setType(formatCsv);
        prot.setPassed(false);
        testProtocol.add(prot);

        /* Request asynchronous export */
        JsonObject requestJson = requestJsonBuilder
            .add("idField", JsonValue.NULL)
            .build();

        String result = runExportTest(baseUrl, formatCsv, prot, requestJson);
        Assert.assertEquals(
            "Unexpected CSV content",
            "hauptprobenNr,umwId,probeId\r\n"
            + "120510002,L6,1000\r\n"
            + "120510001,L6,1001\r\n",
            result);

        prot.setPassed(true);
    }

    /**
     * Test asynchronous CSV export of a Sample identified by ID.
     */
    @Ignore
    @Test
    @InSequence(3)
    @RunAsClient
    public final void testCsvExportProbeById(
        @ArquillianResource URL baseUrl
    ) throws InterruptedException, CharacterCodingException {
        Protocol prot = new Protocol();
        prot.setName("asyncexport service");
        prot.setType("filtered csv");
        prot.setPassed(false);
        testProtocol.add(prot);

        /* Request asynchronous export */
        JsonObject requestJson = requestJsonBuilder
            .add("idField", "hauptproben_nr")
            .add("idFilter", Json.createArrayBuilder().add("120510002"))
            .build();

        String result = runExportTest(baseUrl, formatCsv, prot, requestJson);
        Assert.assertEquals(
            "Unexpected CSV content",
            "hauptprobenNr,umwId,probeId\r\n120510002,L6,1000\r\n",
            result);

        prot.setPassed(true);
    }

    /**
     * Test asynchronous CSV export of Sample objects including measms.
     */
    @Ignore
    @Test
    @InSequence(3)
    @RunAsClient
    public final void testCsvExportProbeSubData(
        @ArquillianResource URL baseUrl
    ) throws InterruptedException, CharacterCodingException {
        Protocol prot = new Protocol();
        prot.setName("asyncexport service");
        prot.setType(formatCsv);
        prot.setPassed(false);
        testProtocol.add(prot);

        /* Request asynchronous export */
        JsonObject requestJson = requestJsonBuilder
            .add("idField", "probeId")
            .add("exportSubData", true)
            .add("subDataColumns", Json.createArrayBuilder().add("extId"))
            .build();

        String result = runExportTest(baseUrl, formatCsv, prot, requestJson);
        Assert.assertEquals(
            "Unexpected CSV content",
            "hauptprobenNr,umwId,probeId,extId\r\n"
            + "120510002,L6,1000,453\r\n"
            + "120510001,L6,1001,\r\n",
            result);

        prot.setPassed(true);
    }

    /**
     * Test asynchronous CSV export of Measm objects including measVals.
     */
    @Ignore
    @Test
    @InSequence(3)
    @RunAsClient
    public final void testCsvExportMeasmSubData(
        @ArquillianResource URL baseUrl
    ) throws InterruptedException, CharacterCodingException {
        Protocol prot = new Protocol();
        prot.setName("asyncexport service");
        prot.setType(formatCsv);
        prot.setPassed(false);
        testProtocol.add(prot);

        String result = runExportTest(
            baseUrl, formatCsv, prot, measmRequestJson);
        Assert.assertEquals(
            "Unexpected CSV content",
            "messungId,id\r\n"
            + "1200,1000\r\n",
            result);

        prot.setPassed(true);
    }

    /**
     * Test asynchronous JSON export of a Sample identified by ID.
     */
    @Ignore
    @Test
    @InSequence(4)
    @RunAsClient
    public final void testJsonExportProbeById(
        @ArquillianResource URL baseUrl
    ) throws InterruptedException, CharacterCodingException {
        Protocol prot = new Protocol();
        prot.setName("asyncexport service");
        prot.setType(formatJson);
        prot.setPassed(false);
        testProtocol.add(prot);

        /* Request asynchronous export */
        JsonObject requestJson = requestJsonBuilder
            .add("idField", "hauptproben_nr")
            .add("idFilter", Json.createArrayBuilder().add("120510002"))
            .build();

        String result = runExportTest(baseUrl, formatJson, prot, requestJson);
        Assert.assertEquals(
            "Unexpected JSON content",
            "{\"120510002\":"
            + "{\"hauptproben_nr\":\"120510002\","
            + "\"umw_id\":\"L6\","
            + "\"probeId\":1000}}",
            result);

        prot.setPassed(true);
    }

    /**
     * Test asynchronous JSON export of a Sample object with measms.
     */
    @Ignore
    @Test
    @InSequence(4)
    @RunAsClient
    public final void testJsonExportProbeSubData(
        @ArquillianResource URL baseUrl
    ) throws InterruptedException, CharacterCodingException {
        Protocol prot = new Protocol();
        prot.setName("asyncexport service");
        prot.setType(formatJson);
        prot.setPassed(false);
        testProtocol.add(prot);

        /* Request asynchronous export */
        JsonObject requestJson = requestJsonBuilder
            .add("idField", "probeId")
            .add("idFilter", Json.createArrayBuilder().add("1000"))
            .add("exportSubData", true)
            .add("subDataColumns", Json.createArrayBuilder().add("extId"))
            .build();

        String result = runExportTest(baseUrl, formatJson, prot, requestJson);
        Assert.assertEquals(
            "Unexpected JSON content",
            "{\"1000\":"
            + "{\"hauptproben_nr\":\"120510002\","
            + "\"umw_id\":\"L6\","
            + "\"probeId\":1000,"
            + "\"Messungen\":[{\"extId\":453}]}}",
            result);

        prot.setPassed(true);
    }

    /**
     * Test asynchronous JSON export of a Measm object with measVals.
     */
    @Ignore
    @Test
    @InSequence(4)
    @RunAsClient
    public final void testJsonExportMeasmSubData(
        @ArquillianResource URL baseUrl
    ) throws InterruptedException, CharacterCodingException {
        Protocol prot = new Protocol();
        prot.setName("asyncexport service");
        prot.setType(formatJson);
        prot.setPassed(false);
        testProtocol.add(prot);

        String result = runExportTest(
            baseUrl, formatJson, prot, measmRequestJson);
        Assert.assertEquals(
            "Unexpected JSON content",
            "{\"1200\":"
            + "{\"messungId\":1200,"
            + "\"messwerte\":[{\"id\":1000}]}}",
            result);

        prot.setPassed(true);
    }

    /**
     * Test asynchronous LAF export of a Sample identified by ID.
     */
    @Ignore
    @Test
    @InSequence(5)
    @RunAsClient
    public final void testLafExportProbeById(
        @ArquillianResource URL baseUrl
    ) throws InterruptedException, CharacterCodingException {
        Protocol prot = new Protocol();
        prot.setName("asyncexport service");
        prot.setType(formatLaf);
        prot.setPassed(false);
        testProtocol.add(prot);

        /* Request asynchronous export */
        final int probeId = 1000;
        JsonObject requestJson = requestJsonBuilder
            .add("proben", Json.createArrayBuilder().add(probeId))
            .build();

        String result = runExportTest(baseUrl, formatLaf, prot, requestJson);
        Assert.assertTrue(
            "Unexpected LAF content",
            result.startsWith("%PROBE%") && result.endsWith("%ENDE%"));

        prot.setPassed(true);
    }

    /**
     * Test asynchronous export of an empty query result.
     */
    @Ignore
    @Test
    @InSequence(6)
    @RunAsClient
    public final void testQueryExportEmpty(
        @ArquillianResource URL baseUrl
    ) throws InterruptedException, CharacterCodingException {
        Protocol prot = new Protocol();
        prot.setName("asyncexport service");
        prot.setType("empty query");
        prot.setPassed(false);
        testProtocol.add(prot);

        /* Request asynchronous export */
        JsonObject requestJson = requestJsonBuilder
            .add("idField", "hauptproben_nr")
            .add("idFilter", Json.createArrayBuilder().add("nonexistent"))
            .build();

        String csvResult = runExportTest(
            baseUrl, formatCsv, prot, requestJson);
        Assert.assertEquals(
            "Unexpected CSV content",
            "hauptprobenNr,umwId,probeId\r\n",
            csvResult);

        String jsonResult = runExportTest(
            baseUrl, formatJson, prot, requestJson);
        Assert.assertEquals(
            "Unexpected JSON content",
            "{}",
            jsonResult);

        prot.setPassed(true);
    }

    /**
     * Test failing asynchronous export with invalid request payload.
     */
    @Ignore
    @Test
    @InSequence(7)
    @RunAsClient
    public final void testAsyncExportFailure(
        @ArquillianResource URL baseUrl
    ) throws InterruptedException, CharacterCodingException {
        Protocol prot = new Protocol();
        prot.setName("asyncexport service");
        prot.setType("invalid request");
        prot.setPassed(false);
        testProtocol.add(prot);

        /* Request asynchronous export */
        JsonObject requestJson = Json.createObjectBuilder()
            // Add arbitrary array to avoid 404 being returned for LAF
            .add("proben", Json.createArrayBuilder().add("xxx"))
            .add("invalidField", "xxx")
            .build();

        startExport(baseUrl, formatCsv, prot, requestJson, Job.Status.ERROR);
        startExport(baseUrl, formatJson, prot, requestJson, Job.Status.ERROR);
        startExport(baseUrl, formatLaf, prot, requestJson, Job.Status.ERROR);

        prot.setPassed(true);
    }

    private String startExport(
        URL baseUrl,
        String format,
        Protocol prot,
        JsonObject requestJson,
        Job.Status expectedStatus
    ) throws InterruptedException {
        Response exportCreated = client.target(
            baseUrl + "data/asyncexport/" + format)
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .post(Entity.entity(requestJson.toString(),
                    MediaType.APPLICATION_JSON));
        JsonObject exportCreatedObject = parseSimpleResponse(
            exportCreated, prot);

        final String refIdKey = "refId";
        assertContains(exportCreatedObject, refIdKey);
        String refId = exportCreatedObject.getString(refIdKey);

        /* Request status of asynchronous export */
        SyncInvoker statusRequest = client.target(
            baseUrl + "data/asyncexport/status/" + refId)
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles);
        JsonObject exportStatusObject = Json.createObjectBuilder().build();
        boolean done = false;
        final Instant waitUntil = Instant.now().plus(Duration.ofMinutes(1));
        final int waitASecond = 1000;
        do {
            exportStatusObject = parseSimpleResponse(statusRequest.get(), prot);

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

        return refId;
    }

    private String runExportTest(
        URL baseUrl, String format, Protocol prot, JsonObject requestJson
    ) throws InterruptedException {
        String refId = startExport(
            baseUrl, format, prot, requestJson, Job.Status.FINISHED);

        /* Request export result */
        Response download = client.target(
            baseUrl + "data/asyncexport/download/" + refId)
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
}
