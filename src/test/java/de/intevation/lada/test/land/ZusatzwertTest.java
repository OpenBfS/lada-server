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
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.ws.rs.client.Client;

import org.junit.Assert;

import de.intevation.lada.Protocol;
import de.intevation.lada.test.ServiceTest;

/**
 * Test zusatzwert entities.
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class ZusatzwertTest extends ServiceTest {

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
            "treeModified"
        });

        // Prepare expected probe object
        JsonObject content =
            readJsonResource("/datasets/dbUnit_probe.json");
        JsonObject messung = content.getJsonArray("lada.sample_specif_meas_val")
            .getJsonObject(0);
        JsonObjectBuilder builder = convertObject(messung);
        builder.add("parentModified", TS1);
        builder.add("readonly", JsonValue.FALSE);
        builder.add("owner", JsonValue.TRUE);
        expectedById = builder.build();
        Assert.assertNotNull(expectedById);

        // Load probe object to test POST request
        create = readJsonResource("/datasets/zusatzwert.json");
        Assert.assertNotNull(create);
    }

    /**
     * Execute the tests.
     */
    public final void execute() {
        get("zusatzwert", "rest/zusatzwert?probeId=1000");
        getById("zusatzwert", "rest/zusatzwert/1000", expectedById);
        JsonObject created = create("zusatzwert", "rest/zusatzwert", create);
        update("zusatzwert", "rest/zusatzwert/1000", "pzsId", "A75", "A74");
        delete(
            "zusatzwert",
            "rest/zusatzwert/" + created.getJsonObject("data").get("id"));
    }
}
