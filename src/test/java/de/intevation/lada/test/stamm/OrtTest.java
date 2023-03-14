/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.stamm;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.client.Client;

import org.junit.Assert;

import de.intevation.lada.Protocol;
import de.intevation.lada.test.ServiceTest;

/**
 * Test ort entities.
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class OrtTest extends ServiceTest {

    private JsonObject expectedById;
    private JsonObject create;
    private JsonObject createIncomplete;

    @Override
    public void init(
        Client c,
        URL baseUrl,
        List<Protocol> protocol
    ) {
        super.init(c, baseUrl, protocol);
        // Attributes with timestamps
        timestampAttributes = Arrays.asList(new String[]{
            "letzteAenderung"
        });
        // Attributes with point geometries
        geomPointAttributes = Arrays.asList(new String[]{
                "geom"
        });

        // Prepare expected object
        JsonObject content = readJsonResource("/datasets/dbUnit_ort.json");
        JsonObject erzeuger =
            content.getJsonArray("master.site").getJsonObject(0);
        JsonObjectBuilder builder = convertObject(erzeuger);
        expectedById = builder.build();
        Assert.assertNotNull(expectedById);

        // Load object to test POST request
        create = readJsonResource("/datasets/ort.json");
        createIncomplete = readJsonResource("/datasets/ort_incomplete.json");
        Assert.assertNotNull(create);
    }

    /**
     * Execute the tests.
     */
    public final void execute() {
        get("ort", "rest/site");

        //Test search interface
        JsonObject result = get("site-search", "rest/site?search=Text");
        Assert.assertNotEquals(0, result.getJsonArray("data").size());

        getById("ort", "rest/site/1000", expectedById);
        int createdId = create("site", "rest/site", create)
            .getJsonObject("data").getInt("id");

        create("site-incomplete", "rest/site", createIncomplete);

        update("site", "rest/site/" + createdId,
            "longText", "Langer Text", "Längerer Text");
        delete("site", "rest/site/" + createdId);
    }
}
