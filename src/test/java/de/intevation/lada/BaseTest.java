/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import org.jboss.logging.Logger;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.postgresql.ds.PGSimpleDataSource;

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

    protected String testDatasetName;

    protected static PGSimpleDataSource ds = new PGSimpleDataSource();

    /**
     * Setup JDBC data source.
     */
    @BeforeClass
    public static void setupDataSource() {
        ds.setServerNames(new String[]{"lada_db"});
        ds.setDatabaseName("lada_test");
        ds.setUser("lada_test");
        ds.setPassword("lada_test");
    }

    /**
     * Set up shared infrastructure for test methods.
     */
    @Before
    public void setup() throws DatabaseUnitException, SQLException, IOException {
        this.cleanup();
        this.testProtocol = new ArrayList<Protocol>();
        this.client = ClientBuilder.newClient();

        // Insert test data
        doDbOperation(DatabaseOperation.CLEAN_INSERT);
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
        addWithDependencies("io.github.classgraph:classgraph", archive);
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
    public void tearDown()
            throws DatabaseUnitException, SQLException, IOException {
        this.client.close();

        // Ensure clean database after test
        cleanup();
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

    private void doDbOperation(
        DatabaseOperation op
    ) throws DatabaseUnitException, SQLException {
        IDataSet dataset = new FlatXmlDataSetBuilder()
            .setColumnSensing(true)
            .build(getClass().getClassLoader()
                .getResourceAsStream(testDatasetName));

        IDatabaseConnection con = new DatabaseConnection(ds.getConnection());
        try {
            DatabaseConfig config = con.getConfig();
            config.setProperty(
                "http://www.dbunit.org/features/qualifiedTableNames", true);
            config.setProperty(
                DatabaseConfig.PROPERTY_DATATYPE_FACTORY,
                new PostgresqlDataTypeFactory());
            op.execute(con, dataset);
        } finally {
            con.close();
        }
    }

    private void cleanup() throws SQLException, IOException {
        String cleanupScript = "datasets/cleanup.sql";
        String sql;
        //Read cleanup script
        try (InputStream is = getClass().getClassLoader()
                .getResourceAsStream(cleanupScript)) {
            if (is == null) {
                throw new IOException(
                    "Could not find cleanup script: " + cleanupScript);
            }
            try (InputStreamReader isr = new InputStreamReader(is);
            BufferedReader reader = new BufferedReader(isr)) {
                sql = reader.lines().collect(
                    Collectors.joining(System.lineSeparator()));
            }
        }
        Connection con = ds.getConnection();
        PreparedStatement stmt = con.prepareStatement(sql);
        stmt.execute();
    }
}
