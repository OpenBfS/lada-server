/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.land;

import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.util.Arrays;

import org.junit.Assert;

import de.intevation.lada.BaseTest;
import de.intevation.lada.test.ServiceTest;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


/**
 * Test query entities.
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class QueryTest extends ServiceTest {

    private static final String KEY_ID = "id";
    private static final String KEY_DATA = "data";
    private JsonObject createPayload;
    private JsonObject updatePayload;
    private static final String URL = "rest/queryuser/";

    @Override
    public void init(
        Client c,
        URL baseUrl
    ) {
        super.init(c, baseUrl);
        // Attributes with timestamps
        timestampAttributes = Arrays.asList(new String[]{});

        // Load probe object to test POST request
        createPayload = readJsonResource("/datasets/query.json");
        updatePayload = readJsonResource("/datasets/query_update.json");
        Assert.assertNotNull(createPayload);
    }

    /**
     * Execute the tests.
     */
    public final void execute() {
        get(URL);

        JsonObject created = create(URL, createPayload);
        int createdId = created
            .getJsonObject(KEY_DATA).getInt(KEY_ID);

        //Update test cannot use ServiceTest functions as there is no
        //GetById interface
        final int idToUpdate = updatePayload.getInt(KEY_ID);
        Response updated = client.target(baseUrl + URL + idToUpdate).request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .accept(MediaType.APPLICATION_JSON)
            .put(Entity.entity(
                updatePayload.toString(), MediaType.APPLICATION_JSON));
        JsonObject updatedContent = BaseTest
            .parseResponse(updated)
            .getJsonObject(KEY_DATA);
        updatedContent.forEach((key, value) ->
            assertEquals(updatePayload.get(key), value));

        delete(URL + createdId);
    }
}
