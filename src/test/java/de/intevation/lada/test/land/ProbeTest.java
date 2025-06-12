/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.land;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.Response.Status;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.jboss.resteasy.api.validation.ResteasyConstraintViolation;
import org.jboss.resteasy.api.validation.ViolationReport;
import org.junit.Assert;

import de.intevation.lada.BaseTest;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.lada.Sample_;
import de.intevation.lada.rest.SampleService;
import de.intevation.lada.test.ServiceTest;

/**
 * Test probe entities.
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class ProbeTest extends ServiceTest {

    private static final String SAMPLE_SERVICE_URL =
        UriBuilder.fromResource(SampleService.class).build() + "/";

    private static final int SAMPLE_ID = 1000;

    private JsonObject expectedById;
    private JsonObject create;

    @Override
    public void init(WebTarget t) {
        super.init(t);

        // Attributes with timestamps
        timestampAttributes = Arrays.asList(new String[]{
            Sample_.LAST_MOD,
            Sample_.SAMPLE_START_DATE,
            Sample_.SCHED_START_DATE,
            Sample_.SCHED_END_DATE,
            Sample_.TREE_MOD
        });

        // Prepare expected probe object
        JsonObject probe = BaseTest.filterJsonArrayById(
            BaseTest.readXmlResource("datasets/dbUnit_lada.xml", Sample.class),
            SAMPLE_ID);
        expectedById = convertObject(probe)
            .addNull(Sample_.MID_SAMPLE_DATE)
            .addNull(Sample_.SAMPLE_END_DATE)
            .addNull(Sample_.DATASET_CREATOR_ID)
            .addNull(Sample_.MPG_CATEG_ID)
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
        get(SAMPLE_SERVICE_URL, Status.METHOD_NOT_ALLOWED);

        final String warningsKey = "warnings";
        final String expectedWarningKey = "geolocats";

        // Assert that GET receives validation warnings in response
        MatcherAssert.assertThat(
            getById(SAMPLE_SERVICE_URL + SAMPLE_ID, expectedById)
                .getJsonObject(warningsKey).keySet(),
            CoreMatchers.hasItem(expectedWarningKey));

        JsonObject created = create(SAMPLE_SERVICE_URL, create);
        // Assert that POST receives validation warnings in response
        MatcherAssert.assertThat(
            created.getJsonObject(warningsKey).keySet(),
            CoreMatchers.hasItem(expectedWarningKey));

        /* Assert that validation of constraints from Default as well as
           CreateErrors group are performed. */
        Sample forbiddenExtIdSample = new Sample();
        forbiddenExtIdSample.setExtId("ZDB123456789012Y");
        List<ResteasyConstraintViolation> viols = create(
            SAMPLE_SERVICE_URL,
            forbiddenExtIdSample,
            Response.Status.BAD_REQUEST,
            ViolationReport.class).getParameterViolations();
        Map<String,String> errs = viols.stream().collect(
            Collectors.toMap(
                err -> {
                    String[] pElems = err.getPath().split("\\.");
                    return pElems[pElems.length - 1];
                },
                err -> err.getMessage()
            ));
        MatcherAssert.assertThat(errs.entrySet(),
            CoreMatchers.hasItems(
                new AbstractMap.SimpleImmutableEntry<>(
                    Sample_.EXT_ID,
                    "muss mit \"^(?!ZDB\\d{12}Y$).*$\" übereinstimmen"),
                new AbstractMap.SimpleImmutableEntry<>(
                    Sample_.REGULATION_ID,
                    "Wert nicht gesetzt")));

        final String updateFieldKey = Sample_.MAIN_SAMPLE_ID;
        final String newValue = "130510002";
        // Assert that PUT receives validation warnings in response
        MatcherAssert.assertThat(
            update(SAMPLE_SERVICE_URL + SAMPLE_ID,
                updateFieldKey, "120510002", newValue)
                .asJsonObject().getJsonObject(warningsKey).keySet(),
            CoreMatchers.hasItem(expectedWarningKey));

        // Ensure invalid envDescripDisplay is rejected
        update(
            SAMPLE_SERVICE_URL + SAMPLE_ID,
            Sample_.ENV_DESCRIP_DISPLAY,
            Json.createValue("D: 59 04 01 00 05 05 01 02 00 00 00 00"),
            Json.createValue(""),
            Status.BAD_REQUEST);

        // Test localized validation during sample creation
        // Errors
        Map<Locale, String> msgs = Map.of(
            Locale.GERMAN, "Größe muss zwischen 0 und 3 sein",
            Locale.US, "size must be between 0 and 3");
        final String envMediumId = Sample_.ENV_MEDIUM_ID;
        JsonObject payload = Json.createObjectBuilder()
            .add(envMediumId, "too much text for envMediumId")
            .add(Sample_.REGULATION_ID, 1)
            .add(Sample_.SAMPLE_METH_ID, 1)
            .add(Sample_.IS_TEST, false)
            .add(Sample_.OPR_MODE_ID, 1)
            .build();
        for (Locale language: msgs.keySet()) {
            JsonArray violations = create(
                SAMPLE_SERVICE_URL, payload, language, Status.BAD_REQUEST)
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
            Locale.GERMAN, "Wert nicht gesetzt",
            Locale.US, "No value provided");
        final String measFacilId = "06010";
        JsonObject wrngPayload = Json.createObjectBuilder()
            .add(Sample_.MEAS_FACIL_ID, measFacilId)
            .add(Sample_.APPR_LAB_ID, measFacilId)
            .add(Sample_.REGULATION_ID, 2)
            .add(Sample_.SAMPLE_METH_ID, 1)
            .add(Sample_.IS_TEST, false)
            .add(Sample_.OPR_MODE_ID, 1)
            .build();
        for (Locale language: wrngs.keySet()) {
            JsonObject response = create(
                SAMPLE_SERVICE_URL, wrngPayload, language, Status.OK);
            final String wrngsKey = "warnings";
            BaseTest.assertContains(response, wrngsKey);
            String violation = response.getJsonObject(wrngsKey)
                .getJsonArray(Sample_.SAMPLE_START_DATE)
                .getString(0);
            Assert.assertEquals(wrngs.get(language), violation);
        }

        getAuditTrail(
            "rest/audit/probe/" + SAMPLE_ID,
            updateFieldKey,
            newValue);
        delete(SAMPLE_SERVICE_URL + created.get("id"));
        final int sampleWithMeasmAndMeasVals = 999;
        delete(SAMPLE_SERVICE_URL + sampleWithMeasmAndMeasVals);
    }
}
