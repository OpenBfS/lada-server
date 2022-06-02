/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada;

import java.io.StringReader;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import org.jboss.logging.Logger;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

/**
 * Base class for Lada server tests.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class BaseTest {

    /**
     * Name of the test archive output file.
     */
    protected static String archiveName = "lada-server-test.war";

    /**
     * User name used for tests.
     */
    public static String testUser = "testeins";

    /**
     * Roles used for tests.
     */
    public static String testRoles = "cn=mst_06_status, cn=land_06_stamm";

    private static Logger logger = Logger.getLogger(BaseTest.class);

    /**
     * Results to print out when tests are done.
     */
    protected static List<Protocol> testProtocol;

    /**
     * The client to be used for interface tests.
     */
    protected Client client;

    /**
     * Enable verbose output for tests.
     */
    protected static boolean verboseLogging = false;

    /**
     * Set up shared infrastructure for test methods.
     */
    @Before
    public void setup() {
        this.testProtocol = new ArrayList<Protocol>();
        this.client = ClientBuilder.newClient();
    }

    /**
     * Create a deployable WAR archive.
     *
     * @throws Exception that happens during build process.
     * @return WebArchive to deploy in wildfly application server.
     */
    @Deployment(testable = true)
    public static WebArchive createDeployment() throws Exception {
        File antlr = Maven.resolver().loadPomFromFile("pom.xml")
            .resolve("org.antlr:antlr4-runtime")
            .withoutTransitivity().asSingleFile();

        WebArchive archive = ShrinkWrap.create(WebArchive.class, archiveName)
            .addPackages(true, ClassLoader.getSystemClassLoader()
                .getDefinedPackage("de.intevation.lada"))
            .addAsResource("shibboleth.properties", "shibboleth.properties")
            .addAsResource("lada_server_en.properties", "lada_server_en.properties")
            .addAsResource("lada_server_de.properties", "lada_server_de.properties")
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
            .addAsLibrary(antlr)
            .addAsResource("META-INF/test-persistence.xml",
                "META-INF/persistence.xml");
        addWithDependencies("org.geotools:gt-api", archive);
        addWithDependencies("org.geotools:gt-referencing", archive);
        addWithDependencies("org.geotools:gt-epsg-hsql", archive);
        addWithDependencies("org.geotools:gt-opengis", archive);

        return archive;
    }

    /**
     * Prints out the test results.
     */
    @After
    public final void printLogs() {
        for (Protocol p : testProtocol) {
            logger.info(p.toString(verboseLogging));
        }
    }

    /**
     * Tear down shared infrastructure for test methods.
     */
    @After
    public void tearDown() {
        this.client.close();
    }

    /**
     * Add a dependency to the given webarchive.
     *
     * @param coordinate
     * @param archive
     */
    private static void addWithDependencies(
        String coordinate, WebArchive archive
    ) {
        File[] files = Maven.resolver().loadPomFromFile("pom.xml")
            .resolve(coordinate).withTransitivity().asFile();
        for (File f : files) {
            archive.addAsLibrary(f);
        }
    }

    /**
     * Utility method to parse JSON in a Response object.
     *
     * Asserts that the response has HTTP status code 200 and a parseable
     * JSON body corresponding to a de.intevation.lada.util.rest.Response.
     *
     * @param response The response to be parsed.
     * @return Parsed JsonObject or null in case of failure
     */
    public static JsonObject parseResponse(Response response) {
        return parseResponse(response, null);
    }

    /**
     * Utility method to parse JSON in a Response object.
     *
     * Asserts that the response has HTTP status code 200 and a parseable
     * JSON body corresponding to a de.intevation.lada.util.rest.Response.
     *
     * @param response The response to be parsed.
     * @param protocol Protocol to add exception info in case of failure
     * @return Parsed JsonObject or null in case of failure
     */
    public static JsonObject parseResponse(
        Response response,
        Protocol protocol
    ) {
        return parseResponse(response, protocol, Response.Status.OK);
    }

    /**
     * Utility method to check status and parse JSON in a Response object
     * corresponding to a de.intevation.lada.util.rest.Response.
     *
     * @param response The response to be parsed.
     * @param protocol Protocol to add exception info in case of failure
     * @param expectedStatus Expected HTTP status code
     * @return Parsed JsonObject or null in case of (expected) failure
     */
    public static JsonObject parseResponse(
        Response response,
        Protocol protocol,
        Response.Status expectedStatus
    ) {
        JsonObject content = parseSimpleResponse(
            response, protocol, expectedStatus);

        /* Verify the response*/
        if (Response.Status.OK.equals(expectedStatus)) {
            final String successKey = "success", messageKey = "message";
            assertContains(content, successKey);
            Assert.assertTrue("Unsuccessful response object:\n" + content,
                content.getBoolean(successKey));
            if (protocol != null) {
                protocol.addInfo(
                    successKey, content.getBoolean(successKey));
            }
            assertContains(content, messageKey);
            Assert.assertEquals("200", content.getString(messageKey));
            if (protocol != null) {
                protocol.addInfo(
                    messageKey, content.getString(messageKey));
            }
        }

        return content;
    }

    /**
     * Utility method to parse JSON in a Response object.
     *
     * Asserts that the response has HTTP status code 200 and a parseable
     * JSON body.
     *
     * @param response The response to be parsed.
     * @param protocol Protocol to add exception info in case of failure
     * @return Parsed JsonObject or null in case of (expected) failure
     */
    public static JsonObject parseSimpleResponse(
        Response response,
        Protocol protocol
    ) {
        return parseSimpleResponse(response, protocol, Response.Status.OK);
    }

    /**
     * Utility method to check status and parse JSON in a Response object.
     *
     * @param response The response to be parsed.
     * @param protocol Protocol to add exception info in case of failure
     * @param expectedStatus Expected HTTP status code
     * @return Parsed JsonObject or null in case of (expected) failure
     */
    public static JsonObject parseSimpleResponse(
        Response response,
        Protocol protocol,
        Response.Status expectedStatus
    ) {
        String responseBody = response.readEntity(String.class);
        logger.trace(responseBody);
        Assert.assertEquals(
            "Unexpected response status code",
            expectedStatus.getStatusCode(),
            response.getStatus());

        if (Response.Status.OK.equals(expectedStatus)) {
            try {
                JsonObject content = Json.createReader(
                    new StringReader(responseBody)).readObject();
                return content;
            } catch (JsonException je) {
                if (protocol != null) {
                    protocol.addInfo("exception", je.getMessage());
                }
                Assert.fail(je.getMessage());
            }
        }
        return null;
    }

    /**
     * Assert that a JsonObject contains a given key.
     *
     * @param json The JSON object to test.
     * @param key The key expected in the JSON object.
     */
    public static void assertContains(JsonObject json, String key) {
        Assert.assertTrue(
            "Response does not contain expected key '" + key + "': "
            + json.toString(),
            json.containsKey(key));
    }
}
