/* Copyright (C) 2021 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.stamm;

import java.net.URL;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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

    private final String name = "spatrefsys";

    private final String url = "rest/spatrefsys/";

    @Override
    public void init(
        Client c,
        URL baseUrl
    ) {
        super.init(c, baseUrl);

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

        WebTarget target = client.target(baseUrl + url);
        Response response = target.request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .post(Entity.entity(
                    requestJson.toString(), MediaType.APPLICATION_JSON));
        JsonObject content = BaseTest.parseResponse(response);

        /* Verify the response*/
        final String dataKey = "data";
        BaseTest.assertContains(content, dataKey);
        JsonObject data = content.getJsonObject(dataKey);
        BaseTest.assertContains(data, xKey);
        BaseTest.assertContains(data, yKey);
        Assert.assertEquals(coord, data.getString(xKey));
        Assert.assertEquals(coord, data.getString(yKey));
    }
}
