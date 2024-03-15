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

import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;
import jakarta.ws.rs.client.Client;

import org.junit.Assert;

import de.intevation.lada.model.lada.StatusProt;
import de.intevation.lada.test.ServiceTest;

/**
 * Test status entities.
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class StatusTest extends ServiceTest {

    private static final long TS1 = 1450371851654L;
    private JsonObject expectedById;
    private JsonObject create;
    private JsonObject reset;
    private JsonObject undeliverable;

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

        // Prepare expected object
        JsonObject status =
            readXmlResource("datasets/dbUnit_lada.xml", StatusProt.class)
            .getJsonObject(0);
        JsonObjectBuilder builder = convertObject(status);
        builder.add("parentModified", TS1);
        builder.add("readonly", JsonValue.FALSE);
        builder.add("owner", JsonValue.TRUE);
        expectedById = builder.build();
        Assert.assertNotNull(expectedById);

        // Load objects to test POST requests
        create = readJsonResource("/datasets/status.json");
        Assert.assertNotNull(create);
        reset = readJsonResource("/datasets/status-reset.json");
        undeliverable = readJsonResource("/datasets/status-undeliverable.json");
    }

    /**
     * Execute the tests.
     */
    public final void execute() {
        get("rest/statusprot?measmId=1000");
        getById("rest/statusprot/1000", expectedById);
        create("rest/statusprot", create);
        create("rest/statusprot", reset);

        // Assert that measVals are deleted if status is set
        // to "nicht lieferbar"
        int measmId = undeliverable.getInt("measmId");
        Assert.assertTrue(
            "Test data must provide measVals for deletion",
            hasMeasVals(measmId));
        create("rest/statusprot", undeliverable);
        Assert.assertFalse(
            "measVals should have been deleted",
            hasMeasVals(measmId));
    }

    private boolean hasMeasVals(int measmId) {
        return !get("rest/measval?measmId=" + measmId)
            .getJsonArray("data").isEmpty();
    }
}
