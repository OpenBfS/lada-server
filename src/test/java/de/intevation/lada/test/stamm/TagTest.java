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
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.Response.Status;

import org.junit.Assert;

import de.intevation.lada.model.master.Tag;
import de.intevation.lada.test.ServiceTest;

/**
 * Test tag entities.
 */
public class TagTest extends ServiceTest {

    private final String tagUrl = "rest/tag/";

    private final String tagNameAttribute = "name";
    private final String measFacilIdKey = "measFacilId";

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
        JsonObject tagToTest = Json.createObjectBuilder()
            .add(measFacilIdKey, "06010")
            .add(tagNameAttribute, "mstTag")
            .build();
        testTagCRUD(tagToTest);
    }

    /**
     * Test netzbetreiber tags.
     */
    public void testNetzbetreiberTag() {
        JsonObject tagToTest = Json.createObjectBuilder()
            .add("networkId", "06")
            .add(tagNameAttribute, "nbTag")
            .build();
        testTagCRUD(tagToTest);
    }

    /**
     * Promote a mst tag to global.
     */
    public void promoteMstTag() {
        JsonObject tagToTest = Json.createObjectBuilder()
            .add(measFacilIdKey, "06010")
            .add(tagNameAttribute, "mstTagPromoted")
            .build();
        JsonObject createResponse = create(tagUrl, tagToTest);
        long createdId = createResponse.getInt("id");
        update(
            tagUrl + createdId,
            measFacilIdKey,
            Json.createValue("06010"),
            null,
            Status.FORBIDDEN);
    }

    /**
     * Test CRUD operations for the given tag.
     * @param tagToTest Tag to test
     */
    private void testTagCRUD(JsonObject tagToTest) {
        JsonObject createResponse = create(tagUrl, tagToTest);
        long createdId = createResponse.getInt("id");
        if (!createResponse.isNull(measFacilIdKey)) {
            String createdGueltigBis = createResponse.getString("valUntil");
            long diff = getDaysFromNow(createdGueltigBis);
            Assert.assertEquals(Tag.MST_TAG_EXPIRATION_TIME, diff);
        }
        String tagUpdated = tagToTest.getString(tagNameAttribute) + "-mod";
        JsonObject updateResponse = update(tagUrl + createdId,
            tagNameAttribute,
            tagToTest.getString(tagNameAttribute),
            tagUpdated).asJsonObject();
        Assert.assertFalse(get(tagUrl).asJsonArray().isEmpty());
        getById(tagUrl + createdId, updateResponse);
        delete(tagUrl + createdId);
    }
}
