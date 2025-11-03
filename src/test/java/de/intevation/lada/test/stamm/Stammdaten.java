/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.stamm;

import static de.intevation.lada.rest.LadaService.PATH_REST;
import static org.junit.Assert.assertTrue;

import jakarta.json.JsonObject;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

import org.junit.Assert;

import de.intevation.lada.BaseTest;
import de.intevation.lada.ClientBaseTest;
import de.intevation.lada.test.ServiceTest;

/**
 * Test Stammdaten entities.
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class Stammdaten extends ServiceTest {

    @Override
    public void init(WebTarget t) {
        super.init(t);
    }

    /**
     * Test the GET Service by requesting all objects.
     *
     * @param type the entity type.
     */
    public final void getAll(String type) {
        Assert.assertNotNull(type);
        get(PATH_REST + type);
    }

    /**
     * Get entity by id.
     * @param type the entity type
     * @param id the entity id
     */
    public final void getById(
        String type,
        Object id
    ) {
        /* Request an object by id*/
        Response response = target.path(PATH_REST + type + "/" + id).request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .get();
        assertTrue("Response should contain an object",
            ClientBaseTest.parseResponse(response) instanceof JsonObject);
    }
}
