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
import jakarta.ws.rs.core.Response.Status;

import org.junit.Assert;

import de.intevation.lada.model.lada.StatusProt_;
import de.intevation.lada.rest.MeasValService;
import de.intevation.lada.rest.StatusProtService;
import de.intevation.lada.test.ServiceTest;


/**
 * Test status entities.
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class StatusTest extends ServiceTest {

    private static final UriBuilder URL = UriBuilder.fromResource(
        StatusProtService.class);
    private static final UriBuilder MEAS_VAL_URL = UriBuilder.fromResource(
        MeasValService.class);

    private JsonObject create;
    private JsonObject reset;

    private JsonObject undeliverablePartiallyValid;
    private JsonObject undeliverableInvalid;

    @Override
    public void init(WebTarget t) {
        super.init(t);

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
        final int expectedMeasmId = 1200;
        int id = get(URL.clone().queryParam("measmId", expectedMeasmId))
            .asJsonArray().getJsonObject(0).getInt("id");

        getById(URL.clone().path(String.valueOf(id)).build().toString(),
            Json.createObjectBuilder()
            .add("measmId", expectedMeasmId)
            .add(StatusProt_.STATUS_MP_ID, 1)
            .build());

        final String url = URL.build().toString();
        create(url, create);
        create(url, reset);

        // Check cases where status is set to undeliverable
        int measmId;
        //Test for measm with partially valid measvals
        //-> MeasVals should be kept
        measmId = undeliverablePartiallyValid.getInt("measmId");
        create(url, undeliverablePartiallyValid, Status.BAD_REQUEST);
        Assert.assertTrue(
            "measVals should have been kept",
            hasMeasVals(measmId));

        //Test for measm with invalid measvals
        //-> expect MeasVals to be deleted
        measmId = undeliverableInvalid.getInt("measmId");
        create(url, undeliverableInvalid);
        Assert.assertFalse(
            "measVals should have been deleted",
            hasMeasVals(measmId));
    }

    private boolean hasMeasVals(int measmId) {
        return !get(MEAS_VAL_URL.clone().queryParam("measmId", measmId))
            .asJsonArray().isEmpty();
    }
}
