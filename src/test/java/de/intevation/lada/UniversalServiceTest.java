/* Copyright (C) 2021 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada;

import java.net.URL;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.jboss.logging.Logger;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

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

    public UniversalServiceTest() {
        this.testDatasetName = "datasets/dbUnit_query.xml";
    }

    private JsonObject requestJson = Json.createObjectBuilder()
        .add("columns", Json.createArrayBuilder()
            .add(Json.createObjectBuilder()
                .add("colIndex", 0)
                .add("filterVal", "")
                .add("isFilterActive", false)
                .add("isFilterNull", false)
                .add("isFilterNegate", false)
                .add("isFilterRegex", false)
                .add("gridColMpId", 1))
            .add(Json.createObjectBuilder()
                .add("colIndex", 1)
                .add("filterVal", "")
                .add("isFilterActive", false)
                .add("isFilterNull", false)
                .add("isFilterNegate", false)
                .add("isFilterRegex", false)
                .add("gridColMpId", 2))
        ).build();

    // Expected statement according to stamm.base_query.sql
    // in dbUnit_probe_query.json
    private final String sqlTemplate = "PREPARE request AS \n"
        + "SELECT hauptproben_nr, umw_id, id AS probeId FROM land.probe%s;\n"
        + "EXECUTE request%s;\nDEALLOCATE request;";

    // A 'hauptproben_nr' from land.probe in dbUnit_probe_query.json
    private final String filterValue = "120510001";

    private JsonObject filteredRequestJson = Json.createObjectBuilder()
        .add("columns", Json.createArrayBuilder()
            .add(Json.createObjectBuilder()
                .add("colIndex", 0)
                .add("filterVal", filterValue)
                .add("isFilterActive", true)
                .add("isFilterNull", false)
                .add("isFilterNegate", false)
                .add("isFilterRegex", false)
                .add("gridColMpId", 1))
            .add(Json.createObjectBuilder()
                .add("colIndex", 1)
                .add("filterVal", "")
                .add("isFilterActive", false)
                .add("isFilterNull", false)
                .add("isFilterNegate", false)
                .add("isFilterRegex", false)
                .add("gridColMpId", 2))
        ).build();

    /**
     * Test fetching all data returned by a query.
     *
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testGetAll(@ArquillianResource URL baseUrl) {
        Response response = client.target(
            baseUrl + "rest/universal")
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .post(Entity.entity(this.requestJson.toString(),
                    MediaType.APPLICATION_JSON));
        JsonObject responseJson = parseResponse(response);

        assertContains(responseJson, totalCountKey);
        Assert.assertEquals(
            totalCount, responseJson.getInt(totalCountKey));

        assertContains(responseJson, dataKey);
        Assert.assertEquals(
            totalCount, responseJson.getJsonArray(dataKey).size());
    }

    /**
     * Test fetching data returned by a query using pages.
     *
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testGetPaged(@ArquillianResource URL baseUrl) {
        final int limit = 1;
        Response response = client.target(
            baseUrl + "rest/universal?start=1&limit=" + limit)
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .post(Entity.entity(this.requestJson.toString(),
                    MediaType.APPLICATION_JSON));
        JsonObject responseJson = parseResponse(response);

        assertContains(responseJson, totalCountKey);
        Assert.assertEquals(
            totalCount, responseJson.getInt(totalCountKey));

        assertContains(responseJson, dataKey);
        Assert.assertEquals(
            limit, responseJson.getJsonArray(dataKey).size());
    }

    /**
     * Test interface to retrieve SQL statement.
     *
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testGetSql(@ArquillianResource URL baseUrl) {
        Response response = client.target(
            baseUrl + "rest/sql")
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .post(Entity.entity(this.requestJson.toString(),
                    MediaType.APPLICATION_JSON));
        JsonObject responseJson = parseResponse(response);

        assertContains(responseJson, dataKey);
        Assert.assertEquals(
            String.format(this.sqlTemplate, "", ""),
            responseJson.getString(dataKey));
    }

    /**
     * Test fetching data returned by a query with filter.
     *
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testGetFiltered(@ArquillianResource URL baseUrl) {
        Response response = client.target(
            baseUrl + "rest/universal")
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .post(Entity.entity(this.filteredRequestJson.toString(),
                    MediaType.APPLICATION_JSON));
        JsonObject responseJson = parseResponse(response);

        assertContains(responseJson, totalCountKey);

        // Filtered for a unique hauptproben_nr
        Assert.assertEquals(
            1, responseJson.getInt(totalCountKey));

        assertContains(responseJson, dataKey);
        Assert.assertEquals(
            this.filterValue,
            responseJson.getJsonArray(dataKey)
                .getJsonObject(0).getString(hpNrKey));
    }

    /**
     * Test interface to retrieve SQL statement with parameters.
     *
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testGetSqlWithParameter(@ArquillianResource URL baseUrl) {
        Response response = client.target(
            baseUrl + "rest/sql")
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .post(Entity.entity(this.filteredRequestJson.toString(),
                    MediaType.APPLICATION_JSON));
        JsonObject responseJson = parseResponse(response);

        assertContains(responseJson, dataKey);
        Assert.assertEquals(
            String.format(
                this.sqlTemplate,
                // Corresponds to stamm.filter.sql in dbUnit_probe_query.json
                " WHERE hauptproben_nr ~ $1",
                "('^" + filterValue + ".*$')"),
            responseJson.getString(dataKey));
    }

    /**
     * Test fetching data returned by a query with empty result set.
     *
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testGetEmpty(@ArquillianResource URL baseUrl) {
        JsonObject requestEmpty = Json.createObjectBuilder()
            .add("columns", Json.createArrayBuilder()
                .add(Json.createObjectBuilder()
                    .add("colIndex", 0)
                    .add("filterVal", "not existing value")
                    .add("isFilterActive", true)
                    .add("isFilterNull", false)
                    .add("isFilterNegate", false)
                    .add("isFilterRegex", false)
                    .add("gridColMpId", 1))
                .add(Json.createObjectBuilder()
                    .add("colIndex", 1)
                    .add("filterVal", "")
                    .add("isFilterActive", false)
                    .add("isFilterNull", false)
                    .add("isFilterNegate", false)
                    .add("isFilterRegex", false)
                    .add("gridColMpId", 2))
        ).build();

        Response response = client.target(
            baseUrl + "rest/universal")
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .post(Entity.entity(requestEmpty.toString(),
                    MediaType.APPLICATION_JSON));
        JsonObject responseJson = parseResponse(response);

        assertContains(responseJson, totalCountKey);

        Assert.assertEquals(
            0, responseJson.getInt(totalCountKey));

        assertContains(responseJson, dataKey);
        Assert.assertTrue(responseJson.getJsonArray(dataKey).isEmpty());
    }

    /**
     * Test fetching data returned by a single-column query.
     *
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testGetSingleColumn(@ArquillianResource URL baseUrl) {
        JsonObject request = Json.createObjectBuilder()
            .add("columns", Json.createArrayBuilder()
                .add(Json.createObjectBuilder()
                    .add("colIndex", 0)
                    .add("filterVal", "")
                    .add("isFilterActive", false)
                    .add("isFilterNull", false)
                    .add("isFilterNegate", false)
                    .add("isFilterRegex", false)
                    .add("gridColMpId", 3))
        ).build();

        Response response = client.target(
            baseUrl + "rest/universal")
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .post(Entity.entity(request.toString(),
                    MediaType.APPLICATION_JSON));
        JsonObject responseJson = parseResponse(response);

        // single-column query should result in JSON objects with
        // key-value pairs representing "readonly" flag and a single data column
        assertContains(responseJson, dataKey);
        JsonObject respObj = (JsonObject)
            responseJson.getJsonArray(dataKey).get(0);
        Assert.assertEquals(2, respObj.size());
        assertContains(respObj, "readonly");
        assertContains(respObj, hpNrKey);
    }
}
