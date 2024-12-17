/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.land;

import java.util.Arrays;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

import org.junit.Assert;

import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.test.ServiceTest;

/**
 * Test messung entities.
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class MessungTest extends ServiceTest {

    private static final long TS1 = 1450371851654L;
    private JsonObject expectedById;
    private JsonObject create;

    @Override
    public void init (WebTarget t) {
        super.init(t);

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
        get("rest/measm", Response.Status.BAD_REQUEST);

        JsonArray measms = get("rest/measm?sampleId=1000", JsonArray.class);
        Assert.assertEquals(1, measms.size());
        // Assert that returned collection elements are authorized and validated
        JsonObject measm = measms.getJsonObject(0);
        Assert.assertNotEquals("Authorization hints not set",
            JsonValue.NULL, measm.get("statusEdit"));
        Assert.assertFalse("Expected warnings missing",
            measm.getJsonObject("warnings").isEmpty());

        getById("rest/measm/1200", expectedById);
        JsonObject created = create("rest/measm", create);

        final String updateFieldKey = "minSampleId";
        final String updateFieldValue = "U200";
        update(
            "rest/measm/1200",
            updateFieldKey,
            "T100",
            updateFieldValue);

        // Test requests with invalid sampleId values
        final String sampleIdKey = "sampleId";
        final JsonValue curSampleId = Json.createValue(1000),
            invalidSampleId = Json.createValue(9999);
        update("rest/measm/1200", sampleIdKey,
            curSampleId, invalidSampleId,
            Response.Status.BAD_REQUEST);
        update("rest/measm/1200", sampleIdKey,
            curSampleId, null,
            Response.Status.BAD_REQUEST);

        getAuditTrail(
            "rest/audit/messung/1200",
            updateFieldKey,
            updateFieldValue);
        delete("rest/measm/" + created.get("id"));
    }
}
