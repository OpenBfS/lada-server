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
import java.lang.reflect.ParameterizedType;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
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
import jakarta.json.JsonValue;
import jakarta.persistence.Convert;
import jakarta.persistence.Table;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
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
import de.intevation.lada.model.lada.Sample_;
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

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
        DateTimeFormatter.ofPattern(JSONBConfig.DATE_FORMAT);

    /**
     * Timestamp attributes.
     */
    protected List<String> timestampAttributes = new ArrayList<String>();

    /**
     * Geometry attributes.
     */
    protected List<String> geomPointAttributes = new ArrayList<String>();

    /**
     * Basis for building requests for interface tests.
     */
    protected WebTarget target;

    /**
     * Initialize the tests.
     * @param c Client instance used for issueing requests.
     * @param bUrl The server url used for the request.
     */
    public void init(WebTarget t) {
        this.target = t;
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
        Scanner scanner = new Scanner(stream, StandardCharsets.UTF_8);
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
                    String rawValue = (String) table.getValue(row, columnName);
                    if (rawValue == null) {
                        continue;
                    }

                    // Get entity attribute type
                    Class<?> type =
                        column.getWriteMethod().getParameterTypes()[0];
                    Object value;

                    // Apply JPA conversion, if any
                    Convert convert = null;
                    Class<?> declaringClass = clazz;
                    do {
                        try {
                            convert = declaringClass.getDeclaredField(key)
                                .getAnnotation(Convert.class);
                            break;
                        } catch (NoSuchFieldException e) {
                            declaringClass = declaringClass.getSuperclass();
                        }
                    } while (declaringClass != null);
                    if (convert != null) {
                        Class<?> converter = convert.converter();
                        // Get database attribute type from AttributeConverter
                        Class<?> attributeType = (Class<?>) (
                            (ParameterizedType) converter
                            .getGenericInterfaces()[0])
                            .getActualTypeArguments()[1];
                        value = converter
                            .getMethod(
                                "convertToEntityAttribute", attributeType)
                            .invoke(
                                converter.getDeclaredConstructor()
                                    .newInstance(),
                                parseXMLAttr(rawValue, attributeType));
                    } else {
                        value = parseXMLAttr(rawValue, type);
                    }

                    if (type.isAssignableFrom(Integer.class)) {
                        builder.add(key, (Integer) value);
                    } else if (type.isAssignableFrom(Double.class)
                        || type.isAssignableFrom(Float.class)
                    ) {
                        builder.add(key, (Double) value);
                    } else if (type.isAssignableFrom(Boolean.class)) {
                        builder.add(key, (Boolean) value);
                    } else {
                        builder.add(key, value.toString());
                    }
                }
                arrayBuilder.add(builder);
            }
            return arrayBuilder.build();
        } catch (DataSetException
            | IntrospectionException
            | ReflectiveOperationException e
        ) {
            throw new RuntimeException(e);
        }
    }

    private Object parseXMLAttr(String value, Class<?> type) {
        if (type.isAssignableFrom(Integer.class)) {
            return Integer.parseInt(value);
        }
        if (type.isAssignableFrom(Double.class)
            || type.isAssignableFrom(Float.class)
        ) {
            return Double.parseDouble(value);
        }
        if (type.isAssignableFrom(Boolean.class)) {
            return Boolean.parseBoolean(value);
        }
        return value;
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

                String dateString = TIMESTAMP_FORMATTER
                    .withZone(ZoneId.of("UTC"))
                    .format(timestamp.toInstant());
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
        return get(parameter, JsonValue.class, expectedStatus);
    }

    /**
     * Base for all GET requests.
     * @param <T> Expected response entity type
     * @param parameter the url parameter used in the request.
     * @param entityType Expected response entity type
     * @return the JSON returned by the service.
     */
    public <T> T get(
        String parameter, Class<T> entityType
    ) {
        return get(parameter, entityType, Response.Status.OK);
    }

    /**
     * Base for all GET requests.
     * @param <T> Expected response entity type
     * @param parameter the url parameter used in the request.
     * @param entityType Expected response entity type
     * @return the JSON returned by the service.
     */
    public <T> T get(
        String parameter, GenericType<T> entityType
    ) {
        return get(parameter, entityType, Response.Status.OK);
    }

    /**
     * Base for all GET requests.
     * @param <T> Expected response entity type
     * @param parameter the url parameter used in the request.
     * @param expectedStatus Expected HTTP status code
     * @param entityType Expected response entity type
     * @return the JSON returned by the service.
     */
    public <T> T get(
        String parameter, Class<T> entityType, Response.Status expectedStatus
    ) {
        return get(parameter, new GenericType<T>(entityType), expectedStatus);
    }

    /**
     * Base for all GET requests.
     * @param <T> Expected response entity type
     * @param parameter the url parameter used in the request.
     * @param expectedStatus Expected HTTP status code
     * @param entityType Expected response entity type
     * @return the JSON returned by the service.
     */
    public <T> T get(
        String parameter,
        GenericType<T> entityType,
        Response.Status expectedStatus
    ) {
        URI uri = URI.create(parameter);
        WebTarget t = target.path(uri.getPath());
        String query = uri.getQuery();
        if (query != null) {
            for (String param: query.split("&")) {
                String[] paramParts = param.split("=");
                t = t.queryParam(paramParts[0], paramParts[1]);
            }
        }
        Response response = t.request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .accept(MediaType.APPLICATION_JSON)
            .get();
        return BaseTest.parseResponse(response, entityType, expectedStatus);
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
        /* Request a object by id*/
        Response response = target.path(parameter).request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .accept(MediaType.APPLICATION_JSON)
            .get();
        /* Verify the response*/
        JsonObject object = BaseTest.parseResponse(response).asJsonObject();
        for (Entry<String, JsonValue> entry : expected.entrySet()) {
            if (entry.getKey().equals("parentModified")
                || entry.getKey().equals(Sample_.TREE_MOD)
                || entry.getKey().equals(Sample_.LAST_MOD)) {
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
    public JsonObject create(String parameter, Object create) {
        return create(parameter, create, Locale.GERMAN, Response.Status.OK);
    }

    /**
     * Test the CREATE Service.
     * @param <T> Expected response entity type
     * @param parameter the parameters used in the request.
     * @param create the object to create, embedded in POST body.
     * @param entityType Expected response entity type
     * @return The resulting json object.
     *
     */
    public <T> T create(
        String parameter, Object create, Class<T> entityType
    ) {
        return create(
            parameter, create, Locale.GERMAN, Response.Status.OK, entityType);
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
        Object create,
        Locale acceptLanguage,
        Response.Status expectedStatus
    ) {
        return create(
            parameter,
            create,
            acceptLanguage,
            expectedStatus,
            JsonObject.class);
    }

    /**
     * Test the CREATE Service.
     * @param <T> Expected response entity type
     * @param parameter the parameters used in the request.
     * @param create the object to create, embedded in POST body.
     * @param acceptLanguage Acceptable language
     * @param expectedStatus Expected HTTP status code
     * @param entityType Expected response entity type
     * @return The resulting json object.
     */
    public <T> T create(
        String parameter,
        Object create,
        Locale acceptLanguage,
        Response.Status expectedStatus,
        Class<T> entityType
    ) {
        /* Send a post request containing a new object*/
        Response response = target.path(parameter).request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .accept(MediaType.APPLICATION_JSON)
            .acceptLanguage(acceptLanguage)
            .post(Entity.entity(create, MediaType.APPLICATION_JSON));
        return BaseTest.parseResponse(response, entityType, expectedStatus);
    }

    /**
     * Test an update service expecting success.
     * @param parameter the parameters used in the request.
     * @param updateAttribute the name of the attribute to update.
     * @param oldValue the value to replace.
     * @param newValue the new value to set.
     * @return The resulting json object.
     */
    public JsonValue update(
        String parameter,
        String updateAttribute,
        String oldValue,
        String newValue
    ) {
        return update(
            parameter,
            updateAttribute,
            Json.createValue(oldValue),
            Json.createValue(newValue),
            Response.Status.OK
        );
    }

    /**
     * Test an update service expecting success.
     * @param parameter the parameters used in the request.
     * @param updateAttribute the name of the attribute to update.
     * @param oldValue the value to replace.
     * @param newValue the new value to set.
     * @return The resulting json object.
     */
    public JsonValue update(
        String parameter,
        String updateAttribute,
        JsonValue oldValue,
        JsonValue newValue
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
    public JsonValue update(
        String parameter,
        String updateAttribute,
        JsonValue oldValue,
        JsonValue newValue,
        Response.Status expectedStatus
    ) {
        String updAttrPointer = "/" + updateAttribute;

        /* Request object corresponding to id in URL */
        Builder requestBuilder = target.path(parameter)
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .accept(MediaType.APPLICATION_JSON);
        JsonObject oldObject = BaseTest.parseResponse(
            requestBuilder.get(), JsonObject.class);

        BaseTest.assertContains(oldObject, updateAttribute);
        Assert.assertEquals(
            "Value in to be updated field '" + updateAttribute + "':",
            oldValue,
            oldObject.getValue(updAttrPointer));

        /* Value replacement */
        JsonObjectBuilder updateBuilder = Json.createObjectBuilder();
        oldObject.forEach((key, value) -> {
            if (key.equals(updateAttribute)) {
                if (newValue == null) {
                    updateBuilder.addNull(updateAttribute);
                } else {
                    updateBuilder.add(updateAttribute, newValue);
                }
            } else {
                updateBuilder.add(key, value);
            }
        });

        /* Send modified object via put request*/
        JsonValue updated = BaseTest.parseResponse(requestBuilder
            .put(Entity.entity(
                    updateBuilder.build(), MediaType.APPLICATION_JSON)),
            expectedStatus);

        /* Verify the response*/
        if (!Response.Status.OK.equals(expectedStatus)) {
            return updated;
        }

        JsonObject updatedObject = updated.asJsonObject();
        if (newValue == null) {
            Assert.assertTrue(
                "Expected '" + updateAttribute + "' to be null",
                updatedObject.isNull(updateAttribute));
        } else {
            Assert.assertEquals(newValue,
                updatedObject.getValue(updAttrPointer));
        }

        final String modTimeKey = Sample_.LAST_MOD;
        if (oldObject.containsKey(modTimeKey)) {
            var oldLastMod = ZonedDateTime.parse(
                oldObject.getString(modTimeKey), TIMESTAMP_FORMATTER);
            var updatedLastMod = ZonedDateTime.parse(
                updatedObject.getString(modTimeKey), TIMESTAMP_FORMATTER);
            Assert.assertTrue(
                "Object modification timestamp did not increase",
                updatedLastMod.isAfter(oldLastMod)
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
        /* Delete object with ID given in URL */
        Response response = target.path(parameter).request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .delete();
        BaseTest.parseResponse(response, expectedStatus);
        if (Response.Status.Family.SUCCESSFUL.equals(
                expectedStatus.getFamily())
        ) {
            // Ensure the resource has actually been deleted
            get(parameter, Response.Status.NOT_FOUND);
        }
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
        Response response = target.path(parameter).request()
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
     * Get the difference in days between the given timestamp and now.
     * @param to timestamp
     * @return Difference in days as long
     */
    protected long getDaysFromNow(String to) {
        ZonedDateTime toDate = ZonedDateTime.parse(to, TIMESTAMP_FORMATTER);
        return getDaysFromNow(toDate.toInstant());
    }

    /**
     * Get the difference in days between the given timestamp and now.
     * @param to timestamp
     * @return Difference in days as long
     */
    protected long getDaysFromNow(Date to) {
        return getDaysFromNow(to.toInstant());
    }

    /**
     * Get the difference in days between the given timestamp and now.
     * @param to timestamp
     * @return Difference in days as long
     */
    protected long getDaysFromNow(Instant to) {
        Instant fromDate = Instant.ofEpochMilli(System.currentTimeMillis())
            .truncatedTo(ChronoUnit.DAYS);
        return ChronoUnit.DAYS.between(fromDate, to);
    }
}
