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
import java.util.ArrayList;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.SyncInvoker;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.persistence.ApplyScriptBefore;
import org.jboss.arquillian.persistence.Cleanup;
import org.jboss.arquillian.persistence.DataSource;
import org.jboss.arquillian.persistence.TestExecutionPhase;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.intevation.lada.exporter.ExportJob;
import de.intevation.lada.model.stammdaten.BaseQuery;

/**
 * Test export services.
 *
 */
@RunWith(Arquillian.class)
public class ExporterTest extends BaseTest {

    private static Logger logger = Logger.getLogger(ExporterTest.class);

    @PersistenceContext
    EntityManager em;

    public ExporterTest() {
        testProtocol = new ArrayList<Protocol>();
    }

    /**
     * Prepare data for CSV export of a Probe object.
     */
    @Test
    @InSequence(1)
    @ApplyScriptBefore("datasets/clean_and_seed.sql")
    @UsingDataSet("datasets/dbUnit_probe_query.json")
    @DataSource("java:jboss/lada-test")
    @Cleanup(phase = TestExecutionPhase.NONE)
    public final void prepareCsvExportProbe() {
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
     * Test asynchronous CSV export of a Probe object.
     */
    @Test
    @InSequence(2)
    @RunAsClient
    public final void testCsvExportProbe(
        @ArquillianResource URL baseUrl
    ) throws InterruptedException, CharacterCodingException {
        System.out.print(".");
        Protocol prot = new Protocol();
        prot.setName("asyncexport service");
        prot.setType("csv");
        prot.setPassed(false);
        testProtocol.add(prot);

        Client client = ClientBuilder.newClient();

        /* Request asynchronous export */
        JsonObject requestJson = Json.createObjectBuilder()
            .add("exportSubData", false)
            .add("idField", JsonValue.NULL)
            .add("timezone", "UTC")
            .add("columns", Json.createArrayBuilder()
                .add(Json.createObjectBuilder()
                    .add("columnIndex", 0)
                    .add("export", true)
                    .add("filterValue", "")
                    .add("filterActive", false)
                    .add("filterIsNull", false)
                    .add("filterNegate", false)
                    .add("filterRegex", false)
                    .add("gridColumnId", 1))
                .add(Json.createObjectBuilder()
                    .add("columnIndex", 1)
                    .add("export", true)
                    .add("filterValue", "")
                    .add("filterActive", false)
                    .add("filterIsNull", false)
                    .add("filterNegate", false)
                    .add("filterRegex", false)
                    .add("gridColumnId", 2))
            ).build();

        Response exportCreated = client.target(
            baseUrl + "data/asyncexport/csv")
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .post(Entity.entity(requestJson.toString(),
                    MediaType.APPLICATION_JSON));
        JsonObject exportCreatedObject = parseResponse(exportCreated, prot);

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
            exportStatusObject = parseResponse(statusRequest.get(), prot);

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
            ExportJob.Status.FINISHED.name().toLowerCase(),
            exportStatusObject.getString(statusKey));

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
        Assert.assertEquals(
            "Unexpected CSV content",
            "hauptprobenNr,umwId\r\n120510002,L6\r\n120510001,L6\r\n",
            download.readEntity(String.class));

        prot.setPassed(true);
    }
}
