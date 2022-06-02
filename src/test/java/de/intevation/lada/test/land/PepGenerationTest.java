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

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.junit.Assert;

import de.intevation.lada.BaseTest;
import de.intevation.lada.Protocol;
import de.intevation.lada.test.ServiceTest;

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
    private static final int ID1010 = 1010;
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

    private static final int A699 = 699;

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

    private static final String TS1 = "1617235200000";
    private static final String TS2 = "1580256000000";
    private static final String TS3 = "1580515200000";
    private static final String TS4 = "1581465600000";
    private static final String TS5 = "1590969600000";
    private static final String TS6 = "1735689600000";
    private static final String TS7 = "1893456000000";
    private static final String TS8 = "1577836800000";
    private static final String TS9 = "1581638400000";
    private static final String TS10 = "1898553600000";
    private static final String TS11 = "1612224000000";
    private static final String TS12 = "1582934400000";
    private static final String TS13 = "1614556800000";
    private static final String TS14 = "1456704000000";
    private static final String TS15 = "1472428800000";
    private static final String TS16 = "1464566400000";
    private static final String TS17 = "1585440000000";
    private static final String TS18 = "1582502400000";
    private static final String TS19 = "1614384000000";
    private static final String TS20 = "1614988800000";
    private static final String TS21 = "1582848000000";

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
        testGenerationRejectInvalidParams();
        testGenerationRejectNegativeParams();
    }

    /**
     * Test the generation of daily probe records.
     */
    private void testDailyGeneration() {
        Protocol prot = new Protocol();
        prot.setName("PEP-Gen");
        prot.setType("Daily");
        prot.setPassed(false);
        protocol.add(prot);

        int mpId = ID1007;
        List<Integer> idParam = new ArrayList<Integer>();
        idParam.add(mpId);

        JsonObject entity = generateFromMpIds(idParam, TS3, TS4);
        checkGeneratedProbeCount(C12, entity, prot, mpId);
        checkGeneratedTag(entity, prot);
        prot.setPassed(true);
    }

    /**
     * Test the generation of weekly probe records.
     */
    private void testWeeklyGeneration() {
        Protocol prot = new Protocol();
        prot.setName("PEP-Gen");
        prot.setType("Weekly");
        prot.setPassed(false);
        protocol.add(prot);

        int mpId = ID1006;
        List<Integer> idParam = new ArrayList<Integer>();
        idParam.add(mpId);

        JsonObject entity = generateFromMpIds(idParam, TS3, TS5);
        checkGeneratedProbeCount(C18, entity, prot, mpId);
        checkGeneratedTag(entity, prot);
        prot.setPassed(true);
    }

    /**
     * Test the generation of two-weekly probe records.
     */
    private void test2WeeklyGeneration() {
        Protocol prot = new Protocol();
        prot.setName("PEP-Gen");
        prot.setType("2Weekly");
        prot.setPassed(false);
        protocol.add(prot);

        int mpId = ID1005;
        List<Integer> idParam = new ArrayList<Integer>();
        idParam.add(mpId);

        JsonObject entity = generateFromMpIds(idParam, TS3, TS5);
        checkGeneratedProbeCount(C9, entity, prot, mpId);
        checkGeneratedTag(entity, prot);

        prot.setPassed(true);
    }

    /**
     * Test the generation of four-weekly probe records.
     */
    private void test4WeeklyGeneration() {
        Protocol prot = new Protocol();
        prot.setName("PEP-Gen");
        prot.setType("4Weekly");
        prot.setPassed(false);
        protocol.add(prot);

        int mpId = ID1004;
        List<Integer> idParam = new ArrayList<Integer>();
        idParam.add(mpId);

        JsonObject entity = generateFromMpIds(idParam, TS3, TS5);
        checkGeneratedProbeCount(C5, entity, prot, mpId);
        checkGeneratedTag(entity, prot);

        prot.setPassed(true);
    }

    /**
     * Test the generation of monthly probe records.
     */
    private void testMonthlyGeneration() {
        Protocol prot = new Protocol();
        prot.setName("PEP-Gen");
        prot.setType("monthly");
        prot.setPassed(false);
        protocol.add(prot);

        int mpId = ID1003;
        List<Integer> id = new ArrayList<Integer>();
        id.add(mpId);

        //Generate 61 records for five years
        JsonObject entity = generateFromMpIds(id, TS6, TS7);
        checkGeneratedProbeCount(C61, entity, prot, mpId);
        checkGeneratedTag(entity, prot);

        prot.setPassed(true);
    }

    /**
     * Check the generation of quarterly probe records.
     */
    private void testQuarterlyGeneration() {
        Protocol prot = new Protocol();
        prot.setName("PEP-Gen");
        prot.setType("quarterly");
        prot.setPassed(false);
        protocol.add(prot);

        int mpId = ID1002;
        List<Integer> id = new ArrayList<Integer>();
        id.add(mpId);

        JsonObject entity = generateFromMpIds(id, TS8, TS11);
        checkGeneratedProbeCount(C4, entity, prot, mpId);
        checkGeneratedTag(entity, prot);

        prot.setPassed(true);
    }

    /**
     * Test the generation of half-yearly probe records.
     */
    private void testHalfYearlyGeneration() {
        Protocol prot = new Protocol();
        prot.setName("PEP-Gen");
        prot.setType("half yearly");
        prot.setPassed(false);
        protocol.add(prot);

        int mpId = ID1001;
        List<Integer> id = new ArrayList<Integer>();
        id.add(mpId);

        JsonObject entity = generateFromMpIds(id, TS8, TS11);
        checkGeneratedProbeCount(2, entity, prot, mpId);
        checkGeneratedTag(entity, prot);

        prot.setPassed(true);
    }

    /**
     * Test a simple yearly generation of probe records.
     */
    private void testYearlyGeneration() {
        Protocol prot = new Protocol();
        prot.setName("PEP-Gen");
        prot.setType("yearly");
        prot.setPassed(false);
        protocol.add(prot);

        int mpId = ID1000;
        List<Integer> id = new ArrayList<Integer>();
        id.add(mpId);

        JsonObject entity = generateFromMpIds(id, TS8, TS7);
        checkGeneratedProbeCount(C11, entity, prot, mpId);
        checkGeneratedTag(entity, prot);

        prot.setPassed(true);
    }

    /**
     * Test the generation of probe records starting on the 29th of February.
     * in a leap year
     */
    private void testYearlyGenerationInLeapYear() {
        Protocol prot = new Protocol();
        prot.setName("PEP-Gen");
        prot.setType("yearly in leap year");
        prot.setPassed(false);
        protocol.add(prot);

        Integer mpId = ID1100;
        List<Integer> idParam = new ArrayList<Integer>();
        idParam.add(mpId);

        JsonObject entity = generateFromMpIds(idParam, TS8, TS10);
        int expectedCount = C11;
        checkGeneratedProbeCount(expectedCount, entity, prot, mpId);
        checkGeneratedTag(entity, prot);

        //Check return data
        String startAttribute = "solldatumBeginn";
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

        prot.setPassed(true);
    }

    /**
     * Test the half yearly generation of probe records in leap years.
     */
    private void testHalfYearlyGenerationInLeapYear() {
        Protocol prot = new Protocol();
        prot.setName("PEP-Gen");
        prot.setType("half yearly in leap year");
        prot.setPassed(false);
        protocol.add(prot);

        Integer mpId = ID1015;
        List<Integer> idParam = new ArrayList<Integer>();
        idParam.add(mpId);

        JsonObject entity = generateFromMpIds(idParam, TS14, TS12);
        checkGeneratedProbeCount(C8, entity, prot, mpId);
        checkGeneratedTag(entity, prot);

        //Check return data
        String startAttribute = "solldatumBeginn";
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

        prot.setPassed(true);
    }

    /**
     * Test the quarterly generation of probe records in leap years.
     */
    private void testQuarterlyGenerationInLeapYear() {
        Protocol prot = new Protocol();
        prot.setName("PEP-Gen");
        prot.setType("quarterly in leap year");
        prot.setPassed(false);
        protocol.add(prot);

        Integer mpId = ID1016;
        List<Integer> idParam = new ArrayList<Integer>();
        idParam.add(mpId);

        JsonObject entity = generateFromMpIds(idParam, TS14, TS12);
        checkGeneratedProbeCount(C16, entity, prot, mpId);
        checkGeneratedTag(entity, prot);

        //Check return data
        String startAttribute = "solldatumBeginn";
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


        prot.setPassed(true);
    }

   /**
     * Test the generation of monthly probe records in leap years.
     */
    private void testMonthlyGenerationInLeapYear() {
        Protocol prot = new Protocol();
        prot.setName("PEP-Gen");
        prot.setType("monthly in leap year");
        prot.setPassed(false);
        protocol.add(prot);

        Integer mpId = ID1103;
        List<Integer> idParam = new ArrayList<Integer>();
        idParam.add(mpId);

        JsonObject entity = generateFromMpIds(idParam, TS2, TS1);
        checkGeneratedProbeCount(C15, entity, prot, mpId);
        checkGeneratedTag(entity, prot);

        //Check return data
        String startAttribute = "solldatumBeginn";
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

        prot.setPassed(true);
    }

    /**
    * Test the generation of four-weekly probe records in leap years.
    */
    private void test4WeeklyGenerationInLeapYear() {
        Protocol prot = new Protocol();
        prot.setName("PEP-Gen");
        prot.setType("4Weekly in leap year");
        prot.setPassed(false);
        protocol.add(prot);

        int mpId = ID1017;
        List<Integer> idParam = new ArrayList<Integer>();
        idParam.add(mpId);

        JsonObject entity = generateFromMpIds(idParam, TS18, TS13);
        int expectedCount = C13;
        checkGeneratedProbeCount(expectedCount, entity, prot, mpId);
        checkGeneratedTag(entity, prot);

        //Check return data
        String startAttribute = "solldatumBeginn";
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


        prot.setPassed(true);
    }

    /**
     * Test the generation of two-weekly probe records in leap years.
    */
    private void test2WeeklyGenerationInLeapYear() {
        Protocol prot = new Protocol();
        prot.setName("PEP-Gen");
        prot.setType("2Weekly in leap year");
        prot.setPassed(false);
        protocol.add(prot);

        int mpId = ID1018;
        List<Integer> idParam = new ArrayList<Integer>();
        idParam.add(mpId);

        JsonObject entity = generateFromMpIds(idParam, TS18, TS13);
        int expectedCount = C26;
        checkGeneratedProbeCount(expectedCount, entity, prot, mpId);
        checkGeneratedTag(entity, prot);

        //Check return data
        String startAttribute = "solldatumBeginn";
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

        prot.setPassed(true);
    }

    /**
     * Test the generation of weekly probe records in leap years.
    */
    private void testWeeklyGenerationInLeapYear() {
        Protocol prot = new Protocol();
        prot.setName("PEP-Gen");
        prot.setType("Weekly in leap year");
        prot.setPassed(false);
        protocol.add(prot);

        int mpId = ID1019;
        List<Integer> idParam = new ArrayList<Integer>();
        idParam.add(mpId);

        JsonObject entity = generateFromMpIds(idParam, TS18, TS13);
        int expectedCount = C53;
        checkGeneratedProbeCount(expectedCount, entity, prot, mpId);
        checkGeneratedTag(entity, prot);

        //Check return data
        String startAttribute = "solldatumBeginn";
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

        prot.setPassed(true);
    }

    /**
     * Test the generation of daily probe records in a leap year.
     * Should generate 368 records from 02/28/2020 to 03/01/2021.
     */
    private void testDailyGenerationInLeapYear() {
        Protocol prot = new Protocol();
        prot.setName("PEP-Gen");
        prot.setType("daily in leap year");
        prot.setPassed(false);
        protocol.add(prot);

        int mpId = ID1012;
        List<Integer> idParam = new ArrayList<Integer>();
        idParam.add(mpId);

        JsonObject entity = generateFromMpIds(idParam, TS21, TS13);
        int expectedCount = C368;
        checkGeneratedProbeCount(expectedCount, entity, prot, mpId);
        checkGeneratedTag(entity, prot);

        //Check return data
        String startAttribute = "solldatumBeginn";
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


        prot.setPassed(true);
    }
    /**
     * Test the generation of probe records with a partial interval set.
     */
    private void testPartialIntevals() {
        Protocol prot = new Protocol();
        prot.setName("PEP-Gen");
        prot.setType("monthly with partial interval");
        prot.setPassed(false);
        protocol.add(prot);

        int mpId = ID1008;
        List<Integer> idParam = new ArrayList<Integer>();
        idParam.add(mpId);

        JsonObject entity = generateFromMpIds(idParam, TS2, TS1);
        checkGeneratedProbeCount(C14, entity, prot, mpId);
        checkGeneratedTag(entity, prot);

        prot.setPassed(true);
    }

    /**
     * Tests the genereation from a list of mpIds.
     */
    private void testGenerationFromIdList() {
        Protocol prot = new Protocol();
        prot.setName("Pep Gen");
        prot.setType("Generation from list");
        prot.setPassed(false);
        protocol.add(prot);

        int monthlyMpId = ID1013;
        int dailyMpId = ID1014;

        List<Integer> idParam = Arrays.asList(monthlyMpId, dailyMpId);
        JsonObject entity = generateFromMpIds(idParam, TS8, TS9);
        //Monthy mp should generate two records
        checkGeneratedProbeCount(2, entity, prot, monthlyMpId);
        //Daily mp should generate 45 records
        checkGeneratedProbeCount(C45, entity, prot, dailyMpId);
        prot.setPassed(true);
    }

    /**
     * Test if a generation request will be rejected if unathorized.
     */
    private void testGenerationRejectUnauthorized() {
        Protocol prot = new Protocol();
        prot.setName("PEP-Gen");
        prot.setType("reject unauthorized");
        prot.setPassed(false);
        protocol.add(prot);

        int mpId = ID1009;
        List<Integer> idParam = new ArrayList<Integer>();
        idParam.add(mpId);

        JsonObject entity = generateFromMpIds(idParam, TS2, TS1);

        //Request should have failed with message 699
        JsonObject data = entity.getJsonObject("data");
        JsonObject mpData =
            data.getJsonObject("proben").getJsonObject(Integer.toString(mpId));

        Assert.assertTrue(mpData.get("data") == JsonValue.NULL);
        Assert.assertFalse(mpData.getBoolean("success"));
        Assert.assertEquals(A699, mpData.getInt("message"));

        prot.setPassed(true);
    }

    /**
     * Test if server rejects a request containing invalid params.
     */
    private void testGenerationRejectInvalidParams() {
        Protocol prot = new Protocol();
        prot.setName("PEP-Gen");
        prot.setType("reject invalid params");
        prot.setPassed(false);
        protocol.add(prot);

        int mpId = ID1010;
        List<Integer> idParam = new ArrayList<Integer>();
        idParam.add(mpId);

        JsonObject entity = generateFromMpIds(idParam, TS1, TS2);

        //Request should have failed with message 699
        JsonObject data = entity.getJsonObject("data");
        JsonObject mpData =
            data.getJsonObject("proben").getJsonObject(Integer.toString(mpId));

        Assert.assertTrue(mpData.get("data") == JsonValue.NULL);
        Assert.assertFalse(mpData.getBoolean("success"));
        Assert.assertEquals(A699, mpData.getInt("message"));

        prot.setPassed(true);
    }

    /**
     * Test if generation request is rejected if time parameters are invalid.
     */
    private void testGenerationRejectNegativeParams() {
        Protocol prot = new Protocol();
        prot.setName("PEP-Gen");
        prot.setType("reject negative params");
        prot.setPassed(false);
        protocol.add(prot);

        int mpId = ID1010;
        List<Integer> idParam = new ArrayList<Integer>();
        idParam.add(mpId);

        JsonObject entity = generateFromMpIds(idParam, "-5", "-1");

        //Request should have failed with message 699
        JsonObject data = entity.getJsonObject("data");
        JsonObject mpData =
            data.getJsonObject("proben").getJsonObject(Integer.toString(mpId));

        Assert.assertTrue(mpData.get("data") == JsonValue.NULL);
        Assert.assertFalse(mpData.getBoolean("success"));
        Assert.assertEquals(A699, mpData.getInt("message"));

        prot.setPassed(true);
    }



    /**
     * Checks if the tag stored in the given entity matches the expected one.
     * @param content Entity to check
     * @param prot Protocol to use
     */
    private void checkGeneratedTag(JsonObject content, Protocol prot) {
        JsonObject data = content.getJsonObject("data");
        String tag = data.getString("tag");

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
     * @param prot Protocol to use
     */
    private void checkGeneratedProbeCount(
        int count,
        JsonObject content,
        Protocol prot,
        int mpId
    ) {
        //Get data for given messprogramm
        JsonObject mpData = content.getJsonObject("data")
            .getJsonObject("proben").getJsonObject(String.valueOf(mpId));
        Assert.assertNotNull(mpData);

        JsonArray proben = mpData.getJsonArray("data");
        prot.addInfo("objects", proben.size());
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

        WebTarget target = client.target(baseUrl + "rest/probe/messprogramm");
        JsonArrayBuilder idArrayBuilder = Json.createArrayBuilder();
        ids.forEach(item -> {
            idArrayBuilder.add(item);
        });
        JsonObject payload = Json.createObjectBuilder()
            .add("start", start)
            .add("end", end)
            .add("ids", idArrayBuilder.build()).build();

        Response response = target.request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .post(Entity.json(payload.toString()));

        JsonObject content = BaseTest.parseResponse(response);
        JsonObject data = content.getJsonObject("data");

        //If a tag was applied, increase serial number
        if (data.containsKey("tag") && data.getString("tag") != null) {
            expectedTagSerNo++;
        }
        return content;
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
            JsonObject data = content.getJsonObject("data");
            JsonArray proben = data.getJsonObject("proben")
                    .getJsonObject(mpId.toString()).getJsonArray("data");
            result = proben.getJsonObject(index);
        } catch (JsonException je) {
            return null;
        }
        return result;
    }
}
