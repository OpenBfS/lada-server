/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.land;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.junit.Assert;

import de.intevation.lada.BaseTest;
import de.intevation.lada.model.master.Tag;
import de.intevation.lada.test.ServiceTest;

/**
 * Test tagzuordnung entities.
 */
public class TagZuordnungTest extends ServiceTest {
    private final String tagUrlSample = "rest/tag/taglinksample/";
    private final String tagUrlMeasm = "rest/tag/taglinkmeasm/";

    private JsonArray createSample;
    private JsonArray createMeasm;
    private JsonArray create2;

    @Override
    public void init(WebTarget t) {
        super.init(t);

        createSample = readJsonArrayResource(
            "/datasets/tagzuordnung_create_sample.json");
        createMeasm = readJsonArrayResource(
            "/datasets/tagzuordnung_create_measm.json");
        Assert.assertNotNull(createSample);
        create2 = readJsonArrayResource("/datasets/tagzuordnung_create2.json");
        Assert.assertNotNull(create2);
    }

    /**
     * Execute Tagzuordnung tests.
     *
     * Executed tests:
     * - Create TagZuordnung with probe
     * - Create Tagzuordnung with messung
     * - Check if referenced tag expiration dates have been updated
     * - Filtering tags by assigned objects
     * - Delete all created Tagzuordnung objects
     */
    public void execute() {
        // test assigning tags
        bulkOperation(tagUrlSample, createSample);
        bulkOperation(tagUrlMeasm, createMeasm);
        // Should accept existing tag links
        bulkOperation(tagUrlSample, createSample);

        JsonArray tagResponse = get("rest/tag/").asJsonArray();
        List<Integer> tagIds = createSample.stream()
            .map(zuord -> zuord.asJsonObject().getInt("tagId"))
            .collect(Collectors.toList());
        List<JsonValue> tags = tagResponse.stream()
            .filter(tag -> tagIds.contains(
                    tag.asJsonObject().getInt("id")))
            .collect(Collectors.toList());
        Assert.assertEquals(2, tags.size());

        // Test validity of newly assigned tags
        tags.forEach(tagVal -> {
            JsonObject tag = (JsonObject) tagVal;
            String gueltigBisLong = tag.getString("valUntil");
            long diffInDays = getDaysFromNow(gueltigBisLong);
            Assert.assertEquals(Tag.MST_TAG_EXPIRATION_TIME, diffInDays);
        });

        // test filtering tags by assignment
        tagResponse = get("rest/tag?sampleId=9999").asJsonArray();
        Assert.assertTrue(
            "Returned data despite filtering for non-existent ID",
            tagResponse.isEmpty());

        tagResponse = get("rest/tag?sampleId=1901").asJsonArray();
        Assert.assertEquals(
            "Number of tags for given Sample ID:",
            3, tagResponse.size());

        tagResponse = get("rest/tag?measmId=1801").asJsonArray();
        Assert.assertEquals(
            "Number of tags for given Messung ID:",
            1, tagResponse.size());

        tagResponse = get("rest/tag?measmId=1801&measmId=1802").asJsonArray();
        Assert.assertTrue(
            "Expected empty result filtering by tagged and un-tagged object",
            tagResponse.isEmpty());

        bulkOperation(tagUrlMeasm, create2);
        tagResponse = get("rest/tag?measmId=1801&measmId=1802").asJsonArray();
        Assert.assertEquals(
            "Number of tags for given Messung IDs:",
            1, tagResponse.size());

        // Test unassigning tags
        bulkOperation(tagUrlSample + "delete", createSample);
        // Should accept existing tag links
        bulkOperation(tagUrlSample + "delete", createSample);
    }

    private void bulkOperation(
        String parameter, JsonArray payload
    ) {
        Response response = target.path(parameter)
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .accept(MediaType.APPLICATION_JSON)
            .post(Entity.entity(
                    payload.toString(), MediaType.APPLICATION_JSON));
        JsonArray content = BaseTest.parseResponse(response).asJsonArray();
        //Check each result
        final String successKey = "success", messageKey = "message";
        content.forEach(object -> {
            JsonObject responseObj = (JsonObject) object;
            BaseTest.assertContains(responseObj, successKey);
            Assert.assertTrue(
                "Unsuccessful response list element:\n" + responseObj,
                responseObj.getBoolean(successKey));
            BaseTest.assertContains(responseObj, messageKey);
            Assert.assertEquals("200", responseObj.getString(messageKey));
        });
    }
}
