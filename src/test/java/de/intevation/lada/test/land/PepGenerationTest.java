/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.land;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;

import org.junit.Assert;

import de.intevation.lada.BaseTest;
import de.intevation.lada.model.lada.Sample_;
import de.intevation.lada.test.ServiceTest;
import de.intevation.lada.util.data.StatusCodes;


/**
 * Class containing methods used for testing the generation of probe records
 * from a messprogramm.
 */
public class PepGenerationTest extends ServiceTest {

    private static final int ID1000 = 1000;
    private static final int ID1001 = 1001;
    private static final int ID1002 = 1002;
    private static final int ID1003 = 1003;
    private static final int ID1004 = 1004;
    private static final int ID1005 = 1005;
    private static final int ID1006 = 1006;
    private static final int ID1007 = 1007;
    private static final int ID1008 = 1008;
    private static final int ID1009 = 1009;
    private static final int ID1012 = 1012;
    private static final int ID1013 = 1013;
    private static final int ID1014 = 1014;
    private static final int ID1015 = 1015;
    private static final int ID1016 = 1016;
    private static final int ID1017 = 1017;
    private static final int ID1018 = 1018;
    private static final int ID1019 = 1019;
    private static final int ID1100 = 1100;
    private static final int ID1103 = 1103;

    private static final int C4 = 4;
    private static final int C5 = 5;
    private static final int C7 = 7;
    private static final int C8 = 8;
    private static final int C9 = 9;
    private static final int C11 = 11;
    private static final int C12 = 12;
    private static final int C13 = 13;
    private static final int C14 = 14;
    private static final int C15 = 15;
    private static final int C16 = 16;
    private static final int C18 = 18;
    private static final int C26 = 26;
    private static final int C45 = 45;
    private static final int C53 = 53;
    private static final int C61 = 61;
    private static final int C368 = 368;

    private static final String TS1 = "2021-04-01T00:00:00.000Z";
    private static final String TS2 = "2020-01-29T00:00:00.000Z";
    private static final String TS3 = "2020-02-01T00:00:00.000Z";
    private static final String TS4 = "2020-02-12T00:00:00.000Z";
    private static final String TS5 = "2020-06-01T00:00:00.000Z";
    private static final String TS6 = "2025-01-01T00:00:00.000Z";
    private static final String TS7 = "2030-01-01T00:00:00.000Z";
    private static final String TS8 = "2020-01-01T00:00:00.000Z";
    private static final String TS9 = "2020-02-14T00:00:00.000Z";
    private static final String TS10 = "2030-03-01T00:00:00.000Z";
    private static final String TS11 = "2021-02-02T00:00:00.000Z";
    private static final String TS12 = "2020-02-29T00:00:00.000Z";
    private static final String TS13 = "2021-03-01T00:00:00.000Z";
    private static final String TS14 = "2016-02-29T00:00:00.000Z";
    private static final String TS15 = "2016-08-29T00:00:00.000Z";
    private static final String TS16 = "2016-05-30T00:00:00.000Z";
    private static final String TS17 = "2020-03-29T00:00:00.000Z";
    private static final String TS18 = "2020-02-24T00:00:00.000Z";
    private static final String TS19 = "2021-02-27T00:00:00.000Z";
    private static final String TS20 = "2021-03-06T00:00:00.000Z";
    private static final String TS21 = "2020-02-28T00:00:00.000Z";

    /**
     * Current expected tag serial number.
     */
    int expectedTagSerNo = 0;

    /**
     * Execute all available tests.
     */
    public void execute() {
        //Test generation in specific intervals
        testDailyGeneration();
        testWeeklyGeneration();
        test2WeeklyGeneration();
        test4WeeklyGeneration();
        testMonthlyGeneration();
        testQuarterlyGeneration();
        testHalfYearlyGeneration();
        testYearlyGeneration();

        //Test generation during leap years with interval offset
        testYearlyGenerationInLeapYear();
        testHalfYearlyGenerationInLeapYear();
        testQuarterlyGenerationInLeapYear();
        testMonthlyGenerationInLeapYear();
        test4WeeklyGenerationInLeapYear();
        test2WeeklyGenerationInLeapYear();
        testWeeklyGenerationInLeapYear();
        testDailyGenerationInLeapYear();

        //Various tests
        testPartialIntevals();
        testGenerationFromIdList();
        testGenerationRejectUnauthorized();
        testZusatzwertgeneration();
    }

