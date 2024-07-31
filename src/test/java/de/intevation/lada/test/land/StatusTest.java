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
import jakarta.ws.rs.client.Client;

import org.junit.Assert;

import de.intevation.lada.model.lada.StatusProt_;
import de.intevation.lada.test.ServiceTest;


/**
 * Test status entities.
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class StatusTest extends ServiceTest {

    private static final long TS1 = 1450371851654L;
    private JsonObject create;
    private JsonObject reset;

    private JsonObject undeliverablePartiallyValid;
    private JsonObject undeliverableInvalid;

    @Override
    public void init(
        Client c,
        URL baseUrl
    ) {
        super.init(c, baseUrl);
        // Attributes with timestamps
        timestampAttributes = Arrays.asList(new String[]{
            "date",
            "treeMod"
        });

        // Load objects to test POST requests
        create = readJsonResource("/datasets/status.json");
        undeliverablePartiallyValid = readJsonResource(
            "/datasets/status_undeliverable_partially_valid.json");
        undeliverableInvalid = readJsonResource(
            "/datasets/status_undeliverable_invalid.json");
        Assert.assertNotNull(create);
        reset = readJsonResource("/datasets/status-reset.json");
    }

    /**
     * Execute the tests.
     */
    public final void execute() {
        final String url = "rest/statusprot/";
        final int expectedMeasmId = 1200;
        int id = get(url + "?measmId=" + expectedMeasmId).asJsonArray()
            .getJsonObject(0).getInt("id");

        getById(url + id,
            Json.createObjectBuilder()
            .add(StatusProt_.MEASM_ID, expectedMeasmId)
            .add(StatusProt_.STATUS_MP_ID, 1)
            .build());

        create(url, create);
        create(url, reset);

        // Check cases where status is set to undeliverable
        int measmId;
        //Test for measm with partially valid measvals
        //-> MeasVals should be kept
        measmId = undeliverablePartiallyValid.getInt(StatusProt_.MEASM_ID);
        create(url, undeliverablePartiallyValid);
        Assert.assertTrue(
            "measVals should have been kept",
            hasMeasVals(measmId));

        //Test for measm with invalid measvals
        //-> expect MeasVals to be deleted
        measmId = undeliverableInvalid.getInt(StatusProt_.MEASM_ID);
        create(url, undeliverableInvalid);
        Assert.assertFalse(
            "measVals should have been deleted",
            hasMeasVals(measmId));
    }

    private boolean hasMeasVals(int measmId) {
        return !get("rest/measval?measmId=" + measmId)
            .asJsonArray().isEmpty();
    }
}
