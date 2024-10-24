/* Copyright (C) 2021 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.jboss.logging.Logger;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
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
    private final String hpNrKey = "main_sample_id";

    public UniversalServiceTest() {
        this.testDatasetName = "datasets/dbUnit_query.xml";
    }

    private JsonArray requestJson = Json.createArrayBuilder()
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
        .build();

    // Expected statement according to master.base_query.sql
    // in dbUnit_query.json
    private final String sqlTemplate = "PREPARE request AS \n"
        + "SELECT main_sample_id, env_medium_id, is_test, id AS probeId "
        + "FROM lada.sample%s;\n"
        + "EXECUTE request%s;\nDEALLOCATE request;";

    // A 'main_sample_id' from lada.sample in dbUnit_query.json
    private final String filterValue = "12051,0001";

    private JsonArray filteredRequestJson = Json.createArrayBuilder()
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
        .build();

    /**
     * Test fetching all data returned by a query.
     *
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testGetAll() {
        Response response = client.target(
            baseUrl + "rest/universal")
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .post(Entity.entity(this.requestJson.toString(),
                    MediaType.APPLICATION_JSON));
        JsonObject responseJson = parseResponse(response).asJsonObject();

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
    public final void testGetPaged() {
        final int limit = 1;
        Response response = client.target(
            baseUrl + "rest/universal?start=1&limit=" + limit)
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .post(Entity.entity(this.requestJson.toString(),
                    MediaType.APPLICATION_JSON));
        JsonObject responseJson = parseResponse(response).asJsonObject();

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
    public final void testGetSql() {
        Response response = client.target(
            baseUrl + "rest/sql")
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .post(Entity.entity(this.requestJson.toString(),
                    MediaType.APPLICATION_JSON));
        String responseBody = assertResponseOK(response);

        Assert.assertEquals(
            String.format(this.sqlTemplate, "", ""),
            responseBody);
    }

    /**
     * Test fetching data returned by a query with filter.
     *
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testGetFiltered() {
        Response response = client.target(
            baseUrl + "rest/universal")
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .post(Entity.entity(this.filteredRequestJson.toString(),
                    MediaType.APPLICATION_JSON));
        JsonObject responseJson = parseResponse(response).asJsonObject();

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
    public final void testGetSqlWithParameter() {
        Response response = client.target(
            baseUrl + "rest/sql")
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .post(Entity.entity(this.filteredRequestJson.toString(),
                    MediaType.APPLICATION_JSON));
        String responseBody = assertResponseOK(response);

        Assert.assertEquals(
            String.format(
                this.sqlTemplate,
                // Corresponds to stamm.filter.sql in dbUnit_probe_query.json
                " WHERE main_sample_id ~ $1",
                "('^" + filterValue + ".*$')"),
            responseBody);
    }

    /**
     * Test fetching data returned by a query with empty result set.
     *
     * @param baseUrl The server url used for the request.
     */
    @Test
    @RunAsClient
    public final void testGetEmpty() {
        JsonArray requestEmpty = Json.createArrayBuilder()
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
            .build();

        Response response = client.target(
            baseUrl + "rest/universal")
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .post(Entity.entity(requestEmpty.toString(),
                    MediaType.APPLICATION_JSON));
        JsonObject responseJson = parseResponse(response).asJsonObject();

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
    public final void testGetSingleColumn() {
        JsonArray request = Json.createArrayBuilder()
            .add(Json.createObjectBuilder()
                .add("colIndex", 0)
                .add("filterVal", "")
                .add("isFilterActive", false)
                .add("isFilterNull", false)
                .add("isFilterNegate", false)
                .add("isFilterRegex", false)
                .add("gridColMpId", 3))
            .build();

        Response response = client.target(
            baseUrl + "rest/universal")
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .post(Entity.entity(request.toString(),
                    MediaType.APPLICATION_JSON));
        JsonObject responseJson = parseResponse(response).asJsonObject();

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
