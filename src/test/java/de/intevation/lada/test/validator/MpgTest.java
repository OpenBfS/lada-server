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
import jakarta.transaction.Transactional;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;

import de.intevation.lada.model.lada.Mpg;
import de.intevation.lada.model.master.SampleSpecif;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Validator;

/**
 * Test validation rules for Mpg objects.
 */
@Transactional
public class MpgTest extends ValidatorBaseTest {

    //Validation keys
    private static final String ENV_DESCRIP_DISPLAY = "envDescripDisplay";
    private static final String SAMPLE_PD_OFFSET = "samplePdOffset";
    private static final String SAMPLE_PD_END_DATE = "samplePdEndDate";
    private static final String SAMPLE_PD_START_DATE = "samplePdStartDate";
    private static final String SAMPLE_SPECIFS_FK_ARRAY = "sampleSpecifs[].id";
    private static final String VALID_END_DATE = "validEndDate";
    private static final String VALID_START_DATE = "validStartDate";

    //Other constants
    private static final String INTERVAL_M = "M";
    private static final String INTERVAL_YEAR = "J";
    private static final String EXISTING_MEAS_FACIL_ID = "06010";
    private static final int EXISTING_SAMPLE_METH_ID = 1;
    private static final int EXISTING_REGULATION_ID = 1;
    private static final int EXISTING_OPR_MODE = 1;
    private static final int DOY_MIN = 1;
    private static final int DOY_MAX = 365;
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

    @Inject
    private Validator<Mpg> validator;

    /**
     * Test mpg objects with valid start and end date below minimum value.
     */
    public void validStartEndDateBelowMin() {
        Mpg mpg = createMinimumValidMpg();
        mpg.setValidStartDate(DOY_MIN - 1);
        mpg.setValidEndDate(DOY_MIN - 1);

        validator.validate(mpg);
        Assert.assertTrue(mpg.hasErrors());
        Assert.assertTrue(mpg.getErrors().containsKey(VALID_START_DATE));
        Assert.assertTrue(mpg.getErrors().containsKey(VALID_END_DATE));
        Assert.assertTrue(mpg.getErrors()
            .get(VALID_START_DATE).contains(StatusCodes.VALUE_OUTSIDE_RANGE));
        Assert.assertTrue(mpg.getErrors()
            .get(VALID_END_DATE).contains(StatusCodes.VALUE_OUTSIDE_RANGE));
    }

    /**
     * Test mpg objects with valid start and end date above maximum value.
     */
    public void validStartEndDateAboveMax() {
        Mpg mpg = createMinimumValidMpg();
        mpg.setValidStartDate(DOY_MAX + 1);
        mpg.setValidEndDate(DOY_MAX + 1);

        validator.validate(mpg);
        Assert.assertTrue(mpg.hasErrors());
        Assert.assertTrue(mpg.getErrors().containsKey(VALID_START_DATE));
        Assert.assertTrue(mpg.getErrors().containsKey(VALID_END_DATE));
        Assert.assertTrue(mpg.getErrors()
            .get(VALID_START_DATE).contains(StatusCodes.VALUE_OUTSIDE_RANGE));
        Assert.assertTrue(mpg.getErrors()
            .get(VALID_END_DATE).contains(StatusCodes.VALUE_OUTSIDE_RANGE));
    }

    /**
     * Test mpg objects with proper valid start and end date.
     */
    public void validMpg() {
        Mpg mpg = createMinimumValidMpg();
        assertNoWarningsOrErrors(mpg);
    }

    /**
     * Test mpg with pdStartDate smaller than valid.
     */
    public void yearIntervalStartDateSmallerThanValid() {
        Mpg mpg = createMinimumValidMpg();
        mpg.setSamplePd(INTERVAL_YEAR);
        mpg.setSamplePdStartDate(PD_1);
        mpg.setSamplePdEndDate(PD_3);
        mpg.setValidStartDate(PD_2);
        mpg.setValidEndDate(PD_2);

        validator.validate(mpg);
        Assert.assertTrue(mpg.hasErrors());
        Assert.assertTrue(mpg.getErrors()
            .containsKey(SAMPLE_PD_START_DATE));
        Assert.assertTrue(mpg.getErrors()
            .get(SAMPLE_PD_START_DATE)
            .contains(String.valueOf(StatusCodes.VALUE_OUTSIDE_RANGE)));
    }

