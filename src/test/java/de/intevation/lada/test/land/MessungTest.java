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
import javax.json.JsonValue;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

import org.junit.Assert;

import de.intevation.lada.model.lada.Measm;
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
    public void init (
        Client c,
        URL baseUrl
    ) {
        super.init(c, baseUrl);
        // Attributes with timestamps
        timestampAttributes = Arrays.asList(new String[]{
            "lastMod",
            "measmStartDate",
            "treeMod"
        });

        // Prepare expected probe object
        JsonObject messung =
            readXmlResource("datasets/dbUnit_lada.xml", Measm.class)
            .getJsonObject(0);
        expectedById = convertObject(messung)
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
        get("measm", "rest/measm", Response.Status.BAD_REQUEST);
        get("measm", "rest/measm?sampleId=1000");
        getById("measm", "rest/measm/1200", expectedById);
        JsonObject created = create("measm", "rest/measm", create);

        final String updateFieldKey = "minSampleId";
        final String updateFieldValue = "U200";
        update(
            "measm",
            "rest/measm/1200",
            updateFieldKey,
            "T100",
            updateFieldValue);
        getAuditTrail(
            "measm",
            "rest/audit/messung/1200",
            updateFieldKey,
            updateFieldValue);
        delete(
            "measm",
            "rest/measm/" + created.getJsonObject("data").get("id"));
    }
}
