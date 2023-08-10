/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.land;

import java.net.URL;
import java.util.Arrays;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.client.Client;

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
    public void init(
        Client c,
        URL baseUrl
    ) {
        super.init(c, baseUrl);
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
        final String name = "mpgmmtmp";
        final String url = "rest/mpgmmtmp/";
        final String id = "1000";

        getById(name, url + id, expectedById);
        get(name, url + "?mpgId=1000");
        update(
            name,
            url + id,
            "mmtId",
            "A3",
            "B3"
        );
        JsonObject created =
            create(name, url, create);
        delete(
            name,
            url + created.getJsonObject("data").get("id"));
    }
}
