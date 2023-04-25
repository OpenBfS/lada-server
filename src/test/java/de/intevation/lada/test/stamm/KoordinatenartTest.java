/* Copyright (C) 2021 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.stamm;

import java.net.URL;
import java.util.List;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.junit.Assert;

import de.intevation.lada.BaseTest;
import de.intevation.lada.Protocol;
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
        URL baseUrl,
        List<Protocol> protocol
    ) {
        super.init(c, baseUrl, protocol);

        // Prepare expected object
        JsonObject content = readJsonResource(
            "/datasets/dbUnit_koordinatenart.json");
        JsonObject erzeuger =
            content.getJsonArray("master.spat_ref_sys").getJsonObject(0);
        JsonObjectBuilder builder = convertObject(erzeuger);
        expectedById = builder.build();
        Assert.assertNotNull(expectedById);
    }

    /**
     * Execute the tests.
     */
    public final void execute() {
        get(name, url);
        getById(name, url + expectedById.getInt("id"), expectedById);
        recalculate();
    }

    private void recalculate() {
        Protocol prot = new Protocol();
        prot.setName(name + " service");
        prot.setType("recalculate");
        prot.setPassed(false);
        protocol.add(prot);

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
        JsonObject content = BaseTest.parseResponse(response, prot);

        /* Verify the response*/
        final String dataKey = "data";
        BaseTest.assertContains(content, dataKey);
        JsonObject data = content.getJsonObject(dataKey);
        BaseTest.assertContains(data, xKey);
        BaseTest.assertContains(data, yKey);
        Assert.assertEquals(coord, data.getString(xKey));
        Assert.assertEquals(coord, data.getString(yKey));

        prot.setPassed(true);
    }
}
