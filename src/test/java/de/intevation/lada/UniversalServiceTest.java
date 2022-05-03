/* Copyright (C) 2021 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada;

import java.net.URL;

import javax.json.Json;
import javax.json.JsonObject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.logging.Logger;
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

import de.intevation.lada.model.stammdaten.BaseQuery;

/**
 * Class to test the Lada server 'universal' service and related SqlService.
 *
 */
@RunWith(Arquillian.class)
public class UniversalServiceTest extends BaseTest {

    private static Logger logger = Logger.getLogger(UniversalServiceTest.class);

    @PersistenceContext
    EntityManager em;

    // The size of the "land.probe" array in dbUnit_probe_query.json
    private final int totalCount = 2;

    // Expected keys in JSON response
    private final String totalCountKey = "totalCount";
    private final String dataKey = "data";
    private final String hpNrKey = "hauptproben_nr";

    private JsonObject requestJson = Json.createObjectBuilder()
        .add("columns", Json.createArrayBuilder()
            .add(Json.createObjectBuilder()
                .add("columnIndex", 0)
                .add("filterValue", "")
                .add("filterActive", false)
                .add("filterIsNull", false)
                .add("filterNegate", false)
                .add("filterRegex", false)
                .add("gridColumnId", 1))
            .add(Json.createObjectBuilder()
                .add("columnIndex", 1)
                .add("filterValue", "")
                .add("filterActive", false)
                .add("filterIsNull", false)
                .add("filterNegate", false)
                .add("filterRegex", false)
                .add("gridColumnId", 2))
        ).build();

    // Expected statement according to stamm.base_query.sql
    // in dbUnit_probe_query.json
    private final String sqlTemplate = "PREPARE request AS \n"
        + "SELECT hauptproben_nr, umw_id FROM land.probe%s;\n"
        + "EXECUTE request%s;\nDEALLOCATE request;";

    // A 'hauptproben_nr' from land.probe in dbUnit_probe_query.json
    private final String filterValue = "120510001";

    private JsonObject filteredRequestJson = Json.createObjectBuilder()
        .add("columns", Json.createArrayBuilder()
            .add(Json.createObjectBuilder()
                .add("columnIndex", 0)
                .add("filterValue", filterValue)
                .add("filterActive", true)
                .add("filterIsNull", false)
                .add("filterNegate", false)
                .add("filterRegex", false)
                .add("gridColumnId", 1))
            .add(Json.createObjectBuilder()
                .add("columnIndex", 1)
                .add("filterValue", "")
                .add("filterActive", false)
                .add("filterIsNull", false)
                .add("filterNegate", false)
                .add("filterRegex", false)
                .add("gridColumnId", 2))
        ).build();

