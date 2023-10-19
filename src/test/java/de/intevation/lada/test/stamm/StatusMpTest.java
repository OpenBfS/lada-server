/* Copyright (C) 2023 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.stamm;

import java.net.URL;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;

import org.junit.Assert;

import de.intevation.lada.BaseTest;
import de.intevation.lada.test.ServiceTest;

/**
 * Test StatusMpService.
 */
public class StatusMpTest extends ServiceTest {

    private static final String URL = "rest/statusmp/";

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
    public void execute() {
        get(URL);

        getById(URL + "1", Json.createObjectBuilder()
            .add("id", 1)
            .add("statusLev", Json.createObjectBuilder()
                .add("id", 1).add("lev", "MST"))
            .add("statusVal", Json.createObjectBuilder()
                .add("id", 0).add("val", "nicht vergeben"))
            .build()
        );

        final int measmId = 1801;
        // Nothing created here, but the method just issues a POST request
        final JsonObject reachable = create(URL + "getbyids",
            Json.createArrayBuilder().add(measmId).build());
        final String dataKey = "data";
        BaseTest.assertContains(reachable, dataKey);
        Assert.assertEquals(Json.createArrayBuilder()
            .add(Json.createObjectBuilder()
                .add("id", 2)
                .add("statusLev", Json.createObjectBuilder()
                    .add("id", 1).add("lev", "MST"))
                .add("statusVal", Json.createObjectBuilder()
                    .add("id", 1).add("val", "plausibel")))
            .add(Json.createObjectBuilder()
                .add("id", 3)
                .add("statusLev", Json.createObjectBuilder()
                    .add("id", 1).add("lev", "MST"))
                .add("statusVal", Json.createObjectBuilder()
                    .add("id", 2).add("val", "nicht repräsentativ")))
            .build(),
            reachable.getJsonArray(dataKey));
    }
}
