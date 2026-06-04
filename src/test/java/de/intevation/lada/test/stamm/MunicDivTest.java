/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.stamm;

import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.UriBuilder;

import static de.intevation.lada.model.master.MunicDiv_.ADMIN_UNIT_ID;
import static org.junit.Assert.assertEquals;

import org.junit.Assert;

import de.intevation.lada.BaseTest;
import de.intevation.lada.model.master.MunicDiv;
import de.intevation.lada.model.master.MunicDiv_;
import de.intevation.lada.rest.MunicDivService;
import de.intevation.lada.test.ServiceTest;

public class MunicDivTest extends ServiceTest {

    private static final UriBuilder MUNIC_DIV_URL_BUILDER =
        UriBuilder.fromResource(MunicDivService.class);
    private static final String MUNIC_DIV_URL =
        MUNIC_DIV_URL_BUILDER.build() + "/";

    private JsonObject expectedById;
    private JsonObject create;

    @Override
    public void init(WebTarget t) {
        super.init(t);

        // Prepare expected object
        JsonObject municDiv =
            BaseTest.readXmlResource("datasets/dbUnit_master.xml", MunicDiv.class)
            .getJsonObject(0);
        JsonObjectBuilder builder = convertObject(municDiv);
        expectedById = builder.build();
        Assert.assertNotNull(expectedById);

        // Load object to test POST request
        create = readJsonResource("/datasets/municDiv.json");
        Assert.assertNotNull(create);
    }

    /**
     * Execute the tests.
     */
    public final void execute() {
        assertEquals("Expected two entries in total",
            2, get(MUNIC_DIV_URL).asJsonArray().size());

        final String adminUnitId = "11000001";
        assertEquals("Expected one entry with given " + ADMIN_UNIT_ID,
            1,
            get(MUNIC_DIV_URL_BUILDER
                .queryParam(ADMIN_UNIT_ID, adminUnitId))
            .asJsonArray().size());

        final int municDivId = 1000;
        getById(MUNIC_DIV_URL + municDivId, expectedById);
        update(
            MUNIC_DIV_URL + municDivId,
            MunicDiv_.NAME,
            "Testname",
            "UpdatedName");

        JsonObject created = create(MUNIC_DIV_URL, create);
        delete(MUNIC_DIV_URL + created.get("id"));
    }
}