    /**
     * Prepare data to be requested via UniversalService.
     */
    @Test
    @InSequence(1)
    @ApplyScriptBefore("datasets/clean_and_seed.sql")
    @UsingDataSet("datasets/dbUnit_probe_query.json")
    @DataSource("java:jboss/lada-test")
    @Cleanup(phase = TestExecutionPhase.NONE)
    public final void prepareUniversalServiceProbe() {
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
     * Test fetching all data returned by a query.
     *
     * @param baseUrl The server url used for the request.
     */
    @Test
    @InSequence(2)
    @RunAsClient
    public final void testGetAll(@ArquillianResource URL baseUrl) {
        System.out.print(".");
        Protocol prot = new Protocol();
        prot.setName("universal service");
        prot.setType("universal get all");
        prot.setPassed(false);
        testProtocol.add(prot);

        Response response = client.target(
            baseUrl + "rest/universal")
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .post(Entity.entity(this.requestJson.toString(),
                    MediaType.APPLICATION_JSON));
        JsonObject responseJson = parseResponse(response, prot);

        assertContains(responseJson, totalCountKey);
        Assert.assertEquals(
            totalCount, responseJson.getInt(totalCountKey));

        assertContains(responseJson, dataKey);
        Assert.assertEquals(
            totalCount, responseJson.getJsonArray(dataKey).size());

        prot.setPassed(true);
    }

    /**
     * Test fetching data returned by a query using pages.
     *
     * @param baseUrl The server url used for the request.
     */
    @Test
    @InSequence(3)
    @RunAsClient
    public final void testGetPaged(@ArquillianResource URL baseUrl) {
        System.out.print(".");
        Protocol prot = new Protocol();
        prot.setName("universal service");
        prot.setType("universal get paged");
        prot.setPassed(false);
        testProtocol.add(prot);

        final int limit = 1;
        Response response = client.target(
            baseUrl + "rest/universal?start=1&limit=" + limit)
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .post(Entity.entity(this.requestJson.toString(),
                    MediaType.APPLICATION_JSON));
        JsonObject responseJson = parseResponse(response, prot);

        assertContains(responseJson, totalCountKey);
        Assert.assertEquals(
            totalCount, responseJson.getInt(totalCountKey));

        assertContains(responseJson, dataKey);
        Assert.assertEquals(
            limit, responseJson.getJsonArray(dataKey).size());

        prot.setPassed(true);
    }

    /**
     * Test interface to retrieve SQL statement.
     *
     * @param baseUrl The server url used for the request.
     */
    @Test
    @InSequence(4)
    @RunAsClient
    public final void testGetSql(@ArquillianResource URL baseUrl) {
        System.out.print(".");
        Protocol prot = new Protocol();
        prot.setName("SQL service");
        prot.setType("SQL service");
        prot.setPassed(false);
        testProtocol.add(prot);

        Response response = client.target(
            baseUrl + "rest/sql")
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .post(Entity.entity(this.requestJson.toString(),
                    MediaType.APPLICATION_JSON));
        JsonObject responseJson = parseResponse(response, prot);

        assertContains(responseJson, dataKey);
        Assert.assertEquals(
            String.format(this.sqlTemplate, "", ""),
            responseJson.getString(dataKey));

        prot.setPassed(true);
    }

    /**
     * Test fetching data returned by a query with filter.
     *
     * @param baseUrl The server url used for the request.
     */
    @Test
    @InSequence(5)
    @RunAsClient
    public final void testGetFiltered(@ArquillianResource URL baseUrl) {
        System.out.print(".");
        Protocol prot = new Protocol();
        prot.setName("universal service");
        prot.setType("universal get filtered");
        prot.setPassed(false);
        testProtocol.add(prot);

        Response response = client.target(
            baseUrl + "rest/universal")
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .post(Entity.entity(this.filteredRequestJson.toString(),
                    MediaType.APPLICATION_JSON));
        JsonObject responseJson = parseResponse(response, prot);

        assertContains(responseJson, totalCountKey);

        // Filtered for a unique hauptproben_nr
        Assert.assertEquals(
            1, responseJson.getInt(totalCountKey));

        assertContains(responseJson, dataKey);
        Assert.assertEquals(
            this.filterValue,
            responseJson.getJsonArray(dataKey)
                .getJsonObject(0).getString(hpNrKey));

        prot.setPassed(true);
    }

    /**
     * Test interface to retrieve SQL statement with parameters.
     *
     * @param baseUrl The server url used for the request.
     */
    @Test
    @InSequence(6)
    @RunAsClient
    public final void testGetSqlWithParameter(@ArquillianResource URL baseUrl) {
        System.out.print(".");
        Protocol prot = new Protocol();
        prot.setName("SQL service");
        prot.setType("SQL service with parameters");
        prot.setPassed(false);
        testProtocol.add(prot);

        Response response = client.target(
            baseUrl + "rest/sql")
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .post(Entity.entity(this.filteredRequestJson.toString(),
                    MediaType.APPLICATION_JSON));
        JsonObject responseJson = parseResponse(response, prot);

        assertContains(responseJson, dataKey);
        Assert.assertEquals(
            String.format(
                this.sqlTemplate,
                // Corresponds to stamm.filter.sql in dbUnit_probe_query.json
                " WHERE hauptproben_nr ~ $1",
                "('^" + filterValue + ".*$')"),
            responseJson.getString(dataKey));

        prot.setPassed(true);
    }

    /**
     * Test fetching data returned by a query with empty result set.
     *
     * @param baseUrl The server url used for the request.
     */
    @Test
    @InSequence(7)
    @RunAsClient
    public final void testGetEmpty(@ArquillianResource URL baseUrl) {
        System.out.print(".");
        Protocol prot = new Protocol();
        prot.setName("universal service");
        prot.setType("universal get empty");
        prot.setPassed(false);
        testProtocol.add(prot);

        JsonObject requestEmpty = Json.createObjectBuilder()
            .add("columns", Json.createArrayBuilder()
                .add(Json.createObjectBuilder()
                    .add("columnIndex", 0)
                    .add("filterValue", "not existing value")
                    .add("filterActive", true)
                    .add("filterIsNull", false)
                    .add("filterNegate", false)
                    .add("filterRegex", false)
                    .add("gridColumnId", 1))
                .add(Json.createObjectBuilder()
                    .add("columnIndex", 1)
                    .add("filterValue", "")
                    .add("filterActive", false)
                    .add("filterIsNull", false)
                    .add("filterNegate", false)
                    .add("filterRegex", false)
                    .add("gridColumnId", 2))
        ).build();

        Response response = client.target(
            baseUrl + "rest/universal")
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .post(Entity.entity(requestEmpty.toString(),
                    MediaType.APPLICATION_JSON));
        JsonObject responseJson = parseResponse(response, prot);

        assertContains(responseJson, totalCountKey);

        Assert.assertEquals(
            0, responseJson.getInt(totalCountKey));

        assertContains(responseJson, dataKey);
        Assert.assertTrue(responseJson.getJsonArray(dataKey).isEmpty());

        prot.setPassed(true);
    }

    /**
     * Test fetching data returned by a single-column query.
     *
     * @param baseUrl The server url used for the request.
     */
    @Test
    @InSequence(8)
    @RunAsClient
    public final void testGetSingleColumn(@ArquillianResource URL baseUrl) {
        System.out.print(".");
        Protocol prot = new Protocol();
        prot.setName("universal service");
        prot.setType("universal get single column");
        prot.setPassed(false);
        testProtocol.add(prot);

        JsonObject request = Json.createObjectBuilder()
            .add("columns", Json.createArrayBuilder()
                .add(Json.createObjectBuilder()
                    .add("columnIndex", 0)
                    .add("filterValue", "")
                    .add("filterActive", false)
                    .add("filterIsNull", false)
                    .add("filterNegate", false)
                    .add("filterRegex", false)
                    .add("gridColumnId", 3))
        ).build();

        Response response = client.target(
            baseUrl + "rest/universal")
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .post(Entity.entity(request.toString(),
                    MediaType.APPLICATION_JSON));
        JsonObject responseJson = parseResponse(response, prot);

        // single-column query should result in JSON objects with
        // key-value pairs representing "readonly" flag and a single data column
        assertContains(responseJson, dataKey);
        JsonObject respObj = (JsonObject)
            responseJson.getJsonArray(dataKey).get(0);
        Assert.assertEquals(2, respObj.size());
        assertContains(respObj, "readonly");
        assertContains(respObj, hpNrKey);

        prot.setPassed(true);
    }
}
