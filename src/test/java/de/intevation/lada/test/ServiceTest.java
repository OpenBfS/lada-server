/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test;

import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Scanner;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.text.WordUtils;
import org.junit.Assert;

import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

import de.intevation.lada.BaseTest;
import de.intevation.lada.Protocol;
import de.intevation.lada.test.land.ProbeTest;

/**
 * Class for Lada service tests.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class ServiceTest {

    private static final String LAT_KEY = "latitude";
    private static final String LONG_KEY = "longitude";

    /**
     * Test protocol for output of results.
     */
    protected List<Protocol> protocol;

    /**
     * Timestamp attributes.
     */
    protected List<String> timestampAttributes = new ArrayList<String>();

    /**
     * Geometry attributes.
     */
    protected List<String> geomPointAttributes = new ArrayList<String>();

    /**
     * The client to be used for interface tests.
     */
    protected Client client;

    /**
     * Base url of the server.
     */
    protected URL baseUrl;

    /**
     * Initialize the tests.
     * @param bUrl The server url used for the request.
     * @param p The resulting test protocol
     */
    public void init(Client c, URL bUrl, List<Protocol> p) {
        this.client = c;
        this.baseUrl = bUrl;
        this.protocol = p;
    }

    /**
     * @return The test protocol
     */
    public List<Protocol> getProtocol() {
        return protocol;
    }

    /**
     * Load JSON resource file.
     * @param resource the resource location
     * @return Object containing the resource.
     */
    protected JsonObject readJsonResource(String resource) {
        InputStream stream =
            ProbeTest.class.getResourceAsStream(resource);
        Scanner scanner = new Scanner(stream, "UTF-8");
        scanner.useDelimiter("\\A");
        String raw = scanner.next();
        scanner.close();
        JsonReader reader = Json.createReader(new StringReader(raw));
        JsonObject content = reader.readObject();
        reader.close();
        return content;
    }

    /**
     * Load JSON resource file as Array.
     * @param resource the resource location
     * @return Array containing the resource.
     */
    protected JsonArray readJsonArrayResource(String resource) {
        InputStream stream =
            ProbeTest.class.getResourceAsStream(resource);
        Scanner scanner = new Scanner(stream, "UTF-8");
        scanner.useDelimiter("\\A");
        String raw = scanner.next();
        scanner.close();
        JsonReader reader = Json.createReader(new StringReader(raw));
        JsonArray content = reader.readArray();
        reader.close();
        return content;
    }

    /**
     * Convert geometries and timestamps.
     * @param object The current version.
     * @param exclusions Keys in object to be excluded in conversion
     * @return Builder with the new version.
     */
    protected JsonObjectBuilder convertObject(
        JsonObject object,
        String... exclusions
    ) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        for (Entry<String, JsonValue> entry : object.entrySet()) {
            if (Arrays.asList(exclusions).contains(entry.getKey())) {
                continue;
            }
            String key = WordUtils.capitalize(
                entry.getKey(), new char[]{'_'}).replaceAll("_", "");
            key = key.replaceFirst(
                key.substring(0, 1), key.substring(0, 1).toLowerCase());
            if (timestampAttributes.contains(key)) {
                Timestamp timestamp = Timestamp.valueOf(
                    entry.getValue().toString().replaceAll("\"", ""));
                builder.add(key, String.valueOf(timestamp.getTime()));
            } else if (geomPointAttributes.contains(key)) {
                // Convert EWKT to latitude and longitude
                String wkt = entry.getValue().toString().split(";")[1];
                try {
                    Geometry geom = new WKTReader().read(wkt);
                    if (!(geom instanceof Point)) {
                        throw new IllegalArgumentException(
                            "WKT does not represent a point");
                    }
                    Point point = (Point) geom;
                    builder.add(LONG_KEY, point.getX());
                    builder.add(LAT_KEY, point.getY());
                } catch (ParseException | IllegalArgumentException e) {
                    Protocol prot = new Protocol();
                    prot.addInfo("exception", e.getMessage());
                    protocol.add(prot);
                    Assert.fail("Exception while parsing WKT '"
                        + wkt + "':\n"
                        + e.getMessage());
                }
            } else {
                builder.add(key, entry.getValue());
            }
        }
        return builder;
    }

    /**
     * Base for all GET requests expecting success.
     * @param name of the entity to request
     * @param parameter the url parameter used in the request.
     * @return the json object returned by the serive.
     */
    public JsonObject get(String name, String parameter) {
        return get(name, parameter, Response.Status.OK);
    }

    /**
     * Base for all GET requests.
     * @param name of the entity to request
     * @param parameter the url parameter used in the request.
     * @param expectedStatus Expected HTTP status code
     * @return the json object returned by the serive.
     */
    public JsonObject get(
        String name, String parameter, Response.Status expectedStatus
    ) {
        Protocol prot = new Protocol();
        prot.setName(name + " service");
        prot.setType("get");
        prot.setPassed(false);
        protocol.add(prot);

        WebTarget target = client.target(baseUrl + parameter);
        Response response = target.request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .get();
        JsonObject content = BaseTest.parseResponse(
            response, prot, expectedStatus);

        if (Response.Status.OK.equals(expectedStatus)) {
            Assert.assertNotNull(content.getJsonArray("data"));
            prot.addInfo("objects", content.getJsonArray("data").size());
        }

        prot.setPassed(true);
        return content;
    }

    /**
     * Test the GET Service by requesting a single object by id.
     * @param name the name of the entity to request.
     * @param parameter the parameters used in the request.
     * @param expected the expected json result.
     * @return The resulting json object.
     */
    public JsonObject getById(
        String name,
        String parameter,
        JsonObject expected
    ) {
        Protocol prot = new Protocol();
        prot.setName(name + " service");
        prot.setType("get by Id");
        prot.setPassed(false);
        protocol.add(prot);

        WebTarget target = client.target(baseUrl + parameter);
        prot.addInfo("parameter", parameter);
        /* Request a object by id*/
        Response response = target.request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .get();
        JsonObject content = BaseTest.parseResponse(response, prot);
        /* Verify the response*/
        Assert.assertFalse(content.getJsonObject("data").isEmpty());
        JsonObject object = content.getJsonObject("data");
        for (Entry<String, JsonValue> entry : expected.entrySet()) {
            if (entry.getKey().equals("parentModified")
                || entry.getKey().equals("treeModified")
                || entry.getKey().equals("letzteAenderung")) {
                continue;
            }
            String key = entry.getKey();
            Assert.assertEquals(
                String.format("%s:", key),
                entry.getValue(),
                object.get(key));
        }
        prot.addInfo("object", "equals");
        prot.setPassed(true);
        return content;
    }

    /**
     * Test the CREATE Service.
     * @param name the name of the entity to request.
     * @param parameter the parameters used in the request.
     * @param create the object to create, embedded in POST body.
     * @return The resulting json object.
     *
     */
    public JsonObject create(String name, String parameter, JsonObject create) {
        Protocol prot = new Protocol();
        prot.setName(name + " service");
        prot.setType("create");
        prot.setPassed(false);
        protocol.add(prot);

        WebTarget target = client.target(baseUrl + parameter);
        /* Send a post request containing a new object*/
        Response response = target.request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .post(Entity.entity(create.toString(), MediaType.APPLICATION_JSON));
        JsonObject content = BaseTest.parseResponse(response, prot);

        prot.setPassed(true);
        return content;
    }

    /**
     * Test service using a list of input objects.
     * @param name the name of the entity to request.
     * @param parameter the parameters used in the request.
     * @param payload the objects embedded in POST body.
     * @return The resulting json object.
     *
     */
    public JsonObject bulkOperation(
        String name, String parameter, JsonArray payload
    ) {
        final String type = "bulk";
        System.out.print(".");
        Protocol prot = new Protocol();
        prot.setName(name + " service");
        prot.setType(type);
        prot.setPassed(false);
        protocol.add(prot);

        WebTarget target = client.target(baseUrl + parameter);
        /* Send a post request containing a new object*/
        Response response = target.request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .post(Entity.entity(
                    payload.toString(), MediaType.APPLICATION_JSON));
        JsonObject content = BaseTest.parseResponse(response, prot);
        //Check each result
        content.getJsonArray("data").forEach(object -> {
            JsonObject responseObj = (JsonObject) object;
            Protocol objectProt = new Protocol();
            prot.setName(name + " service");
            prot.setType(type);
            Assert.assertTrue(
                "Unsuccessful response list element:\n" + responseObj,
                responseObj.getBoolean("success"));
            Assert.assertEquals("200", responseObj.getString("message"));
            objectProt.setPassed(true);
            protocol.add(objectProt);
        });
        prot.setPassed(true);
        return content;
    }

    /**
     * Test an update service.
     * @param name the name of the entity to request.
     * @param parameter the parameters used in the request.
     * @param updateAttribute the name of the attribute to update.
     * @param oldValue the value to replace.
     * @param newValue the new value to set.
     * @return The resulting json object.
     */
    public JsonObject update(
        String name,
        String parameter,
        String updateAttribute,
        String oldValue,
        String newValue
    ) {
        Protocol prot = new Protocol();
        prot.setName(name + " service");
        prot.setType("update");
        prot.setPassed(false);
        protocol.add(prot);

        /* Request object corresponding to id in URL */
        final String objKey = "data";
        WebTarget target = client.target(baseUrl + parameter);
        Response response = target.request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .get();
        JsonObject oldObject = BaseTest.parseResponse(
            response, prot).getJsonObject(objKey);

        /* Value replacement */
        JsonObjectBuilder updateBuilder = Json.createObjectBuilder();
        oldObject.forEach((key, value) -> {
            if (key.equals(updateAttribute)) {
                updateBuilder.add(updateAttribute, newValue);
            } else {
                updateBuilder.add(key, value);
            }
        });
        String updatedEntity = updateBuilder.build().toString();
        prot.addInfo("updated datafield", updateAttribute);
        prot.addInfo("updated value", oldValue);
        prot.addInfo("updated to", newValue);

        /* Send modified object via put request*/
        WebTarget putTarget = client.target(baseUrl + parameter);
        Response updated = putTarget.request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .put(Entity.entity(updatedEntity, MediaType.APPLICATION_JSON));

        /* Verify the response*/
        JsonObject updatedObject = BaseTest.parseResponse(updated, prot);
        Assert.assertEquals(newValue,
            updatedObject.getJsonObject("data").getString(updateAttribute));

        final String modTimeKey = "letzteAenderung";
        if (oldObject.containsKey(modTimeKey)) {
            Assert.assertTrue(
                "Object modification timestamp did not increase",
                Long.parseLong(
                    updatedObject.getJsonObject(objKey).getString(modTimeKey))
                > Long.parseLong(oldObject.getString(modTimeKey))
            );
        }

        prot.setPassed(true);
        return updatedObject;
    }

    /**
     * Test the DELETE Service.
     * @param name the name of the entity to delete.
     * @param parameter the parameters used in the request.
     * @return The resulting json object.
     */
    public JsonObject delete(String name, String parameter) {
        Protocol prot = new Protocol();
        prot.setName(name + " service");
        prot.setType("delete");
        prot.setPassed(false);
        protocol.add(prot);

        WebTarget target =
            client.target(baseUrl + parameter);
        prot.addInfo("parameter", parameter);
        /* Delete object with ID given in URL */
        Response response = target.request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .delete();
        JsonObject content = BaseTest.parseResponse(response, prot);

        prot.setPassed(true);
        return content;
    }

    /**
     * Get the difference in days between the given timestamps.
     * @param to Date as unix timestamp
     * @return Difference in days as long
     */
    protected long getDaysFromNow(String to) {
        LocalDateTime fromDate = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(System.currentTimeMillis()),
            ZoneOffset.UTC)
            .truncatedTo(ChronoUnit.DAYS);
        LocalDateTime toDate = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(Long.valueOf(to)),
            ZoneOffset.UTC)
            .truncatedTo(ChronoUnit.DAYS);
        return ChronoUnit.DAYS.between(fromDate, toDate);
    }
}
