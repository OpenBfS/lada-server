/* Copyright (C) 2021 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada;

import java.net.URL;
import java.util.ArrayList;

import javax.json.Json;
import javax.json.JsonObject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
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

import de.intevation.lada.model.stammdaten.BaseQuery;

/**
 * Class to test the Lada server 'universal' service.
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

    public UniversalServiceTest() {
        testProtocol = new ArrayList<Protocol>();
    }

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

        Client client = ClientBuilder.newClient();

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

        Client client = ClientBuilder.newClient();

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
}
