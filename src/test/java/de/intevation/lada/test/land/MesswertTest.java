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
import javax.ws.rs.core.Response;

import org.junit.Assert;

import de.intevation.lada.BaseTest;
import de.intevation.lada.Protocol;
import de.intevation.lada.test.ServiceTest;

/**
 * Test messwert entities.
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class MesswertTest extends ServiceTest {

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
        JsonObject content = readJsonResource("/datasets/dbUnit_probe.json");
        JsonObject messwert =
            content.getJsonArray("land.messwert").getJsonObject(0);
        JsonObjectBuilder builder = convertObject(messwert);
        builder.add("parentModified", TS1);
        builder.add("readonly", JsonValue.FALSE);
        builder.add("owner", JsonValue.TRUE);
        expectedById = builder.build();
        Assert.assertNotNull(expectedById);

        // Load probe object to test POST request
        create = readJsonResource("/datasets/messwert.json");
        Assert.assertNotNull(create);
    }

    /**
     * Execute the tests.
     */
    public final void execute() {
        get("messwert", "rest/messwert?messungsId=1200");
        getById("messwert", "rest/messwert/10000", expectedById);
        normalize(expectedById);
        JsonObject created = create("messwert", "rest/messwert", create);
        update("messwert", "rest/messwert/10000", "messwertNwg", "<", ">");
        delete(
            "messwert",
            "rest/messwert/" + created.getJsonObject("data").get("id"));
    }

    /**
     * Test messwert normalization
     */
    private void normalize(JsonObject oldValue) {
        System.out.print(".");
        Protocol prot = new Protocol();
        prot.setName("messwert" + " service");
        prot.setType("normalize");
        prot.setPassed(false);
        protocol.add(prot);

        Response normalized = client.target(
            baseUrl + "rest/messwert/normalize?messungsId=1200")
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .put(null);
        JsonObject normalizedObject = BaseTest.parseResponse(normalized, prot);

        /* Verify the response*/
        boolean success = normalizedObject.getBoolean("success");
        Assert.assertTrue(
            "Unsuccessful response object:\n" + normalizedObject,
            success);
        prot.addInfo("success", success);
        String message = normalizedObject.getString("message");
        Assert.assertEquals("200", message);
        prot.addInfo("message", message);

        // The following makes assumptions about the first entry only
        JsonObject normalizedMesswert =
            normalizedObject.getJsonArray("data").getJsonObject(0);

        /* Verify normalized unit */
        final String unitK = "mehId";
        final int unitV = 208; // converted from 207
        Assert.assertEquals(
            unitV,
            normalizedMesswert.getInt(unitK));

        /* Verify normalized value */
        final String valueK = "nwgZuMesswert";
        final double valueFactor = 2; // factor for 207 -> 208
        final double epsilon = 1e-10;
        Assert.assertEquals(
            oldValue.getJsonNumber(valueK).doubleValue() * valueFactor,
            normalizedMesswert.getJsonNumber(valueK).doubleValue(),
            epsilon);

        prot.setPassed(true);
    }
}
