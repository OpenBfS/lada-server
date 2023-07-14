/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.stamm;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;

import de.intevation.lada.BaseTest;
import de.intevation.lada.test.ServiceTest;

/**
 * Test Stammdaten entities.
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class Stammdaten extends ServiceTest {

    private static Map<String, Matcher> matchers;

    @Override
    public void init(
        Client c,
        URL baseUrl
    ) {
        super.init(c, baseUrl);

        matchers = new HashMap<>();
        matchers.put("regulation",
            Matchers.containsInAnyOrder(
                "id",
                "lastMod",
                "descr",
                "regulation"
            )
        );
        matchers.put("measunit",
            Matchers.containsInAnyOrder(
                "id",
                "lastMod",
                "name",
                "unitSymbol",
                "eudfUnitId",
                "eudfConversFactor",
                "primary"
            )
        );
        matchers.put("measd",
            Matchers.containsInAnyOrder(
                "id",
                "lastMod",
                "descr",
                "defColor",
                "eudfNuclId",
                "idfExtId",
                "isRefNucl",
                "bvlFormatId",
                "name"
            )
        );
        matchers.put("mmt",
            Matchers.containsInAnyOrder(
                "id",
                "lastMod",
                "descr",
                "name"
            )
        );
        matchers.put("measfacil",
            Matchers.containsInAnyOrder(
                "id",
                "lastMod",
                "trunkCode",
                "address",
                "name",
                "measFacilType",
                "networkId"
            )
        );
        matchers.put("network",
            Matchers.containsInAnyOrder(
                "id",
                "lastMod",
                "isActive",
                "idfNetworkId",
                "isFmn",
                "mailList",
                "name"
            )
        );
        matchers.put("pflichtmessgroesse",
            Matchers.containsInAnyOrder(
                "id",
                "letzteAenderung",
                "messgroesseId",
                "datenbasisId",
                "mmtId",
                "umwId"
            )
        );
        matchers.put("samplemeth",
            Matchers.containsInAnyOrder(
                "id",
                "lastMod",
                "name",
                "extId",
                "eudfSampleMethId"
            )
        );
        matchers.put("samplespecif",
            Matchers.containsInAnyOrder(
                "id",
                "lastMod",
                "name",
                "eudfKeyword",
                "extId",
                "measUnitId"
            )
        );
        matchers.put("location",
            Matchers.containsInAnyOrder(
                "id",
                "beschreibung",
                "bezeichnung",
                "hoeheLand",
                "koordXExtern",
                "koordYExtern",
                "latitude",
                "longitude",
                "letzteAenderung",
                "unscharf",
                "netzbetreiberId",
                "staatId",
                "verwaltungseinheitId",
                "otyp",
                "koordinatenartId",
                "geom"
            )
        );
        matchers.put("spatrefsys",
            Matchers.containsInAnyOrder(
                "id",
                "lastMod",
                "idfGeoKey",
                "name"
            )
        );
        matchers.put("siteclass",
            Matchers.containsInAnyOrder(
            "id",
            "lastMod",
            "name",
            "extId"
            )
        );
        matchers.put("typeregulation",
            Matchers.containsInAnyOrder(
            "id",
            "name",
            "lastMod"
            )
        );
        matchers.put("poi",
            Matchers.containsInAnyOrder(
            "id",
            "name",
            "lastMod"
            )
        );
        matchers.put("state",
            Matchers.containsInAnyOrder(
                "id",
                "lastMod",
                "isEuCountry",
                "ctryOrigId",
                "coordXExt",
                "coordYExt",
                "ctry",
                "iso3166",
                "intVehRegCode",
                "spatRefSysId"
            )
        );
        matchers.put("targactmmtgr",
            Matchers.containsInAnyOrder(
                "id",
                "name",
                "targEnvGrDispl"
            )
        );
        matchers.put("targenvgr",
            Matchers.containsInAnyOrder(
                "id",
                "name",
                "descr"
            )
        );
        matchers.put("envmedium",
            Matchers.containsInAnyOrder(
                "id",
                "lastMod",
                "descr",
                "name",
                "unit1",
                "unit2"
            )
        );
        matchers.put("envspecifmp",
            Matchers.containsInAnyOrder(
                "id",
                "sampleSpecifId",
                "envMediumId",
                "lastMod"
            )
        );
        matchers.put("adminunit",
            Matchers.containsInAnyOrder(
                "id",
                "name",
                "stateId",
                "isState",
                "isMunic",
                "isRuralDist",
                "isGovDist",
                "ruralDistId",
                "latitude",
                "longitude",
                "zip",
                "govDistId"
            )
        );
        matchers.put("reiaggr",
            Matchers.containsInAnyOrder(
                "id",
                "name",
                "descr",
                "lastMod"
            )
        );
    }

    /**
     * Test the GET Service by requesting all objects.
     *
     * @param type the entity type.
     */
    public final void getAll(
        String type
    ) {
        Assert.assertNotNull(type);
        get(type, "rest/" + type);
    }

    /**
     * Get entity by id.
     * @param type the entity type
     * @param id the entity id
     */
    @SuppressWarnings("unchecked")
    public final void getById(
        String type,
        Object id
    ) {
        /* Create a client*/
        WebTarget target =
            client.target(baseUrl + "rest/" + type + "/" + id);
        /* Request an object by id*/
        Response response = target.request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .get();
        JsonObject content = BaseTest.parseResponse(response);
        /* Verify the response*/
        MatcherAssert.assertThat(content.getJsonObject("data").keySet(),
            matchers.get(type));
    }
}
