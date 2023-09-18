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

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.ws.rs.client.Client;

import org.junit.Assert;

import de.intevation.lada.model.lada.CommSample;
import de.intevation.lada.test.ServiceTest;

/**
 * Test probe kommentar entities.
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class KommentarPTest extends ServiceTest {

    private static final long TS1 = 1450371851654L;
    private JsonObject expectedById;
    private JsonObject create;

    @Override
    public void init(
        Client c,
        URL baseUrl
    ) {
        super.init(c, baseUrl);
        // Attributes with timestamps
        timestampAttributes = Arrays.asList(new String[]{
            "date",
            "treeModified"
        });

        // Prepare expected probe object
        JsonObject content =
            readXmlResource("datasets/dbUnit_lada.xml", CommSample.class)
            .getJsonObject(0);
        JsonObjectBuilder builder = convertObject(content);
        builder.add("parentModified", TS1);
        builder.add("readonly", JsonValue.FALSE);
        builder.add("owner", JsonValue.TRUE);
        expectedById = builder.build();
        Assert.assertNotNull(expectedById);

        // Load probe object to test POST request
        create = readJsonResource("/datasets/pkommentar.json");
        Assert.assertNotNull(create);
    }

    /**
     * Execute the tests.
     */
    public final void execute() {
        get("rest/commsample?sampleId=1000");
        getById("rest/commsample/1000", expectedById);
        JsonObject created = create("rest/commsample", create);
        update(
            "rest/commsample/1000",
            "text", "Testkommentar",
            "Testkommentar geändert");
        delete(
            "rest/commsample/" + created.getJsonObject("data").get("id"));
    }
}
