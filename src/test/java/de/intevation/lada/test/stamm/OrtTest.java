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
import java.util.List;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Assert;

import de.intevation.lada.BaseTest;
import de.intevation.lada.Protocol;
import de.intevation.lada.test.ServiceTest;

/**
 * Test ort entities.
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class OrtTest extends ServiceTest {

    private JsonObject expectedById;
    private JsonObject create;
    private byte[] imgBytes;
    private byte[] mapBytes;
    private JsonObject createIncomplete;

    @Override
    public void init(
        Client c,
        URL baseUrl,
        List<Protocol> protocol
    ) {
        super.init(c, baseUrl, protocol);
        // Attributes with timestamps
        timestampAttributes = Arrays.asList(new String[]{
            "letzteAenderung"
        });
        // Attributes with point geometries
        geomPointAttributes = Arrays.asList(new String[]{
                "geom"
        });

        // Prepare expected object
        JsonObject content = readJsonResource("/datasets/dbUnit_ort.json");
        JsonObject erzeuger =
            content.getJsonArray("master.site").getJsonObject(0);
        JsonObjectBuilder builder = convertObject(erzeuger);
        expectedById = builder.build();
        Assert.assertNotNull(expectedById);

        // Load object to test POST request
        create = readJsonResource("/datasets/ort.json");
        createIncomplete = readJsonResource("/datasets/ort_incomplete.json");
        Assert.assertNotNull(create);

        //Create dummy image bytes
        imgBytes = "siteImage".getBytes();
        mapBytes = "siteMap".getBytes();
    }

    /**
     * Test the site image upload function.
     *
     * Passes if:
     *   - An image can be uploaded using bytes
     *   - The bytes received via the get interface equal the uploaded bytes
     * @param bytes Bytes to use for tests
     * @param parameter Url parameter
     */
    private void testUploadImage(byte[] bytes, String parameter) {
        Protocol prot = new Protocol();
        prot.setName("site image service");
        prot.setType("create");
        prot.setPassed(false);
        protocol.add(prot);

        WebTarget reqTarget = client.target(baseUrl + parameter);
        Builder reqBuilder = reqTarget.request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles);

        // Get empty image
        Response emptyResponse = reqBuilder.get();
        Assert.assertEquals(
            Status.NO_CONTENT.getStatusCode(), emptyResponse.getStatus());

        // Upload image
        Response postResponse = reqBuilder.post(Entity.entity(
                bytes, MediaType.APPLICATION_OCTET_STREAM_TYPE));
        Assert.assertEquals(
            Status.NO_CONTENT.getStatusCode(), postResponse.getStatus());

        // Get image
        reqBuilder = reqTarget.request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles);
        Response response = reqBuilder.get();
        Assert.assertEquals(
            Status.OK.getStatusCode(), response.getStatus());
        byte[] responseBytes = response.readEntity(byte[].class);
        Assert.assertArrayEquals(bytes, responseBytes);

        // Delete image
        Response deleteResponse = reqBuilder.delete();
        Assert.assertEquals(
            Status.NO_CONTENT.getStatusCode(), deleteResponse.getStatus());
        prot.setPassed(true);
    }

    /**
     * Execute the tests.
     */
    public final void execute() {
        get("ort", "rest/site");

        //Test search interface
        JsonObject result = get("site-search", "rest/site?search=Text");
        Assert.assertNotEquals(0, result.getJsonArray("data").size());

        getById("ort", "rest/site/1000", expectedById);
        int createdId = create("site", "rest/site", create)
            .getJsonObject("data").getInt("id");

        /*Test creation of site objects without an admin unit
          which should be completed by the server*/
        create("site-incomplete", "rest/site", createIncomplete);

        update("site", "rest/site/" + createdId,
            "longText", "Langer Text", "Längerer Text");
        //Test site images
        testUploadImage(imgBytes, "rest/site/" + createdId + "/img");
        testUploadImage(mapBytes, "rest/site/" + createdId + "/map");
        delete("site", "rest/site/" + createdId);
    }
}
