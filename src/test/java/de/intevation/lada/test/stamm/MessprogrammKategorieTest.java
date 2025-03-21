/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.stamm;

import java.util.Arrays;

import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.client.WebTarget;

import org.junit.Assert;

import de.intevation.lada.model.master.MpgCateg;
import de.intevation.lada.test.ServiceTest;

/**
 * Test messprogramm kategorie entities.
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class MessprogrammKategorieTest extends ServiceTest {

    private JsonObject expectedById;
    private JsonObject create;

    @Override
    public void init(WebTarget t) {
        super.init(t);

        // Attributes with timestamps
        timestampAttributes = Arrays.asList(new String[]{
            "lastMod"
        });

        // Prepare expected object
        JsonObject erzeuger =
            readXmlResource("datasets/dbUnit_master.xml", MpgCateg.class)
            .getJsonObject(0);
        JsonObjectBuilder builder = convertObject(erzeuger);
        expectedById = builder.build();
        Assert.assertNotNull(expectedById);

        // Load object to test POST request
        create = readJsonResource("/datasets/messprogrammkategorie.json");
        Assert.assertNotNull(create);
    }

    /**
     * Execute the tests.
     */
    public final void execute() {
        get("rest/mpgcateg");
        getById("rest/mpgcateg/1000", expectedById);
        update(
            "rest/mpgcateg/1000",
            "name",
            "Testbezeichnung",
            "geändert");
        JsonObject created = create("rest/mpgcateg", create);
        delete("rest/mpgcateg/" + created.get("id"));
    }
}
