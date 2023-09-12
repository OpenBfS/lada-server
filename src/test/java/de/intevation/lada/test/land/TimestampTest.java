/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.land;

import java.net.URL;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;

import org.junit.Assert;

import de.intevation.lada.test.ServiceTest;

/**
 * Test different timestamp formats.
 */
public class TimestampTest extends ServiceTest {

    @Override
    public void init(
        Client c,
        URL baseUrl
    ) {
        super.init(c, baseUrl);
    }

    /**
     * Execute the tests.
     */
    public final void execute() {
        final String sampleStartDateKey = "sampleStartDate";
        final String expectedOutput = "2015-02-08T09:58:00.000Z";
        String[] input = {
            expectedOutput, // like JavaScript's Date.toISOString()
            "2015-02-08T09:58:00.00Z", // fraction with less digits
            "2015-02-08T11:58:00.000+02:00", // with offset
            "2015-02-08T11:58:00+02:00", // without fraction of second
            "2015-02-08T11:58+02:00", // without second of minute
        };
        for (String i: input) {
            JsonObject create = Json.createObjectBuilder()
                .add("oprModeId", 1)
                .add("regulationId", 2)
                .add("measFacilId", "06010")
                .add("apprLabId", "06010")
                .add("sampleMethId", 1)
                .add("isTest", true)
                .add(sampleStartDateKey, i)
                .build();
            JsonObject created = create("probe", "rest/sample", create);
            Assert.assertEquals(
                expectedOutput,
                created.getJsonObject("data").getString(sampleStartDateKey));
        }
    }
}