    /**
     * Test mpg with pdEndDate greater than valid end date.
     */
    public void yearIntervalStartDateGreaterThanValid() {
        Mpg mpg = createMinimumValidMpg();
        mpg.setSamplePd(INTERVAL_YEAR);
        mpg.setSamplePdStartDate(PD_3);
        mpg.setSamplePdEndDate(PD_4);
        mpg.setValidStartDate(PD_2);
        mpg.setValidEndDate(PD_2);

        validator.validate(mpg);
        Assert.assertTrue(mpg.hasErrors());
        Assert.assertTrue(mpg.getErrors()
            .containsKey(SAMPLE_PD_START_DATE));
        Assert.assertTrue(mpg.getErrors()
            .get(SAMPLE_PD_START_DATE)
            .contains(String.valueOf(StatusCodes.VALUE_OUTSIDE_RANGE)));
    }

    /**
     * Test mpg with pdEndDate smaller than valid start date.
     */
    public void yearIntervalEndDateSmallerThanValid() {
        Mpg mpg = createMinimumValidMpg();
        mpg.setSamplePd(INTERVAL_YEAR);
        mpg.setSamplePdStartDate(PD_3);
        mpg.setSamplePdEndDate(PD_1);
        mpg.setValidStartDate(PD_2);
        mpg.setValidEndDate(PD_2);

        validator.validate(mpg);
        Assert.assertTrue(mpg.hasErrors());
        Assert.assertTrue(mpg.getErrors()
            .containsKey(SAMPLE_PD_END_DATE));
        Assert.assertTrue(mpg.getErrors()
            .get(SAMPLE_PD_END_DATE)
            .contains(String.valueOf((StatusCodes.VALUE_OUTSIDE_RANGE))));
    }

    /**
     * Test mpg with pdEndDate greater than valid end date.
     */
    public void yearIntervalEndDateGreaterThanValid() {
        Mpg mpg = createMinimumValidMpg();
        mpg.setSamplePd(INTERVAL_YEAR);
        mpg.setSamplePdStartDate(PD_2);
        mpg.setSamplePdEndDate(PD_4);
        mpg.setValidStartDate(PD_1);
        mpg.setValidEndDate(PD_3);

        validator.validate(mpg);
        Assert.assertTrue(mpg.hasErrors());
        Assert.assertTrue(mpg.getErrors()
            .containsKey(SAMPLE_PD_END_DATE));
        Assert.assertTrue(mpg.getErrors()
            .get(SAMPLE_PD_END_DATE)
            .contains(String.valueOf(StatusCodes.VALUE_OUTSIDE_RANGE)));
    }

    /**
     * Test mpg with invalid sample pd offset.
     */
    public void yearIntervalInvalidSamplePdOffset() {
        Mpg mpg = createMinimumValidMpg();
        mpg.setSamplePd(INTERVAL_YEAR);
        mpg.setSamplePdStartDate(PD_1);
        mpg.setSamplePdEndDate(PD_2);
        mpg.setSamplePdOffset(DOY_MAX);
        mpg.setValidStartDate(PD_2);
        mpg.setValidEndDate(PD_3);

        validator.validate(mpg);
        Assert.assertTrue(mpg.hasErrors());
        Assert.assertTrue(mpg.getErrors()
            .containsKey(SAMPLE_PD_OFFSET));
        Assert.assertTrue(mpg.getErrors()
            .get(SAMPLE_PD_OFFSET)
            .contains(String.valueOf(StatusCodes.VALUE_OUTSIDE_RANGE)));
    }

    /**
     * Test mpg with year interval and valid dates.
     */
    public void yearValidInterval() {
        Mpg mpg = createMinimumValidMpg();
        mpg.setSamplePd(INTERVAL_YEAR);
        mpg.setValidStartDate(PD_1);
        mpg.setValidEndDate(PD_4);
        mpg.setSamplePdStartDate(PD_2);
        mpg.setSamplePdEndDate(PD_3);
        mpg.setSamplePdOffset(DOY_MAX - 1);
        validator.validate(mpg);
        assertNoWarningsOrErrors(mpg);
    }

    /**
     * Test mpg with samplePdStartDate below lower limit.
     */
    public void samplePdStartDateBelowLimit() {
        Mpg mpg = createMinimumValidMpg();
        mpg.setSamplePd(INTERVAL_M);
        mpg.setValidStartDate(DOM_MIN);
        mpg.setValidEndDate(DOM_MAX);
        mpg.setSamplePdStartDate(0);
        mpg.setSamplePdEndDate(PD_1);

        validator.validate(mpg);
        Assert.assertTrue(mpg.hasErrors());
        MatcherAssert.assertThat(mpg.getErrors().keySet(),
            CoreMatchers.hasItem(SAMPLE_PD_START_DATE));
        MatcherAssert.assertThat(mpg.getErrors().get(SAMPLE_PD_START_DATE),
            CoreMatchers.hasItem(GREATER_OR_EQUAL_ONE));
    }

