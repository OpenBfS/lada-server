/* Copyright (C) 2021 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.stamm;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.junit.Assert;

import de.intevation.lada.BaseTest;
import de.intevation.lada.model.master.SpatRefSys;
import de.intevation.lada.test.ServiceTest;

/**
 * Tests for KoordinatenartService.
 */
public class KoordinatenartTest extends ServiceTest {

    public static final int KDA_ID = 4; // decimal geodetic

    private JsonObject expectedById;

    private final String url = "rest/spatrefsys/";

    @Override
    public void init(WebTarget t) {
        super.init(t);

        // Prepare expected object
        JsonObject erzeuger =
            readXmlResource("datasets/dbUnit_master.xml", SpatRefSys.class)
            .getJsonObject(0);
        JsonObjectBuilder builder = convertObject(erzeuger);
        expectedById = builder.build();
        Assert.assertNotNull(expectedById);
    }

    /**
     * Execute the tests.
     */
    public final void execute() {
        get(url);
        getById(url + expectedById.getInt("id"), expectedById);
        recalculate();
    }

    private void recalculate() {
        final String xKey = "x", yKey = "y", coord = "1";
        JsonObject requestJson = Json.createObjectBuilder()
            .add("from", KDA_ID)
            .add("to", KDA_ID)
            .add(xKey, coord)
            .add(yKey, coord)
            .build();

        Response response = target.path(url).request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .post(Entity.entity(
                    requestJson.toString(), MediaType.APPLICATION_JSON));

        /* Verify the response*/
        JsonObject data = BaseTest.parseResponse(response).asJsonObject();
        BaseTest.assertContains(data, xKey);
        BaseTest.assertContains(data, yKey);
        Assert.assertEquals(coord, data.getString(xKey));
        Assert.assertEquals(coord, data.getString(yKey));
    }
}
