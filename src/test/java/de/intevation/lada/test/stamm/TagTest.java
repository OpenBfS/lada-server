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
        Tag tagToTest = new Tag();
        tagToTest.setMeasFacilId("06010");
        tagToTest.setName("mstTag");
        testTagCRUD(tagToTest);
    }

    /**
     * Test netzbetreiber tags.
     */
    public void testNetzbetreiberTag() {
        Tag tagToTest = new Tag();
        tagToTest.setNetworkId("06");
        tagToTest.setName("nbTag");
        testTagCRUD(tagToTest);
    }

    /**
     * Promote a mst tag to global.
     */
    public void promoteMstTag() {
        Tag tagToTest = new Tag();
        tagToTest.setMeasFacilId("06010");
        tagToTest.setName("mstTagPromoted");
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
    private void testTagCRUD(Tag tagToTest) {
        JsonObject createResponse = create(tagUrl, tagToTest);
        long createdId = createResponse.getInt("id");
        if (!createResponse.isNull(measFacilIdKey)) {
            String createdGueltigBis = createResponse.getString("valUntil");
            long diff = getDaysFromNow(createdGueltigBis);
            Assert.assertEquals(Tag.MST_TAG_EXPIRATION_TIME, diff);
        }
        String tagUpdated = tagToTest.getName() + "-mod";
        JsonObject updateResponse = update(tagUrl + createdId,
            "name",
            tagToTest.getName(),
            tagUpdated).asJsonObject();
        Assert.assertFalse(get(tagUrl).asJsonArray().isEmpty());
        getById(tagUrl + createdId, updateResponse);
        delete(tagUrl + createdId);
    }
}