    /**
     * Test mpg with samplePdEndDate below lower limit.
     */
    public void samplePdEndDateBelowLimit() {
        Mpg mpg = createMinimumValidMpg();
        mpg.setSamplePd(INTERVAL_M);
        mpg.setValidStartDate(DOM_MIN);
        mpg.setValidEndDate(DOM_MAX);
        mpg.setSamplePdStartDate(PD_1);
        mpg.setSamplePdEndDate(0);

        validator.validate(mpg);
        Assert.assertTrue(mpg.hasErrors());
        MatcherAssert.assertThat(mpg.getErrors().keySet(),
            CoreMatchers.hasItem(SAMPLE_PD_END_DATE));
        MatcherAssert.assertThat(mpg.getErrors().get(SAMPLE_PD_END_DATE),
            CoreMatchers.hasItem(GREATER_OR_EQUAL_ONE));
    }

    /**
     * Test mpg with samplePdOffset below lower limit.
     */
    public void samplePdOffsetBelowLimit() {
        Mpg mpg = createMinimumValidMpg();
        mpg.setSamplePd(INTERVAL_M);
        mpg.setValidStartDate(DOM_MIN);
        mpg.setValidEndDate(DOM_MAX);
        mpg.setSamplePdStartDate(PD_1);
        mpg.setSamplePdEndDate(PD_1);
        mpg.setSamplePdOffset(-1);


        validator.validate(mpg);
        Assert.assertTrue(mpg.hasErrors());
        MatcherAssert.assertThat(mpg.getErrors().keySet(),
            CoreMatchers.hasItem(SAMPLE_PD_OFFSET));
        MatcherAssert.assertThat(mpg.getErrors().get(SAMPLE_PD_OFFSET),
            CoreMatchers.hasItem("must be greater than or equal to 0"));
    }

    /**
     * Test mpg with invalid samplePd.
     */
    public void invalidSamplePd() {
        Mpg mpg = createMinimumValidMpg();
        mpg.setSamplePd("X42");

        validator.validate(mpg);
        Assert.assertTrue(mpg.hasErrors());
        final String errorKey = "samplePd";
        MatcherAssert.assertThat(mpg.getErrors().keySet(),
            CoreMatchers.hasItem(errorKey));
        MatcherAssert.assertThat(mpg.getErrors().get(errorKey),
            CoreMatchers.hasItem("must match \"" + Mpg.SAMPLE_PD_REGEX + "\""));
    }

    /**
     * Test mpg with samplePdStartDate greater than interval max.
     */
    public void samplePdStartDateGreaterThanIntervalMax() {
        Mpg mpg = createMinimumValidMpg();
        mpg.setSamplePd(INTERVAL_M);
        mpg.setValidStartDate(DOM_MIN);
        mpg.setValidEndDate(DOM_MAX);
        mpg.setSamplePdStartDate(DOM_MAX + 1);
        mpg.setSamplePdEndDate(DOM_MAX);

        validator.validate(mpg);
        Assert.assertTrue(mpg.hasErrors());
        Assert.assertTrue(mpg.getErrors()
            .containsKey(SAMPLE_PD_START_DATE));
        Assert.assertTrue(mpg.getErrors().get(SAMPLE_PD_START_DATE).contains(
                String.valueOf(StatusCodes.VALUE_OUTSIDE_RANGE)));
    }

    /**
     * Test mpg with samplePdEndDate greater than interval max.
     */
    public void samplePdEndDateGreaterThanIntervalMax() {
        Mpg mpg = createMinimumValidMpg();
        mpg.setSamplePd(INTERVAL_M);
        mpg.setValidStartDate(DOM_MIN);
        mpg.setValidEndDate(DOM_MAX);
        mpg.setSamplePdStartDate(DOM_MIN);
        mpg.setSamplePdEndDate(DOM_MAX + 1);

        validator.validate(mpg);
        Assert.assertTrue(mpg.hasErrors());
        Assert.assertTrue(mpg.getErrors()
            .containsKey(SAMPLE_PD_END_DATE));
        Assert.assertTrue(mpg.getErrors()
            .get(SAMPLE_PD_END_DATE)
            .contains(String.valueOf(StatusCodes.VALUE_OUTSIDE_RANGE)));
    }

    /**
     * Test mpg with samplePdOffset greater than interval max.
     */
    public void samplePdOffsetGreaterThanIntervalMax() {
        Mpg mpg = createMinimumValidMpg();
        mpg.setSamplePd(INTERVAL_M);
        mpg.setValidStartDate(DOM_MIN);
        mpg.setValidEndDate(DOM_MAX);
            mpg.setSamplePdStartDate(DOM_MIN);
        mpg.setSamplePdEndDate(DOM_MAX);
        mpg.setSamplePdOffset(DOM_MAX + 1);

        validator.validate(mpg);
        Assert.assertTrue(mpg.hasErrors());
        Assert.assertTrue(mpg.getErrors()
            .containsKey(SAMPLE_PD_OFFSET));
        Assert.assertTrue(mpg.getErrors()
            .get(SAMPLE_PD_OFFSET)
            .contains(String.valueOf(StatusCodes.VALUE_OUTSIDE_RANGE)));
    }

