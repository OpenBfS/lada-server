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

import de.intevation.lada.model.master.DatasetCreator;
import de.intevation.lada.test.ServiceTest;

/**
 * Test datensatz erzeuger entities.
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class DatensatzErzeugerTest extends ServiceTest {
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
            "lastMod"
        });

        // Prepare expected object
        JsonObject erzeuger =
            readXmlResource("datasets/dbUnit_master.xml", DatasetCreator.class)
            .getJsonObject(0);
        JsonObjectBuilder builder = convertObject(erzeuger);
        expectedById = builder.build();
        Assert.assertNotNull(expectedById);

        // Load object to test POST request
        create = readJsonResource("/datasets/datensatzerzeuger.json");
        Assert.assertNotNull(create);
    }

    /**
     * Execute the tests.
     */
    public final void execute() {
        get("rest/datasetcreator");
        getById(
            "rest/datasetcreator/1000",
            expectedById);
        update(
            "rest/datasetcreator/1000",
            "descr",
            "Testbezeichnung",
            "geändert");
        JsonObject created = create(
            "rest/datasetcreator", create);
        delete("rest/datasetcreator/" + created.get("id"));
    }
}
