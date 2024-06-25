/* Copyright (C) 2023 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.validator;

import java.util.Set;

import jakarta.inject.Inject;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

import de.intevation.lada.model.lada.Mpg;
import de.intevation.lada.model.lada.Mpg_;
import de.intevation.lada.model.master.SampleSpecif;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Validator;

/**
 * Test validation rules for Mpg objects.
 */
public class MpgTest extends ValidatorBaseTest {

    //Validation keys
    private static final String SAMPLE_SPECIFS_FK_ARRAY = "sampleSpecifs[].id";

    //Other constants
    private static final String EXISTING_MEAS_FACIL_ID = "06010";
    private static final int EXISTING_SAMPLE_METH_ID = 1;
    private static final int EXISTING_REGULATION_ID = 1;
    private static final int EXISTING_OPR_MODE = 1;
    private static final int DOM_MIN = 1;
    private static final int DOM_MAX = 31;

    private static final int PD_1 = 1;
    private static final int PD_2 = 2;
    private static final int PD_3 = 3;
    private static final int PD_4 = 4;

    private static final String EXAMPLE_ENV_DESCRIP_FROM_SAMPLE_DATA
        = "D: 10 11 12 00 00 00 00 00 00 00 00 00";

    private static final String GREATER_OR_EQUAL_ONE =
        "must be greater than or equal to 1";
    private static final String VALUE_OUTSIDE_RANGE =
        "Value outside range of validity";

    @Inject
    private Validator<Mpg> validator;

    /**
     * Test mpg objects with valid start and end date below minimum value.
     */
    @Test
    public void validStartEndDateBelowMin() {
        Mpg mpg = createMinimumValidMpg();
        mpg.setValidStartDate(Mpg.DOY_MIN - 1);
        mpg.setValidEndDate(Mpg.DOY_MIN - 1);

        validateStartEndDate(
            mpg, "must be greater than or equal to " + Mpg.DOY_MIN);
    }

    /**
     * Test mpg objects with valid start and end date above maximum value.
     */
    @Test
    public void validStartEndDateAboveMax() {
        Mpg mpg = createMinimumValidMpg();
        mpg.setValidStartDate(Mpg.DOY_MAX + 1);
        mpg.setValidEndDate(Mpg.DOY_MAX + 1);
        validateStartEndDate(
            mpg, "must be less than or equal to " + Mpg.DOY_MAX);
    }

    private void validateStartEndDate(Mpg mpg, String expectedError) {
        validator.validate(mpg);
        assertHasErrors(mpg);
        MatcherAssert.assertThat(mpg.getErrors().keySet(),
            CoreMatchers.hasItems(Mpg_.VALID_START_DATE, Mpg_.VALID_END_DATE));
        MatcherAssert.assertThat(mpg.getErrors().get(Mpg_.VALID_START_DATE),
            CoreMatchers.hasItem(expectedError));
        MatcherAssert.assertThat(mpg.getErrors().get(Mpg_.VALID_END_DATE),
            CoreMatchers.hasItem(expectedError));
    }

    /**
     * Test mpg objects with proper valid start and end date.
     */
    @Test
    public void validMpg() {
        Mpg mpg = createMinimumValidMpg();
        assertNoMessages(mpg);
    }

    /**
     * Test mpg with pdStartDate smaller than valid.
     */
    @Test
    public void yearIntervalStartDateSmallerThanValid() {
        Mpg mpg = createMinimumValidMpg();
        mpg.setSamplePd(Mpg.YEARLY);
        mpg.setSamplePdStartDate(PD_1);
        mpg.setSamplePdEndDate(PD_3);
        mpg.setValidStartDate(PD_2);
        mpg.setValidEndDate(PD_2);

        validateStartDateOutsideRange(mpg);
    }

    /**
     * Test mpg with pdEndDate greater than valid end date.
     */
    @Test
    public void yearIntervalStartDateGreaterThanValid() {
        Mpg mpg = createMinimumValidMpg();
        mpg.setSamplePd(Mpg.YEARLY);
        mpg.setSamplePdStartDate(PD_3);
        mpg.setSamplePdEndDate(PD_4);
        mpg.setValidStartDate(PD_2);
        mpg.setValidEndDate(PD_2);

        validateStartDateOutsideRange(mpg);
    }

    private void validateStartDateOutsideRange(Mpg mpg) {
        validator.validate(mpg);
        assertHasErrors(mpg);
        MatcherAssert.assertThat(mpg.getErrors().keySet(),
            CoreMatchers.hasItem(Mpg_.SAMPLE_PD_START_DATE));
        MatcherAssert.assertThat(mpg.getErrors().get(Mpg_.SAMPLE_PD_START_DATE),
            CoreMatchers.hasItem(VALUE_OUTSIDE_RANGE));
    }

