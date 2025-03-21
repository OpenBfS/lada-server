/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.land;

import java.util.Arrays;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.client.WebTarget;

import org.junit.Assert;

import de.intevation.lada.model.lada.MpgMmtMp;
import de.intevation.lada.test.ServiceTest;

/**
 * Test MessprogrammMmt entities.
 */
public class MessprogrammMmtTest extends ServiceTest {
    private JsonObject expectedById;
    private JsonObject create;

    @Override
    public void init(WebTarget t) {
        super.init(t);

        // Attributes with timestamps
        timestampAttributes = Arrays.asList(new String[]{
            "letzteAenderung"
        });

        // Prepare expected object
        JsonObject messprogrammMmt =
            readXmlResource("datasets/dbUnit_lada.xml", MpgMmtMp.class)
            .getJsonObject(0);
        JsonObjectBuilder builder = convertObject(messprogrammMmt);
        builder.add("measds", Json.createArrayBuilder().add(56));
        expectedById = builder.build();
        Assert.assertNotNull(expectedById);

        // Load object to test POST request
        create = readJsonResource("/datasets/messprogramm_mmt.json");
        Assert.assertNotNull(create);
    }

    /**
     * Execute the tests.
     */
    public final void execute() {
        final String url = "rest/mpgmmtmp/";
        final String id = "1000";

        getById(url + id, expectedById);
        get(url + "?mpgId=1000");
        update(url + id, "mmtId", "A3", "B3");
        JsonObject created = create(url, create);
        delete(url + created.get("id"));
    }
}
