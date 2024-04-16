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
import jakarta.json.JsonArray;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;

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
        Response response = client.target(baseUrl + URL + "getbyids")
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .accept(MediaType.APPLICATION_JSON)
            .post(Entity.entity(
                    Json.createArrayBuilder().add(measmId).build().toString(),
                    MediaType.APPLICATION_JSON));
        final JsonArray reachable =
            BaseTest.parseResponse(response).asJsonArray();
        MatcherAssert.assertThat(
            reachable,
            CoreMatchers.hasItems(
                Json.createObjectBuilder()
                .add("id", 2)
                .add("statusLev", Json.createObjectBuilder()
                    .add("id", 1).add("lev", "MST"))
                .add("statusVal", Json.createObjectBuilder()
                    .add("id", 1).add("val", "plausibel"))
                .build(),
                Json.createObjectBuilder()
                .add("id", 3)
                .add("statusLev", Json.createObjectBuilder()
                    .add("id", 1).add("lev", "MST"))
                .add("statusVal", Json.createObjectBuilder()
                    .add("id", 2).add("val", "nicht repräsentativ"))
                .build()));
    }
}
