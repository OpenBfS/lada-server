/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Scanner;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;
import jakarta.persistence.Table;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.NoSuchColumnException;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.junit.Assert;

import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

import de.intevation.lada.BaseTest;
import de.intevation.lada.model.NamingStrategy;
import de.intevation.lada.test.land.ProbeTest;
import de.intevation.lada.util.rest.JSONBConfig;

/**
 * Class for Lada service tests.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class ServiceTest {

    private static final String LAT_KEY = "latitude";
    private static final String LONG_KEY = "longitude";

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
     * @param c Client instance used for issueing requests.
     * @param bUrl The server url used for the request.
     */
    public void init(Client c, URL bUrl) {
        this.client = c;
        this.baseUrl = bUrl;
    }

    /**
     * Filter the given JsonArray for an object with the given id.
     * @param array Array to filter
     * @param id Id to search for
     * @return JsonObject with the given id
     */
    protected JsonObject filterJsonArrayById(JsonArray array, int id) {
        return array
            .stream()
            .filter(val -> id == val.asJsonObject().getInt("id"))
            .findFirst().get()
            .asJsonObject();
    }

    /**
     * Read txt resource and return as string.
     * @param resource Resource to read
     * @return Resource as string
     */
    protected String readTxtResource(String resource) {
        InputStream stream =
            ProbeTest.class.getResourceAsStream(resource);
        Scanner scanner = new Scanner(stream, "UTF-8");
        scanner.useDelimiter("\\A");
        String raw = scanner.next();
        scanner.close();
        return raw;
    }

    /**
     * Read the given xml resource and return as JSON.
     * @param resource Name of resource with DbUnit XML dataset
     * @param clazz Model class for which data are extracted from resource
     * @return Array of objcets from the given resource corresponding to
     * clazz as JSON
     * @throws RuntimeException if resource cannot be read as DbUnit dataset
     * or bean introspection of clazz fails
     */
    protected JsonArray readXmlResource(String resource, Class<?> clazz) {
        try {
            IDataSet xml = new FlatXmlDataSetBuilder()
                .setColumnSensing(true)
                .build(getClass().getClassLoader()
                    .getResourceAsStream(resource));

            BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
            String tablename = clazz.getAnnotation(Table.class).schema() + "."
                + NamingStrategy.camelToSnake(
                    beanInfo.getBeanDescriptor().getName());
            ITable table = xml.getTable(tablename);
            ITableMetaData datasetMetadata = xml.getTableMetaData(tablename);

            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            for (int row = 0; row < table.getRowCount(); row++) {
                JsonObjectBuilder builder = Json.createObjectBuilder();
                for (
                    PropertyDescriptor column: beanInfo.getPropertyDescriptors()
                ) {
                    //Check if column is present in dataset
                    String key = column.getName();
                    String columnName = NamingStrategy.camelToSnake(key);
                    try {
                        datasetMetadata.getColumnIndex(columnName);
                    } catch (NoSuchColumnException nsce) {
                        continue;
                    }
                    Object value = table.getValue(row, columnName);
                    if (value == null) {
                        continue;
                    }
                    Class<?> type =
                        column.getWriteMethod().getParameterTypes()[0];
                    if (type.isAssignableFrom(Integer.class)) {
                        builder.add(key, Integer.parseInt((String) value));
                    } else if (type.isAssignableFrom(Double.class)
                        || type.isAssignableFrom(Float.class)
                    ) {
                        builder.add(key, Double.parseDouble((String) value));
                    } else if (type.isAssignableFrom(Boolean.class)) {
                        builder.add(key, Boolean.parseBoolean((String) value));
                    } else {
                        builder.add(key, (String) value);
                    }
                }
                arrayBuilder.add(builder);
            }
            return arrayBuilder.build();
        } catch (DataSetException | IntrospectionException e) {
            throw new RuntimeException(e);
        }
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

            String key = entry.getKey();
            if (timestampAttributes.contains(key)) {
                Timestamp timestamp = Timestamp.valueOf(
                    entry.getValue().toString().replaceAll("\"", ""));
                DateTimeFormatter formatter = DateTimeFormatter
                    .ofPattern(JSONBConfig.DATE_FORMAT)
                    .withZone(ZoneId.of("UTC"));

                String dateString = formatter.format(timestamp.toInstant());
                builder.add(key, dateString);
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
     * @param parameter the url parameter used in the request.
     * @return the JSON returned by the service
     */
    public JsonValue get(String parameter) {
        return get(parameter, Response.Status.OK);
    }

    /**
     * Base for all GET requests.
     * @param parameter the url parameter used in the request.
     * @param expectedStatus Expected HTTP status code
     * @return the JSON returned by the service.
     */
    public JsonValue get(
        String parameter, Response.Status expectedStatus
    ) {
        WebTarget target = client.target(baseUrl + parameter);
        Response response = target.request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .accept(MediaType.APPLICATION_JSON)
            .get();
        return BaseTest.parseResponse(response, expectedStatus);
    }

    /**
     * Test the GET Service by requesting a single object by id.
     * @param parameter the parameters used in the request.
     * @param expected the expected json result.
     * @return The resulting json object.
     */
    public JsonObject getById(
        String parameter,
        JsonObject expected
    ) {
        WebTarget target = client.target(baseUrl + parameter);
        /* Request a object by id*/
        Response response = target.request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .accept(MediaType.APPLICATION_JSON)
            .get();
        /* Verify the response*/
        JsonObject object = BaseTest.parseResponse(response).asJsonObject();
        for (Entry<String, JsonValue> entry : expected.entrySet()) {
            if (entry.getKey().equals("parentModified")
                || entry.getKey().equals("treeMod")
                || entry.getKey().equals("lastMod")) {
                continue;
            }
            String key = entry.getKey();
            Assert.assertEquals(
                String.format("%s:", key),
                entry.getValue(),
                object.get(key));
        }
        return object;
    }

    /**
     * Test the CREATE Service.
     * @param parameter the parameters used in the request.
     * @param create the object to create, embedded in POST body.
     * @return The resulting json object.
     *
     */
    public JsonObject create(String parameter, JsonStructure create) {
        return create(parameter, create, Locale.GERMAN, Response.Status.OK);
    }

    /**
     * Test the CREATE Service.
     * @param parameter the parameters used in the request.
     * @param create the object to create, embedded in POST body.
     * @param acceptLanguage Acceptable language
     * @param expectedStatus Expected HTTP status code
     * @return The resulting json object.
     *
     */
    public JsonObject create(
        String parameter,
        JsonStructure create,
        Locale acceptLanguage,
        Response.Status expectedStatus
    ) {
        WebTarget target = client.target(baseUrl + parameter);
        /* Send a post request containing a new object*/
        Response response = target.request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .accept(MediaType.APPLICATION_JSON)
            .acceptLanguage(acceptLanguage)
            .post(Entity.entity(create.toString(), MediaType.APPLICATION_JSON));
        return BaseTest.parseResponse(response, expectedStatus).asJsonObject();
    }

    /**
     * Test an update service expecting success.
     * @param parameter the parameters used in the request.
     * @param updateAttribute the name of the attribute to update.
     * @param oldValue the value to replace.
     * @param newValue the new value to set.
     * @return The resulting json object.
     */
    public JsonObject update(
        String parameter,
        String updateAttribute,
        String oldValue,
        String newValue
    ) {
        return update(
            parameter,
            updateAttribute,
            oldValue,
            newValue,
            Response.Status.OK
        );
    }

    /**
     * Test an update service.
     * @param parameter the parameters used in the request.
     * @param updateAttribute the name of the attribute to update.
     * @param oldValue the value to replace.
     * @param newValue the new value to set.
     * @param expectedStatus Expected HTTP status code
     * @return The resulting json object.
     */
    public JsonObject update(
        String parameter,
        String updateAttribute,
        String oldValue,
        String newValue,
        Response.Status expectedStatus
    ) {
        /* Request object corresponding to id in URL */
        WebTarget target = client.target(baseUrl + parameter);
        Response response = target.request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .get();
        JsonObject oldObject = BaseTest.parseResponse(
            response).asJsonObject();

        BaseTest.assertContains(oldObject, updateAttribute);
        Assert.assertEquals(
            "Value in to be updated field '" + updateAttribute + "':",
            oldValue,
            oldObject.getString(updateAttribute));

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

        /* Send modified object via put request*/
        WebTarget putTarget = client.target(baseUrl + parameter);
        Response updated = putTarget.request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .accept(MediaType.APPLICATION_JSON)
            .put(Entity.entity(updatedEntity, MediaType.APPLICATION_JSON));

        /* Verify the response*/
        JsonObject updatedObject = BaseTest.parseResponse(
            updated, expectedStatus).asJsonObject();
        if (!Response.Status.OK.equals(expectedStatus)) {
            return updatedObject;
        }

        Assert.assertEquals(newValue,
            updatedObject.getString(updateAttribute));

        final String modTimeKey = "letzteAenderung";
        if (oldObject.containsKey(modTimeKey)) {
            Assert.assertTrue(
                "Object modification timestamp did not increase",
                Long.parseLong(updatedObject.getString(modTimeKey))
                > Long.parseLong(oldObject.getString(modTimeKey))
            );
        }

        return updatedObject;
    }

    /**
     * Test the DELETE Service.
     * @param parameter the parameters used in the request.
     */
    public void delete(String parameter) {
        delete(parameter, Response.Status.NO_CONTENT);
    }

    /**
     * Test the DELETE Service.
     * @param parameter the parameters used in the request.
     * @param expectedStatus Expected HTTP status code
     */
    public void delete(String parameter, Response.Status expectedStatus) {
        WebTarget target =
            client.target(baseUrl + parameter);
        /* Delete object with ID given in URL */
        Response response = target.request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .delete();
        BaseTest.parseResponse(response, expectedStatus);
    }

    /**
     * Test AuditTrailService.
     *
     * @param parameter the parameters used in the request.
     * @param updateFieldKey Key of field expected to be changed.
     * @param newValue Value of field expected to be changed.
     */
    protected void getAuditTrail(
        String parameter,
        String updateFieldKey,
        String newValue
    ) {
        WebTarget target =
            client.target(baseUrl + parameter);
        Response response = target.request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .get();
        JsonObject data = BaseTest.parseResponse(response).asJsonObject();

        final String auditKey = "audit";
        BaseTest.assertContains(data, auditKey);
        JsonArray audit = data.getJsonArray(auditKey);
        Assert.assertTrue(
            "Missing audit entry for field '" + updateFieldKey
            + "' changed to value '" + newValue + "'",
            hasAuditedUpdate(audit, updateFieldKey, newValue));
    }

    private boolean hasAuditedUpdate(
        JsonArray audit,
        String updateFieldKey,
        String newValue
    ) {
        final String changedFieldsKey = "changedFields",
            actionKey = "action",
            snakeFieldKey = NamingStrategy.camelToSnake(updateFieldKey);
        for (JsonValue v : audit) {
            JsonObject o = (JsonObject) v;
            BaseTest.assertContains(o, actionKey);
            BaseTest.assertContains(o, changedFieldsKey);
            JsonObject changedFields = o.getJsonObject(changedFieldsKey);
            if ("U".equals(o.getString(actionKey))
                && changedFields.containsKey(snakeFieldKey)
                && newValue.equals(
                    changedFields.getString(snakeFieldKey))
            ) {
                return true;
            }
        }
        return false;
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
        LocalDateTime toDate = LocalDateTime.parse(
            to, DateTimeFormatter.ofPattern(JSONBConfig.DATE_FORMAT));
        return ChronoUnit.DAYS.between(fromDate, toDate);
    }
}