    /**
     * Test mpg with pdEndDate smaller than valid start date.
     */
    @Test
    public void yearIntervalEndDateSmallerThanValid() {
        Mpg mpg = createMinimumValidMpg();
        mpg.setSamplePd(Mpg.YEARLY);
        mpg.setSamplePdEndDate(PD_1);
        mpg.setValidStartDate(PD_2);
        mpg.setValidEndDate(PD_2);

        validateEndDateOutsideRange(mpg);
    }

    /**
     * Test mpg with pdEndDate greater than valid end date.
     */
    @Test
    public void yearIntervalEndDateGreaterThanValid() {
        Mpg mpg = createMinimumValidMpg();
        mpg.setSamplePd(Mpg.YEARLY);
        mpg.setSamplePdStartDate(PD_2);
        mpg.setSamplePdEndDate(PD_4);
        mpg.setValidStartDate(PD_1);
        mpg.setValidEndDate(PD_3);

        validateEndDateOutsideRange(mpg);
    }

    private void validateEndDateOutsideRange(Mpg mpg) {
        validator.validate(mpg);
        assertHasErrors(mpg);
        MatcherAssert.assertThat(mpg.getErrors().keySet(),
            CoreMatchers.hasItem(Mpg_.SAMPLE_PD_END_DATE));
        MatcherAssert.assertThat(mpg.getErrors().get(Mpg_.SAMPLE_PD_END_DATE),
            CoreMatchers.hasItem(VALUE_OUTSIDE_RANGE));
    }

    /**
     * Test mpg with invalid sample pd offset.
     */
    @Test
    public void yearIntervalInvalidSamplePdOffset() {
        Mpg mpg = createMinimumValidMpg();
        mpg.setSamplePd(Mpg.YEARLY);
        mpg.setSamplePdStartDate(PD_1);
        mpg.setSamplePdEndDate(PD_2);
        mpg.setSamplePdOffset(Mpg.DOY_MAX);
        mpg.setValidStartDate(PD_2);
        mpg.setValidEndDate(PD_3);

        validator.validate(mpg);
        assertHasErrors(mpg);
        MatcherAssert.assertThat(mpg.getErrors().keySet(),
            CoreMatchers.hasItem(Mpg_.SAMPLE_PD_OFFSET));
        MatcherAssert.assertThat(mpg.getErrors().get(Mpg_.SAMPLE_PD_OFFSET),
            CoreMatchers.hasItem(VALUE_OUTSIDE_RANGE));
    }

    /**
     * Test mpg with year interval and valid dates.
     */
    @Test
    public void yearValidInterval() {
        Mpg mpg = createMinimumValidMpg();
        mpg.setSamplePd(Mpg.YEARLY);
        mpg.setValidStartDate(PD_1);
        mpg.setValidEndDate(PD_4);
        mpg.setSamplePdStartDate(PD_2);
        mpg.setSamplePdEndDate(PD_3);
        mpg.setSamplePdOffset(Mpg.DOY_MAX - 1);
        validator.validate(mpg);
        assertNoMessages(mpg);
    }

    /**
     * Test mpg with samplePdStartDate below lower limit.
     */
    @Test
    public void samplePdStartDateBelowLimit() {
        Mpg mpg = createMinimumValidMpg();
        mpg.setSamplePd(Mpg.MONTHLY);
        mpg.setValidStartDate(DOM_MIN);
        mpg.setValidEndDate(DOM_MAX);
        mpg.setSamplePdStartDate(0);
        mpg.setSamplePdEndDate(PD_1);

        validator.validate(mpg);
        assertHasErrors(mpg);
        MatcherAssert.assertThat(mpg.getErrors().keySet(),
            CoreMatchers.hasItem(Mpg_.SAMPLE_PD_START_DATE));
        MatcherAssert.assertThat(mpg.getErrors().get(Mpg_.SAMPLE_PD_START_DATE),
            CoreMatchers.hasItem(GREATER_OR_EQUAL_ONE));
    }

    /**
     * Test mpg with samplePdEndDate below lower limit.
     */
    @Test
    public void samplePdEndDateBelowLimit() {
        Mpg mpg = createMinimumValidMpg();
        mpg.setSamplePd(Mpg.MONTHLY);
        mpg.setValidStartDate(DOM_MIN);
        mpg.setValidEndDate(DOM_MAX);
        mpg.setSamplePdStartDate(PD_1);
        mpg.setSamplePdEndDate(0);

        validator.validate(mpg);
        assertHasErrors(mpg);
        MatcherAssert.assertThat(mpg.getErrors().keySet(),
            CoreMatchers.hasItem(Mpg_.SAMPLE_PD_END_DATE));
        MatcherAssert.assertThat(mpg.getErrors().get(Mpg_.SAMPLE_PD_END_DATE),
            CoreMatchers.hasItem(GREATER_OR_EQUAL_ONE));
    }