    /**
     * Test the generation of daily probe records.
     */
    private void testDailyGeneration() {
        int mpId = ID1007;
        List<Integer> idParam = new ArrayList<Integer>();
        idParam.add(mpId);

        JsonObject entity = generateFromMpIds(idParam, TS3, TS4);
        checkGeneratedProbeCount(C12, entity, mpId);
        checkGeneratedTag(entity);
    }

    /**
     * Test the generation of weekly probe records.
     */
    private void testWeeklyGeneration() {
        int mpId = ID1006;
        List<Integer> idParam = new ArrayList<Integer>();
        idParam.add(mpId);

        JsonObject entity = generateFromMpIds(idParam, TS3, TS5);
        checkGeneratedProbeCount(C18, entity, mpId);
        checkGeneratedTag(entity);
    }

    /**
     * Test the generation of two-weekly probe records.
     */
    private void test2WeeklyGeneration() {
        int mpId = ID1005;
        List<Integer> idParam = new ArrayList<Integer>();
        idParam.add(mpId);

        JsonObject entity = generateFromMpIds(idParam, TS3, TS5);
        checkGeneratedProbeCount(C9, entity, mpId);
        checkGeneratedTag(entity);
    }

    /**
     * Test the generation of four-weekly probe records.
     */
    private void test4WeeklyGeneration() {
        int mpId = ID1004;
        List<Integer> idParam = new ArrayList<Integer>();
        idParam.add(mpId);

        JsonObject entity = generateFromMpIds(idParam, TS3, TS5);
        checkGeneratedProbeCount(C5, entity, mpId);
        checkGeneratedTag(entity);
    }

    /**
     * Test the generation of monthly probe records.
     */
    private void testMonthlyGeneration() {
        int mpId = ID1003;
        List<Integer> id = new ArrayList<Integer>();
        id.add(mpId);

        //Generate 61 records for five years
        JsonObject entity = generateFromMpIds(id, TS6, TS7);
        checkGeneratedProbeCount(C61, entity, mpId);
        checkGeneratedTag(entity);
    }

    /**
     * Check the generation of quarterly probe records.
     */
    private void testQuarterlyGeneration() {
        int mpId = ID1002;
        List<Integer> id = new ArrayList<Integer>();
        id.add(mpId);

        JsonObject entity = generateFromMpIds(id, TS8, TS11);
        checkGeneratedProbeCount(C4, entity, mpId);
        checkGeneratedTag(entity);
    }

    /**
     * Test the generation of half-yearly probe records.
     */
    private void testHalfYearlyGeneration() {
        int mpId = ID1001;
        List<Integer> id = new ArrayList<Integer>();
        id.add(mpId);

        JsonObject entity = generateFromMpIds(id, TS8, TS11);
        checkGeneratedProbeCount(2, entity, mpId);
        checkGeneratedTag(entity);
    }

    /**
     * Test a simple yearly generation of probe records.
     */
    private void testYearlyGeneration() {
        int mpId = ID1000;
        List<Integer> id = new ArrayList<Integer>();
        id.add(mpId);

        JsonObject entity = generateFromMpIds(id, TS8, TS7);
        checkGeneratedProbeCount(C11, entity, mpId);
        checkGeneratedTag(entity);
    }

    /**
     * Test the generation of probe records starting on the 29th of February.
     * in a leap year
     */
    private void testYearlyGenerationInLeapYear() {
        Integer mpId = ID1100;
        List<Integer> idParam = new ArrayList<Integer>();
        idParam.add(mpId);

        JsonObject entity = generateFromMpIds(idParam, TS8, TS10);
        int expectedCount = C11;
        checkGeneratedProbeCount(expectedCount, entity, mpId);
        checkGeneratedTag(entity);

        //Check return data
        String startAttribute = "schedStartDate";
        Map<Integer, String> expectedValues = new HashMap<>();
        //Expected first record: 02/29/2020 @ 12:00am (UTC)
        expectedValues.put(0, TS12);
        //Expected second record: 03/01/2021 @ 12:00am (UTC)
        expectedValues.put(1, TS13);
        //Expected last record: 02/29/2030 @ 12:00am (UTC)
        expectedValues.put(expectedCount - 1, TS10);
        checkEntityAttributeValues(
            entity,
            mpId,
            startAttribute,
            expectedValues);

    }

