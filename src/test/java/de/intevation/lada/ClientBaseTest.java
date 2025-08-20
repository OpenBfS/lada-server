/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada;

import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;

import de.intevation.lada.util.rest.JSONBConfig;


/**
 * Base class for Lada server tests using
 * {@link org.jboss.arquillian.container.test.api.RunAsClient}.
 */
public class ClientBaseTest extends BaseTest {

    /**
     * The client used for interface tests.
     */
    private static Client client;

    /**
     * The base URL used for interface tests.
     */
    @ArquillianResource
    private URL baseUrl;

    /**
     * Basis for building requests for interface tests.
     */
    protected WebTarget target;

    /**
     * Set up shared infrastructure for test methods.
     */
    @BeforeClass
    public static void setupClient() {
        client = ClientBuilder.newClient().register(JSONBConfig.class);
    }

    @Before
    public void setupTarget() {
        if (this.target == null) {
            this.target = client.target(this.baseUrl.toString());
        }
    }

    /**
     * Tear down shared infrastructure for test methods.
     */
    @AfterClass
    public static void tearDownClient() {
        client.close();
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
    public static JsonValue parseResponse(Response response) {
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
        Class<T> entityType,
        Response.Status expectedStatus
    ) {
        return parseResponse(
            response, new GenericType<T>(entityType), expectedStatus);
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
    public static String assertResponseOK(Response response) {
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
        if (expectedStatus.getStatusCode() != response.getStatus()) {
            Assert.assertEquals(
                    "Unexpected status code with response\n"
                            + response.readEntity(String.class) + "\n",
                    expectedStatus.getStatusCode(),
                    response.getStatus());
        }
        return response.readEntity(entityType);
    }

    protected static void assertJsonContainsValidationMessage(
        JsonObject json,
        String expectedPath,
        String expectedMessage
    ) {
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
}
