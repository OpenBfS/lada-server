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
import java.util.List;
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
import de.intevation.lada.Protocol;
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
        URL baseUrl,
        List<Protocol> protocol
    ) {
        super.init(c, baseUrl, protocol);

        matchers = new HashMap<>();
        matchers.put("datenbasis",
            Matchers.containsInAnyOrder(
                "id",
                "letzteAenderung",
                "beschreibung",
                "datenbasis"
            )
        );
        matchers.put("messeinheit",
            Matchers.containsInAnyOrder(
                "id",
                "letzteAenderung",
                "beschreibung",
                "einheit",
                "eudfMesseinheitId",
                "umrechnungsFaktorEudf",
                "primary"
            )
        );
        matchers.put("messgroesse",
            Matchers.containsInAnyOrder(
                "id",
                "letzteAenderung",
                "beschreibung",
                "defaultFarbe",
                "eudfNuklidId",
                "idfNuklidKey",
                "istLeitnuklid",
                "kennungBvl",
                "messgroesse"
            )
        );
        matchers.put("messmethode",
            Matchers.containsInAnyOrder(
                "id",
                "letzteAenderung",
                "beschreibung",
                "messmethode"
            )
        );
        matchers.put("messstelle",
            Matchers.containsInAnyOrder(
                "id",
                "letzteAenderung",
                "amtskennung",
                "beschreibung",
                "messStelle",
                "mstTyp",
                "netzbetreiberId"
            )
        );
        matchers.put("netzbetreiber",
            Matchers.containsInAnyOrder(
                "id",
                "letzteAenderung",
                "aktiv",
                "idfNetzbetreiber",
                "isBmn",
                "mailverteiler",
                "netzbetreiber"
            )
        );
        matchers.put("pflichtmessgroesse",
            Matchers.containsInAnyOrder(
                "id",
                "letzteAenderung",
                "messgroesseId",
                "datenbasisId",
                "messMethodeId",
                "umwId"
            )
        );
        matchers.put("probenart",
            Matchers.containsInAnyOrder(
                "id",
                "letzteAenderung",
                "beschreibung",
                "probenart",
                "probenartEudfId"
            )
        );
        matchers.put("probenzusatz",
            Matchers.containsInAnyOrder(
                "id",
                "letzteAenderung",
                "beschreibung",
                "eudfKeyword",
                "zusatzwert",
                "messEinheitId"
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
                "nutsCode",
                "unscharf",
                "netzbetreiberId",
                "staatId",
                "verwaltungseinheitId",
                "otyp",
                "koordinatenartId",
                "geom"
            )
        );
        matchers.put("koordinatenart",
            Matchers.containsInAnyOrder(
                "id",
                "letzteAenderung",
                "idfGeoKey",
                "koordinatenart"
            )
        );
        matchers.put("staat",
            Matchers.containsInAnyOrder(
                "id",
                "letzteAenderung",
                "eu",
                "hklId",
                "koordXExtern",
                "koordYExtern",
                "staat",
                "staatIso",
                "kdaId"
            )
        );
        matchers.put("umwelt",
            Matchers.containsInAnyOrder(
                "id",
                "letzteAenderung",
                "beschreibung",
                "umweltBereich",
                "mehId",
                "secMehId"
            )
        );
        matchers.put("verwaltungseinheit",
            Matchers.containsInAnyOrder(
                "id",
                "bezeichnung",
                "bundesland",
                "isBundesland",
                "isGemeinde",
                "isLandkreis",
                "isRegbezirk",
                "kreis",
                "latitude",
                "longitude",
                "nuts",
                "plz",
                "regbezirk"
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
        getAll(type, "rest/" + type);
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
        System.out.print(".");
        Protocol prot = new Protocol();
        prot.setName(type + "Service");
        prot.setType("get by Id");
        prot.setPassed(false);
        protocol.add(prot);

        /* Create a client*/
        WebTarget target =
            client.target(baseUrl + "rest/" + type + "/" + id);
        prot.addInfo(type + "Id", id);
        /* Request an object by id*/
        Response response = target.request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .get();
        JsonObject content = BaseTest.parseResponse(response, prot);
        /* Verify the response*/
        Assert.assertTrue("Unsuccessful response object:\n" + content,
            content.getBoolean("success"));
        prot.addInfo("success", content.getBoolean("success"));
        Assert.assertEquals("200", content.getString("message"));
        prot.addInfo("message", content.getString("message"));
        MatcherAssert.assertThat(content.getJsonObject("data").keySet(),
            matchers.get(type));
        prot.addInfo("object", "equals");
    }
}