    /**
     * Test the half yearly generation of probe records in leap years.
     */
    private void testHalfYearlyGenerationInLeapYear() {
        Integer mpId = ID1015;
        List<Integer> idParam = new ArrayList<Integer>();
        idParam.add(mpId);

        JsonObject entity = generateFromMpIds(idParam, TS14, TS12);
        checkGeneratedProbeCount(C8, entity, mpId);
        checkGeneratedTag(entity);

        //Check return data
        String startAttribute = "schedStartDate";
        Map<Integer, String> expectedValues = new HashMap<>();
        //Expected first record: 08/28/2016 @ 12:00am (UTC)
        expectedValues.put(0, TS15);
        //Expected last record: 02/29/2020 @ 12:00am (UTC)
        expectedValues.put(C7, TS12);
        checkEntityAttributeValues(
            entity,
            mpId,
            startAttribute,
            expectedValues);
    }

    /**
     * Test the quarterly generation of probe records in leap years.
     */
    private void testQuarterlyGenerationInLeapYear() {
        Integer mpId = ID1016;
        List<Integer> idParam = new ArrayList<Integer>();
        idParam.add(mpId);

        JsonObject entity = generateFromMpIds(idParam, TS14, TS12);
        checkGeneratedProbeCount(C16, entity, mpId);
        checkGeneratedTag(entity);

        //Check return data
        String startAttribute = "schedStartDate";
        Map<Integer, String> expectedValues = new HashMap<>();
        //Expected first record: 05/30/2016 @ 12:00am (UTC)
        expectedValues.put(0, TS16);
        //Expected last record: 02/29/2020 @ 12:00am (UTC)
        expectedValues.put(C15, TS12);
        checkEntityAttributeValues(
            entity,
            mpId,
            startAttribute,
            expectedValues);
    }

   /**
     * Test the generation of monthly probe records in leap years.
     */
    private void testMonthlyGenerationInLeapYear() {
        Integer mpId = ID1103;
        List<Integer> idParam = new ArrayList<Integer>();
        idParam.add(mpId);

        JsonObject entity = generateFromMpIds(idParam, TS2, TS1);
        checkGeneratedProbeCount(C15, entity, mpId);
        checkGeneratedTag(entity);

        //Check return data
        String startAttribute = "schedStartDate";
        Map<Integer, String> expectedValues = new HashMap<>();
        //Expected first record: 02/29/2020 @ 12:00am (UTC)
        expectedValues.put(0, TS12);
        //Expected second record: 03/29/2020 @ 12:00am (UTC)
        expectedValues.put(1, TS17);
        checkEntityAttributeValues(
            entity,
            mpId,
            startAttribute,
            expectedValues);
    }

    /**
    * Test the generation of four-weekly probe records in leap years.
    */
    private void test4WeeklyGenerationInLeapYear() {
        int mpId = ID1017;
        List<Integer> idParam = new ArrayList<Integer>();
        idParam.add(mpId);

        JsonObject entity = generateFromMpIds(idParam, TS18, TS13);
        int expectedCount = C13;
        checkGeneratedProbeCount(expectedCount, entity, mpId);
        checkGeneratedTag(entity);

        //Check return data
        String startAttribute = "schedStartDate";
        Map<Integer, String> expectedValues = new HashMap<>();
        //Expected first record: 02/29/2020 @ 12:00am (UTC)
        expectedValues.put(0, TS12);
        //Expected last record: 02/27/2021 @ 12:00am (UTC)
        expectedValues.put(expectedCount - 1, TS19);
        checkEntityAttributeValues(
            entity,
            mpId,
            startAttribute,
            expectedValues);
    }

    /**
     * Test the generation of two-weekly probe records in leap years.
    */
    private void test2WeeklyGenerationInLeapYear() {
        int mpId = ID1018;
        List<Integer> idParam = new ArrayList<Integer>();
        idParam.add(mpId);

        JsonObject entity = generateFromMpIds(idParam, TS18, TS13);
        int expectedCount = C26;
        checkGeneratedProbeCount(expectedCount, entity, mpId);
        checkGeneratedTag(entity);

        //Check return data
        String startAttribute = "schedStartDate";
        Map<Integer, String> expectedValues = new HashMap<>();
        //Expected first record: 02/29/2020 @ 12:00am (UTC)
        expectedValues.put(0, TS12);
        //Expected last record: 02/27/2021 @ 12:00am (UTC)
        expectedValues.put(expectedCount - 1, TS19);
        checkEntityAttributeValues(
            entity,
            mpId,
            startAttribute,
            expectedValues);
    }