    /**
     * Test mpg with samplePdOffset below lower limit.
     */
    @Test
    public void samplePdOffsetBelowLimit() {
        Mpg mpg = createMinimumValidMpg();
        mpg.setSamplePd(Mpg.MONTHLY);
        mpg.setValidStartDate(DOM_MIN);
        mpg.setValidEndDate(DOM_MAX);
        mpg.setSamplePdStartDate(PD_1);
        mpg.setSamplePdEndDate(PD_1);
        mpg.setSamplePdOffset(-1);


        validator.validate(mpg);
        assertHasErrors(mpg);
        MatcherAssert.assertThat(mpg.getErrors().keySet(),
            CoreMatchers.hasItem(Mpg_.SAMPLE_PD_OFFSET));
        MatcherAssert.assertThat(mpg.getErrors().get(Mpg_.SAMPLE_PD_OFFSET),
            CoreMatchers.hasItem("must be greater than or equal to 0"));
    }

    /**
     * Test mpg with invalid samplePd.
     */
    @Test
    public void invalidSamplePd() {
        Mpg mpg = createMinimumValidMpg();
        mpg.setSamplePd("X42");

        validator.validate(mpg);
        assertHasErrors(mpg);
        final String errorKey = "samplePd";
        MatcherAssert.assertThat(mpg.getErrors().keySet(),
            CoreMatchers.hasItem(errorKey));
        MatcherAssert.assertThat(mpg.getErrors().get(errorKey),
            CoreMatchers.hasItem("must match \"" + Mpg.SAMPLE_PD_REGEX + "\""));
    }

    /**
     * Test mpg with samplePdStartDate greater than interval max.
     */
    @Test
    public void samplePdStartDateGreaterThanIntervalMax() {
        Mpg mpg = createMinimumValidMpg();
        mpg.setSamplePd(Mpg.MONTHLY);
        mpg.setValidStartDate(DOM_MIN);
        mpg.setValidEndDate(DOM_MAX);
        mpg.setSamplePdStartDate(DOM_MAX + 1);

        validator.validate(mpg);
        assertHasErrors(mpg);
        MatcherAssert.assertThat(mpg.getErrors().keySet(),
            CoreMatchers.hasItem(Mpg_.SAMPLE_PD_START_DATE));
        MatcherAssert.assertThat(mpg.getErrors().get(Mpg_.SAMPLE_PD_START_DATE),
            CoreMatchers.hasItem(VALUE_OUTSIDE_RANGE));
    }

    /**
     * Test mpg with samplePdEndDate greater than interval max.
     */
    @Test
    public void samplePdEndDateGreaterThanIntervalMax() {
        Mpg mpg = createMinimumValidMpg();
        mpg.setSamplePd(Mpg.MONTHLY);
        mpg.setValidStartDate(DOM_MIN);
        mpg.setValidEndDate(DOM_MAX);
        mpg.setSamplePdStartDate(DOM_MIN);
        mpg.setSamplePdEndDate(DOM_MAX + 1);

        validator.validate(mpg);
        assertHasErrors(mpg);
        MatcherAssert.assertThat(mpg.getErrors().keySet(),
            CoreMatchers.hasItem(Mpg_.SAMPLE_PD_END_DATE));
        MatcherAssert.assertThat(mpg.getErrors().get(Mpg_.SAMPLE_PD_END_DATE),
            CoreMatchers.hasItem(VALUE_OUTSIDE_RANGE));
    }

    /**
     * Test mpg with samplePdOffset greater than interval max.
     */
    @Test
    public void samplePdOffsetGreaterThanIntervalMax() {
        Mpg mpg = createMinimumValidMpg();
        mpg.setSamplePd(Mpg.MONTHLY);
        mpg.setValidStartDate(DOM_MIN);
        mpg.setValidEndDate(DOM_MAX);
            mpg.setSamplePdStartDate(DOM_MIN);
        mpg.setSamplePdEndDate(DOM_MAX);
        mpg.setSamplePdOffset(DOM_MAX + 1);

        validator.validate(mpg);
        assertHasErrors(mpg);
        MatcherAssert.assertThat(mpg.getErrors().keySet(),
            CoreMatchers.hasItem(Mpg_.SAMPLE_PD_OFFSET));
        MatcherAssert.assertThat(mpg.getErrors().get(Mpg_.SAMPLE_PD_OFFSET),
            CoreMatchers.hasItem(VALUE_OUTSIDE_RANGE));
    }

