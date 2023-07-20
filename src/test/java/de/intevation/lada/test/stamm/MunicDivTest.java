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

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.client.Client;

import org.dbunit.dataset.IDataSet;
import org.junit.Assert;

import de.intevation.lada.test.ServiceTest;

public class MunicDivTest extends ServiceTest {

    private JsonObject expectedById;
    private JsonObject create;

    @Override
    public void init(
        Client c,
        URL baseUrl,
        IDataSet dbDataset
    ) {
        super.init(c, baseUrl, dbDataset);
        // Attributes with timestamps
        timestampAttributes = Arrays.asList(new String[]{
            "lastMod"
        });

        // Prepare expected object
        JsonObject municDiv =
            readXmlResource(
                "datasets/dbUnit_master.xml", "master.munic_div")
            .getJsonObject(0);
        JsonObjectBuilder builder = convertObject(municDiv);
        expectedById = builder.build();
        Assert.assertNotNull(expectedById);

        // Load object to test POST request
        create = readJsonResource("/datasets/municDiv.json");
        Assert.assertNotNull(create);
    }

    /**
     * Execute the tests.
     */
    public final void execute() {
        get("municdiv", "rest/municdiv");
        getById("municdiv", "rest/municdiv/1000", expectedById);
        update(
            "municdiv",
            "rest/municdiv/1000",
            "name",
            "Testname",
            "UpdatedName");
        JsonObject created = create("municdiv", "rest/municdiv", create);
        delete(
            "municdiv",
            "rest/municdiv/" + created.getJsonObject("data").get("id"));
    }
}
