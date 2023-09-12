/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.stamm;

import java.net.URL;
import java.util.Arrays;

import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.client.Client;

import org.junit.Assert;

import de.intevation.lada.model.master.Sampler;
import de.intevation.lada.test.ServiceTest;

/**
 * Test Probenehmer entities.
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class ProbenehmerTest extends ServiceTest {

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
            "letzteAenderung"
        });

        // Load object to test POST request
        create = readJsonResource("/datasets/probenehmer.json");

        // Prepare expected object
        JsonObject probenehmer = filterJsonArrayById(
            readXmlResource("datasets/dbUnit_master.xml", Sampler.class),
            1000);

        JsonObjectBuilder builder = convertObject(probenehmer);
        expectedById = builder.build();
        Assert.assertNotNull(expectedById);

        Assert.assertNotNull(create);
    }

    /**
     * Execute the tests.
     */
    public final void execute() {
        get("sampler", "rest/sampler");
        getById("sampler", "rest/sampler/1000", expectedById);
        update(
            "sampler",
            "rest/sampler/1000",
            "descr",
            "Testbezeichnung",
            "geändert");
        JsonObject created = create("sampler", "rest/sampler", create);
        delete(
            "sampler",
            "rest/sampler/" + created.getJsonObject("data").get("id"));
    }
}
