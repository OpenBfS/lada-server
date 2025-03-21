/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.land;

import java.util.Arrays;

import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;

import de.intevation.lada.BaseTest;
import de.intevation.lada.model.lada.MeasVal;
import de.intevation.lada.test.ServiceTest;

/**
 * Test messwert entities.
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class MesswertTest extends ServiceTest {

    private JsonObject expectedById;
    private JsonObject create;

    private final int measmId = 1200;

    @Override
    public void init(WebTarget t) {
        super.init(t);

        // Attributes with timestamps
        timestampAttributes = Arrays.asList(new String[]{
            "lastMod",
            "treeMod"
        });

        // Prepare expected probe object
        JsonObject messwert =
            readXmlResource("datasets/dbUnit_lada.xml", MeasVal.class)
            .getJsonObject(0);
        JsonObjectBuilder builder = convertObject(messwert);
        builder.add("readonly", JsonValue.FALSE);
        builder.add("owner", JsonValue.TRUE);
        expectedById = builder.build();
        Assert.assertNotNull(expectedById);

        // Load probe object to test POST request
        create = readJsonResource("/datasets/messwert.json");
        Assert.assertNotNull(create);
    }

    /**
     * Execute the tests.
     */
    public final void execute() {
        // Assert that GET receives warnings in items of response array
        final String warningsKey = "warnings";
        final String[] expectedWarningKeys = {
            "measUnitId", "measdId", "error" };
        MatcherAssert.assertThat(
            get("rest/measval?measmId=" + measmId).asJsonArray().getJsonObject(0)
                .getJsonObject(warningsKey).keySet(),
            CoreMatchers.hasItems(expectedWarningKeys));

        getById("rest/measval/10000", expectedById);
        normalize(expectedById);
        JsonObject created = create("rest/measval", create);
        update("rest/measval/10000", "lessThanLOD", "<", ">");
        delete("rest/measval/" + created.get("id"));
    }

    /**
     * Test messwert normalization.
     */
    private void normalize(JsonObject oldValue) {
        Response normalized = target
            .path("rest/measval/normalize")
            .queryParam("measmId", measmId)
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .put(null);

        // The following makes assumptions about the first entry only
        JsonObject normalizedMesswert = BaseTest.parseResponse(normalized)
            .asJsonArray().getJsonObject(0);

        /* Verify normalized unit */
        final String unitK = "measUnitId";
        final int unitV = 208; // converted from 207
        Assert.assertEquals(
            unitV,
            normalizedMesswert.getInt(unitK));

        /* Verify normalized value */
        final String valueK = "detectLim";
        final double valueFactor = 2; // factor for 207 -> 208
        final double epsilon = 1e-10;
        Assert.assertEquals(
            oldValue.getJsonNumber(valueK).doubleValue() * valueFactor,
            normalizedMesswert.getJsonNumber(valueK).doubleValue(),
            epsilon);
    }
}
