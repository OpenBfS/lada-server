/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.land;

import java.net.URL;
import java.util.Arrays;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.ws.rs.client.Client;

import org.junit.Assert;

import de.intevation.lada.test.ServiceTest;

public class GeolocatMpgTest extends ServiceTest {

    private static final long TS1 = 1450371851654L;
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
            "lastMod"
        });

        // Prepare expected probe object
        JsonObject content =
            readJsonResource("/datasets/dbUnit_messprogramm.json");
        JsonObject geolocat =
            content.getJsonArray("lada.geolocat_mpg").getJsonObject(0);
        JsonObjectBuilder builder = convertObject(geolocat);
        builder.add("parentModified", TS1);
        builder.add("readonly", JsonValue.FALSE);
        builder.add("owner", JsonValue.TRUE);
        expectedById = builder.build();
        Assert.assertNotNull(expectedById);

        // Load probe object to test POST request
        create = readJsonResource("/datasets/geolocatMpg.json");
        Assert.assertNotNull(create);
    }

    /**
     * Execute the tests.
     */
    public final void execute() {
        get("geolocatmpg", "rest/geolocatmpg?mpgId=1000");
        getById("geolocatmpg", "rest/geolocatmpg/1000", expectedById);
        JsonObject created =
            create("geolocatmpg", "rest/geolocatmpg", create);
        update(
            "geolocatmpg",
            "rest/geolocatmpg/1000",
            "addSiteText",
            "Test",
            "Test geändert");
        delete(
            "geolocatmpg",
            "rest/geolocatmpg/" + created.getJsonObject("data").get("id"));
    }
}