    /**
     * Test mpg with samplePdStart date greater than samplePdEnd.
     */
    @Test
    public void samplePdStartGreaterThanEnd() {
        Mpg mpg = createMinimumValidMpg();
        mpg.setSamplePd(Mpg.MONTHLY);
        mpg.setValidStartDate(DOM_MIN);
        mpg.setValidEndDate(DOM_MAX);
        mpg.setSamplePdStartDate(PD_4);
        mpg.setSamplePdEndDate(PD_1);

        validator.validate(mpg);
        assertHasErrors(mpg);

        MatcherAssert.assertThat(mpg.getErrors().keySet(),
            CoreMatchers.hasItems(Mpg_.SAMPLE_PD_START_DATE,
                Mpg_.SAMPLE_PD_END_DATE));
        String errorMsg = "Begin must be before end";
        MatcherAssert.assertThat(mpg.getErrors().get(Mpg_.SAMPLE_PD_START_DATE),
            CoreMatchers.hasItem(errorMsg));
        MatcherAssert.assertThat(mpg.getErrors().get(Mpg_.SAMPLE_PD_END_DATE),
            CoreMatchers.hasItem(errorMsg));
    }

    /**
     * Test mpg with invalid sampleSpecif.
     */
    @Test
    public void sampleSpecifDoesNotExist() {
        SampleSpecif spec = new SampleSpecif();
        final String specId = "A99";
        spec.setId(specId);
        spec.setName("name");
        spec.setExtId("A");
        Mpg mpg = createMinimumValidMpg();
        mpg.setSampleSpecifs(Set.of(spec));

        validator.validate(mpg);
        assertHasErrors(mpg);
        MatcherAssert.assertThat(mpg.getErrors().keySet(),
            CoreMatchers.hasItem(SAMPLE_SPECIFS_FK_ARRAY));
        MatcherAssert.assertThat(mpg.getErrors().get(SAMPLE_SPECIFS_FK_ARRAY),
            CoreMatchers.hasItem("'" + specId + "' is no valid primary key"));
    }

    /**
     * Test mpg with sampleSpecif.
     */
    @Test
    public void sampleSpecifDoesExist() {
        SampleSpecif spec = new SampleSpecif();
        spec.setId("A42");
        spec.setName("name");
        spec.setExtId("A");
        Mpg mpg = createMinimumValidMpg();
        mpg.setSampleSpecifs(Set.of(spec));
        validator.validate(mpg);
        assertNoMessages(mpg);
    }

    /**
     * Test mpg with invalid envDescripDisplay.
     */
    @Test
    public void envDescripDisplayInvalidDisplayString() {
        Mpg mpg = createMinimumValidMpg();
        mpg.setEnvMediumId("L42");
        mpg.setEnvDescripDisplay("77 88 99 00");

        validator.validate(mpg);
        assertHasErrors(mpg);
        MatcherAssert.assertThat(mpg.getErrors().keySet(),
            CoreMatchers.hasItem(Mpg_.ENV_DESCRIP_DISPLAY));
        MatcherAssert.assertThat(mpg.getErrors()
            .get(Mpg_.ENV_DESCRIP_DISPLAY),
            CoreMatchers.hasItem("must match \"D:( [0-9][0-9]){12}\""));
    }

    /**
     * Test mpg without matching envMediumId.
     */
    @Test
    public void envDescripWithoutMatchingEnvMediumId() {
        Mpg mpg = createMinimumValidMpg();
        mpg.setEnvDescripDisplay(EXAMPLE_ENV_DESCRIP_FROM_SAMPLE_DATA);
        mpg.setEnvMediumId("L54");

        assertHasNotifications(
            validator.validate(mpg),
            Mpg_.ENV_MEDIUM_ID,
            String.valueOf(StatusCodes.VALUE_NOT_MATCHING));
    }

    /**
     * Create mpg with a minimum set of fields to be validated.
     * @return Mpg.
     */
    private Mpg createMinimumValidMpg() {
        Mpg mpg = new Mpg();
        mpg.setOprModeId(EXISTING_OPR_MODE);
        mpg.setMeasFacilId(EXISTING_MEAS_FACIL_ID);
        mpg.setApprLabId(EXISTING_MEAS_FACIL_ID);
        mpg.setRegulationId(EXISTING_REGULATION_ID);
        mpg.setSampleMethId(EXISTING_SAMPLE_METH_ID);
        mpg.setSamplePd(Mpg.YEARLY);
        mpg.setSamplePdStartDate(Mpg.DOY_MIN);
        mpg.setSamplePdEndDate(Mpg.DOY_MAX);
        mpg.setValidStartDate(Mpg.DOY_MIN);
        mpg.setValidEndDate(Mpg.DOY_MAX);
        mpg.setEnvDescripDisplay(EXAMPLE_ENV_DESCRIP_FROM_SAMPLE_DATA);
        mpg.setEnvMediumId("N71");
        return mpg;
    }
}
