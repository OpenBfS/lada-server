/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.stamm;

import java.net.URL;
import java.util.List;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.junit.Assert;

import de.intevation.lada.Protocol;
import de.intevation.lada.model.stammdaten.Tag;
import de.intevation.lada.test.ServiceTest;

/**
 * Test tag entities.
 */
public class TagTest extends ServiceTest {

    private JsonObject create;

    private final String name = "tag";
    private final String tagUrl = "rest/tag/";

    @Override
    public void init(
        URL baseUrl,
        List<Protocol> protocol
    ) {
        super.init(baseUrl, protocol);
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
        JsonObject createResponse = create(name, "rest/tag", tagToTest);
        long createdId = createResponse.getJsonObject("data").getInt("id");
        update(name, tagUrl + createdId, "typId",
            "mst",
            "global");
    }

    /**
     * Test CRUD operations for the given tag.
     * @param tagToTest Tag to test
     */
    private void testTagCRUD(JsonObject tagToTest) {

        long now = System.currentTimeMillis();
        JsonObject createResponse = create(name, "rest/tag", tagToTest);
        long createdId = createResponse.getJsonObject("data").getInt("id");
        String createdTyp = createResponse
            .getJsonObject("data").getString("typId");
        if (createdTyp.equals("mst") || createdTyp.equals("auto")) {
            long createdGueltigBis
                = createResponse.getJsonObject("data")
                .getJsonNumber("gueltigBis").longValue();
            long diff = getDiffInDays(now, createdGueltigBis);
            Assert.assertEquals(Tag.MST_TAG_EXPIRATION_TIME, diff);
        }
        String tagUpdated = tagToTest.getString("tag") + "-mod";
        JsonObject updateResponse = update(name, tagUrl + createdId, "tag",
            tagToTest.getString("tag"),
            tagUpdated);
        getAll(name, tagUrl);
        getById(name, tagUrl + createdId, updateResponse.getJsonObject("data"));
        delete(name, tagUrl + createdId);
    }

    /**
     * Create json for a mst tag.
     * @return Tag json object
     */
    private JsonObject createTagJson(String type, String tag) {
        JsonObjectBuilder builder = convertObject(create);
        builder.add("typId", type);
        builder.add("tag", tag);
        return builder.build();
    }
}
