/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.stamm;

import java.net.URL;

import jakarta.json.Json;
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

    private final String tagUrl = "rest/tag/";

    private final String tagNameAttribute = "name";

    @Override
    public void init(
        Client c,
        URL baseUrl
    ) {
        super.init(c, baseUrl);
    }

    /**
     * Execute the tests.
     */
    public void execute() {
        testMstTag();
        testNetzbetreiberTag();
        promoteMstTag();
        delete(tagUrl + "1003"); // Delete tag with assignment
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
        JsonObject createResponse = create(tagUrl, tagToTest);
        long createdId = createResponse.getInt("id");
        update(tagUrl + createdId, "tagType", "mst", "global");
    }

    /**
     * Test CRUD operations for the given tag.
     * @param tagToTest Tag to test
     */
    private void testTagCRUD(JsonObject tagToTest) {
        JsonObject createResponse = create(tagUrl, tagToTest);
        long createdId = createResponse.getInt("id");
        String createdTyp = createResponse.getString("tagType");
        if (createdTyp.equals("mst") || createdTyp.equals("auto")) {
            String createdGueltigBis = createResponse.getString("valUntil");
            long diff = getDaysFromNow(createdGueltigBis);
            Assert.assertEquals(Tag.MST_TAG_EXPIRATION_TIME, diff);
        }
        String tagUpdated = tagToTest.getString(tagNameAttribute) + "-mod";
        JsonObject updateResponse = update(tagUrl + createdId,
            tagNameAttribute,
            tagToTest.getString(tagNameAttribute),
            tagUpdated);
        Assert.assertFalse(get(tagUrl).asJsonArray().isEmpty());
        getById(tagUrl + createdId, updateResponse);
        delete(tagUrl + createdId);
    }

    private JsonObject createTagJson(String type, String tag) {
        JsonObjectBuilder builder = Json.createObjectBuilder()
            .add("tagType", type)
            .add("name", tag);
        switch (type) {
        case "mst":
            builder.add("measFacilId", "06010");
            break;
        case "netz":
            builder.add("networkId", "06");
        default:
            // Nothing to do for global tag
        }
        return builder.build();
    }
}