    /**
     * Test the generation of weekly probe records in leap years.
    */
    private void testWeeklyGenerationInLeapYear() {
        int mpId = ID1019;
        List<Integer> idParam = new ArrayList<Integer>();
        idParam.add(mpId);

        JsonObject entity = generateFromMpIds(idParam, TS18, TS13);
        int expectedCount = C53;
        checkGeneratedProbeCount(expectedCount, entity, mpId);
        checkGeneratedTag(entity);

        //Check return data
        String startAttribute = "schedStartDate";
        Map<Integer, String> expectedValues = new HashMap<>();
        //Expected first record: 02/29/2020 @ 12:00am (UTC)
        expectedValues.put(0, TS12);
        //Expected last record: 03/06/2021 @ 12:00am (UTC)
        expectedValues.put(expectedCount - 1, TS20);
        checkEntityAttributeValues(
            entity,
            mpId,
            startAttribute,
            expectedValues);
    }

    /**
     * Test the generation of daily probe records in a leap year.
     * Should generate 368 records from 02/28/2020 to 03/01/2021.
     */
    private void testDailyGenerationInLeapYear() {
        int mpId = ID1012;
        List<Integer> idParam = new ArrayList<Integer>();
        idParam.add(mpId);

        JsonObject entity = generateFromMpIds(idParam, TS21, TS13);
        int expectedCount = C368;
        checkGeneratedProbeCount(expectedCount, entity, mpId);
        checkGeneratedTag(entity);

        //Check return data
        String startAttribute = "schedStartDate";
        Map<Integer, String> expectedValues = new HashMap<>();
        //Expected first record: 02/28/2020 @ 12:00am (UTC)
        expectedValues.put(0, TS21);
        //Expected second record: 02/29/2020 @ 12:00am (UTC)
        expectedValues.put(1, TS12);
        //Expected last record: 03/01/2021 @ 12:00am (UTC)
        expectedValues.put(expectedCount - 1, TS13);
        checkEntityAttributeValues(
            entity,
            mpId,
            startAttribute,
            expectedValues);
    }

    /**
     * Test the generation of probe records with a partial interval set.
     */
    private void testPartialIntevals() {
        int mpId = ID1008;
        List<Integer> idParam = new ArrayList<Integer>();
        idParam.add(mpId);

        JsonObject entity = generateFromMpIds(idParam, TS2, TS1);
        checkGeneratedProbeCount(C14, entity, mpId);
        checkGeneratedTag(entity);
    }

    /**
     * Tests the genereation from a list of mpIds.
     */
    private void testGenerationFromIdList() {
        int monthlyMpId = ID1013;
        int dailyMpId = ID1014;

        List<Integer> idParam = Arrays.asList(monthlyMpId, dailyMpId);
        JsonObject entity = generateFromMpIds(idParam, TS8, TS9);
        //Monthy mp should generate two records
        checkGeneratedProbeCount(2, entity, monthlyMpId);
        //Daily mp should generate 45 records
        checkGeneratedProbeCount(C45, entity, dailyMpId);
    }

    /**
     * Test if a generation request will be rejected if unathorized.
     */
    private void testGenerationRejectUnauthorized() {
        int mpId = ID1009;
        List<Integer> idParam = new ArrayList<Integer>();
        idParam.add(mpId);

        JsonObject entity = generateFromMpIds(idParam, TS2, TS1);

        //Request should have failed with message 699
        JsonObject mpData =
            entity.getJsonObject("proben").getJsonObject(Integer.toString(mpId));

        Assert.assertTrue(mpData.get("data") == JsonValue.NULL);
        Assert.assertFalse(mpData.getBoolean("success"));
        Assert.assertEquals(StatusCodes.NOT_ALLOWED, mpData.getInt("message"));
    }

