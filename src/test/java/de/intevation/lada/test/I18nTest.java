/* Copyright (C) 2023 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test;

import java.io.StringReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.intevation.lada.BaseTest;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@RunWith(Arquillian.class)
public class I18nTest extends BaseTest {

    private static final String HEADER_ACCEPT = "Accept";
    private static final String HEADER_ACCEPT_LANGUAGE = "Accept-Language";
    private static final String HEADER_X_SHIB_ROLES = "X-SHIB-roles";
    private static final String HEADER_X_SHIB_USER = "X-SHIB-user";

    private static final String KEY_PARAMETER_VIOLATIONS
        = "parameterViolations";
    private static final String PATH_PREFIX = "create.arg0.";
    private static final String VALIDATION_KEY_PREFIX = "validation#";

    private static final String EN_US = "en-US";
    private static final String DE_DE = "de-De";

    private static final String ENV_MEDIUM_ID = "envMediumId";
    private static final int ENV_MEDIUM_ID_MAX_SIZE = 15;

    @ArquillianResource
    private URL baseUrl;

    private String[] locales = new String[]{DE_DE, EN_US};

    /**
     * Constructor.
     */
    public I18nTest() {
        testDatasetName = "datasets/dbUnit_import.xml";
    }

    /**
     * Test localized validation during sample creation.
     */
    @Test
    @RunAsClient
    public void testSampleValidation() {
        Map<String, String> msgs = new HashMap<String, String>();
        msgs.put(DE_DE, "Größe muss zwischen 0 und 3 sein");
        msgs.put(EN_US, "size must be between 0 and 3");
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add(ENV_MEDIUM_ID,
            StringUtils.repeat("*", ENV_MEDIUM_ID_MAX_SIZE));
        builder.add("regulationId", 1);
        builder.add("sampleMethId", 1);
        builder.add("isTest", false);
        builder.add("oprModeId", 1);

        createAndCheckForLocalizedMessage(
            "rest/sample",
            builder.build().toString(), ENV_MEDIUM_ID, msgs);
    }

    /**
     * Test localized validation during sample import.
     */
    @Test
    @RunAsClient
    public void testImportValidation() {
        final String laf =
            "%PROBE%\n"
            + "UEBERTRAGUNGSFORMAT           \"7\"\n"
            + "VERSION                       \"0084\"\n"
            + "DATENBASIS_S                  02\n"
            + "NETZKENNUNG                   \"06\"\n"
            + "HAUPTPROBENNUMMER             \"xxx\"\n"
            + "PROBENART                     \"E\"\n"
            + "ZEITBASIS_S                   2\n"
            + "SOLL_DATUM_UHRZEIT_A          20120101 0000\n"
            + "SOLL_DATUM_UHRZEIT_E          20100131 2159\n"
            + "PROBENAHME_DATUM_UHRZEIT_A    20120104 0630\n"
            + "PROBENAHME_DATUM_UHRZEIT_E    20100104 0630\n"
            + "UMWELTBEREICH_S               \"N23\"\n"
            + "DESKRIPTOREN                  \"012503020402000002000000\"\n"
            + "PROBENAHMEINSTITUTION         \"AV16\"\n"
            + "P_HERKUNFTSLAND_S             00000000\n"
            + "P_GEMEINDESCHLUESSEL          06634014\n"
            + "P_KOORDINATEN_S               05 \"32541043\" \"5665935\"\n"
            + " %ENDE%\"\n";

        //Import validation should fall back to default
        final String expectedMessage = "darf nicht null sein";
        String key = "probe";
        for (String locale: locales) {
            Response response = client.target(
                baseUrl + "data/import/laf")
                .request()
                .header(HEADER_X_SHIB_USER, BaseTest.testUser)
                .header(HEADER_X_SHIB_ROLES, BaseTest.testRoles)
                .header("X-LADA-MST", "06010")
                .header(HEADER_ACCEPT_LANGUAGE, locale)
                .header(HEADER_ACCEPT, MediaType.APPLICATION_JSON)
                .post(Entity.entity(laf, MediaType.TEXT_PLAIN));
            String responseBody = response.readEntity(String.class);
            JsonObject content = Json.createReader(
                    new StringReader(responseBody)).readObject();
            JsonArray parameterViolations = content
                .getJsonObject("data")
                .getJsonObject("errors")
                .getJsonArray("xxx");

            parameterViolations.forEach(val -> {
                JsonObject obj = (JsonObject) val;
                if (obj.getString("value")
                        .equals(VALIDATION_KEY_PREFIX + key)) {
                    String message = obj.getString("code");
                    Assert.assertTrue(message.equals(expectedMessage));
                }
            });
        }
    }

    /**
     * Try to create an object and check for a localized validation message.
     * @param url
     * @param payload
     * @param key
     * @param values
     */
    private void createAndCheckForLocalizedMessage(
            String url, String payload,
            String key, Map<String, String> values) {
        for (String locale: locales) {
            WebTarget target = client.target(baseUrl + url);
            Response response = target.request()
                .header(HEADER_X_SHIB_USER, BaseTest.testUser)
                .header(HEADER_X_SHIB_ROLES, BaseTest.testRoles)
                .header(HEADER_ACCEPT_LANGUAGE, locale)
                .header(HEADER_ACCEPT, MediaType.APPLICATION_JSON)
                .post(Entity.entity(payload, MediaType.APPLICATION_JSON));
            Assert.assertTrue(
                response.getStatus() == Status.BAD_REQUEST.getStatusCode());
            String responseBody = response.readEntity(String.class);
            JsonObject content = Json.createReader(
                    new StringReader(responseBody)).readObject();
            JsonArray parameterViolations
                = content.getJsonArray(KEY_PARAMETER_VIOLATIONS);
            parameterViolations.forEach(val -> {
                JsonObject obj = (JsonObject) val;
                if (obj.getString("path").equals(PATH_PREFIX + key)) {
                    String message = obj.getString("message");
                    String expectedMessage = values.get(locale);
                    Assert.assertTrue(message.equals(expectedMessage));
                }
            });
        }
    }
}
