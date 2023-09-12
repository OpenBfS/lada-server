/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.stamm;

import java.net.URL;

import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.client.Client;

import org.junit.Assert;

import de.intevation.lada.model.master.Tag;
import de.intevation.lada.test.ServiceTest;

/**
 * Test tag entities.
 */
public class TagTest extends ServiceTest {

    private JsonObject create;

    private final String name = "tag";
    private final String tagUrl = "rest/tag/";

    private final String dataKey = "data";

    private final String tagNameAttribute = "name";

    @Override
    public void init(
        Client c,
        URL baseUrl
    ) {
        super.init(c, baseUrl);
        create = readJsonResource("/datasets/tag_create.json");
        Assert.assertNotNull(create);
    }

    /**
     * Execute the tests.
     */
    public void execute() {
        testMstTag();
        testNetzbetreiberTag();
        promoteMstTag();
        delete(name, tagUrl + "103"); // Delete tag with assignment
    }

    /**
     * Test mst tags.
     */
    public void testMstTag() {
        JsonObject tagToTest = createTagJson(Tag.TAG_TYPE_MST, "mstTag");
        testTagCRUD(tagToTest);
    }

    /**
     * Test netzbetreiber tags.
     */
    public void testNetzbetreiberTag() {
        JsonObject tagToTest
            = createTagJson(Tag.TAG_TYPE_NETZBETREIBER, "nbTag");
        testTagCRUD(tagToTest);
    }

    /**
     * Promote a mst tag to global.
     */
    public void promoteMstTag() {
        JsonObject tagToTest = createTagJson(
            Tag.TAG_TYPE_MST, "mstTagPromoted");
        JsonObject createResponse = create(name, tagUrl, tagToTest);
        long createdId = createResponse.getJsonObject(dataKey).getInt("id");
        update(name, tagUrl + createdId, "tagType",
            "mst",
            "global");
    }

    /**
     * Test CRUD operations for the given tag.
     * @param tagToTest Tag to test
     */
    private void testTagCRUD(JsonObject tagToTest) {
        JsonObject createResponse = create(name, tagUrl, tagToTest);
        long createdId = createResponse.getJsonObject(dataKey).getInt("id");
        String createdTyp = createResponse
            .getJsonObject(dataKey).getString("tagType");
        if (createdTyp.equals("mst") || createdTyp.equals("auto")) {
            String createdGueltigBis = createResponse.getJsonObject(dataKey)
                .getString("valUntil");
            long diff = getDaysFromNow(createdGueltigBis);
            Assert.assertEquals(Tag.MST_TAG_EXPIRATION_TIME, diff);
        }
        String tagUpdated = tagToTest.getString(tagNameAttribute) + "-mod";
        JsonObject updateResponse = update(name, tagUrl + createdId,
            tagNameAttribute,
            tagToTest.getString(tagNameAttribute),
            tagUpdated);
        JsonObject getResponse = get(name, tagUrl);
        Assert.assertFalse(getResponse.getJsonArray(dataKey).isEmpty());
        getById(name, tagUrl + createdId,
            updateResponse.getJsonObject(dataKey));
        delete(name, tagUrl + createdId);
    }

    /**
     * Create json for a mst tag.
     * @return Tag json object
     */
    private JsonObject createTagJson(String type, String tag) {
        JsonObjectBuilder builder = convertObject(create);
        builder.add("tagType", type);
        builder.add(name, tag);
        return builder.build();
    }
}
