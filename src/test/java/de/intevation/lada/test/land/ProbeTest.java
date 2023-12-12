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
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response.Status;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;

import de.intevation.lada.model.lada.Sample;
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
        URL baseUrl
    ) {
        super.init(c, baseUrl);
        // Attributes with timestamps
        timestampAttributes = Arrays.asList(new String[]{
            "lastMod",
            "sampleStartDate",
            "schedStartDate",
            "schedEndDate",
            "treeMod"
        });

        // Prepare expected probe object
        JsonObject probe = filterJsonArrayById(
            readXmlResource("datasets/dbUnit_lada.xml", Sample.class),
            1000);
        expectedById = convertObject(probe)
            .addNull("midSampleDate")
            .addNull("sampleEndDate")
            .addNull("datasetCreatorId")
            .addNull("mpgCategId")
            .add("readonly", false)
            .add("owner", true)
            .build();

        // Load probe object to test POST request
        create = readJsonResource("/datasets/probe.json");
        Assert.assertNotNull(create);
    }

    /**
     * Execute the tests.
     */
    public final void execute() {
        get("rest/sample", Status.METHOD_NOT_ALLOWED);

        final String dataKey = "data";
        final String warningsKey = "warnings";
        final String expectedWarningKey = "entnahmeOrt";

        MatcherAssert.assertThat(
            getById("rest/sample/1000", expectedById)
                .getJsonObject(dataKey).getJsonObject(warningsKey).keySet(),
            CoreMatchers.hasItem(expectedWarningKey));

        JsonObject created = create("rest/sample", create);
        MatcherAssert.assertThat(
            created.getJsonObject(dataKey).getJsonObject(warningsKey).keySet(),
            CoreMatchers.hasItem(expectedWarningKey));

        final String updateFieldKey = "mainSampleId";
        final String newValue = "130510002";
        MatcherAssert.assertThat(
            update("rest/sample/1000", updateFieldKey, "120510002", newValue)
                .getJsonObject(dataKey).getJsonObject(warningsKey).keySet(),
            CoreMatchers.hasItem(expectedWarningKey));

        // Ensure invalid envDescripDisplay is rejected
        update(
            "rest/sample/1000",
            "envDescripDisplay",
            "D: 59 04 01 00 05 05 01 02 00 00 00 00",
            "",
            Status.BAD_REQUEST);

        getAuditTrail(
            "rest/audit/probe/1000",
            updateFieldKey,
            newValue);
        delete("rest/sample/" + created.getJsonObject("data").get("id"));
    }
}
