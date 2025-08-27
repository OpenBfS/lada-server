/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.stamm;

import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.UriBuilder;

import org.junit.Assert;

import de.intevation.lada.BaseTest;
import de.intevation.lada.model.master.DatasetCreator;
import de.intevation.lada.model.master.DatasetCreator_;
import de.intevation.lada.rest.DatasetCreatorService;
import de.intevation.lada.test.ServiceTest;

/**
 * Test datensatz erzeuger entities.
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class DatensatzErzeugerTest extends ServiceTest {
    private JsonObject expectedById;
    private JsonObject create;

    @Override
    public void init(WebTarget t) {
        super.init(t);

        // Prepare expected object
        JsonObject erzeuger =
            BaseTest.readXmlResource("datasets/dbUnit_master.xml", DatasetCreator.class)
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
        final String url = UriBuilder.fromResource(DatasetCreatorService.class)
            .build().getPath() + "/";
        final int id = 1000;
        get(url, DatasetCreator.class);
        getById(url + id, expectedById);
        update(
            url + id,
            DatasetCreator_.DESCR,
            "Testbezeichnung",
            "geändert");
        JsonObject created = create(url, create);
        delete(url + created.get(DatasetCreator_.ID));
    }
}