    private void testZusatzwertgeneration() {
        final int mpId = 1020;
        List <Integer> idParam = new ArrayList<Integer>();
        idParam.add(mpId);

        JsonObject entity = generateFromMpIds(idParam, TS3, TS11);
        JsonArray proben = entity.getJsonObject("proben")
            .getJsonObject(String.valueOf(mpId)).getJsonArray("data");
        Assert.assertFalse("No samples generated", proben.isEmpty());

        proben.forEach((probe) -> {
            JsonObject probeObject = (JsonObject) probe;
            Integer probeId = probeObject.getInt(Sample_.ID);
            Response response = client.target(
                baseUrl + "rest/samplespecifmeasval?sampleId=" + probeId)
                .request()
                .header("X-SHIB-user", BaseTest.testUser)
                .header("X-SHIB-roles", BaseTest.testRoles)
                .get();
            JsonArray zwData = BaseTest.parseResponse(response).asJsonArray();
            Assert.assertFalse(zwData.isEmpty());
        });
    }

    /**
     * Checks if the tag stored in the given entity matches the expected one.
     * @param content Entity to check
     */
    private void checkGeneratedTag(JsonObject content) {
        String tag = content.getString("tag");

        String date = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyyMMdd"));
        String expectedTag = "PEP_" + date + "_" + expectedTagSerNo;

        Assert.assertFalse(tag == null || tag.equals(""));
        Assert.assertTrue(tag.equals(expectedTag));
    }

    /**
     * Checks if a generation process resulted in the expected number of
     * probe records.
     * @param count Expected count of records
     * @param content Result entity to check
     */
    private void checkGeneratedProbeCount(
        int count,
        JsonObject content,
        int mpId
    ) {
        //Get data for given messprogramm
        JsonObject mpData = content
            .getJsonObject("proben").getJsonObject(String.valueOf(mpId));
        Assert.assertNotNull(mpData);

        JsonArray proben = mpData.getJsonArray("data");
        Assert.assertEquals(count, proben.size());
    }

    /**
     * Generate probe records from a list of messprogramm ids, a start
     * timestamp.
     * and an end timestamp
     * @param ids List of messprogramm ids to generate from
     * @param start Timestamp in ms to start with
     * @param end Timestamp in ms to end with
     * @return JsonObject containing the generated objects
     */
    private JsonObject generateFromMpIds(
        List<Integer> ids, String start, String end
    ) {
        JsonArrayBuilder idArrayBuilder = Json.createArrayBuilder();
        ids.forEach(item -> {
            idArrayBuilder.add(item);
        });
        JsonObject payload = Json.createObjectBuilder()
            .add("start", start)
            .add("end", end)
            .add("ids", idArrayBuilder.build()).build();

        Response response = client.target(baseUrl + "rest/sample/messprogramm")
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .post(Entity.json(payload.toString()));

        JsonObject data = BaseTest.parseResponse(response).asJsonObject();

        //If a tag was applied, increase serial number
        if (data.containsKey("tag") && data.getString("tag") != null) {
            expectedTagSerNo++;
        }
        return data;
    }

    /**
     * Check if the given entity's attribute equals the expected values
     * given in a map.
     * @param entity Entity to check
     * @param mpId mpId to check
     * @param attribute Attribute name to check
     * @param expectedValues Map containing record index as key and expected
     * value as value
     */
    private void checkEntityAttributeValues(
        JsonObject entity,
        Integer mpId,
        String attribute,
        Map<Integer, String> expectedValues
    ) {
        expectedValues.forEach((index, value) -> {
            JsonObject record = getRecordAtIndex(entity, mpId, index);
            Assert.assertNotNull(record);
            String startDate = record.getString(attribute);
            Assert.assertEquals(value, startDate);
        });
    }

    /**
     * Parses an entity and returns the record at the given index for
     * messprogramm with given id.
     * @param content Entity to use
     * @param mpId MpId to uses
     * @param index Record index
     * @return Record as JsonObject
     */
    private JsonObject getRecordAtIndex(
        JsonObject content,
        Integer mpId,
        int index) {
        JsonObject result = null;
        try {
            JsonArray proben = content.getJsonObject("proben")
                    .getJsonObject(mpId.toString()).getJsonArray("data");
            result = proben.getJsonObject(index);
        } catch (JsonException je) {
            return null;
        }
        return result;
    }
}
