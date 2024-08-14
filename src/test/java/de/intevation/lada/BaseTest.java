/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada;

import java.io.StringReader;
import java.sql.SQLException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParsingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;

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
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
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
     * Database connection.
     */
    private IDatabaseConnection con;

    /**
     * Enable verbose output for tests.
     */
    protected static boolean verboseLogging = false;

    protected String testDatasetName;

    private static final String DATASETS_DIR = "datasets";
    private static final String CLEANUP_SCRIPT = DATASETS_DIR + "/cleanup.sql";
    private static final String NULL_PLACEHOLDER = "[null]";

    /**
     * Set up shared infrastructure for test methods.
     */
    @Before
    public void setup()
        throws DatabaseUnitException, SQLException, IOException {
        this.client = ClientBuilder.newClient();

        // Set up database connection
        PGSimpleDataSource ds = new PGSimpleDataSource();
        final String testDbUserPw = "lada_test";
        ds.setServerNames(new String[]{"db"});
        ds.setDatabaseName(testDbUserPw);
        ds.setUser(testDbUserPw);
        ds.setPassword(testDbUserPw);
        this.con = new DatabaseConnection(ds.getConnection());
        DatabaseConfig config = con.getConfig();
        config.setProperty(
            DatabaseConfig.FEATURE_QUALIFIED_TABLE_NAMES,
            true);
        config.setProperty(
            DatabaseConfig.PROPERTY_DATATYPE_FACTORY,
            new PostgresqlDataTypeFactory());

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
        //Get compile and runtime dependencies from pom.xml
        // Note: Test dependencies can not be added this way as they seem to
        //       break the deployment
        File[] compileAndRuntimeDeps = Maven.resolver()
            .loadPomFromFile("pom.xml")
            .importCompileAndRuntimeDependencies().resolve()
            .withTransitivity().asFile();

        final String beansXmlResource = "META-INF/beans.xml";
        WebArchive archive = ShrinkWrap.create(WebArchive.class, archiveName)
            .addPackages(true, ClassLoader.getSystemClassLoader()
                .getDefinedPackage("de.intevation.lada"))
            .addAsResource("lada_en.properties", "lada_en.properties")
            .addAsResource("ValidationMessages.properties",
                "ValidationMessages.properties")
            .addAsLibraries(compileAndRuntimeDeps)
            .addAsResource("META-INF/test-persistence.xml",
                "META-INF/persistence.xml")
            .addAsResource(beansXmlResource, beansXmlResource)
            //Add cleanup script and datasets for container mode tests
            .addAsResource(DATASETS_DIR, DATASETS_DIR);
        //Add additional test dependencies
        addWithDependencies("org.postgresql:postgresql", archive);
        addWithDependencies("net.postgis:postgis-jdbc", archive);
        addWithDependencies("org.dbunit:dbunit", archive);
        addWithDependencies(
            "org.jboss.arquillian.extension:arquillian-transaction-api",
            archive);
        addWithDependencies(
            "org.jboss.arquillian.extension:arquillian-transaction-jta",
            archive);
        addWithDependencies(
            "org.eclipse.parsson:parsson",
            archive);
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
        con.close();
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
     * JSON body.
     *
     * @param response The response to be parsed.
     * @return Parsed JSON
     */
    public static JsonValue parseResponse(
        Response response
    ) {
        return parseResponse(response, Response.Status.OK);
    }

    /**
     * Utility method to check status and parse JSON in a Response object.
     *
     * @param response The response to be parsed.
     * @param expectedStatus Expected HTTP status code
     * @return Parsed JSON
     */
    public static JsonValue parseResponse(
        Response response,
        Response.Status expectedStatus
    ) {
        String responseBody = assertResponseStatus(response, expectedStatus);
        if (responseBody != null && !responseBody.isEmpty()) {
            try (JsonReader reader = Json.createReader(
                    new StringReader(responseBody))) {
                JsonValue json = reader.readValue();
                return Response.Status.OK.equals(expectedStatus)
                    // Successful response should not contain validation errors
                    ? verifyResponseObject(json)
                    : json;
            } catch (JsonParsingException je) {
                // Non-JSON response body
                return null;
            }
        }
        return null;
    }

    /**
     * Recursively check JSON document for errors.
     * @param json The JSON document
     * @return The unaltered document if no errors were found
     */
    private static JsonValue verifyResponseObject(JsonValue json) {
        if (JsonValue.ValueType.OBJECT.equals(json.getValueType())) {
            final String errKey = "errors";
            JsonObject jo = json.asJsonObject();
            if (jo.containsKey(errKey)
                && JsonValue.ValueType.OBJECT.equals(
                    jo.getValue("/" + errKey).getValueType())
                && !JsonValue.EMPTY_JSON_OBJECT.equals(
                    jo.getJsonObject(errKey))
            ) {
                Assert.fail(
                    String.format("Response contains errors: %s",
                        jo.getJsonObject(errKey)));
            }
        } else if (JsonValue.ValueType.ARRAY.equals(json.getValueType())) {
            for (JsonValue jv: json.asJsonArray()) {
                verifyResponseObject(jv);
            }
        }
        return json;
    }

    /**
     * Utility method to check status of a Response object.
     *
     * @param response The response to be parsed.
     * @return Response body
     */
    public static String assertResponseOK(
        Response response
    ) {
        return assertResponseStatus(response, Response.Status.OK);
    }

    /**
     * Utility method to check status of a Response object.
     *
     * @param response The response to be parsed.
     * @param expectedStatus Expected HTTP status code
     * @return Response body
     */
    public static String assertResponseStatus(
        Response response,
        Response.Status expectedStatus
    ) {
        String responseBody = response.readEntity(String.class);
        Assert.assertEquals(
            "Unexpected status code with response\n" + responseBody + "\n",
            expectedStatus.getStatusCode(),
            response.getStatus());
        return responseBody;
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

        op.execute(con, dataset);
    }

    private void cleanup()
        throws DatabaseUnitException, SQLException, IOException {
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
        con.getConnection().prepareStatement(sql).execute();
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
        IDataSet xmlDataset = new FlatXmlDataSetBuilder()
            .setColumnSensing(true)
            .build(getClass().getClassLoader()
                .getResourceAsStream(expectedDataset));
        //Replace null placeholders with null references
        ReplacementDataSet iExpectedDataset = new ReplacementDataSet(
                xmlDataset);
        iExpectedDataset.addReplacementObject(NULL_PLACEHOLDER, null);

        IDataSet iActualDataset = con.createDataSet();
        Assertion.assertEqualsIgnoreCols(
            iExpectedDataset, iActualDataset, tableName, ignoredCols);
    }

    protected IDatabaseConnection getConnection() {
        return con;
    }
}
