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
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import org.jboss.logging.Logger;
import org.dbunit.Assertion;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
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
     * The client to be used for interface tests.
     */
    protected Client client;

    /**
     * Enable verbose output for tests.
     */
    protected static boolean verboseLogging = false;

    protected String testDatasetName;

    protected static PGSimpleDataSource ds = new PGSimpleDataSource();

    protected IDataSet dbDataset;

    private static final String DATASETS_DIR = "datasets";
    private static final String CLEANUP_SCRIPT = DATASETS_DIR + "/cleanup.sql";
    private static final String NULL_PLACEHOLDER = "[null]";

    /**
     * Setup JDBC data source.
     */
    @BeforeClass
    public static void setupDataSource() {
        ds.setServerNames(new String[]{"db"});
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
        this.client = ClientBuilder.newClient();
        this.dbDataset = getNewDbConnection().createDataSet();

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
                "META-INF/persistence.xml")
            //Add cleanup script and datasets for container mode tests
            .addAsResource(DATASETS_DIR, DATASETS_DIR);
        addWithDependencies("org.geotools:gt-api", archive);
        addWithDependencies("org.geotools:gt-referencing", archive);
        addWithDependencies("org.geotools:gt-epsg-hsql", archive);
        addWithDependencies("org.geotools:gt-opengis", archive);
        addWithDependencies("io.github.classgraph:classgraph", archive);
        addWithDependencies("org.postgresql:postgresql", archive);
        addWithDependencies("net.postgis:postgis-jdbc", archive);
        return archive;
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
    public static JsonObject parseResponse(
        Response response
    ) {
        return parseResponse(response, Response.Status.OK);
    }

    /**
     * Utility method to check status and parse JSON in a Response object
     * corresponding to a de.intevation.lada.util.rest.Response.
     *
     * @param response The response to be parsed.
     * @param expectedStatus Expected HTTP status code
     * @return Parsed JsonObject or null in case of (expected) failure
     */
    public static JsonObject parseResponse(
        Response response,
        Response.Status expectedStatus
    ) {
        JsonObject content = parseSimpleResponse(
            response, expectedStatus);

        /* Verify the response*/
        if (Response.Status.OK.equals(expectedStatus)) {
            final String successKey = "success", messageKey = "message";
            assertContains(content, successKey);
            Assert.assertTrue("Unsuccessful response object:\n" + content,
                content.getBoolean(successKey));
            assertContains(content, messageKey);
            Assert.assertEquals("200", content.getString(messageKey));
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
     * @return Parsed JsonObject or null in case of (expected) failure
     */
    public static JsonObject parseSimpleResponse(
        Response response
    ) {
        return parseSimpleResponse(response, Response.Status.OK);
    }

    /**
     * Utility method to check status and parse JSON in a Response object.
     *
     * @param response The response to be parsed.
     * @param expectedStatus Expected HTTP status code
     * @return Parsed JsonObject or null in case of (expected) failure
     */
    public static JsonObject parseSimpleResponse(
        Response response,
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
        setupDataSource();
        IDataSet dataset = new FlatXmlDataSetBuilder()
            .setColumnSensing(true)
            .build(getClass().getClassLoader()
                .getResourceAsStream(testDatasetName));

        IDatabaseConnection con = getNewDbConnection();
        try {
            op.execute(con, dataset);
        } finally {
            con.close();
        }
    }

    private void cleanup() throws SQLException, IOException {
        setupDataSource();
        String sql;
        //Read cleanup script
        try (InputStream is = getClass().getClassLoader()
                .getResourceAsStream(CLEANUP_SCRIPT)) {
            if (is == null) {
                throw new IOException(
                    "Could not find cleanup script: " + CLEANUP_SCRIPT);
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

    /**
     * Check if given table matches the expected dataset.
     *
     * The expected datasets must contain all of the tables columns to ensure
     * the correct column count.
     * Null values can be set using the "[null]" placeholder.
     * @param expectedDataset Path to expected xml dataset.
     * @param tableName Table to check.
     * @param ignoredCols Columns to ignore.
     * @throws DatabaseUnitException
     * @throws SQLException
     */
    public void shouldMatchDataSet(
            String expectedDataset,
            String tableName, String[] ignoredCols)
            throws DatabaseUnitException, SQLException {
        setupDataSource();
        IDatabaseConnection con = getNewDbConnection();
        IDataSet xmlDataset = new FlatXmlDataSetBuilder()
            .setColumnSensing(true)
            .build(getClass().getClassLoader()
                .getResourceAsStream(expectedDataset));
        //Replace null placeholders with null references
        ReplacementDataSet iExpectedDataset = new ReplacementDataSet(
                xmlDataset);
        iExpectedDataset.addReplacementObject(NULL_PLACEHOLDER, null);

        try {
            IDataSet iActualDataset = con.createDataSet();
            Assertion.assertEqualsIgnoreCols(
                iExpectedDataset, iActualDataset, tableName, ignoredCols);
        } finally {
            con.close();
        }

    }

    private IDatabaseConnection getNewDbConnection()
            throws DatabaseUnitException, SQLException {
        IDatabaseConnection con = new DatabaseConnection(ds.getConnection());
        DatabaseConfig config = con.getConfig();
            config.setProperty(
                "http://www.dbunit.org/features/qualifiedTableNames", true);
            config.setProperty(
                DatabaseConfig.PROPERTY_DATATYPE_FACTORY,
                new PostgresqlDataTypeFactory());
        return con;
    }
}
