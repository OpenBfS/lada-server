/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada;

import java.net.URL;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
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
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.postgresql.ds.PGSimpleDataSource;

import de.intevation.lada.util.rest.JSONBConfig;


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
     * The base URL to be used for interface tests.
     */
    @ArquillianResource
    protected URL baseUrl;

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

        this.client = ClientBuilder.newClient().register(JSONBConfig.class);
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
        final String threadContextProviderResource = "META-INF/services/"
            + "jakarta.enterprise.concurrent.spi.ThreadContextProvider";
        WebArchive archive = ShrinkWrap.create(WebArchive.class, archiveName)
            .add(new FileAsset(new File("src/main/webapp/WEB-INF/web.xml")),
                "WEB-INF/web.xml")
            .addPackages(true, ClassLoader.getSystemClassLoader()
                .getDefinedPackage("de.intevation.lada"))
            .addAsResource("lada_en.properties", "lada_en.properties")
            .addAsResource("ValidationMessages.properties",
                "ValidationMessages.properties")
            .addAsLibraries(compileAndRuntimeDeps)
            .addAsResource("META-INF/test-persistence.xml",
                "META-INF/persistence.xml")
            .addAsResource(beansXmlResource, beansXmlResource)
            .addAsResource(threadContextProviderResource,
                threadContextProviderResource)
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
     * Also asserts that the response does not contain validation errors.
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
     * Also asserts that the response does not contain validation errors,
     * if expected status code is 200.
     *
     * @param response The response to be parsed.
     * @param expectedStatus Expected HTTP status code
     * @return Parsed JSON
     */
    public static JsonValue parseResponse(
        Response response,
        Response.Status expectedStatus
    ) {
        JsonValue json = parseResponse(
            response, new GenericType<JsonValue>() { }, expectedStatus);
        return Response.Status.OK.equals(expectedStatus)
            // Successful response should not contain validation errors
            ? verifyResponseObject(json)
            : json;
    }

    /**
     * Utility method to parse JSON in a Response object.
     *
     * Asserts that the response has HTTP status code 200 and a parseable
     * JSON body matching the expected entity type.
     *
     * @param <T> Expected response entity type
     * @param response The response to be parsed.
     * @param entityType Expected response entity type
     * @return Parsed entity
     */
    public static <T> T parseResponse(
        Response response, Class<T> entityType
    ) {
        return parseResponse(
            response, new GenericType<T>(entityType), Response.Status.OK);
    }

    /**
     * Utility method to parse JSON in a Response object.
     *
     * Asserts that the response has HTTP status code 200 and a parseable
     * JSON body matching the expected entity type.
     *
     * @param <T> Expected response entity type
     * @param response The response to be parsed.
     * @param entityType Expected response entity type
     * @return Parsed entity
     */
    public static <T> T parseResponse(
        Response response, GenericType<T> entityType
    ) {
        return parseResponse(response, entityType, Response.Status.OK);
    }

    /**
     * Utility method to check status and parse JSON in a Response object.
     *
     * @param <T> Expected response entity type
     * @param response The response to be parsed.
     * @param entityType Expected response entity type
     * @param expectedStatus Expected HTTP status code
     * @return Parsed entity
     */
    public static <T> T parseResponse(
        Response response,
        GenericType<T> entityType,
        Response.Status expectedStatus
    ) {
        if (MediaType.APPLICATION_JSON_TYPE.isCompatible(
                response.getMediaType())
        ) {
            return assertResponseStatus(response, entityType, expectedStatus);
        }
        // Non-JSON response body
        assertResponseStatus(
            response, new GenericType<String>() { }, expectedStatus);
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
        return assertResponseStatus(
            response, new GenericType<String>() { }, Response.Status.OK);
    }

    /**
     * Utility method to check status of a Response object.
     *
     * @param <T> Expected response entity type
     * @param response The response to be parsed.
     * @param entityType Expected response entity type
     * @param expectedStatus Expected HTTP status code
     * @return Response body
     */
    public static <T> T assertResponseStatus(
        Response response,
        GenericType<T> entityType,
        Response.Status expectedStatus
    ) {
        T responseBody = response.readEntity(entityType);
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

    protected static void assertJsonContainsValidationMessage(JsonObject json,
            String expectedPath, String expectedMessage) {
        assertNotNull("JSON must not be null", json);
        JsonArray violations = json.getJsonArray("parameterViolations");
        AtomicBoolean containsPath = new AtomicBoolean(false);
        violations.forEach(violation -> {
            String path = violation.asJsonObject().getString("path", null);
            String message = violation.asJsonObject()
                .getString("message", null);
            if (path.equals(expectedPath)) {
                containsPath.set(true);
                assertEquals(
                    "Validation messages do not match",
                    expectedMessage, message);
            }
        });
        assertTrue("Found no validation message for the given path",
            containsPath.get());
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
