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

import de.intevation.lada.model.lada.SampleSpecifMeasVal;
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
        URL baseUrl
    ) {
        super.init(c, baseUrl);
        // Attributes with timestamps
        timestampAttributes = Arrays.asList(new String[]{
            "lastMod",
            "treeMod"
        });

        // Prepare expected probe object
        JsonObject content = readXmlResource(
            "datasets/dbUnit_lada.xml", SampleSpecifMeasVal.class)
            .getJsonObject(0);
        JsonObjectBuilder builder = convertObject(content);
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
        get("rest/samplespecifmeasval?sampleId=1000");
        getById("rest/samplespecifmeasval/1000", expectedById);
        JsonObject created = create("rest/samplespecifmeasval", create);
        update("rest/samplespecifmeasval/1000", "sampleSpecifId", "A75", "A74");
        delete("rest/samplespecifmeasval/"
            + created.getJsonObject("data").get("id"));
    }
}
