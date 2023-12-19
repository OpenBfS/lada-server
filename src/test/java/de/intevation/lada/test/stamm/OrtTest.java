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
import java.util.Base64;

import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.junit.Assert;

import de.intevation.lada.BaseTest;
import de.intevation.lada.model.master.Site;
import de.intevation.lada.test.ServiceTest;

/**
 * Test ort entities.
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class OrtTest extends ServiceTest {

    private JsonObject expectedById;
    private JsonObject create;
    private String testImage;
    private JsonObject createIncomplete;

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
        // Attributes with point geometries
        geomPointAttributes = Arrays.asList(new String[]{
                "geom"
        });

        // Prepare expected object
        JsonObject erzeuger =
            readXmlResource("datasets/dbUnit_master.xml", Site.class)
            .getJsonObject(0);
        JsonObjectBuilder builder = convertObject(erzeuger);
        expectedById = builder.build();
        Assert.assertNotNull(expectedById);

        // Load object to test POST request
        create = readJsonResource("/datasets/ort.json");
        createIncomplete = readJsonResource("/datasets/ort_incomplete.json");
        Assert.assertNotNull(create);

        testImage = readTxtResource("/datasets/testImage.txt");
    }

    /**
     * Test the site image upload function.
     *
     * Passes if:
     *   - An image can be uploaded using bytes
     *   - The bytes received via the get interface equal the uploaded bytes
     * @param imageDataUrl Image as dataurl to use for tests
     * @param parameter Url parameter
     */
    private void testUploadImage(String imageDataUrl, String parameter) {
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
                imageDataUrl, MediaType.TEXT_PLAIN));
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

        //Convert dataurl to bytes and use as expected results
        String encodingPrefix = "base64,";
        int contentStartIndex = imageDataUrl.indexOf(encodingPrefix)
                + encodingPrefix.length();
        byte[] bytes = Base64.getDecoder().decode(
            imageDataUrl.substring(contentStartIndex));

        Assert.assertArrayEquals(bytes, responseBytes);

        // Delete image
        Response deleteResponse = reqBuilder.delete();
        Assert.assertEquals(
            Status.NO_CONTENT.getStatusCode(), deleteResponse.getStatus());
    }

    /**
     * Execute the tests.
     */
    public final void execute() {
        get("rest/site");

        //Test search interface
        JsonObject result = get("rest/site?search=Text");
        Assert.assertNotEquals(0, result.getJsonArray("data").size());

        getById("rest/site/1000", expectedById);
        int createdId = create("rest/site", create)
            .getJsonObject("data").getInt("id");

        /*Test creation of site objects without an admin unit
          which should be completed by the server*/
        create("rest/site", createIncomplete);

        update("rest/site/" + createdId,
            "longText", "Langer Text", "Längerer Text");
        //Test site images
        testUploadImage(testImage, "rest/site/" + createdId + "/img");
        testUploadImage(testImage, "rest/site/" + createdId + "/map");
        delete("rest/site/" + createdId);
    }
}
