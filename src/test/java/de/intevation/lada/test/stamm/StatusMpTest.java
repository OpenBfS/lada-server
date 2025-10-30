/* Copyright (C) 2023 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.stamm;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;

import de.intevation.lada.BaseTest;
import de.intevation.lada.ClientBaseTest;
import de.intevation.lada.model.master.StatusLev_;
import de.intevation.lada.model.master.StatusMpView;
import de.intevation.lada.model.master.StatusMp_;
import de.intevation.lada.model.master.StatusVal_;
import de.intevation.lada.rest.StatusMpService;
import de.intevation.lada.rest.StatusMpViewService;
import de.intevation.lada.test.ServiceTest;

/**
 * Test StatusMpService.
 */
public class StatusMpTest extends ServiceTest {

    private static final String STATUS_MP_URL = UriBuilder
        .fromResource(StatusMpService.class).build() + "/";
    private static final String STATUS_MP_VIEW_URL = UriBuilder
        .fromResource(StatusMpViewService.class).build() + "/";

    @Override
    public void init(WebTarget t) {
        super.init(t);
    }

    /**
     * Execute the tests.
     */
    public void execute() {
        get(STATUS_MP_URL);

        // Test endpoint to get value by statusMpId
        final int statusMpId = 1;
        final String lev = "MST", val = "nicht vergeben";
        getById(STATUS_MP_URL + statusMpId, Json.createObjectBuilder()
            .add(StatusMp_.ID, statusMpId)
            .add(StatusMp_.STATUS_LEV, Json.createObjectBuilder()
                .add(StatusLev_.ID, 1).add(StatusLev_.LEV, lev))
            .add(StatusMp_.STATUS_VAL, Json.createObjectBuilder()
                .add(StatusVal_.ID, 0).add(StatusVal_.VAL, val))
            .build()
        );

        // Test service for human readable descriptions
        String statusComb = get(STATUS_MP_VIEW_URL,
            new GenericType<List<StatusMpView>>() {})
            .stream()
            .filter(e -> e.getStatusMpId().equals(1))
            .map(StatusMpView::getStatusComb).findFirst().get();
        assertEquals(lev + " - " + val, statusComb);

        // Test endpoint to get values by measmIds
        final int measmId = 1801;
        Response response = target.path(STATUS_MP_URL + "getbyids")
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .accept(MediaType.APPLICATION_JSON)
            .post(Entity.entity(
                    Json.createArrayBuilder().add(measmId).build().toString(),
                    MediaType.APPLICATION_JSON));
        final JsonArray reachable =
            ClientBaseTest.parseResponse(response).asJsonArray();
        MatcherAssert.assertThat(
            reachable,
            CoreMatchers.hasItems(
                Json.createObjectBuilder()
                .add(StatusMp_.ID, 2)
                .add(StatusMp_.STATUS_LEV, Json.createObjectBuilder()
                    .add(StatusLev_.ID, 1).add(StatusLev_.LEV, lev))
                .add(StatusMp_.STATUS_VAL, Json.createObjectBuilder()
                    .add(StatusVal_.ID, 1).add(StatusVal_.VAL, "plausibel"))
                .build(),
                Json.createObjectBuilder()
                .add(StatusMp_.ID, 3)
                .add(StatusMp_.STATUS_LEV, Json.createObjectBuilder()
                    .add(StatusLev_.ID, 1).add(StatusLev_.LEV, lev))
                .add(StatusMp_.STATUS_VAL, Json.createObjectBuilder()
                    .add(StatusVal_.ID, 2)
                    .add(StatusVal_.VAL, "nicht repräsentativ"))
                .build()));
    }
}
