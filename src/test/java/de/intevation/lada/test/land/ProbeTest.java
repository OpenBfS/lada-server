/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.land;

import java.net.URL;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.Response.Status;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;

import de.intevation.lada.BaseTest;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.test.ServiceTest;

/**
 * Test probe entities.
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class ProbeTest extends ServiceTest {

    private JsonObject expectedById;
    private JsonObject create;

    @Override
    public void init(
        Client c,
        URL baseUrl
    ) {
        super.init(c, baseUrl);
        // Attributes with timestamps
        timestampAttributes = Arrays.asList(new String[]{
            "lastMod",
            "sampleStartDate",
            "schedStartDate",
            "schedEndDate",
            "treeMod"
        });

        // Prepare expected probe object
        JsonObject probe = filterJsonArrayById(
            readXmlResource("datasets/dbUnit_lada.xml", Sample.class),
            1000);
        expectedById = convertObject(probe)
            .addNull("midSampleDate")
            .addNull("sampleEndDate")
            .addNull("datasetCreatorId")
            .addNull("mpgCategId")
            .add("readonly", false)
            .add("owner", true)
            .build();

        // Load probe object to test POST request
        create = readJsonResource("/datasets/probe.json");
        Assert.assertNotNull(create);
    }

    /**
     * Execute the tests.
     */
    public final void execute() {
        get("rest/sample", Status.METHOD_NOT_ALLOWED);

        final String warningsKey = "warnings";
        final String expectedWarningKey = "geolocats";

        // Assert that GET receives validation warnings in response
        MatcherAssert.assertThat(
            getById("rest/sample/1000", expectedById)
                .getJsonObject(warningsKey).keySet(),
            CoreMatchers.hasItem(expectedWarningKey));

        JsonObject created = create("rest/sample", create);
        // Assert that POST receives validation warnings in response
        MatcherAssert.assertThat(
            created.getJsonObject(warningsKey).keySet(),
            CoreMatchers.hasItem(expectedWarningKey));

        final String updateFieldKey = "mainSampleId";
        final String newValue = "130510002";
        // Assert that PUT receives validation warnings in response
        MatcherAssert.assertThat(
            update("rest/sample/1000", updateFieldKey, "120510002", newValue)
                .asJsonObject().getJsonObject(warningsKey).keySet(),
            CoreMatchers.hasItem(expectedWarningKey));

        // Ensure invalid envDescripDisplay is rejected
        update(
            "rest/sample/1000",
            "envDescripDisplay",
            "D: 59 04 01 00 05 05 01 02 00 00 00 00",
            "",
            Status.BAD_REQUEST);

        // Test localized validation during sample creation
        // Errors
        Map<Locale, String> msgs = Map.of(
            Locale.GERMAN, "Größe muss zwischen 0 und 3 sein",
            Locale.US, "size must be between 0 and 3");
        final String envMediumId = "envMediumId";
        JsonObject payload = Json.createObjectBuilder()
            .add(envMediumId, "too much text for envMediumId")
            .add("regulationId", 1)
            .add("sampleMethId", 1)
            .add("isTest", false)
            .add("oprModeId", 1)
            .build();
        for (Locale language: msgs.keySet()) {
            JsonArray violations = create(
                "rest/sample", payload, language, Status.BAD_REQUEST)
                .getJsonArray("parameterViolations");
            violations.forEach(val -> {
                JsonObject obj = (JsonObject) val;
                if (
                    obj.getString("path").equals("create.arg0." + envMediumId)
                ) {
                    Assert.assertEquals(
                        msgs.get(language), obj.getString("message"));
                }
            });
        }
        // Warnings
        Map<Locale, String> wrngs = Map.of(
            Locale.GERMAN, "darf nicht null sein",
            Locale.US, "must not be null");
        final String measFacilId = "06010";
        JsonObject wrngPayload = Json.createObjectBuilder()
            .add("measFacilId", measFacilId)
            .add("apprLabId", measFacilId)
            .add("regulationId", 2)
            .add("sampleMethId", 1)
            .add("isTest", false)
            .add("oprModeId", 1)
            .build();
        for (Locale language: wrngs.keySet()) {
            JsonObject response = create(
                "rest/sample", wrngPayload, language, Status.OK);
            final String wrngsKey = "warnings";
            BaseTest.assertContains(response, wrngsKey);
            String violation = response.getJsonObject(wrngsKey)
                .getJsonArray("sampleStartDate")
                .getString(0);
            Assert.assertEquals(wrngs.get(language), violation);
        }

        getAuditTrail(
            "rest/audit/probe/1000",
            updateFieldKey,
            newValue);
        delete("rest/sample/" + created.get("id"));
    }
}
