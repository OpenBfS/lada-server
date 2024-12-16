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
import jakarta.json.JsonValue;
import jakarta.ws.rs.client.WebTarget;

import org.junit.Assert;

import de.intevation.lada.model.lada.Geolocat;
import de.intevation.lada.test.ServiceTest;

/**
 * Test ortzuordnung entities.
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class OrtszuordnungTest extends ServiceTest {

    private static final long TS1 = 1450371851654L;
    private JsonObject expectedById;
    private JsonObject create;

    @Override
    public void init(WebTarget t) {
        super.init(t);

        // Attributes with timestamps
        timestampAttributes = Arrays.asList(new String[]{
            "lastMod"
        });

        expectedById = convertObject(
            readXmlResource("datasets/dbUnit_lada.xml", Geolocat.class)
                .getJsonObject(0))
            .add("parentModified", TS1)
            .add("readonly", JsonValue.FALSE)
            .add("owner", JsonValue.TRUE)
            .build();
        Assert.assertNotNull(expectedById);

        // Load probe object to test POST request
        create = readJsonResource("/datasets/ortszuordnung.json");
        Assert.assertNotNull(create);
    }

    /**
     * Execute the tests.
     */
    public final void execute() {
        get("rest/geolocat?sampleId=1000");
        getById("rest/geolocat/1000", expectedById);
        JsonObject created =
            create("rest/geolocat", create);
        update(
            "rest/geolocat/1000",
            "addSiteText",
            "Test",
            "Test geändert");
        delete("rest/geolocat/" + created.get("id"));
    }
}
