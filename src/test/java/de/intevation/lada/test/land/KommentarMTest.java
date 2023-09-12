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

import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;
import jakarta.ws.rs.client.Client;

import org.junit.Assert;

import de.intevation.lada.model.lada.CommMeasm;
import de.intevation.lada.test.ServiceTest;

/**
 * Test messung kommentar entities.
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class KommentarMTest extends ServiceTest {

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
        JsonObject messung =
            readXmlResource("datasets/dbUnit_lada.xml", CommMeasm.class)
            .getJsonObject(0);
        JsonObjectBuilder builder = convertObject(messung);
        builder.add("parentModified", TS1);
        builder.add("readonly", JsonValue.FALSE);
        builder.add("owner", JsonValue.TRUE);
        expectedById = builder.build();
        Assert.assertNotNull(expectedById);

        // Load probe object to test POST request
        create = readJsonResource("/datasets/mkommentar.json");
        Assert.assertNotNull(create);
    }

    /**
     * Execute the tests.
     */
    public final void execute() {
        get("commmeasm", "rest/commmeasm?measmId=1200");
        getById("commmeasm", "rest/commmeasm/1000", expectedById);
        JsonObject created = create("commmeasm", "rest/commmeasm", create);
        update(
            "commmeasm",
            "rest/commmeasm/1000",
            "text", "Testkommentar",
            "Testkommentar geändert");
        delete(
            "commmeasm",
            "rest/commmeasm/" + created.getJsonObject("data").get("id"));
    }

}
