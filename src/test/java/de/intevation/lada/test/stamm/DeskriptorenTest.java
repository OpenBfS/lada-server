/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.stamm;

import java.net.URL;
import java.util.List;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.client.Client;

import org.junit.Assert;

import de.intevation.lada.Protocol;
import de.intevation.lada.test.ServiceTest;

/**
 * Test Deskriptor entities.
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class DeskriptorenTest extends ServiceTest {

    private JsonObject expectedById;

    @Override
    public void init(
        Client c,
        URL baseUrl,
        List<Protocol> protocol
    ) {
        super.init(c, baseUrl, protocol);

        // Prepare expected object
        JsonObject content =
            readJsonResource("/datasets/dbUnit_deskriptor.json")
                .getJsonArray("master.env_descrip").getJsonObject(0);
        JsonObjectBuilder builder = convertObject(content);
        expectedById = builder.build();
        Assert.assertNotNull(expectedById);
    }

    /**
     * Execute the tests.
     */
    public final void execute() {
        get("deskriptor", "rest/deskriptor?layer=1");
        get("deskriptor", "rest/deskriptor?layer=1&parents=1&parents=2");
        getById("deskriptor", "rest/deskriptor/1000", expectedById);
    }
}
