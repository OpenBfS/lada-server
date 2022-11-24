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

public class MunicDivTest extends ServiceTest {

    private JsonObject expectedById;
    private JsonObject create;

    @Override
    public void init(
        Client c,
        URL baseUrl,
        List<Protocol> protocol
    ) {
        super.init(c, baseUrl, protocol);
        // Attributes with timestamps
        timestampAttributes = Arrays.asList(new String[]{
            "lastMod"
        });

        // Prepare expected object
        JsonObject content =
            readJsonResource("/datasets/dbUnit_municDiv.json");
        JsonObject municDiv =
            content.getJsonArray("master.munic_div").getJsonObject(0);
        JsonObjectBuilder builder = convertObject(municDiv);
        expectedById = builder.build();
        Assert.assertNotNull(expectedById);

        // Load object to test POST request
        create = readJsonResource("/datasets/municDiv.json");
        Assert.assertNotNull(create);
    }

    /**
     * Execute the tests.
     */
    public final void execute() {
        get("gemeindeuntergliederung", "rest/gemeindeuntergliederung");
        getById("gemeindeuntergliederung", "rest/gemeindeuntergliederung/1000", expectedById);
        update(
            "gemeindeuntergliederung",
            "rest/gemeindeuntergliederung/1000",
            "name",
            "Testname",
            "UpdatedName");
        JsonObject created = create("gemeindeuntergliederung", "rest/gemeindeuntergliederung", create);
        delete(
            "gemeindeuntergliederung",
            "rest/gemeindeuntergliederung/" + created.getJsonObject("data").get("id"));
    }
}
