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
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response.Status;

import org.junit.Assert;

import de.intevation.lada.Protocol;
import de.intevation.lada.test.ServiceTest;

/**
 * Test probe entities.
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class ProbeTest extends ServiceTest {

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
            "lastMod",
            "sampleStartDate",
            "schedStartDate",
            "schedEndDate",
            "treeMod"
        });

        // Prepare expected probe object
        JsonObject content = readJsonResource("/datasets/dbUnit_probe.json");
        JsonObject probe = content.getJsonArray("lada.sample").getJsonObject(0);
        JsonObjectBuilder builder = convertObject(probe);
        builder.addNull("midSampleDate");
        builder.addNull("sampleEndDate");
        builder.addNull("datasetCreatorId");
        builder.addNull("stateMpgId");
        builder.add("readonly", false);
        builder.add("owner", true);
        expectedById = builder.build();
        Assert.assertNotNull(expectedById);

        // Load probe object to test POST request
        create = readJsonResource("/datasets/probe.json");
        Assert.assertNotNull(create);
    }

    /**
     * Execute the tests.
     */
    public final void execute() {
        get("probe", "rest/sample", Status.METHOD_NOT_ALLOWED);
        getById("probe", "rest/sample/1000", expectedById);
        JsonObject created = create("probe", "rest/sample", create);
        update(
            "probe",
            "rest/sample/1000",
            "mainSampleId",
            "120510002",
            "130510002");
        delete(
            "probe",
            "rest/sample/" + created.getJsonObject("data").get("id"));
    }
}
