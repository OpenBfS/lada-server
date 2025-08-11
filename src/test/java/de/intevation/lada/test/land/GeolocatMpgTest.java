/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.land;

import jakarta.json.Json;
import jakarta.json.JsonValue;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.UriBuilder;

import de.intevation.lada.BaseTest;
import de.intevation.lada.model.lada.GeolocatMpg;
import de.intevation.lada.model.lada.GeolocatMpg_;
import de.intevation.lada.rest.GeolocatMpgService;


public class GeolocatMpgTest extends OrtszuordnungTest {

    @Override
    public void init(WebTarget t) {
        super.init(t);

        urlPath = UriBuilder.fromResource(GeolocatMpgService.class)
            .build().getPath() + "/";

        getParam = "mpgId";

        expectedById = convertObject(BaseTest.filterJsonArrayById(
                BaseTest.readXmlResource(
                    "datasets/dbUnit_lada.xml", GeolocatMpg.class),
                expectedId))
            .add(GeolocatMpg_.SITE, expectedSite)
            .add("readonly", JsonValue.FALSE)
            .add("owner", JsonValue.TRUE)
            .build();

        create = Json.createObjectBuilder(
            readJsonResource("/datasets/geolocatMpg.json"))
            .add(GeolocatMpg_.SITE, expectedSite)
            .build();

        parentIdField = GeolocatMpg_.MPG_ID;
    }
}
