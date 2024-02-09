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
import jakarta.transaction.Transactional;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;

import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Validator;

/**
 * Test sample validations.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Transactional
public class ProbeTest extends ValidatorBaseTest {

    //Validation keys
    private static final String ENTNAHME_ORT = "geolocats";
    private static final String ENV_DESCRIP_DISPLAY = "envDescripDisplay";
    private static final String ENV_MEDIUM_ID = "envMediumId";
    private static final String EXT_ID = "extId";
    private static final String GEOLOCAT_R = "REIMesspunkt";
    private static final String MAIN_SAMPLE_ID = "mainSampleId";
    private static final String NUCL_FACIL_GR_ID = "nuclFacilGrId";
    private static final String ORIG_DATE = "origDate";
    private static final String REI_AG_GR_ID = "reiAgGrId";
    private static final String SAMPLE_START_DATE = "sampleStartDate";
    private static final String SAMPLE_END_DATE = "sampleEndDate";
    private static final String SAMPLE_SPECIF_MEAS_VAL
        = "sampleSpecifMeasVals";
    private static final String SAMPLE_METH_ID = "sampleMethId";

    //Test data ids
    private static final String ENV_MEDIUM_L42 = "L42";
    private static final String ENV_MEDIUM_N71 = "N71";
    private static final Integer EXAMPLE_REI_AG_GR_ID = 1;
    private static final Integer EXAMPLE_NUCL_FACIL_ID = 1;
    private static final String MST_06010 = "06010";
    private static final Integer REGULATION_ID_161 = 1;
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

    private static final int SAMPLE_ID = 25000;
    private static final int SAMPLE_ID_REI = 25001;

    private static final String EXISTING_MAIN_SAMPLE_ID = "120510002";
    private static final String NEW_MAIN_SAMPLE_ID = "4564567890";
    private static final String VALID_ENV_DESCRIP_DISPLAY_FOR_N71
        = "D: 10 11 12 00 00 00 00 00 00 00 00 00";

    @Inject
    private Validator<Sample> validator;

    //Expected validation messages
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
    public void validSample() {
        Sample sample = createMinimumValidSample();
        validator.validate(sample);
        assertNoWarningsOrErrors(sample);
    }

    /**
     * Test REI sample with R type geolocat.
     */
    public void validReiSample() {
        Sample sample = createMinimumValidREISample();
        validator.validate(sample);
        assertNoWarningsOrErrors(sample);
    }
    /**
     * Test no hauptprobennr.
     */
    public void hasNoHauptprobenNr() {
        Sample sample = createMinimumValidSample();
        sample.setMainSampleId(null);

        validator.validate(sample);
        Assert.assertTrue(sample.hasNotifications());
        Assert.assertTrue(sample.getNotifications()
            .containsKey(MAIN_SAMPLE_ID));
        Assert.assertTrue(
            sample.getNotifications().get(MAIN_SAMPLE_ID).contains(
                StatusCodes.VALUE_MISSING));
    }

    /**
     * Test new existing hpnr.
     */
    public void existingHauptprobenNrNew() {
        Sample sample = createMinimumValidSample();
        sample.setId(null);
        sample.setMainSampleId(EXISTING_MAIN_SAMPLE_ID);

        validator.validate(sample);
        Assert.assertTrue(sample.hasErrors());
        Assert.assertTrue(sample.getErrors().containsKey(MAIN_SAMPLE_ID));
        MatcherAssert.assertThat(
            sample.getErrors().get(MAIN_SAMPLE_ID),
            CoreMatchers.hasItem(valMessageUniqueMainSampleIdMeasFacilId));
    }

    /**
     * Test new unique hpnr.
     */
    public void uniqueHauptprobenNrNew() {
        Sample sample = createMinimumValidSample();
        sample.setId(null);
        sample.setMainSampleId(NEW_MAIN_SAMPLE_ID);

        validator.validate(sample);
        assertNoWarningsOrErrors(sample);
    }

    /**
     * Test update unique hpnr.
     */
    public void uniqueHauptprobenNrUpdate() {
        Sample sample = createMinimumValidSample();
        sample.setId(SAMPLE_ID);
        sample.setMainSampleId(NEW_MAIN_SAMPLE_ID);

        validator.validate(sample);
        assertNoWarningsOrErrors(sample);
    }

    /**
     * Test update of existing hpnr..
     */
    public void existingHauptprobenNrUpdate() {
        Sample sample = createMinimumValidSample();
        sample.setId(SAMPLE_ID);
        sample.setMainSampleId(EXISTING_MAIN_SAMPLE_ID);

        validator.validate(sample);
        Assert.assertTrue(sample.hasErrors());
        Assert.assertTrue(sample.getErrors().containsKey(MAIN_SAMPLE_ID));
        MatcherAssert.assertThat(
            sample.getErrors().get(MAIN_SAMPLE_ID),
            CoreMatchers.hasItem(valMessageUniqueMainSampleIdMeasFacilId));
    }

    /**
     * Test entnahmeort.
     */
    public void hasEntnahmeOrt() {
        Sample sample = createMinimumValidSample();
        sample.setIsTest(false);

        validator.validate(sample);
        assertNoWarningsOrErrors(sample);
    }

    /**
     * Test no entnahmeort.
     */
    public void hasNoEntnahmeOrt() {
        Sample sample = createMinimumValidSample();
        sample.setId(ID710);
        sample.setIsTest(false);

        validator.validate(sample);
        Assert.assertTrue(sample.hasWarnings());
        Assert.assertTrue(sample.getWarnings().containsKey(ENTNAHME_ORT));
        Assert.assertTrue(
            sample.getWarnings().get(ENTNAHME_ORT).contains(
                String.valueOf(StatusCodes.VALUE_MISSING)));
    }

    /**
     * Test probenahmebegin.
     */
    public void hasProbeentnahmeBegin() {
        Sample sample = createMinimumValidSample();
        sample.setSampleStartDate(new Timestamp(TS1));
        sample.setSampleEndDate(new Timestamp(TS2));
        sample.setIsTest(false);

        validator.validate(sample);
        assertNoWarningsOrErrors(sample);
    }

    /**
     * Test no probenahme begin.
     */
    public void hasNoProbeentnahmeBegin() {
        Sample sample = createMinimumValidSample();
        sample.setSampleStartDate(null);

        validator.validate(sample);
        Assert.assertTrue(sample.hasWarnings());
        Assert.assertTrue(
            sample.getWarnings().containsKey(SAMPLE_START_DATE));
        Assert.assertTrue(
            sample.getWarnings().get(SAMPLE_START_DATE).contains(
                String.valueOf(StatusCodes.VALUE_MISSING)));
    }

    /**
     * Test probenahme begin without end.
     */
    public void timeNoEndProbeentnahmeBegin() {
        Sample sample = createMinimumValidSample();
        sample.setSampleStartDate(new Timestamp(TS1));
        sample.setSampleEndDate(null);

        validator.validate(sample);
        Assert.assertTrue(
            sample.getWarnings().get(SAMPLE_END_DATE).contains(
                String.valueOf(StatusCodes.VALUE_MISSING)));
    }

    /**
     * Test probenahme begin without begin.
     */
    public void timeNoBeginProbeentnahmeBegin() {
        Sample sample = createMinimumValidSample();
        sample.setSampleStartDate(null);
        sample.setSampleEndDate(new Timestamp(TS1));

        validator.validate(sample);
        Assert.assertTrue(
            sample.getWarnings().get(SAMPLE_START_DATE).contains(
                String.valueOf(StatusCodes.VALUE_MISSING)));
        Assert.assertTrue(
            sample.getWarnings().get(SAMPLE_START_DATE).contains(
                String.valueOf(StatusCodes.DATE_BEGIN_AFTER_END)));
    }

    /**
     * Test probenahme begin after end.
     */
    public void timeBeginAfterEndProbeentnahmeBegin() {
        Sample sample = createMinimumValidSample();
        sample.setSampleStartDate(new Timestamp(TS2));
        sample.setSampleEndDate(new Timestamp(TS1));
        validator.validate(sample);
        Assert.assertTrue(
            sample.getWarnings().get(SAMPLE_START_DATE).contains(
                String.valueOf(StatusCodes.DATE_BEGIN_AFTER_END)));
    }

    /**
     * Test probenahmebegin in future.
     */
    public void timeBeginFutureProbeentnahmeBegin() {
        Sample sample = createMinimumValidSample();
        sample.setSampleStartDate(new Timestamp(TS3));

        validator.validate(sample);
        Assert.assertTrue(
            sample.getWarnings().get(SAMPLE_START_DATE).contains(
                String.valueOf(StatusCodes.DATE_IN_FUTURE)));
    }

    /**
     * Test no umwelt.
     */
    public void hasNoUmwelt() {
        Sample sample = createMinimumValidSample();
        sample.setEnvMediumId(null);

        validator.validate(sample);
        Assert.assertTrue(sample.hasWarnings());
        Assert.assertTrue(sample.getWarnings().containsKey(ENV_MEDIUM_ID));
        MatcherAssert.assertThat(
            sample.getWarnings().get(ENV_MEDIUM_ID),
            CoreMatchers.hasItem("must not be blank"));
    }

    /**
     * Test empty umwelt.
     */
    public void hasEmptyUmwelt() {
        Sample sample = createMinimumValidSample();
        sample.setEnvMediumId("");

        validator.validate(sample);
        Assert.assertTrue(sample.hasErrors());
        Assert.assertTrue(sample.getErrors().containsKey(ENV_MEDIUM_ID));
    }

    /**
     * Test sampleMeth validation.
     */
    public void peBeginEqualsPeEnd() {
        Instant now = Instant.now();
        Sample sample = createMinimumValidSample();
        sample.setSampleStartDate(Date.from(now));
        sample.setSampleEndDate(Date.from(now));

        validator.validate(sample);
        assertNoWarningsOrErrors(sample);
    }

    /**
     * Test sampleMeth validation.
     */
    public void peBeginDoesNotEqualPeEnd() {
        Instant now = Instant.now();
        Instant yesterday = now.minus(1, ChronoUnit.DAYS);

        Sample sample = createMinimumValidREISample();
        sample.setSampleMethId(SAMPLE_METH_ID_INDIVIDUAL);
        sample.setSampleStartDate(Date.from(yesterday));
        sample.setSampleEndDate(Date.from(now));

        validator.validate(sample);
        Assert.assertTrue(sample.hasWarnings());
        Assert.assertTrue(sample.getWarnings().containsKey(SAMPLE_METH_ID));
        Assert.assertTrue(sample.getWarnings().get(SAMPLE_METH_ID).contains(
                String.valueOf(StatusCodes.VAL_SINGLE_DATE)));
    }

    /**
     * Test orig date is after sample start date.
     */
    public void origDateAfterSampleStartDate() {
        Instant now = Instant.now();
        Instant yesterday = now.minus(1, ChronoUnit.DAYS);
        Sample sample = createMinimumValidSample();
        sample.setSampleStartDate(Date.from(yesterday));
        sample.setOrigDate(Date.from(now));

        validator.validate(sample);
        Assert.assertTrue(sample.hasWarnings());
        Assert.assertTrue(sample.getWarnings().containsKey(ORIG_DATE));
        Assert.assertTrue(sample.getWarnings().get(ORIG_DATE).contains(
                String.valueOf(StatusCodes.URSPR_DATE_BEFORE_BEGIN)));
    }

    /**
     * Test orig date is before sample start date.
     */
    public void origDateBeforeSampleStartDate() {
        Instant now = Instant.now();
        Instant yesterday = now.minus(1, ChronoUnit.DAYS);
        Sample sample = createMinimumValidSample();
        sample.setSampleStartDate(Date.from(now));
        sample.setOrigDate(Date.from(yesterday));

        validator.validate(sample);
        assertNoWarningsOrErrors(sample);
    }

    /**
     * Test sample without envDescripDisplay.
     */
    public void noEnvDescripDisplay() {
        Sample sample = createMinimumValidSample();
        sample.setEnvDescripDisplay(null);

        validator.validate(sample);
        Assert.assertTrue(sample.hasWarnings());
        Assert.assertTrue(sample.getWarnings()
            .containsKey(ENV_DESCRIP_DISPLAY));
        Assert.assertTrue(sample.getWarnings().get(ENV_DESCRIP_DISPLAY)
            .contains(String.valueOf(StatusCodes.VALUE_MISSING)));
    }

    /**
     * Test sample with envDescripDisplay S1 not set.
     */
    public void envDescripDisplayS1NotSet() {
        Sample sample = createMinimumValidREISample();
        sample.setEnvDescripDisplay("D: 00 00 00 00 00 00 00 00 00 00 00 00");

        validator.validate(sample);
        Assert.assertTrue(sample.hasWarnings());
        Assert.assertTrue(sample.getWarnings()
            .containsKey(ENV_DESCRIP_DISPLAY));
        Assert.assertTrue(sample.getWarnings().get(ENV_DESCRIP_DISPLAY)
            .contains(String.valueOf(StatusCodes.VAL_S1_NOTSET)));
    }

    /**
     * Test sample with inval envDescripDisplay.
     */
    public void envDescripDisplayInvalidDisplayString() {
        Sample sample = createMinimumValidSample();
        sample.setEnvDescripDisplay("77 88 99 00");

        validator.validate(sample);
        Assert.assertTrue(sample.hasErrors());
        MatcherAssert.assertThat(sample.getErrors().keySet(),
            CoreMatchers.hasItem(ENV_DESCRIP_DISPLAY));
        MatcherAssert.assertThat(sample.getErrors().get(ENV_DESCRIP_DISPLAY),
            CoreMatchers.hasItem("must match \"D:( [0-9][0-9]){12}\""));
    }

    /**
     * Test sample without matching envMediumId.
     */
    public void envDescripWithoutMatchingEnvMediumId() {
        Sample sample = createMinimumValidSample();
        sample.setEnvDescripDisplay(VALID_ENV_DESCRIP_DISPLAY_FOR_N71);
        sample.setEnvMediumId("L54");
        String warningKey = ENV_MEDIUM_ID;
        validator.validate(sample);
        Assert.assertTrue(sample.hasWarnings());
        Assert.assertTrue(sample.getWarnings()
            .containsKey(warningKey));
        Assert.assertTrue(sample.getWarnings().get(warningKey)
            .contains(String.valueOf(StatusCodes.VALUE_NOT_MATCHING)));
    }

    /**
     * Test sample with sampleEndDate.
     */
    public void hasSampleEndDate() {
        Instant now = Instant.now();
        Instant yesterday = now.minus(1, ChronoUnit.DAYS);
        Sample sample = createMinimumValidREISample();
        sample.setSampleStartDate(Date.from(yesterday));
        sample.setSampleEndDate(Date.from(now));

        validator.validate(sample);
        assertNoWarningsOrErrors(sample);
    }

    /**
     * Test sample without sample end date.
     */
    public void hasNoSampleEndDate() {
        Instant now = Instant.now();
        Sample sample = createMinimumValidREISample();
        sample.setSampleStartDate(Date.from(now));
        sample.setSampleEndDate(null);

        validator.validate(sample);
        Assert.assertTrue(sample.hasWarnings());
        Assert.assertTrue(sample.getWarnings()
            .containsKey(SAMPLE_END_DATE));
        Assert.assertTrue(sample.getWarnings().get(SAMPLE_END_DATE)
            .contains(String.valueOf(StatusCodes.VALUE_MISSING)));
    }

    /**
     * Test sample without sample end date.
     */
    public void hasSampleEndDateBeforeBegin() {
        Instant now = Instant.now();
        Instant yesterday = now.minus(1, ChronoUnit.DAYS);
        Sample sample = createMinimumValidSample();
        sample.setSampleStartDate(Date.from(now));
        sample.setSampleEndDate(Date.from(yesterday));

        validator.validate(sample);
        Assert.assertTrue(sample.hasWarnings());
        Assert.assertTrue(sample.getWarnings()
            .containsKey(SAMPLE_END_DATE));
        Assert.assertTrue(sample.getWarnings().get(SAMPLE_END_DATE)
            .contains(String.valueOf(StatusCodes.VALUE_MISSING)));
    }

    /**
     * Test REI sample with no R type geolocat.
     */
    public void hasNoRTypeGeolocat() {
        Sample sample = createMinimumValidSample();
        sample.setRegulationId(REGULATION_ID_REI);

        validator.validate(sample);
        Assert.assertTrue(sample.hasWarnings());
        Assert.assertTrue(sample.getWarnings()
            .containsKey(GEOLOCAT_R));
        Assert.assertTrue(sample.getWarnings().get(GEOLOCAT_R)
            .contains(String.valueOf(StatusCodes.VALUE_MISSING)));
    }

    /**
     * Test sample without REI regulation but REI fields.
     */
    public void sampleShouldNotHaveREIData() {
        Sample sample = createMinimumValidSample();
        sample.setReiAgGrId(EXAMPLE_REI_AG_GR_ID);
        sample.setNuclFacilGrId(EXAMPLE_NUCL_FACIL_ID);

        validator.validate(sample);
        Assert.assertTrue(sample.hasWarnings());
        MatcherAssert.assertThat(
            sample.getWarnings().keySet(),
            CoreMatchers.hasItems(REI_AG_GR_ID, NUCL_FACIL_GR_ID));
        MatcherAssert.assertThat(
            sample.getWarnings().get(REI_AG_GR_ID),
            CoreMatchers.hasItem(
                String.valueOf(StatusCodes.VALUE_NOT_MATCHING)));
        MatcherAssert.assertThat(
            sample.getWarnings().get(NUCL_FACIL_GR_ID),
            CoreMatchers.hasItem(
                String.valueOf(StatusCodes.VALUE_NOT_MATCHING)));
    }

    /**
     * Test sample with REI regulation but without REI fields.
     */
    public void sampleShouldHaveREIData() {
        Sample sample = createMinimumValidSample();
        sample.setRegulationId(REGULATION_ID_REI);

        validator.validate(sample);
        Assert.assertTrue(sample.hasWarnings());
        boolean hasBothWarningKeys =
            sample.getWarnings().containsKey(REI_AG_GR_ID)
            && sample.getWarnings().containsKey(NUCL_FACIL_GR_ID);
        Assert.assertTrue(hasBothWarningKeys);
        boolean hasCorrectWarningValues =
            sample.getWarnings().get(REI_AG_GR_ID).contains(
                String.valueOf(StatusCodes.VALUE_MISSING))
            && sample.getWarnings().get(NUCL_FACIL_GR_ID).contains(
                String.valueOf(StatusCodes.VALUE_MISSING));
        Assert.assertTrue(hasCorrectWarningValues);
    }

    /**
     * Test REI sample without matching envMedium.
     */
    public void reiSampleWithoutMatchingEnvMedium() {
        Sample sample = createMinimumValidREISample();
        sample.setEnvMediumId(ENV_MEDIUM_L42);

        validator.validate(sample);
        Assert.assertTrue(sample.hasWarnings());
        Assert.assertTrue(sample.getWarnings()
            .containsKey(ENV_MEDIUM_ID));
        Assert.assertTrue(sample.getWarnings().get(ENV_MEDIUM_ID)
            .contains(String.valueOf(StatusCodes.VAL_UWB_NOT_MATCHING_REI)));
    }

    /**
     * Test sample with sample specif but without matching env medium.
     */
    public void sampleSpecifMesValWithoutMatchingEnvMedium() {
        Sample sample = createMinimumValidSample();
        sample.setId(ID1000);
        sample.setEnvMediumId(ENV_MEDIUM_L42);

        validator.validate(sample);
        Assert.assertTrue(sample.hasWarnings());
        Assert.assertTrue(sample.getWarnings()
            .containsKey(SAMPLE_SPECIF_MEAS_VAL));
        Assert.assertTrue(sample.getWarnings().get(SAMPLE_SPECIF_MEAS_VAL)
            .contains(String.valueOf(StatusCodes.VAL_PZW)));

    }

    /**
     * Test sample with samplespecif with matching env medium.
     */
    public void sampleSpecifMesValWithMatchingEnvMedium() {
        Sample sample = createMinimumValidSample();
        sample.setId(ID1000);
        validator.validate(sample);
        assertNoWarningsOrErrors(sample);
    }

    /**
     * Test sample without a unique extId.
     */
    public void noUniqueExtId() {
        Sample sample = createMinimumValidSample();
        sample.setExtId("sample_ext_id");

        validator.validate(sample);
        Assert.assertTrue(sample.hasErrors());
        Assert.assertTrue(sample.getErrors()
            .containsKey(EXT_ID));
        MatcherAssert.assertThat(
            sample.getErrors().get(EXT_ID),
            CoreMatchers.hasItem(valMessageUniqueExtId));
    }

    /**
     * Test sample with unique ext id.
     */
    public void uniqueExtId() {
        Sample sample = createMinimumValidSample();
        sample.setExtId("SomethingUnique");

        validator.validate(sample);
        assertNoWarningsOrErrors(sample);
    }

    /**
     * Create a minimum valid sample.
     * @return sample
     */
    private Sample createMinimumValidSample() {
        Sample sample = new Sample();
        sample.setId(SAMPLE_ID);
        sample.setApprLabId(EXISTING_APPR_LAB_ID);
        sample.setMeasFacilId(MST_06010);
        sample.setOprModeId(EXISTING_OPR_MODE);
        sample.setEnvDescripDisplay(VALID_ENV_DESCRIP_DISPLAY_FOR_N71);
        sample.setEnvMediumId(ENV_MEDIUM_N71);
        sample.setSampleStartDate(new Date());
        sample.setSampleEndDate(new Date());
        sample.setRegulationId(REGULATION_ID_161);
        sample.setSampleMethId(SAMPLE_METH_ID_CONT);
        sample.setIsTest(false);
        return sample;
    }

    /**
     * Create a minimum valid rei sample.
     * @return sample
     */
    private Sample createMinimumValidREISample() {
        Sample sample = createMinimumValidSample();
        sample.setId(SAMPLE_ID_REI);
        sample.setRegulationId(REGULATION_ID_REI);
        sample.setReiAgGrId(EXAMPLE_REI_AG_GR_ID);
        sample.setNuclFacilGrId(EXAMPLE_NUCL_FACIL_ID);
        return sample;
    }
}
