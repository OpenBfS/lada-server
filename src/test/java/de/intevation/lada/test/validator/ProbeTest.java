/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.validator;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.ResourceBundle;

import jakarta.inject.Inject;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Test;

import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.lada.Sample_;
import de.intevation.lada.util.data.EnvMedia;
import de.intevation.lada.util.data.Repository;


/**
 * Test sample validations.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class ProbeTest extends ValidatorBaseTest {

    //Validation keys
    private static final String GEOLOCATS = "geolocats";

    //Test data ids
    private static final String ENV_MEDIUM_L42 = "L42";
    private static final String ENV_MEDIUM_L54 = "L54";
    private static final String ENV_MEDIUM_N71 = "N71";
    private static final Integer EXAMPLE_REI_AG_GR_ID = 1;
    private static final Integer EXAMPLE_NUCL_FACIL_ID = 1;
    private static final String MST_06010 = "06010";
    private static final Integer REGULATION_ID_REI = 4;
    private static final Integer SAMPLE_METH_ID_INDIVIDUAL = 1;
    private static final Integer SAMPLE_METH_ID_CONT = 9;
    private static final String EXISTING_APPR_LAB_ID = MST_06010;
    private static final int EXISTING_OPR_MODE = 1;

    //Other constants
    private static final long TS1 = 1376287046510L;
    private static final long TS2 = 1376287046511L;
    private static final long TS3 = 2376287046511L;
    private static final int ID710 = 710;
    private static final int ID1000 = 1000;

    private static final String EXISTING_MAIN_SAMPLE_ID = "120510002";
    private static final String NEW_MAIN_SAMPLE_ID = "4564567890";
    private static final String VALID_ENV_DESCRIP_DISPLAY_FOR_N71
        = "D: 10 11 12 00 00 00 00 00 00 00 00 00";

    @Inject
    private Repository repository;

    //Expected validation messages
    private static final String MSG_NOT_NULL = "No value provided";
    private static final String MSG_NOT_BLANK = "No value provided";
    private static final String MSG_VALUE_MISSING = "A value must be provided";
    private static final String MSG_MULTIPLE_U_SITE =
        "Only one single site of origin should be given";
    private static final String MSG_NO_SAMPLING_LOC =
        "A sampling location must be provided";
    private static final String MSG_S1_NOT_SET = "At least S1 must be set";
    private static final String MSG_ENV_NOT_MATCHING =
        "Environment description does not match environmental medium";
    private static final String MSG_INVALID_DESC_TPL =
        "Invalid descriptor combination: %s does not match";
    private static final String UNIQUE_PLACEHOLDER = "{fields}";
    private final String valMessageUniqueMainSampleIdMeasFacilId;
    private final String valMessageUniqueExtId;

    /**
     * Constructor.
     */
    public ProbeTest() {
        ResourceBundle validationMessages
            = ResourceBundle.getBundle("ValidationMessages");
        String uniquePattern = validationMessages.getString(
            "de.intevation.lada.validation.constraints.Unique.message");
        valMessageUniqueMainSampleIdMeasFacilId = uniquePattern
            .replace(UNIQUE_PLACEHOLDER, "[mainSampleId, isTest, measFacilId]");
        valMessageUniqueExtId = uniquePattern
            .replace(UNIQUE_PLACEHOLDER, "[extId]");
    }

    /**
     * Test hauptprobennr.
     */
    @Test
    public void validSample() {
        Sample sample = createMinimumValidSample();
        validator.validate(sample);
        assertNoMessages(sample);
    }

    /**
     * Test REI sample with R type geolocat.
     */
    @Test
    public void validReiSample() {
        Sample sample = createMinimumValidREISample();
        validator.validate(sample);
        assertNoMessages(sample);
    }

    /**
     * Test no hauptprobennr.
     */
    @Test
    public void hasNoHauptprobenNr() {
        Sample sample = createMinimumValidSample();
        sample.setMainSampleId(null);

        assertHasNotifications(
            validator.validate(sample),
            Sample_.MAIN_SAMPLE_ID,
            MSG_NOT_BLANK);
    }

    /**
     * Test new existing hpnr.
     */
    @Test
    public void existingHauptprobenNrNew() {
        Sample sample = createMinimumValidSample();
        sample.setId(null);
        sample.setMainSampleId(EXISTING_MAIN_SAMPLE_ID);

        validator.validate(sample);
        Assert.assertTrue(sample.hasErrors());
        Assert.assertTrue(sample.getErrors()
            .containsKey(Sample_.MAIN_SAMPLE_ID));
        MatcherAssert.assertThat(
            sample.getErrors().get(Sample_.MAIN_SAMPLE_ID),
            CoreMatchers.hasItem(valMessageUniqueMainSampleIdMeasFacilId));
    }

    /**
     * Test new unique hpnr.
     */
    @Test
    public void uniqueHauptprobenNrNew() {
        Sample sample = createMinimumValidSample();
        sample.setId(null);
        sample.setMainSampleId(NEW_MAIN_SAMPLE_ID);

        assertHasWarnings(
            validator.validate(sample),
            GEOLOCATS,
            MSG_NO_SAMPLING_LOC);
    }

    /**
     * Test update unique hpnr.
     */
    @Test
    public void uniqueHauptprobenNrUpdate() {
        Sample sample = createMinimumValidSample();
        sample.setMainSampleId(NEW_MAIN_SAMPLE_ID);

        validator.validate(sample);
        assertNoMessages(sample);
    }

    /**
     * Test update of existing hpnr..
     */
    @Test
    public void existingHauptprobenNrUpdate() {
        Sample sample = createMinimumValidSample();
        sample.setMainSampleId(EXISTING_MAIN_SAMPLE_ID);

        validator.validate(sample);
        Assert.assertTrue(sample.hasErrors());
        Assert.assertTrue(sample.getErrors()
            .containsKey(Sample_.MAIN_SAMPLE_ID));
        MatcherAssert.assertThat(
            sample.getErrors().get(Sample_.MAIN_SAMPLE_ID),
            CoreMatchers.hasItem(valMessageUniqueMainSampleIdMeasFacilId));
    }

    /**
     * Test entnahmeort.
     */
    @Test
    public void hasEntnahmeOrt() {
        Sample sample = createMinimumValidSample();
        sample.setIsTest(false);

        validator.validate(sample);
        assertNoMessages(sample);
    }

    /**
     * Test no entnahmeort.
     */
    @Test
    public void hasNoEntnahmeOrt() {
        Sample sample = createMinimumValidSample();
        sample.setId(ID710);
        sample.setIsTest(false);

        assertHasWarnings(
            validator.validate(sample),
            GEOLOCATS,
            MSG_NO_SAMPLING_LOC);
    }

    /**
     * Non-unique site of origin: geolocats with type U and R.
     */
    @Test
    public void hasGeolocatUAndR() {
        final int sampleId = 3000;
        Sample sample = repository.getById(Sample.class, sampleId);

        assertHasWarnings(
            validator.validate(sample),
            GEOLOCATS,
            MSG_MULTIPLE_U_SITE);
    }

    /**
     * Non-unique site of origin: two geolocats with type U.
     */
    @Test
    public void hasGeolocatTwoTimesU() {
        final int sampleId = 2000;
        Sample sample = repository.getById(Sample.class, sampleId);

        assertHasWarnings(
            validator.validate(sample),
            GEOLOCATS,
            MSG_MULTIPLE_U_SITE);
    }


    /**
     * Test probenahmebegin.
     */
    @Test
    public void hasProbeentnahmeBegin() {
        Sample sample = createMinimumValidSample();
        sample.setSampleStartDate(new Timestamp(TS1));
        sample.setSampleEndDate(new Timestamp(TS2));
        sample.setIsTest(false);

        validator.validate(sample);
        assertNoMessages(sample);
    }

    /**
     * Test no probenahme begin.
     */
    @Test
    public void hasNoProbeentnahmeBegin() {
        Sample sample = createMinimumValidSample();
        sample.setSampleStartDate(null);

        assertHasWarnings(
            validator.validate(sample),
            Sample_.SAMPLE_START_DATE,
            MSG_NOT_NULL);
    }

    /**
     * Test probenahme begin without end.
     */
    @Test
    public void timeNoEndProbeentnahmeBegin() {
        Sample sample = createMinimumValidSample();
        sample.setSampleStartDate(new Timestamp(TS1));
        sample.setSampleEndDate(null);

        assertHasWarnings(
            validator.validate(sample),
            Sample_.SAMPLE_END_DATE,
            MSG_VALUE_MISSING);
    }

    /**
     * Test probenahme begin without end.
     */
    @Test
    public void timeNoEndProbeentnahmeBeginSampleMethS() {
        Sample sample = createMinimumValidSample();
        final Integer sampleMethIdS = 3;
        sample.setSampleMethId(sampleMethIdS);
        sample.setSampleStartDate(new Timestamp(TS1));
        sample.setSampleEndDate(null);

        assertHasWarnings(
            validator.validate(sample),
            Sample_.SAMPLE_END_DATE,
            MSG_VALUE_MISSING);
    }

    /**
     * Test probenahme begin after end.
     */
    @Test
    public void timeBeginAfterEndProbeentnahmeBegin() {
        Sample sample = createMinimumValidSample();
        sample.setSampleStartDate(new Timestamp(TS2));
        sample.setSampleEndDate(new Timestamp(TS1));

        assertHasWarnings(
            validator.validate(sample),
            Sample_.SAMPLE_START_DATE,
            "Begin must be before end");
    }

    /**
     * Test probenahmebegin in future.
     */
    @Test
    public void timeBeginFutureProbeentnahmeBegin() {
        Sample sample = createMinimumValidSample();
        sample.setSampleStartDate(new Timestamp(TS3));

        assertHasWarnings(
            validator.validate(sample),
            Sample_.SAMPLE_START_DATE,
            "must be a date in the past or in the present");
    }

    /**
     * Test no umwelt.
     */
    @Test
    public void hasNoUmwelt() {
        Sample sample = createMinimumValidSample();
        sample.setEnvMediumId(null);

        validator.validate(sample);
        Assert.assertTrue(sample.hasWarnings());
        Assert.assertTrue(sample.getWarnings()
            .containsKey(Sample_.ENV_MEDIUM_ID));
        MatcherAssert.assertThat(
            sample.getWarnings().get(Sample_.ENV_MEDIUM_ID),
            CoreMatchers.hasItem(MSG_NOT_BLANK));
    }

    /**
     * Test empty umwelt.
     */
    @Test
    public void hasEmptyUmwelt() {
        Sample sample = createMinimumValidSample();
        sample.setEnvMediumId("");

        validator.validate(sample);
        Assert.assertTrue(sample.hasErrors());
        Assert.assertTrue(sample.getErrors()
            .containsKey(Sample_.ENV_MEDIUM_ID));
    }

    /**
     * Test sampleMeth validation.
     */
    @Test
    public void peBeginEqualsPeEnd() {
        Instant now = Instant.now();
        Sample sample = createMinimumValidSample();
        sample.setSampleStartDate(Date.from(now));
        sample.setSampleEndDate(Date.from(now));

        validator.validate(sample);
        assertNoMessages(sample);
    }

    /**
     * Test sampleMeth validation.
     */
    @Test
    public void peBeginDoesNotEqualPeEnd() {
        Instant now = Instant.now();
        Instant yesterday = now.minus(1, ChronoUnit.DAYS);

        Sample sample = createMinimumValidREISample();
        sample.setSampleMethId(SAMPLE_METH_ID_INDIVIDUAL);
        sample.setSampleStartDate(Date.from(yesterday));
        sample.setSampleEndDate(Date.from(now));

        assertHasWarnings(
            validator.validate(sample),
            Sample_.SAMPLE_METH_ID,
            "Individual sample expects sample start date = sample end date");
    }

    /**
     * Test orig date is after sample start date.
     */
    @Test
    public void origDateAfterSampleStartDate() {
        Instant now = Instant.now();
        Instant yesterday = now.minus(1, ChronoUnit.DAYS);
        Sample sample = createMinimumValidSample();
        sample.setSampleStartDate(Date.from(yesterday));
        sample.setOrigDate(Date.from(now));

        assertHasWarnings(
            validator.validate(sample),
            Sample_.ORIG_DATE,
            "Time of origin must be before or equal to sampling time");
    }

    /**
     * Test orig date is before sample start date.
     */
    @Test
    public void origDateBeforeSampleStartDate() {
        Instant now = Instant.now();
        Instant yesterday = now.minus(1, ChronoUnit.DAYS);
        Sample sample = createMinimumValidSample();
        sample.setSampleStartDate(Date.from(now));
        sample.setOrigDate(Date.from(yesterday));

        validator.validate(sample);
        assertNoMessages(sample);
    }

    /**
     * Test sample without envDescripDisplay.
     */
    @Test
    public void noEnvDescripDisplay() {
        Sample sample = createMinimumValidSample();
        sample.setEnvDescripDisplay(null);

        assertHasWarnings(
            validator.validate(sample),
            Sample_.ENV_DESCRIP_DISPLAY,
            MSG_NOT_BLANK);
    }

    /**
     * Test envDescripDisplay with no level set.
     */
    @Test
    public void envDescripDisplayNoLevelSet() {
        Sample sample = createMinimumValidSample();
        sample.setEnvDescripDisplay("D: 00 00 00 00 00 00 00 00 00 00 00 00");

        assertHasWarnings(
            validator.validate(sample),
            Sample_.ENV_DESCRIP_DISPLAY,
            MSG_S1_NOT_SET);
    }

    /**
     * Test sample with envDescripDisplay S1 not set.
     */
    @Test
    public void envDescripDisplayS1NotSet() {
        Sample sample = createMinimumValidSample();
        sample.setEnvDescripDisplay("D: 01 00 00 00 00 00 00 00 00 00 00 00");

        assertHasWarnings(
            validator.validate(sample),
            Sample_.ENV_DESCRIP_DISPLAY,
            MSG_S1_NOT_SET);
    }

    /**
     * Test sample with invalid envDescripDisplay.
     */
    @Test
    public void envDescripDisplayInvalidDisplayString() {
        Sample sample = createMinimumValidSample();
        sample.setEnvDescripDisplay("77 88 99 00");

        assertHasErrors(
            validator.validate(sample),
            Sample_.ENV_DESCRIP_DISPLAY,
            "must match \"" + EnvMedia.ENV_DESCRIP_PATTERN + "\"");
    }

    /**
     * Test sample with all parts of envDescripDisplay invalid.
     */
    @Test
    public void envDescripDisplayAllPartsInvalid() {
        Sample sample = createMinimumValidSample();
        sample.setEnvDescripDisplay("D: 77 88 99 00 77 88 99 00 77 88 99 00");

        assertHasWarnings(
            validator.validate(sample),
            Sample_.ENV_DESCRIP_DISPLAY,
            String.format(MSG_INVALID_DESC_TPL, "s00"));
    }

    /**
     * Test sample with invalid parts of envDescripDisplay.
     */
    @Test
    public void envDescripDisplayInvalidParts() {
        Sample sample = createMinimumValidSample();
        sample.setEnvDescripDisplay("D: 10 11 99 00 77 88 99 00 77 88 99 00");

        assertHasWarnings(
            validator.validate(sample),
            Sample_.ENV_DESCRIP_DISPLAY,
            String.format(MSG_INVALID_DESC_TPL, "s02"));
    }

    /**
     * Test §161 sample without matching envMediumId.
     */
    @Test
    public void envDescripWithoutMatchingEnvMediumId161() {
        envDescripWithoutMatchingEnvMediumId(1);
    }

    /**
     * Test §162 sample without matching envMediumId.
     */
    @Test
    public void envDescripWithoutMatchingEnvMediumId162() {
        envDescripWithoutMatchingEnvMediumId(2);
    }

    private void envDescripWithoutMatchingEnvMediumId(int regulationId) {
        Sample sample = createMinimumValidSample();
        sample.setRegulationId(regulationId);
        sample.setEnvDescripDisplay(VALID_ENV_DESCRIP_DISPLAY_FOR_N71);
        sample.setEnvMediumId(ENV_MEDIUM_L54);

        assertHasWarnings(
            validator.validate(sample),
            Sample_.ENV_MEDIUM_ID,
            MSG_ENV_NOT_MATCHING);
    }

    /**
     * Test §161 sample with ambiguous EnvMedium match.
     */
    @Test
    public void envDescripAmbiguouslyMatchingEnvMediumId161() {
        Sample sample = envDescripAmbiguouslyMatchingEnvMediumId(
            ENV_MEDIUM_L54, 1);
        assertHasNotifications(sample, Sample_.ENV_MEDIUM_ID,
            "Environment description ambiguously matches environmental medium");
        // Be sure the expected notification is not accompanied by a warning
        Assert.assertFalse(
            "Sample has unexpected warnings: " + sample.getWarnings().keySet(),
            sample.hasWarnings());
    }

    /**
     * Test §161 sample with no EnvMedium match but matching multiple
     * EnvDescripEnvMediumMps.
     */
    @Test
    public void envDescripAmbiguouslyNonMatchingEnvMediumId161() {
        assertHasWarnings(
            envDescripAmbiguouslyMatchingEnvMediumId(ENV_MEDIUM_N71, 1),
            Sample_.ENV_MEDIUM_ID,
            MSG_ENV_NOT_MATCHING);
    }

    /**
     * Test §162 sample with ambiguous EnvMedium match.
     */
    @Test
    public void envDescripAmbiguouslyMatchingEnvMediumId162() {
        assertHasWarnings(
            envDescripAmbiguouslyMatchingEnvMediumId(ENV_MEDIUM_L54, 2),
            Sample_.ENV_MEDIUM_ID,
            MSG_ENV_NOT_MATCHING);
    }

    private Sample envDescripAmbiguouslyMatchingEnvMediumId(
        String envMediumId, int regulationId
    ) {
        Sample sample = createMinimumValidSample();
        sample.setRegulationId(regulationId);
        sample.setEnvMediumId(envMediumId);

        // EnvMedia.findEnvDescripEnvMediumMps() returns to mappings
        // with given test data:
        sample.setEnvDescripDisplay("D: 02 02 42 00 00 00 00 00 00 00 00 00");

        return validator.validate(sample);
    }

    /**
     * Test sample with sampleEndDate.
     */
    @Test
    public void hasSampleEndDate() {
        Instant now = Instant.now();
        Instant yesterday = now.minus(1, ChronoUnit.DAYS);
        Sample sample = createMinimumValidREISample();
        sample.setSampleStartDate(Date.from(yesterday));
        sample.setSampleEndDate(Date.from(now));

        validator.validate(sample);
        assertNoMessages(sample);
    }

    /**
     * Test REI sample without sample end date.
     */
    @Test
    public void hasNoSampleEndDate() {
        Instant now = Instant.now();
        Sample sample = createMinimumValidREISample();
        sample.setSampleStartDate(Date.from(now));
        sample.setSampleEndDate(null);

        assertHasWarnings(
            validator.validate(sample),
            Sample_.SAMPLE_END_DATE,
            MSG_VALUE_MISSING);
    }

    /**
     * Test REI sample with no R type geolocat.
     */
    @Test
    public void hasNoRTypeGeolocat() {
        Sample sample = createMinimumValidSample();
        sample.setRegulationId(REGULATION_ID_REI);

        assertHasWarnings(
            validator.validate(sample),
            GEOLOCATS,
            MSG_NO_SAMPLING_LOC);
    }

    /**
     * Test sample without REI regulation but REI fields.
     */
    @Test
    public void sampleShouldNotHaveREIData() {
        Sample sample = createMinimumValidSample();
        sample.setReiAgGrId(EXAMPLE_REI_AG_GR_ID);
        sample.setNuclFacilGrId(EXAMPLE_NUCL_FACIL_ID);

        validator.validate(sample);
        final String expectedMsg = "Values do not match";
        assertHasWarnings(sample, Sample_.REI_AG_GR_ID, expectedMsg);
        assertHasWarnings(sample, Sample_.NUCL_FACIL_GR_ID, expectedMsg);
    }

    /**
     * Test sample with REI regulation but without REI fields.
     */
    @Test
    public void sampleShouldHaveREIData() {
        Sample sample = createMinimumValidSample();
        sample.setRegulationId(REGULATION_ID_REI);

        validator.validate(sample);
        assertHasWarnings(sample, Sample_.REI_AG_GR_ID, MSG_VALUE_MISSING);
        assertHasWarnings(sample, Sample_.NUCL_FACIL_GR_ID, MSG_VALUE_MISSING);
    }

    /**
     * Test REI sample without matching envMedium.
     */
    @Test
    public void reiSampleWithoutMatchingEnvMedium() {
        Sample sample = createMinimumValidREISample();
        sample.setEnvMediumId(ENV_MEDIUM_L42);

        assertHasWarnings(
            validator.validate(sample),
            Sample_.ENV_MEDIUM_ID,
            "Environmental medium does not match ReiAgGr");
    }

    /**
     * Test sample with samplespecif with matching env medium.
     */
    @Test
    public void sampleSpecifMesValWithMatchingEnvMedium() {
        Sample sample = createMinimumValidSample();
        sample.setId(ID1000);
        validator.validate(sample);
        assertNoMessages(sample);
    }

    /**
     * Test sample without a unique extId.
     */
    @Test
    public void noUniqueExtId() {
        Sample sample = createMinimumValidSample();
        sample.setExtId("sample_ext_id");

        validator.validate(sample);
        Assert.assertTrue(sample.hasErrors());
        Assert.assertTrue(sample.getErrors()
            .containsKey(Sample_.EXT_ID));
        MatcherAssert.assertThat(
            sample.getErrors().get(Sample_.EXT_ID),
            CoreMatchers.hasItem(valMessageUniqueExtId));
    }

    /**
     * Test sample with unique ext id.
     */
    @Test
    public void uniqueExtId() {
        Sample sample = createMinimumValidSample();
        sample.setExtId("SomethingUnique");

        validator.validate(sample);
        assertNoMessages(sample);
    }

    /**
     * Create a minimum valid sample.
     * @return sample
     */
    private Sample createMinimumValidSample() {
        final int sampleId = 25000;
        final int regulationId = 1;
        Sample sample = new Sample();
        sample.setId(sampleId);
        sample.setMainSampleId("test");
        sample.setApprLabId(EXISTING_APPR_LAB_ID);
        sample.setMeasFacilId(MST_06010);
        sample.setOprModeId(EXISTING_OPR_MODE);
        sample.setEnvDescripDisplay(VALID_ENV_DESCRIP_DISPLAY_FOR_N71);
        sample.setEnvMediumId(ENV_MEDIUM_N71);
        sample.setSampleStartDate(new Date());
        sample.setSampleEndDate(new Date());
        sample.setRegulationId(regulationId);
        sample.setSampleMethId(SAMPLE_METH_ID_CONT);
        sample.setIsTest(false);
        return sample;
    }

    /**
     * Create a minimum valid rei sample.
     * @return sample
     */
    private Sample createMinimumValidREISample() {
        final int sampleId = 25001;
        Sample sample = createMinimumValidSample();
        sample.setId(sampleId);
        sample.setRegulationId(REGULATION_ID_REI);
        sample.setReiAgGrId(EXAMPLE_REI_AG_GR_ID);
        sample.setNuclFacilGrId(EXAMPLE_NUCL_FACIL_ID);
        return sample;
    }
}
