/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.UUID;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.SyncInvoker;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.logging.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.intevation.lada.rest.AsyncLadaService.AsyncJobResponse;
import de.intevation.lada.data.requests.Laf8ImportParameters;
import de.intevation.lada.data.requests.Laf9ImportParameters;
import de.intevation.lada.data.requests.LafImportParameters;
import de.intevation.lada.importer.laf.Laf9ImportJob;
import de.intevation.lada.model.lada.CommMeasm;
import de.intevation.lada.model.lada.CommSample;
import de.intevation.lada.model.lada.Geolocat;
import de.intevation.lada.model.lada.MeasVal;
import de.intevation.lada.model.lada.MeasVal_;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Measm_;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.lada.Sample_;
import de.intevation.lada.model.lada.StatusProt;
import de.intevation.lada.model.master.Site;
import de.intevation.lada.model.master.Site_;
import de.intevation.lada.model.master.Tag;
import de.intevation.lada.model.lada.SampleSpecifMeasVal;
import de.intevation.lada.model.lada.SampleSpecifMeasVal_;
import de.intevation.lada.util.data.Job;
import de.intevation.lada.util.data.Job.JobStatus;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.JSONBConfig;


/**
 * Class to test the Lada-Importer.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@RunWith(Arquillian.class)
public class ImporterTest extends BaseTest {

    private static final Logger LOG = Logger.getLogger(ImporterTest.class);

    private static final String ASYNC_IMPORT_URL = "data/import/async/";

    public static final String SAMPLES_KEY = "samples";
    private static final String SAMPLE_IDS_KEY = "sampleIds";
    private static final String TAGS_KEY = "tags";
    private static final String ERRORS_KEY = "errors";
    private static final String WARNINGS_KEY = "warnings";
    private static final String OWNER_KEY = "owner";
    private static final String REPORT_ITEM_KEY_KEY = "key";
    private static final String REPORT_ITEM_VALUE_KEY = "value";
    private static final String REPORT_ITEM_CODE_KEY = "code";
    private static final String MSG_FORBIDDEN = "Forbidden";
    private static final String TYPE_REGULATION_E = "E";

    private static final Jsonb JSONB_SPARSE = JsonbBuilder.create(
        JSONBConfig.JSONB_CONFIG.withNullValues(false));

    private final String existingExtId = "T001";
    private final String foreignExtId = "foreign";
    private final String existingMainSampleId = "120510002";
    private final int existingMeasmExtId = 453;
    private final String existingSiteExtId = "D_00191";
    private final String mstId = "06010";
    private final String regulation = "test";
    private final String sampleSpecifId = "A1";
    private final String envDescrip = "D: 01 01 00 00 00 00 00 00 00 00 00 00";
    private final String mmtId = "A3";
    private final String invalidMmtId = "XXX";
    private final String measd = "H-3";
    private final String measUnit = "Bq/kgFM";
    private final String existingAssociatedTag = "associated";
    private final String existingNotAssociatedTag = "not associated";
    private final String laf8Template = "%%PROBE%%\n"
        + "UEBERTRAGUNGSFORMAT 7\n"
        + "VERSION \"0084\"\n"
        + "HAUPTPROBENNUMMER \"%s\"\n"
        + "PROBENART \"E\"\n"
        + "TESTDATEN 0\n"
        + "MESSPROGRAMM_S 1\n"
        + "DATENBASIS \"%s\"\n"
        + "PZB_S \"%s\" 42 \"\" 5.0\n"
        + "PROBENAHME_DATUM_UHRZEIT_A 20120510 0900\n"
        + "DESKRIPTOREN \"" + envDescrip.replaceAll("(D:| )", "") + "\"\n"
        + "%s"
        + "%%MESSUNG%%\n"
        + "MESSMETHODE_S \"" + mmtId + "\"\n"
        + "MESSWERT \"%s\" 0 \"%s\" 4.4\n"
        + "%s"
        + "%%ENDE%%\n";

    private Map<String, JsonValue> expectedAttrs = Map.of(
        Sample_.IS_TEST, JsonValue.FALSE,
        Sample_.MEAS_FACIL_ID, Json.createValue(mstId),
        Sample_.REGULATION_ID, Json.createValue(1),
        Sample_.SAMPLE_SPECIF_MEAS_VALS, Json.createArrayBuilder()
        .add(Json.createObjectBuilder()
            .add(SampleSpecifMeasVal_.SAMPLE_SPECIF_ID, sampleSpecifId))
        .build(),
        Sample_.MEASMS, Json.createArrayBuilder()
        .add(Json.createObjectBuilder()
            .add(Measm_.MMT_ID, mmtId)
            .add(Measm_.MEAS_VALS, Json.createArrayBuilder()
                .add(Json.createObjectBuilder()
                    .add(MeasVal_.MEASD_ID, 1)
                    .add(MeasVal_.MEAS_UNIT_ID, 1))))
        .build()
    );

    public ImporterTest() {
        testDatasetName = "datasets/dbUnit_import.xml";
    }

    /**
     * Cancel asynchronous jobs in order to allow database cleanup.
     */
    @After
    public void cancelJobs() {
        target.path(ASYNC_IMPORT_URL + "cancel")
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .get();
    }

    /**
     * Test synchronous import of a Sample object.
     */
    @Test
    @RunAsClient
    public final void testImportProbe() {
        final String laf = String.format(
            laf8Template, randomProbeId(),
            regulation, sampleSpecifId, "", measd, measUnit, "");

        /* Request synchronous import */
        Response importResponse = target
            .path("data/import/laf")
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .header("X-LADA-MST", mstId)
            .post(Entity.entity(laf, MediaType.TEXT_PLAIN));
        JsonObject importResponseObject =
            parseResponse(importResponse).asJsonObject();

        /* Check if a Sample object has been imported */
        assertContains(importResponseObject, SAMPLE_IDS_KEY);
        Assert.assertEquals(1,
            importResponseObject.getJsonArray(SAMPLE_IDS_KEY).size());
    }

    /**
     * Test successful asynchronous LAF8 import of a Sample object.
     */
    @Test
    @RunAsClient
    public final void testAsyncLaf8ImportSuccess()
        throws InterruptedException, CharacterCodingException {
        final String lafSampleId = randomProbeId();
        final String laf = String.format(
            laf8Template, lafSampleId,
            regulation, sampleSpecifId, "", measd, measUnit, "");
        Map<String, JsonValue> verify = new HashMap<>(expectedAttrs);
        verify.put(Sample_.MAIN_SAMPLE_ID, Json.createValue(lafSampleId));
        JsonObject report = testAsyncLaf8Import(laf, lafSampleId, true, verify);

        JsonObject expectedWarning = Json.createObjectBuilder()
            .add(REPORT_ITEM_KEY_KEY, "validation#probe")
            .add(REPORT_ITEM_VALUE_KEY, Sample_.GEOLOCATS)
            .add(REPORT_ITEM_CODE_KEY, "A sampling location must be provided")
            .build();
        MatcherAssert.assertThat(
            report.getJsonObject(WARNINGS_KEY).getJsonArray(lafSampleId),
            CoreMatchers.hasItem(expectedWarning));

        JsonObject expectedNotification = Json.createObjectBuilder()
            .add("key", "validation#messwert")
            .add("value", MeasVal_.MEAS_VAL)
            .add("code", "must be greater than 0")
            .build();
        MatcherAssert.assertThat(
            report.getJsonObject("notifications").getJsonArray(lafSampleId),
            CoreMatchers.hasItem(expectedNotification));
    }

    /**
     * Test successful asynchronous LAF9 import of a Sample object.
     */
    @Test
    @RunAsClient
    public final void testAsyncLaf9ImportSuccess()
        throws InterruptedException, CharacterCodingException {
        final String lafSampleId = randomProbeId();
        Sample laf = prepareLaf9Data();
        laf.setMainSampleId(lafSampleId);

        // IDs are expected to be ignored instead of causing errors
        laf.setId(1);
        Measm lafMeasm = laf.getMeasms().get(0);
        lafMeasm.setId(1);
        lafMeasm.getMeasVals().get(0).setId(1);
        lafMeasm.getCommMeasms().get(0).setId(1);

        /* Expected result has additional StatusProt generated by DB
           and additional generated tag */
        Sample expected = prepareLaf9Data();
        expected.setMainSampleId(lafSampleId);
        StatusProt generatedStatus = new StatusProt();
        generatedStatus.setStatusMpId(1);
        expected.getMeasms().get(0).getStatusProts().add(0, generatedStatus);
        Tag generatedTag = new Tag();
        generatedTag.setIsAutoTag(true);
        expected.getTags().add(generatedTag);
        expected.getMeasms().forEach(m -> m.getTags().add(generatedTag));

        testAsyncLaf9Import(
            laf, lafSampleId, true, false, expected,
            OWNER_KEY, Site_.REFERENCE_COUNT);
    }

    /**
     * Test asynchronous LAF9 import of a Sample without geolocats.
     */
    @Test
    @RunAsClient
    public final void testAsyncLaf9ImportNoGeolocats()
        throws InterruptedException, CharacterCodingException {
        final String lafSampleId = randomProbeId();
        Sample laf = prepareLaf9Sample();
        laf.setMainSampleId(lafSampleId);

        JsonObject report = testAsyncLaf9Import(
            laf, lafSampleId, true, false, laf, OWNER_KEY, TAGS_KEY);
        JsonArray reportSamples = report.getJsonArray(SAMPLES_KEY);
        assertHasMessage(
            WARNINGS_KEY,
            reportSamples.getJsonObject(0),
            Sample_.GEOLOCATS,
            "A sampling location must be provided");
    }

    /**
     * Test successful asynchronous LAF8 update import of a Sample object.
     */
    @Test
    @RunAsClient
    public final void testAsyncLaf8UpdateImport()
        throws InterruptedException, CharacterCodingException {
        final String laf = String.format(
            laf8Template, existingMainSampleId,
            "test2", sampleSpecifId, "", measd, measUnit, "");

        Map<String, JsonValue> verify = new HashMap<>(expectedAttrs);
        verify.put(
            Sample_.MAIN_SAMPLE_ID, Json.createValue(existingMainSampleId));
        verify.put(Sample_.REGULATION_ID, Json.createValue(2));

        JsonObject report =
            testAsyncLaf8Import(laf, existingMainSampleId, true, verify);
        assertMeasValIsReplaced(report);
    }

    private void assertMeasValIsReplaced(JsonObject report) {
        // Assert that measVal is replaced
        final int existingMeasmId = 1;
        List<Measm> measms = getImportedSample(report, Sample.class)
            .getMeasms();
        Assert.assertEquals(1, measms.size());
        MatcherAssert.assertThat(
            measms.stream().map(Measm::getId).toList(),
            CoreMatchers.hasItem(existingMeasmId));
        final int existingMeasValId = 1;
        Collection<MeasVal> measVals = measms.get(0).getMeasVals();
        Assert.assertEquals(1, measVals.size());
        MatcherAssert.assertThat(
            measVals.stream().map(MeasVal::getId).toList(),
            CoreMatchers.not(CoreMatchers.hasItem(existingMeasValId)));
    }

    /**
     * Test successful asynchronous LAF9 update import.
     */
    @Test
    @RunAsClient
    public final void testAsyncLaf9UpdateImport()
        throws InterruptedException, CharacterCodingException {
        Sample laf = prepareLaf9Data();

        // Update sample
        laf.setMainSampleId(existingMainSampleId);
        final int updateRegId = 2;
        laf.setRegulationId(updateRegId);

        // Update sampleSpecifMeasVal
        SampleSpecifMeasVal sampleSpecifMeasVal
            = laf.getSampleSpecifMeasVals().get(0);
        sampleSpecifMeasVal.setMeasVal(1d);
        sampleSpecifMeasVal.setError(0f);
        sampleSpecifMeasVal.setSmallerThan("<");

        // Update site
        laf.getGeolocats().get(0).getSite().setExtId(existingSiteExtId);

        testAsyncLaf9Import(
            laf, existingMainSampleId, true, true, laf,
            "readonly", OWNER_KEY, Measm_.STATUS_PROTS, TAGS_KEY,
            Site_.REFERENCE_COUNT);
    }

    /**
     * Test successful asynchronous LAF9 update import of Measm objects.
     */
    @Test
    @RunAsClient
    public final void testAsyncLaf9UpdateMeasmImport()
        throws InterruptedException, CharacterCodingException {
        Sample laf = new Sample();
        laf.setExtId(existingExtId);

        Measm measmUpdate = new Measm();
        measmUpdate.setExtId(existingMeasmExtId);
        final String minSampleId = "test";
        measmUpdate.setMinSampleId(minSampleId);

        Measm measmNew = new Measm();
        measmNew.setMmtId("I3");

        laf.setMeasms(List.of(measmUpdate, measmNew));

        testAsyncLaf9Import(
            laf, existingMainSampleId, true, true, laf, OWNER_KEY, TAGS_KEY);
    }

    /**
     * Test successful asynchronous LAF9 update import of MeasVal objects,
     * which means existing measVals are replaced.
     */
    @Test
    @RunAsClient
    public final void testAsyncLaf9UpdateMeasValImport()
        throws InterruptedException, CharacterCodingException {
        Sample laf = new Sample();
        laf.setExtId(existingExtId);

        Measm measm = new Measm();
        measm.setExtId(existingMeasmExtId);

        MeasVal measVal = new MeasVal();
        measVal.setMeasdId(1);
        measVal.setMeasUnitId(1);
        measm.setMeasVals(List.of(measVal));

        laf.setMeasms(List.of(measm));

        JsonObject report = testAsyncLaf9Import(
            laf, existingMainSampleId, true, true, laf, OWNER_KEY, TAGS_KEY);
        assertMeasValIsReplaced(report);
    }

    /**
     * Asynchronous LAF9 import reports error, if given measVals with
     * duplicate measurand.
     */
    @Test
    @RunAsClient
    public final void testAsyncLaf9DupMeasValImport()
        throws InterruptedException, CharacterCodingException {
        Sample laf = new Sample();
        laf.setExtId(existingExtId);

        Measm measm = new Measm();
        measm.setExtId(existingMeasmExtId);

        MeasVal measVal1 = new MeasVal();
        measVal1.setMeasdId(1);
        measVal1.setMeasUnitId(1);
        MeasVal measVal2 = new MeasVal();
        measVal2.setMeasdId(1);
        measVal2.setMeasUnitId(1);
        measm.setMeasVals(List.of(measVal1, measVal2));

        laf.setMeasms(List.of(measm));

        JsonObject report = testAsyncLaf9Import(
            laf, existingMainSampleId, false, true, laf);
        assertHasError(
            report.getJsonArray(SAMPLES_KEY).getJsonObject(0)
            .getJsonArray(Sample_.MEASMS).getJsonObject(0)
            .getJsonArray(Measm_.MEAS_VALS).getJsonObject(1),
            MeasVal_.MEASD_ID,
            "Non-unique value combination for [measdId, measm]");
    }

    /**
     * Test successful asynchronous LAF9 update import of sample tags.
     */
    @Test
    @RunAsClient
    public final void testAsyncLaf9UpdateTagsImport()
        throws InterruptedException, CharacterCodingException {
        Sample laf = new Sample();
        laf.setExtId(existingExtId);
        // Existing already associated tag
        Tag associatedTag = new Tag();
        associatedTag.setName(existingAssociatedTag);
        // Existing tag
        Tag existingTag = new Tag();
        existingTag.setName(existingNotAssociatedTag);
        // New tag
        Tag tag = new Tag();
        tag.setName("test");
        tag.setMeasFacilId(mstId);
        laf.setTags(List.of(associatedTag, existingTag, tag));

        Sample expected = new Sample();
        expected.setExtId(existingExtId);
        // Import creates an additional tag
        Tag importTag = new Tag();
        importTag.setIsAutoTag(true);
        expected.setTags(List.of(associatedTag, existingTag, tag, importTag));

        testAsyncLaf9Import(
            laf, existingMainSampleId, true, true, expected, OWNER_KEY);
    }

    /**
     * Asynchronous LAF9 update import of tags does not remove existing tags.
     */
    @Test
    @RunAsClient
    public final void testAsyncLaf9UpdateTagsLeaveExisting()
        throws InterruptedException, CharacterCodingException {
        Sample laf = new Sample();
        laf.setExtId(existingExtId);
        // Existing tag
        Tag existingTag = new Tag();
        existingTag.setName(existingNotAssociatedTag);
        // New tag
        Tag tag = new Tag();
        tag.setName("test");
        tag.setMeasFacilId(mstId);
        laf.setTags(List.of(existingTag, tag));

        Sample expected = new Sample();
        expected.setExtId(existingExtId);
        // Existing already associated tag
        Tag associatedTag = new Tag();
        associatedTag.setName(existingAssociatedTag);
        // Import creates an additional tag
        Tag importTag = new Tag();
        importTag.setIsAutoTag(true);
        expected.setTags(List.of(associatedTag, existingTag, tag, importTag));

        testAsyncLaf9Import(
            laf, existingMainSampleId, true, true, expected, OWNER_KEY);
    }

    /**
     * Asynchronous LAF9 import reports error, if given invalid child object.
     */
    @Test
    @RunAsClient
    public final void testAsyncLaf9WithInvalidChildImport()
        throws InterruptedException, CharacterCodingException {
        final String lafSampleId = randomProbeId();
        Sample laf = prepareLaf9Data();
        laf.setMainSampleId(lafSampleId);
        Measm measm = new Measm();
        measm.setMmtId(invalidMmtId);
        laf.getMeasms().add(measm);

        Sample expected = prepareLaf9Data();
        expected.setMainSampleId(lafSampleId);

        assertHasInvalidMmtError(testAsyncLaf9Import(
                laf, existingMainSampleId, false, true, expected));
    }

    /**
     * Asynchronous LAF9 import reports error, if given invalid child object
     * for update.
     */
    @Test
    @RunAsClient
    public final void testAsyncLaf9UpdateWithInvalidChildImport()
        throws InterruptedException, CharacterCodingException {
        Sample laf = new Sample();
        laf.setExtId(existingExtId);
        Measm measm = new Measm();
        measm.setExtId(existingMeasmExtId);
        measm.setMmtId(invalidMmtId);
        laf.setMeasms(List.of(measm));
        MeasVal measVal = new MeasVal();
        measVal.setMeasdId(2);
        measVal.setMeasUnitId(2);
        measm.setMeasVals(List.of(measVal));

        // Sample and all child objects should remain unchanged
        final int existingSampleId = 1;
        JsonObject expected = getSample(existingSampleId, JsonObject.class);

        assertHasInvalidMmtError(testAsyncLaf9Import(
                laf, existingMainSampleId, false, true, expected));
    }

    private void assertHasInvalidMmtError(JsonObject report) {
        assertHasError(
            report.getJsonArray(SAMPLES_KEY).getJsonObject(0)
            .getJsonArray(Sample_.MEASMS).stream()
            .filter(m -> invalidMmtId.equals(
                    m.asJsonObject().getString(Measm_.MMT_ID)))
            .findFirst().get().asJsonObject(),
            Measm_.MMT_ID,
            "size must be between 0 and 2");
    }

    /**
     * Test failing sample identification with LAF8.
     */
    @Test
    @RunAsClient
    public final void testAsyncLaf8IdentifyFail()
        throws InterruptedException, CharacterCodingException {
        final String newMainSampleId = "XXX";
        final String laf = String.format(
            laf8Template, newMainSampleId,
            "test", sampleSpecifId,
            "PROBE_ID \"" + existingExtId + "\"\n", measd, measUnit, "");

        JsonObject report = testAsyncLaf8Import(laf, newMainSampleId, false);
        JsonObject expectedError = Json.createObjectBuilder()
            .add(REPORT_ITEM_KEY_KEY, "duplicate")
            .add(REPORT_ITEM_VALUE_KEY, "")
            .add(REPORT_ITEM_CODE_KEY, String.valueOf(StatusCodes.IMP_PRESENT))
            .build();
        MatcherAssert.assertThat(
            report.getJsonObject(ERRORS_KEY).getJsonArray(existingExtId),
            CoreMatchers.hasItem(expectedError));
    }

    /**
     * Test failing sample identification with LAF9.
     */
    @Test
    @RunAsClient
    public final void testAsyncLaf9IdentifyFail()
        throws InterruptedException, CharacterCodingException {
        Sample laf = prepareLaf9Data();
        laf.setExtId(existingExtId);
        final String newMainSampleId = "XXX";
        laf.setMainSampleId("XXX");

        JsonObject report = testAsyncLaf9Import(
            laf, newMainSampleId, false, false, expectedAttrs);
        JsonObject actualErrors = report.getJsonArray(SAMPLES_KEY)
            .getJsonObject(0).getJsonObject(ERRORS_KEY);
        MatcherAssert.assertThat(actualErrors.keySet(),
            CoreMatchers.hasItem(Laf9ImportJob.ERR_IDENTIFICATION_KEY));
        MatcherAssert.assertThat(
            actualErrors.getJsonArray(Laf9ImportJob.ERR_IDENTIFICATION_KEY),
            CoreMatchers.hasItem(Json.createValue(
                    String.valueOf(StatusCodes.IMP_INVALID_VALUE))));
    }

    /**
     * Failing sample creation authorization with LAF9.
     */
    @Test
    @RunAsClient
    public final void asyncLaf9AuthFail()
        throws InterruptedException, CharacterCodingException {
        final String lafSampleId = randomProbeId();
        Sample laf = prepareLaf9Data();
        laf.setMainSampleId(lafSampleId);
        laf.setMeasFacilId("06011");

        assertForbidden(testAsyncLaf9Import(
                laf, lafSampleId, false, false, expectedAttrs));
    }

    /**
     * Failing sample update authorization with LAF9.
     */
    @Test
    @RunAsClient
    public final void asyncLaf9UpdateAuthFail()
        throws InterruptedException, CharacterCodingException {
        Sample laf = new Sample();
        laf.setExtId(foreignExtId);
        laf.setMeasFacilId(mstId);

        assertForbidden(testAsyncLaf9Import(
                laf, foreignExtId, false, true, expectedAttrs));
    }

    private void assertForbidden(JsonObject report) {
        JsonArray reportSamples = report.getJsonArray(SAMPLES_KEY);
        assertHasError(
            reportSamples.getJsonObject(0),
            Laf9ImportJob.ERR_AUTHORIZATION_KEY,
            MSG_FORBIDDEN);
    }

    /**
     * Failing authorization of tagging sample with LAF9.
     */
    @Test
    @RunAsClient
    public final void asyncLaf9TagSampleAuthFail()
        throws InterruptedException, CharacterCodingException {
        Sample laf = new Sample();
        laf.setExtId(foreignExtId);
        Tag existingTag = new Tag();
        existingTag.setName(existingNotAssociatedTag);
        laf.setTags(List.of(existingTag));

        JsonObject report = testAsyncLaf9Import(
            laf, foreignExtId, false, true, expectedAttrs);
        assertHasError(
            report.getJsonArray(SAMPLES_KEY).getJsonObject(0)
            .getJsonArray(TAGS_KEY).getJsonObject(0),
            Laf9ImportJob.ERR_AUTHORIZATION_KEY,
            MSG_FORBIDDEN);
    }

    /**
     * Failing authorization of tagging Measm with LAF9.
     */
    @Test
    @RunAsClient
    public final void asyncLaf9TagMeasmAuthFail()
        throws InterruptedException, CharacterCodingException {
        Sample laf = new Sample();
        laf.setExtId(foreignExtId);
        final int existingMeasmExtId = 453;
        Measm measm = new Measm();
        measm.setExtId(existingMeasmExtId);
        Tag existingTag = new Tag();
        existingTag.setName(existingNotAssociatedTag);
        measm.setTags(List.of(existingTag));
        laf.setMeasms(List.of(measm));

        JsonObject report = testAsyncLaf9Import(
            laf, foreignExtId, false, true, expectedAttrs);
        assertHasError(
            report.getJsonArray(SAMPLES_KEY).getJsonObject(0)
            .getJsonArray(Sample_.MEASMS).getJsonObject(0)
            .getJsonArray(TAGS_KEY).getJsonObject(0),
            Laf9ImportJob.ERR_AUTHORIZATION_KEY,
            MSG_FORBIDDEN);
    }

    /**
     * Test import with lowercase LAF keywords.
     */
    @Test
    @RunAsClient
    public final void testImportLowercaseKeywords()
        throws InterruptedException, CharacterCodingException {
        final String lafSampleId = randomProbeId();
        final String lowerCaseLAF = String.format(
            laf8Template, lafSampleId, regulation, sampleSpecifId,
            "", measd, measUnit, "").lines().map(line -> {
                    if (line.matches("^\\w+ .*")) {
                        String[] words = line.split(" ");
                        words[0] = words[0].toLowerCase();
                        return String.join(" ", words);
                    }
                    return line;
                }).collect(Collectors.joining("\n"));
        testAsyncLaf8Import(lowerCaseLAF, lafSampleId, true);
    }

    /**
     * Test asynchronous LAF8 import with invalid input.
     */
    @Test
    @RunAsClient
    public final void testAsyncLaf8ImportNoSuccess()
        throws InterruptedException, CharacterCodingException {
        testAsyncLaf8Import("no valid LAF", "", false);
    }

    /**
     * Test asynchronous LAF9 import with invalid input.
     */
    @Test
    @RunAsClient
    public final void testAsyncLaf9ImportNoSuccess()
        throws InterruptedException, CharacterCodingException {
        // Only Arrays of non-null objects should be accepted as list of samples
        final JsonValue jsonString = Json.createValue("test");
        final JsonValue jsonNumber = Json.createValue(1);
        List<JsonValue> invalidLists = List.of(
            JsonValue.NULL,
            jsonString,
            jsonNumber,
            JsonValue.TRUE,
            JsonValue.EMPTY_JSON_OBJECT,
            Json.createArrayBuilder().add(JsonValue.NULL).build()
            /* JSON binding accepts any JSON value as object. See
               https://github.com/eclipse-ee4j/yasson/issues/672
            Json.createArrayBuilder().add(jsonString).build(),
            Json.createArrayBuilder().add(jsonNumber).build(),
            Json.createArrayBuilder().add(JsonValue.TRUE).build(),
            Json.createArrayBuilder().add(JsonValue.EMPTY_JSON_ARRAY).build()
            */
            );
        for (JsonValue invalidList : invalidLists) {
            JsonValue payload = Json.createObjectBuilder()
                .add("files", Json.createObjectBuilder()
                    .add("invalidFile", invalidList))
                .add("measFacilId", mstId)
                .build();
            parseResponse(target
                .path(ASYNC_IMPORT_URL)
                .path("laf9")
                .request()
                .header("X-SHIB-user", BaseTest.testUser)
                .header("X-SHIB-roles", BaseTest.testRoles)
                .post(Entity.entity(payload, MediaType.APPLICATION_JSON)),
                Response.Status.BAD_REQUEST);
        }
    }

    /**
     * Test message localization in asynchronous import.
     */
    @Test
    @RunAsClient
    public final void testAsyncImportProbeI18n()
        throws InterruptedException, CharacterCodingException {
        final String lafSampleId = randomProbeId();
        final String noOprModeLAF = String.format(
            laf8Template, lafSampleId, regulation, sampleSpecifId,
            "", measd, measUnit, "").lines().filter(
                line -> !line.startsWith("MESSPROGRAMM")).collect(
                    Collectors.joining("\n"));

        Map<Locale, String> msgs = Map.of(
            Locale.GERMAN, "Wert nicht gesetzt",
            Locale.US, "No value provided");
        for (Locale locale: msgs.keySet()) {
            JsonObject report = testAsyncLaf8Import(
                locale, noOprModeLAF, "", false);
            assertContains(report, ERRORS_KEY);
            JsonArray errors = report.getJsonObject(ERRORS_KEY)
                .getJsonArray(lafSampleId);
            JsonObject expectedError = Json.createObjectBuilder()
                .add(REPORT_ITEM_KEY_KEY, "validation#probe")
                .add(REPORT_ITEM_VALUE_KEY, "oprModeId")
                .add(REPORT_ITEM_CODE_KEY, msgs.get(locale)).build();
            Assert.assertTrue(
                "Missing error " + expectedError.toString()
                + " in " + errors.toString(),
                errors.contains(expectedError));
        }
    }

    /**
     * Test asynchronous import of a Sample object with attribute conversion.
     */
    @Test
    @RunAsClient
    public final void testAsyncImportProbeImportConfConvert()
        throws InterruptedException, CharacterCodingException {
        final String lafSampleId = randomProbeId();
        testAsyncLaf8Import(
            String.format(
                laf8Template, lafSampleId, "conv", sampleSpecifId,
                "", measd, measUnit, ""),
            lafSampleId,
            true);
    }

    /**
     * Test asynchronous import with attribute transformation in MeasVal.
     */
    @Test
    @RunAsClient
    public final void testAsyncImportMeasValImportConfTransform()
        throws InterruptedException, CharacterCodingException {
        final String lafSampleId = randomProbeId();
        testAsyncLaf8Import(
            String.format(
                laf8Template, lafSampleId, "conv", sampleSpecifId,
                "", "H 3", measUnit, ""),
            lafSampleId,
            true);
    }

    /**
     * Test asynchronous import with attribute conversion
     * in SampleSpecifMeasVal.
     */
    @Test
    @RunAsClient
    public final void testAsyncImportSampleSpecifMeasValImportConfTransform()
        throws InterruptedException, CharacterCodingException {
        final String lafSampleId = randomProbeId();
        testAsyncLaf8Import(
            String.format(
                laf8Template, lafSampleId, "conv", "XX",
                "", measd, measUnit, ""),
            lafSampleId,
            true);
    }

    /**
     * Test asynchronous import including sampling location.
     * Ensure that no false warning about missing sampling location occurs.
     */
    @Test
    @RunAsClient
    public final void testAsyncLaf8ImportGeolocatE()
        throws InterruptedException, CharacterCodingException {
        final String lafSampleId = randomProbeId();
        testAsyncLaf8ImportNoWarnings(
            String.format(
                laf8Template, lafSampleId,
                regulation, sampleSpecifId,
                "P_KOORDINATEN_S 04 \"7.1\" \"50.4\"\n",
                measd, measUnit, ""),
            lafSampleId,
            true);
    }

    /**
     * Test asynchronous import including sampling location.
     * Ensure that no false warning about missing sampling location occurs.
     */
    @Test
    @RunAsClient
    public final void testAsyncLaf9ImportGeolocatE()
        throws InterruptedException, CharacterCodingException {
        final String lafSampleId = randomProbeId();
        Sample laf = prepareLaf9Data();
        laf.setMainSampleId(lafSampleId);

        Geolocat samplingLocation = new Geolocat();
        samplingLocation.setTypeRegulation(TYPE_REGULATION_E);
        Site site = new Site();
        site.setExtId(existingSiteExtId);
        samplingLocation.setSite(site);
        laf.setGeolocats(List.of(samplingLocation));

        // Serialize including empty warnings
        JsonObject expected = JSONBConfig.JSONB.fromJson(
            JSONBConfig.JSONB.toJson(laf), JsonObject.class);

        testAsyncLaf9Import(
            laf, lafSampleId, true, true, expected,
            // Ignore anything completed by server
            Sample_.ID, Sample_.EXT_ID, Sample_.LAST_MOD, Sample_.TREE_MOD,
            Sample_.ENV_DESCRIP_NAME, Sample_.ENV_MEDIUM_ID, OWNER_KEY,
            // Ignore all associations
            Sample_.SAMPLE_SPECIF_MEAS_VALS, Sample_.GEOLOCATS,
            Sample_.COMM_SAMPLES, TAGS_KEY, Sample_.MEASMS);
    }

    /**
     * Test asynchronous import with invalid site.
     */
    @Test
    @RunAsClient
    public final void laf9InvalidSite()
        throws InterruptedException, CharacterCodingException {
        final String lafSampleId = randomProbeId();
        Sample laf = prepareLaf9Data();
        laf.setMainSampleId(lafSampleId);

        Site site = laf.getGeolocats().get(0).getSite();
        site.setAdminUnitId(null);
        site.setLongText("This site misses required attributes");

        testAsyncLaf9Import(laf, lafSampleId, false, true, laf);
    }

    /**
     * Test asynchronous import updating sampling location.
     */
    @Test
    @RunAsClient
    public final void testAsyncImportUpdateGeolocatE()
        throws InterruptedException, CharacterCodingException {
        final String lafSampleId = existingMainSampleId;
        testAsyncLaf8ImportNoWarnings(
            String.format(
                laf8Template, lafSampleId,
                regulation, sampleSpecifId,
                "P_KOORDINATEN_S 04 \"7.1\" \"50.4\"\n",
                measd, measUnit, ""),
            lafSampleId,
            true);
    }

    /**
     * Test asynchronous import including status.
     */
    @Test
    @RunAsClient
    public final void testAsyncImportStatus()
        throws InterruptedException, CharacterCodingException {
        final String lafSampleId = randomProbeId();
        testAsyncLaf8ImportNoWarnings(
            String.format(
                laf8Template, lafSampleId,
                regulation, sampleSpecifId,
                "P_KOORDINATEN_S 04 \"7.1\" \"50.4\"\n",
                measd, measUnit,
                "BEARBEITUNGSSTATUS 1000\n"),
            lafSampleId,
            true);
    }

    /**
     * Test creating new sample with forbidden extId via LAF8.
     */
    @Test
    @RunAsClient
    public final void asyncImportLaf8ForbiddenExtId()
        throws InterruptedException, CharacterCodingException {
        final String lafSampleId = "ZDB123456789012Y";
        JsonObject report = testAsyncLaf8Import(
                String.format(
                    laf8Template, lafSampleId,
                    regulation, sampleSpecifId,
                    "PROBE_ID \"" + lafSampleId + "\"\n",
                    measd, measUnit,
                    "BEARBEITUNGSSTATUS 1000\n"),
                lafSampleId,
                false);
        JsonObject expectedError = Json.createObjectBuilder()
            .add(REPORT_ITEM_KEY_KEY, "validation#probe")
            .add(REPORT_ITEM_VALUE_KEY, Sample_.EXT_ID)
            .add(REPORT_ITEM_CODE_KEY, "ExtId only for LFGB")
            .build();
        MatcherAssert.assertThat(
            report.getJsonObject(ERRORS_KEY).getJsonArray(lafSampleId),
            CoreMatchers.hasItem(expectedError));
    }

    /**
     * Test creating new sample with forbidden extId via LAF9.
     */
    @Test
    @RunAsClient
    public final void asyncImportLaf9ForbiddenExtId()
        throws InterruptedException, CharacterCodingException {
        final String lafSampleId = "ZDB123456789012Y";
        Sample laf = prepareLaf9Data();
        laf.setMainSampleId(lafSampleId);
        laf.setExtId(lafSampleId);
        JsonObject report = testAsyncLaf9Import(
            laf, lafSampleId, false, false, Map.of());
        JsonArray reportSamples = report.getJsonArray(SAMPLES_KEY);
        assertHasError(
            reportSamples.getJsonObject(0),
            Sample_.EXT_ID,
            "ExtId only for LFGB");
    }

    /**
     * Ensure setting status with warning in measVal fails.
     */
    @Test
    @RunAsClient
    public final void asyncImportStatusWarningMeasVal()
        throws InterruptedException, CharacterCodingException {
        final String lafSampleId = randomProbeId();
        testAsyncLaf8Import(
            String.format(
                laf8Template, lafSampleId,
                regulation, sampleSpecifId,
                "P_KOORDINATEN_S 04 \"7.1\" \"50.4\"\n",
                // Measurand does not match measuring method
                "Mangan", measUnit,
                "BEARBEITUNGSSTATUS 1000\n"),
            lafSampleId,
            false);
    }

    /**
     * Test "Zeitbasis" handling in LAF8 import.
     */
    @Test
    @RunAsClient
    public final void testZeitbasis()
        throws InterruptedException, CharacterCodingException {
        testZeitbasis("ZEITBASIS", "\"MESZ\"", false);
        testZeitbasis("ZEITBASIS", "\"INVALID\"", true);
        testZeitbasis("ZEITBASIS_S", "1", false);
        testZeitbasis("ZEITBASIS_S", "0", true);

        // Use default from import_conf
        testZeitbasis("", "", false);
    }

    private void testZeitbasis(
        String lafKey,
        String value,
        boolean expectWarning
    ) throws InterruptedException, CharacterCodingException {
        final String lafSampleId = randomProbeId();
        // Add "ZEITBASIS" attribute to LAF string
        String lafZb = String.format(
            laf8Template,
            lafSampleId,
            regulation,
            sampleSpecifId,
            lafKey + " " + value + "\n",
            measd,
            measUnit,
            "");
        LOG.trace(lafZb);

        JsonArray warnings = testAsyncLaf8Import(lafZb, lafSampleId, true)
            .getJsonObject(WARNINGS_KEY).getJsonArray(lafSampleId);
        LOG.trace(warnings);
        if (expectWarning) {
            JsonObject expectedWarning = Json.createObjectBuilder()
                .add(REPORT_ITEM_KEY_KEY, lafKey)
                .add(REPORT_ITEM_VALUE_KEY, value.replace("\"", ""))
                .add(REPORT_ITEM_CODE_KEY, String.valueOf(
                        StatusCodes.IMP_INVALID_VALUE))
                .build();
            Assert.assertFalse(
                "Missing warning: " + expectedWarning.toString(),
                !warnings.contains(expectedWarning));
        } else {
            for (JsonValue warningVal: warnings) {
                JsonObject warning = (JsonObject) warningVal;
                Assert.assertFalse(
                    "Unexpected warning: " + warning.toString(),
                    warning.getString(REPORT_ITEM_KEY_KEY)
                    .startsWith("ZEITBASIS"));
            }
        }
    }

    private JsonObject testAsyncLaf8ImportNoWarnings(
        String lafData,
        String lafSampleId,
        boolean expectSuccess
    ) throws InterruptedException, CharacterCodingException {
        JsonObject response = testAsyncLaf8Import(
            lafData, lafSampleId, expectSuccess);
        JsonObject warnings = response.getJsonObject(WARNINGS_KEY);
        if (!warnings.isEmpty()) {
            Assert.fail("Unexpected warnings: "
                + warnings.getJsonArray(lafSampleId));
        }
        return response;
    }

    private JsonObject testAsyncLaf8Import(
        String lafData,
        String lafSampleId,
        boolean expectSuccess
    ) throws InterruptedException, CharacterCodingException {
        return testAsyncLaf8Import(
            Locale.US, lafData, lafSampleId, expectSuccess, expectedAttrs);
    }

    private JsonObject testAsyncLaf8Import(
        String lafData,
        String lafSampleId,
        boolean expectSuccess,
        Map<String, JsonValue> verify
    ) throws InterruptedException, CharacterCodingException {
        return testAsyncLaf8Import(
            Locale.US, lafData, lafSampleId, expectSuccess, verify);
    }

    private JsonObject testAsyncLaf8Import(
        Locale locale,
        String lafData,
        String lafSampleId,
        boolean expectSuccess
    ) throws InterruptedException, CharacterCodingException {
        return testAsyncLaf8Import(
            locale, lafData, lafSampleId, expectSuccess, expectedAttrs);
    }

    private JsonObject testAsyncLaf8Import(
        Locale locale,
        String lafData,
        String lafSampleId,
        boolean expectSuccess,
        Map<String, JsonValue> verify
    ) throws InterruptedException, CharacterCodingException {
        final String fileName = "test.laf";

        /* Request asynchronous import */
        var requestJson = new Laf8ImportParameters();
        requestJson.setEncoding(StandardCharsets.UTF_8);
        requestJson.setMeasFacilId(mstId);
        requestJson.setFiles(Map.of(fileName, Base64.getEncoder()
                .encodeToString(lafData.getBytes(StandardCharsets.UTF_8))));

        JsonObject fileReport = runAsyncImport(
            target, "laf", locale, requestJson, expectSuccess)
            .getJsonObject(fileName);
        if (!expectSuccess) {
            return fileReport;
        }
        return checkImportedData(fileReport, verify);
    }

    private JsonObject testAsyncLaf9Import(
        Sample lafData,
        String lafSampleId,
        boolean expectSuccess,
        boolean sparse,
        Sample verify,
        String... ignore
    ) throws InterruptedException, CharacterCodingException {
        return testAsyncLaf9Import(
            lafData, lafSampleId, expectSuccess, sparse,
            JSONBConfig.JSONB.fromJson(
                JSONB_SPARSE.toJson(verify), JsonObject.class),
            ignore);
    }

    private JsonObject testAsyncLaf9Import(
        Sample lafData,
        String lafSampleId,
        boolean expectSuccess,
        boolean sparse,
        Map<String, JsonValue> verify,
        String... ignore
    ) throws InterruptedException, CharacterCodingException {
        final String fileName = "test.json";

        Jsonb jsonb = sparse ? JSONB_SPARSE : JSONBConfig.JSONB;

        /* Request asynchronous import */
        var requestJson = new Laf9ImportParameters();
        requestJson.setMeasFacilId(mstId);
        requestJson.setFiles(Map.of(
                fileName,
                List.of(jsonb.fromJson(
                        jsonb.toJson(lafData), JsonObject.class))));

        JsonObject fileReport = runAsyncImport(
            target, "laf9", Locale.getDefault(), requestJson, expectSuccess)
            .getJsonObject(fileName);
        LOG.tracef("LAF 9 report: %s", fileReport);
        if (!expectSuccess) {
            return fileReport;
        }
        return checkImportedData(fileReport, verify, ignore);
    }

    private JsonObject checkImportedData(
        JsonObject fileReport,
        Map<String, JsonValue> verify,
        String... ignore
    ) {
        // Test if data correctly entered database
        JsonObject importedSample =
            getImportedSample(fileReport, JsonObject.class);
        BaseTest.verify(verify, importedSample, ignore);

        return fileReport;
    }

    private <T> T getImportedSample(JsonObject fileReport, Class<T> type) {
        final int sampleId;
        if (!fileReport.containsKey(SAMPLES_KEY)) {
            sampleId = fileReport.getJsonArray(SAMPLE_IDS_KEY)
                .getJsonNumber(0).intValue();
        } else {
            sampleId = JSONBConfig.JSONB.fromJson(
                fileReport.getJsonArray(SAMPLES_KEY).get(0).toString(),
                Sample.class).getId();
        }
        return getSample(sampleId, type);
    }

    private <T> T getSample(int sampleId, Class<T> type) {
        Response importedSampleResponse = target
            .path("rest/sample/" + sampleId)
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .get();
        return parseResponse(importedSampleResponse, type);
    }

    private String randomProbeId() {
        final int probeIdLength = 16;
        return UUID.randomUUID().toString().substring(0, probeIdLength);
   }

    public static JsonObject runAsyncImport(
        WebTarget target,
        String path,
        Locale locale,
        LafImportParameters<?> requestJson,
        boolean expectSuccess
    ) throws InterruptedException {
        LOG.tracef(
            "Request payload: %s", JSONBConfig.JSONB.toJson(requestJson));
        Response importCreated = target.path(ASYNC_IMPORT_URL)
            .path(path)
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .acceptLanguage(locale)
            .post(Entity.entity(requestJson, MediaType.APPLICATION_JSON));
        String jobId = parseResponse(importCreated, AsyncJobResponse.class)
            .getJobId();

        /* Request status of asynchronous import */
        SyncInvoker statusRequest = target
            .path(ASYNC_IMPORT_URL + "status/" + jobId)
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles);
        JobStatus importStatusObject;
        boolean done = false;
        final Instant waitUntil = Instant.now().plus(Duration.ofMinutes(1));
        final int waitASecond = 1000;
        do {
            Response response = statusRequest.get();
            importStatusObject =
                parseResponse(response, JobStatus.class);
            done = importStatusObject.isDone();
            Assert.assertTrue(
                "Import not done within one minute",
                waitUntil.isAfter(Instant.now()));
            Thread.sleep(waitASecond);
        } while (!done);

        Assert.assertEquals(
            Job.Status.FINISHED.name(),
            importStatusObject.getStatus().name());

        /* Fetch import result report */
        Response reportResponse = target
            .path(ASYNC_IMPORT_URL + "result/" + jobId)
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .get();
        JsonObject report = parseResponse(reportResponse).asJsonObject();

        for (String fileName: requestJson.getFiles().keySet()) {
            assertContains(report, fileName);
            JsonObject fileReport = report.getJsonObject(fileName);

            final String successKey = "success";
            assertContains(fileReport, successKey);
            boolean success = fileReport.getBoolean(successKey);
            if (!expectSuccess) {
                Assert.assertFalse(
                    "Unexpectedly successful import: " + fileReport, success);
                return report;
            }
            Assert.assertTrue(
                "Unsuccessful import: " + fileReport, success);
        }
        return report;
    }

    private Sample prepareLaf9Sample() {
        Sample laf9Template = new Sample();
        laf9Template.setMeasFacilId(mstId);
        laf9Template.setApprLabId(mstId);
        laf9Template.setOprModeId(1);
        laf9Template.setRegulationId(1);
        laf9Template.setSampleMethId(1);
        laf9Template.setIsTest(false);
        laf9Template.setEnvDescripDisplay(envDescrip);
        laf9Template.setSampleStartDate(new Date(0l));
        return laf9Template;
    }

    private Sample prepareLaf9Data() {
        Sample laf9Template = prepareLaf9Sample();

        SampleSpecifMeasVal sampleSpecif = new SampleSpecifMeasVal();
        sampleSpecif.setSampleSpecifId(sampleSpecifId);
        laf9Template.setSampleSpecifMeasVals(List.of(sampleSpecif));

        CommSample commSample = new CommSample();
        commSample.setText("sample comment");
        commSample.setMeasFacilId(mstId);
        laf9Template.setCommSamples(List.of(commSample));

        Site site = new Site();
        site.setAdminUnitId("dA");
        Geolocat geolocat = new Geolocat();
        geolocat.setSite(site);
        geolocat.setTypeRegulation(TYPE_REGULATION_E);
        laf9Template.setGeolocats(List.of(geolocat));

        // Existing tag
        Tag existingTag = new Tag();
        existingTag.setName(existingNotAssociatedTag);
        // New tag
        Tag tag = new Tag();
        tag.setName("test");
        tag.setMeasFacilId(mstId);
        laf9Template.setTags(new ArrayList<>(List.of(existingTag, tag)));

        Measm measm = new Measm();
        measm.setMmtId(mmtId);
        measm.setTags(new ArrayList<>(List.of(tag)));
        laf9Template.setMeasms(new ArrayList<>(List.of(measm)));

        MeasVal measVal = new MeasVal();
        measVal.setMeasdId(1);
        measVal.setMeasUnitId(1);
        measVal.setMeasVal(1d);
        measVal.setError(1f);
        measm.setMeasVals(List.of(measVal));

        StatusProt statusProt = new StatusProt();
        statusProt.setMeasFacilId(mstId);
        statusProt.setStatusMpId(2);
        measm.setStatusProts(new ArrayList<>(List.of(statusProt)));

        CommMeasm commMeasm = new CommMeasm();
        commMeasm.setMeasFacilId(mstId);
        commMeasm.setText("test");
        measm.setCommMeasms(List.of(commMeasm));

        return laf9Template;
    }

    private void assertHasError(JsonObject object, String key, String value) {
        assertHasMessage(ERRORS_KEY, object, key, value);
    }

    private void assertHasMessage(
        String msgLevelKey, JsonObject object, String key, String value
    ) {
        JsonObject expectedErrors = object.getJsonObject(msgLevelKey);
        MatcherAssert.assertThat(expectedErrors.keySet(),
            CoreMatchers.hasItem(key));
        MatcherAssert.assertThat(expectedErrors.getJsonArray(key),
            CoreMatchers.hasItem(Json.createValue(value)));
    }
}
