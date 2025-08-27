/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.stamm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.List;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.Response.Status;

import org.junit.Assert;

import de.intevation.lada.BaseTest;
import de.intevation.lada.ClientBaseTest;
import de.intevation.lada.model.master.Tag;
import de.intevation.lada.model.master.Tag_;
import de.intevation.lada.rest.TagService;
import de.intevation.lada.test.ServiceTest;

/**
 * Test tag entities.
 */
public class TagTest extends ServiceTest {

    /**
     * Default time in days after which measFacil tags expire.
     */
    public static final int MF_TAG_EXPIRATION_DAYS = 365;

    private static final String TAG_URL =
        UriBuilder.fromResource(TagService.class).build().getPath() + "/";

    private static final String NETWORK_ID = "06";
    private static final String MEAS_FACIL_ID = "06010";

    @Override
    public void init(WebTarget t) {
        super.init(t);
    }

    /**
     * Execute the tests.
     */
    public void execute() {
        testMstTag();
        testNetzbetreiberTag();
        promoteToNetwork();
        promoteToGlobal();
        resetValidity();
        delete(TAG_URL + "1003"); // Delete tag with assignment
    }

    /**
     * Test mst tags.
     */
    public void testMstTag() {
        Tag tagToTest = new Tag();
        tagToTest.setMeasFacilId(MEAS_FACIL_ID);
        tagToTest.setName("mstTag");
        testTagCRUD(tagToTest);
    }

    /**
     * Test netzbetreiber tags.
     */
    public void testNetzbetreiberTag() {
        Tag tagToTest = new Tag();
        tagToTest.setNetworkId(NETWORK_ID);
        tagToTest.setName("nbTag");
        testTagCRUD(tagToTest);
    }

    /**
     * Promote a measFacil tag to network.
     */
    public void promoteToNetwork() {
        final String name = "test";
        Tag tagToTest = new Tag();
        tagToTest.setMeasFacilId(MEAS_FACIL_ID);
        tagToTest.setName(name);
        Tag created = create(TAG_URL, tagToTest, Tag.class);
        created.setMeasFacilId(null);
        created.setNetworkId(NETWORK_ID);

        Invocation.Builder builder = target
            .path(TAG_URL + created.getId())
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .accept(MediaType.APPLICATION_JSON);

        // Requires unsetting valUntil
        ClientBaseTest.parseResponse(
            builder.put(Entity.entity(created, MediaType.APPLICATION_JSON)),
            Tag.class,
            Response.Status.BAD_REQUEST);
        created.setValUntil(null);
        Tag updated = builder.put(
            Entity.entity(created, MediaType.APPLICATION_JSON), Tag.class);
        assertEquals(name, updated.getName());
        assertNull(updated.getMeasFacilId());
        assertEquals(NETWORK_ID, updated.getNetworkId());
        assertNull(updated.getValUntil());
    }

    /**
     * Resetting validity of measFacil tag.
     */
    public void resetValidity() {
        final String name = "test";
        Tag tagToTest = new Tag();
        tagToTest.setMeasFacilId(MEAS_FACIL_ID);
        tagToTest.setName(name);
        Tag created = create(TAG_URL, tagToTest, Tag.class);

        created.setValUntil(null);
        Tag updated = target
            .path(TAG_URL + created.getId())
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .accept(MediaType.APPLICATION_JSON)
            .put(Entity.entity(created, MediaType.APPLICATION_JSON), Tag.class);
        assertEquals(MEAS_FACIL_ID, updated.getMeasFacilId());
        checkValUntil(updated);
    }

    /**
     * Promote a tag to global.
     */
    public void promoteToGlobal() {
        Tag tagToTest = new Tag();
        tagToTest.setNetworkId(NETWORK_ID);
        tagToTest.setName("TagPromoted");
        JsonObject createResponse = create(TAG_URL, tagToTest);
        long createdId = createResponse.getInt(Tag_.ID);
        update(
            TAG_URL + createdId,
            Tag_.NETWORK_ID,
            Json.createValue(NETWORK_ID),
            null,
            Status.FORBIDDEN);
    }

    /**
     * Test CRUD operations for the given tag.
     * @param tagToTest Tag to test
     */
    private void testTagCRUD(Tag tagToTest) {
        Tag createResponse = create(TAG_URL, tagToTest, Tag.class);
        checkValUntil(createResponse);
        String tagUpdated = tagToTest.getName() + "-mod";
        int createdId = createResponse.getId();
        JsonObject updateResponse = update(TAG_URL + createdId,
            Tag_.NAME,
            tagToTest.getName(),
            tagUpdated).asJsonObject();
        Assert.assertFalse(
            get(TAG_URL, new GenericType<List<Tag>>() { }).isEmpty());
        getById(TAG_URL + createdId, updateResponse);
        delete(TAG_URL + createdId);
    }

    private void checkValUntil(Tag tag) {
        if (tag.getMeasFacilId() != null) {
            Date valUntil = tag.getValUntil();
            Assert.assertNotNull(valUntil);
            long diff = getDaysFromNow(valUntil);
            Assert.assertEquals(MF_TAG_EXPIRATION_DAYS, diff);
        }
    }
}
