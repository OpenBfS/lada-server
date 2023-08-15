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

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.Response.Status;

import org.junit.Assert;

import de.intevation.lada.model.lada.Mpg;
import de.intevation.lada.test.ServiceTest;

/**
 * Test messprogramm entities.
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class MessprogrammTest extends ServiceTest {
    private JsonObject expectedById;
    private JsonObject expectedSample;
    private JsonObject create;

    @Override
    public void init(
        Client c,
        URL baseUrl
    ) {
        super.init(c, baseUrl);
        // Attributes with timestamps
        timestampAttributes = Arrays.asList(new String[]{
            "lastMod",
            "treeModified"
        });

        // Prepare expected object
        JsonObject messprogramm =
            readXmlResource("datasets/dbUnit_lada.xml", Mpg.class)
            .getJsonObject(0);
        JsonObjectBuilder builder = convertObject(messprogramm);
        builder.add("oprModeId", 1);
        builder.add("samplePdOffset", 0);
        builder.addNull("commSample");
        builder.addNull("samplerId");
        expectedById = builder.build();
        Assert.assertNotNull(expectedById);

        //Prepare expected sample object
        builder = Json.createObjectBuilder();
        builder.add("mpgId", 999);
        expectedSample = builder.build();

        // Load object to test POST request
        create = readJsonResource("/datasets/messprogramm.json");
        Assert.assertNotNull(create);
    }

    /**
     * Execute the tests.
     */
    public final void execute() {
        get("mpg", "rest/mpg", Status.METHOD_NOT_ALLOWED);
        getById("mpg", "rest/mpg/999", expectedById);
        update(
            "mpg",
            "rest/mpg/999",
            "envDescripDisplay",
            "D: 50 90 01 06 02 05 00 00 00 00 00 00",
            "D: 50 90 01 06 02 05 00 00 00 00 00 01");

        // Ensure invalid envDescripDisplay is rejected
        update(
            "mpg",
            "rest/mpg/999",
            "envDescripDisplay",
            "D: 50 90 01 06 02 05 00 00 00 00 00 01",
            "D: ",
            Status.BAD_REQUEST);

        //Check if referencing probe still has an mpgId
        getById("mpg->sample", "rest/sample/999", expectedSample);
        JsonObject created =
            create("mpg", "rest/mpg", create);
        delete(
            "mpg",
            "rest/mpg/" + created.getJsonObject("data").get("id"));
    }
}
