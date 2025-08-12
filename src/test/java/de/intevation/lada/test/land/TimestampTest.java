/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.land;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.UriBuilder;

import org.junit.Assert;

import de.intevation.lada.model.lada.Sample_;
import de.intevation.lada.rest.SampleService;
import de.intevation.lada.test.ServiceTest;

/**
 * Test different timestamp formats.
 */
public class TimestampTest extends ServiceTest {

    @Override
    public void init(WebTarget t) {
        super.init(t);
    }

    /**
     * Execute the tests.
     */
    public final void execute() {
        final String sampleStartDateKey = Sample_.SAMPLE_START_DATE;
        final String expectedOutput = "2015-02-08T09:58:00.000Z";
        String[] input = {
            expectedOutput, // like JavaScript's Date.toISOString()
            "2015-02-08T11:58:00.000+02:00", // with offset
            "2015-02-08T11:58:00+02:00", // without fraction of second
            "2015-02-08T11:58+02:00", // without second of minute
        };
        for (String i: input) {
            JsonObject create = Json.createObjectBuilder()
                .add(Sample_.OPR_MODE_ID, 1)
                .add(Sample_.REGULATION_ID, 2)
                .add(Sample_.MEAS_FACIL_ID, "06010")
                .add(Sample_.APPR_LAB_ID, "06010")
                .add(Sample_.SAMPLE_METH_ID, 1)
                .add(Sample_.IS_TEST, true)
                .add(sampleStartDateKey, i)
                .build();
            JsonObject created = create(
                UriBuilder.fromResource(SampleService.class).build().getPath(),
                create);
            Assert.assertEquals(
                expectedOutput,
                created.getString(sampleStartDateKey));
        }
    }
}
