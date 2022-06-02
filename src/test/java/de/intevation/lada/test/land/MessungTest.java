/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.land;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

import org.junit.Assert;

import de.intevation.lada.Protocol;
import de.intevation.lada.test.ServiceTest;

/**
 * Test messung entities.
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class MessungTest extends ServiceTest {

    private static final int ID1000 = 1000;
    private static final long TS1 = 1450371851654L;
    private JsonObject expectedById;
    private JsonObject create;

    @Override
    public void init(
        Client c,
        URL baseUrl,
        List<Protocol> protocol
    ) {
        super.init(c, baseUrl, protocol);
        // Attributes with timestamps
        timestampAttributes = Arrays.asList(new String[]{
            "letzteAenderung",
            "messzeitpunkt",
            "treeModified"
        });

        // Prepare expected probe object
        JsonObject content = readJsonResource("/datasets/dbUnit_probe.json");
        JsonObject messung =
            content.getJsonArray("land.messung").getJsonObject(0);
        // Automatic conversion of key for external ID does not work
        final String extIdKey = "ext_id";
        expectedById = convertObject(messung, extIdKey)
            .add("externeMessungsId", messung.get(extIdKey))
            .add("parentModified", TS1)
            .add("readonly", JsonValue.FALSE)
            .add("owner", JsonValue.TRUE)
            .add("status", ID1000)
            .build();
        Assert.assertNotNull(expectedById);

        // Load probe object to test POST request
        create = readJsonResource("/datasets/messung.json");
        Assert.assertNotNull(create);
    }

    /**
     * Execute the tests.
     */
    public final void execute() {
        get("messung", "rest/messung", Response.Status.BAD_REQUEST);
        get("messung", "rest/messung?probeId=1000");
        getById("messung", "rest/messung/1200", expectedById);
        JsonObject created = create("messung", "rest/messung", create);
        update("messung", "rest/messung/1200", "nebenprobenNr", "T100", "U200");
        delete(
            "messung",
            "rest/messung/" + created.getJsonObject("data").get("id"));
    }
}
