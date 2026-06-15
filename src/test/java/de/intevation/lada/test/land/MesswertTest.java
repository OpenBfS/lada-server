/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.land;

import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import static de.intevation.lada.model.lada.MeasVal_.DETECT_LIM;
import static de.intevation.lada.util.auth.Authentication.HEADER_X_SHIB_ROLES;
import static de.intevation.lada.util.auth.Authentication.HEADER_X_SHIB_USER;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;

import de.intevation.lada.BaseTest;
import de.intevation.lada.ClientBaseTest;
import de.intevation.lada.model.lada.MeasVal;
import de.intevation.lada.model.lada.MeasVal_;
import de.intevation.lada.rest.MeasValService;
import de.intevation.lada.test.ServiceTest;

/**
 * Test messwert entities.
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class MesswertTest extends ServiceTest {

    private static final UriBuilder MEASVAL_URI_BUILDER =
        UriBuilder.fromResource(MeasValService.class);
    private static final String MEASVAL_URL =
        MEASVAL_URI_BUILDER.build() + "/";

    private JsonObject expectedById;
    private JsonObject create;

    private final int measmId = 1200;

    @Override
    public void init(WebTarget t) {
        super.init(t);

        // Prepare expected probe object
        JsonObject messwert =
            BaseTest.readXmlResource("datasets/dbUnit_lada.xml", MeasVal.class)
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
            MeasVal_.MEAS_UNIT_ID, MeasVal_.ERROR };
        MatcherAssert.assertThat(
            get(MEASVAL_URI_BUILDER.clone().queryParam("measmId", measmId))
                .asJsonArray().getJsonObject(0)
                .getJsonObject(warningsKey).keySet(),
            CoreMatchers.hasItems(expectedWarningKeys));

        final int measValId = 10000;
        getById(MEASVAL_URL + measValId, expectedById);
        normalize(expectedById);
        JsonObject created = create(MEASVAL_URL, create);
        update(MEASVAL_URL + measValId, MeasVal_.LESS_THAN_LOD, "<", ">");
        delete(MEASVAL_URL + created.get(MeasVal_.ID));
    }

    /**
     * Test messwert normalization.
     */
    private void normalize(JsonObject oldValue) {
        Response normalized = target
            .path(MEASVAL_URI_BUILDER.clone().path("normalize")
                .build().toString())
            .queryParam("measmId", measmId)
            .request()
            .header(HEADER_X_SHIB_USER, BaseTest.testUser)
            .header(HEADER_X_SHIB_ROLES, BaseTest.testRoles)
            .put(null);

        // The following makes assumptions about the first entry only
        JsonObject normalizedMesswert = ClientBaseTest.parseResponse(normalized)
            .asJsonArray().getJsonObject(0);

        /* Verify normalized unit */
        final int unitV = 208; // converted from 207
        Assert.assertEquals(
            unitV,
            normalizedMesswert.getInt(MeasVal_.MEAS_UNIT_ID));

        /* Verify normalized value */
        final double valueFactor = 2; // factor for 207 -> 208
        final double epsilon = 1e-10;
        Assert.assertEquals(
            oldValue.getJsonNumber(DETECT_LIM).doubleValue() * valueFactor,
            normalizedMesswert.getJsonNumber(DETECT_LIM).doubleValue(),
            epsilon);
    }
}
