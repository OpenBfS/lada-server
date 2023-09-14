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
import java.util.Locale;

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
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


@RunWith(Arquillian.class)
public class I18nTest extends BaseTest {

    private static final String HEADER_X_SHIB_ROLES = "X-SHIB-roles";
    private static final String HEADER_X_SHIB_USER = "X-SHIB-user";

    private static final String VALIDATION_KEY_PREFIX = "validation#";

    @ArquillianResource
    private URL baseUrl;

    private Locale[] locales = new Locale[]{Locale.GERMAN, Locale.US};

    /**
     * Constructor.
     */
    public I18nTest() {
        testDatasetName = "datasets/dbUnit_import.xml";
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
        for (Locale locale: locales) {
            Response response = client.target(
                baseUrl + "data/import/laf")
                .request()
                .header(HEADER_X_SHIB_USER, BaseTest.testUser)
                .header(HEADER_X_SHIB_ROLES, BaseTest.testRoles)
                .header("X-LADA-MST", "06010")
                .acceptLanguage(locale)
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
}
