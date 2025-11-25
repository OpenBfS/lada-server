/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.land;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;

import de.intevation.lada.BaseTest;
import de.intevation.lada.ClientBaseTest;
import de.intevation.lada.model.lada.TagLink_;
import de.intevation.lada.model.master.Tag;
import de.intevation.lada.rest.TagLinkMeasmService;
import de.intevation.lada.rest.TagLinkSampleService;
import de.intevation.lada.rest.TagService;
import de.intevation.lada.test.ServiceTest;
import de.intevation.lada.test.stamm.TagTest;
import de.intevation.lada.util.data.StatusCodes;

/**
 * Test tagzuordnung entities.
 */
public class TagZuordnungTest extends ServiceTest {

    private final UriBuilder tagUrl = UriBuilder
        .fromResource(TagService.class);
    private final UriBuilder tagUrlSample = UriBuilder
        .fromResource(TagLinkSampleService.class);
    private final UriBuilder tagUrlMeasm = UriBuilder
        .fromResource(TagLinkMeasmService.class);

    private final String sampleIdParam = "sampleId";
    private final String measmIdParam = "measmId";

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

        List<Tag> tagResponse = getTags(null);
        List<Integer> tagIds = createSample.stream()
            .map(zuord -> zuord.asJsonObject().getInt(TagLink_.TAG_ID))
            .collect(Collectors.toList());
        List<Tag> tags = tagResponse.stream()
            .filter(tag -> tagIds.contains(tag.getId()))
            .collect(Collectors.toList());
        Assert.assertEquals(2, tags.size());

        // Test validity of newly assigned tags
        tags.forEach(tag -> {
            long diffInDays = getDaysFromNow(tag.getValUntil());
            Assert.assertEquals(TagTest.MF_TAG_EXPIRATION_DAYS, diffInDays);
        });

        // test filtering tags by assignment
        final int nonExistent = 9999;
        tagResponse = getTags(sampleIdParam, nonExistent);
        assertTrue(
            "Returned data despite filtering for non-existent ID",
            tagResponse.isEmpty());

        final int sampleId = 1901;
        tagResponse = getTags(sampleIdParam, sampleId);
        Assert.assertEquals(
            "Number of tags for given Sample ID:",
            3, tagResponse.size());

        final int measmId1 = 1801;
        tagResponse = getTags(measmIdParam, measmId1);
        Assert.assertEquals(
            "Number of tags for given Messung ID:",
            1, tagResponse.size());

        final int measmId2 = 1802;
        tagResponse = getTags(measmIdParam, measmId1, measmId2);
        assertTrue("Expected empty result", tagResponse.isEmpty());

        bulkOperation(tagUrlMeasm, create2);
        tagResponse = getTags(measmIdParam, measmId1, measmId2);
        Assert.assertEquals(
            "Number of tags for given Messung IDs:",
            1, tagResponse.size());

        final int tagId = 1002;
        testUnassigningTags(TagLinkSampleService.class, sampleId, tagId);
        testUnassigningTags(TagLinkMeasmService.class, measmId1, tagId);
    }

    private void testUnassigningTags(
        Class<?> service, int taggableId, int tagId
    ) {
        String param = TagLinkSampleService.class.equals(service)
            ? sampleIdParam
            : measmIdParam;
        // Ensure to be deleted association exists
        MatcherAssert.assertThat(
            String.format(
                "To be deleted association with %s does not exist", param),
            getTags(param, taggableId).stream().map(Tag::getId).toList(),
            CoreMatchers.hasItem(tagId)
        );

        // Delete association
        UriBuilder deleteUri = UriBuilder.fromResource(service)
            .path(service, "deleteTagReference");
        JsonArray payload = Json.createArrayBuilder()
            .add(Json.createObjectBuilder()
                .add(param, taggableId)
                .add(TagLink_.TAG_ID, tagId))
            .build();
        bulkOperation(deleteUri, payload);

        // Ensure deleted association no longer exists
        MatcherAssert.assertThat(
            String.format(
                "To be deleted association with %s still exists", param),
            getTags(param, taggableId).stream().map(Tag::getId).toList(),
            CoreMatchers.not(CoreMatchers.hasItem(tagId))
        );

        // Should accept non-existent (already deleted) tag links
        bulkOperation(deleteUri, payload);
    }

    private void bulkOperation(
        UriBuilder path, JsonArray payload
    ) {
        Response response = target.path(path.build().toString())
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .accept(MediaType.APPLICATION_JSON)
            .post(Entity.entity(
                    payload.toString(), MediaType.APPLICATION_JSON));
        JsonArray content = ClientBaseTest.parseResponse(response)
            .asJsonArray();

        //Check each result
        final String successKey = "success", messageKey = "message";
        content.forEach(object -> {
            JsonObject responseObj = (JsonObject) object;
            BaseTest.assertContains(responseObj, successKey);
            assertTrue(
                "Unsuccessful response list element:\n" + responseObj,
                responseObj.getBoolean(successKey));
            BaseTest.assertContains(responseObj, messageKey);
            Assert.assertEquals(
                String.valueOf(StatusCodes.OK),
                responseObj.getString(messageKey));
        });
    }

    private List<Tag> getTags(String param, Object... values) {
        UriBuilder url = tagUrl.clone();
        if (param != null) {
            url.queryParam(param, values);
        }
        return get(url.build().toString(), new GenericType<List<Tag>>() { });
    }
}
