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

import org.junit.Assert;

import de.intevation.lada.Protocol;
import de.intevation.lada.model.stammdaten.TagTyp;
import de.intevation.lada.test.ServiceTest;

/**
 * Test tagzuordnung entities.
 */
public class TagZuordnungTest extends ServiceTest {
    private final String name = "tagzuordnung";
    private final String tagUrl = "rest/tag/zuordnung/";

    private JsonArray create;

    private final String data = "data";

    @Override
    public void init(
        URL baseUrl,
        List<Protocol> protocol
    ) {
        super.init(baseUrl, protocol);
        create = readJsonArrayResource("/datasets/tagzuordnung_create.json");
        Assert.assertNotNull(create);
    }

    /**
     * Execute Tagzuordnung tests.
     *
     * Executed tests:
     * - Create TagZuordnung with probe
     * - Create Tagzuordnung with messung
     * - Check if referenced tag expiration dates have been updated
     * - Delete all created Tagzuordnung objects
     */
    public void execute() {
        JsonObject createResponse = bulkCreate(name, tagUrl, create);
        long nowLong = System.currentTimeMillis();
        JsonObject tagResponse = getAll("tag", "rest/tag/");
        JsonArray tags = tagResponse.getJsonArray(data);
        tags.forEach(tagVal -> {
            JsonObject tag = (JsonObject) tagVal;
            long gueltigBisLong = tag.getJsonNumber("gueltigBis").longValue();
            long diffInDays = getDiffInDays(nowLong, gueltigBisLong);
            Assert.assertEquals(TagTyp.MST_TAG_EXPIRATION_TIME, diffInDays);
        });

        createResponse.getJsonObject(data).forEach((key, value) -> {
            delete(name, tagUrl + key);
        });
    }
}