    /**
     * Test mpg with samplePdStart date greater than samplePdEnd.
     */
    public void samplePdStartGreaterThanEnd() {
        Mpg mpg = createMinimumValidMpg();
        mpg.setSamplePd(INTERVAL_M);
        mpg.setValidStartDate(DOM_MIN);
        mpg.setValidEndDate(DOM_MAX);
        mpg.setSamplePdStartDate(PD_4);
        mpg.setSamplePdEndDate(PD_1);

        validator.validate(mpg);
        Assert.assertTrue(mpg.hasErrors());

        Assert.assertTrue(mpg.getErrors()
            .containsKey(SAMPLE_PD_START_DATE));
        Assert.assertTrue(mpg.getErrors()
            .get(SAMPLE_PD_START_DATE)
            .contains(String.valueOf(StatusCodes.DATE_BEGIN_AFTER_END)));

        Assert.assertTrue(mpg.getErrors()
            .containsKey(SAMPLE_PD_END_DATE));
        Assert.assertTrue(mpg.getErrors()
            .get(SAMPLE_PD_END_DATE)
            .contains(String.valueOf(StatusCodes.DATE_BEGIN_AFTER_END)));
    }

    /**
     * Test mpg with invalid sampleSpecif.
     */
    public void sampleSpecifDoesNotExist() {
        SampleSpecif spec = new SampleSpecif();
        final String specId = "A99";
        spec.setId(specId);
        spec.setName("name");
        spec.setExtId("A");
        Mpg mpg = createMinimumValidMpg();
        mpg.setSampleSpecifs(Set.of(spec));

        validator.validate(mpg);
        Assert.assertTrue(mpg.hasErrors());
        MatcherAssert.assertThat(mpg.getErrors().keySet(),
            CoreMatchers.hasItem(SAMPLE_SPECIFS_FK_ARRAY));
        MatcherAssert.assertThat(mpg.getErrors().get(SAMPLE_SPECIFS_FK_ARRAY),
            CoreMatchers.hasItem("'" + specId + "' is no valid primary key"));
    }

    /**
     * Test mpg with sampleSpecif.
     */
    public void sampleSpecifDoesExist() {
        SampleSpecif spec = new SampleSpecif();
        spec.setId("A42");
        spec.setName("name");
        spec.setExtId("A");
        Mpg mpg = createMinimumValidMpg();
        mpg.setSampleSpecifs(Set.of(spec));
        validator.validate(mpg);
        assertNoWarningsOrErrors(mpg);
    }

    /**
     * Test mpg with invalid envDescripDisplay.
     */
    public void envDescripDisplayInvalidDisplayString() {
        Mpg mpg = createMinimumValidMpg();
        mpg.setEnvMediumId("L42");
        mpg.setEnvDescripDisplay("77 88 99 00");

        validator.validate(mpg);
        Assert.assertTrue(mpg.hasErrors());
        MatcherAssert.assertThat(mpg.getErrors().keySet(),
            CoreMatchers.hasItem(ENV_DESCRIP_DISPLAY));
        MatcherAssert.assertThat(mpg.getErrors().get(ENV_DESCRIP_DISPLAY),
            CoreMatchers.hasItem("must match \"D:( [0-9][0-9]){12}\""));
    }

    /**
     * Test mpg without matching envMediumId.
     */
    public void envDescripWithoutMatchingEnvMediumId() {
        Mpg mpg = createMinimumValidMpg();
        mpg.setEnvDescripDisplay(EXAMPLE_ENV_DESCRIP_FROM_SAMPLE_DATA);
        mpg.setEnvMediumId("L54");

        String warningKey = "envMediumId";
        validator.validate(mpg);
        Assert.assertTrue(mpg.hasWarnings());
        Assert.assertTrue(mpg.getWarnings()
            .containsKey(warningKey));
        Assert.assertTrue(mpg.getWarnings().get(warningKey)
            .contains(String.valueOf(StatusCodes.VALUE_NOT_MATCHING)));
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
        mpg.setSamplePd(INTERVAL_YEAR);
        mpg.setSamplePdStartDate(DOY_MIN);
        mpg.setSamplePdEndDate(DOY_MAX);
        mpg.setValidStartDate(DOY_MIN);
        mpg.setValidEndDate(DOY_MAX);
        mpg.setEnvDescripDisplay(EXAMPLE_ENV_DESCRIP_FROM_SAMPLE_DATA);
        mpg.setEnvMediumId("N71");
        return mpg;
    }
}
