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
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.client.Client;

import org.junit.Assert;

import de.intevation.lada.Protocol;
import de.intevation.lada.test.ServiceTest;

/**
 * Test MessprogrammMmt entities.
 */
public class MessprogrammMmtTest extends ServiceTest {
    private JsonObject expectedById;
    private JsonObject create;

    /**
     * @return The test protocol
     */
    public List<Protocol> getProtocol() {
        return protocol;
    }

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

        // Prepare expected object
        JsonObject content =
            readJsonResource("/datasets/dbUnit_messprogramm.json");
        JsonObject messprogrammMmt =
            content.getJsonArray("lada.mpg_mmt_mp").getJsonObject(0);
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
        final String name = "messprogrammmmt";
        final String url = "rest/messprogrammmmt/";
        final String id = "1000";

        getById(name, url + id, expectedById);
        get(name, url + "?messprogrammId=1000");
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
