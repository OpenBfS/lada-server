/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.land;

import java.net.URL;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;

import org.junit.Assert;

import de.intevation.lada.Protocol;
import de.intevation.lada.model.stammdaten.Tag;
import de.intevation.lada.test.ServiceTest;

/**
 * Test tagzuordnung entities.
 */
public class TagZuordnungTest extends ServiceTest {
    private final String name = "tagzuordnung";
    private final String tagUrl = "rest/tag/zuordnung/";

    private JsonArray create;
    private JsonArray create2;

    private final String data = "data";

    @Override
    public void init(
        Client c,
        URL baseUrl,
        List<Protocol> protocol
    ) {
        super.init(c, baseUrl, protocol);
        create = readJsonArrayResource("/datasets/tagzuordnung_create.json");
        Assert.assertNotNull(create);
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
        bulkOperation(name, tagUrl, create);
        long nowLong = System.currentTimeMillis();
        JsonObject tagResponse = get("tag", "rest/tag/");
        JsonArray tags = tagResponse.getJsonArray(data);
        Assert.assertEquals(2, tags.size());
        tags.forEach(tagVal -> {
            JsonObject tag = (JsonObject) tagVal;
            long gueltigBisLong = tag.getJsonNumber("gueltigBis").longValue();
            long diffInDays = getDiffInDays(nowLong, gueltigBisLong);
            Assert.assertEquals(Tag.MST_TAG_EXPIRATION_TIME, diffInDays);
        });

        // test filtering tags by assignment
        tagResponse = get("tag", "rest/tag?pid=9999");
        Assert.assertTrue(
            "Returned data despite filtering for non-existent ID",
            tagResponse.getJsonArray(data).isEmpty());

        tagResponse = get("tag", "rest/tag?pid=1901");
        Assert.assertEquals(
            "Expected one tag für given Probe ID",
            1, tagResponse.getJsonArray(data).size());

        tagResponse = get("tag", "rest/tag?mid=1801");
        Assert.assertEquals(
            "Expected one tag für given Messung ID",
            1, tagResponse.getJsonArray(data).size());

        tagResponse = get("tag", "rest/tag?mid=1801&mid=1802");
        Assert.assertTrue(
            "Expected empty result filtering by tagged and un-tagged object",
            tagResponse.getJsonArray(data).isEmpty());

        bulkOperation(name, tagUrl, create2);
        tagResponse = get("tag", "rest/tag?mid=1801&mid=1802");
        Assert.assertEquals(
            "Expected one tag für given Messung IDs",
            1, tagResponse.getJsonArray(data).size());

        // Test unassigning tags
        bulkOperation(name, tagUrl + "delete", create);
    }
}
