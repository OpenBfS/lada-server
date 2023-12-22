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

import org.junit.Assert;

import de.intevation.lada.model.lada.Mpg;
import de.intevation.lada.model.master.SampleSpecif;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Validator;
import de.intevation.lada.validation.Violation;

/**
 * Test validation rules for Mpg objects.
 */
@Transactional
public class MpgTest {

    //Validation keys
    private static final String ENV_DESCRIP_DISPLAY = "envDescripDisplay";
    private static final String SAMPLE_PD = "samplePD";
    private static final String SAMPLE_PD_OFFSET = "samplePdOffset";
    private static final String SAMPLE_PD_END_DATE = "samplePdEndDate";
    private static final String SAMPLE_PD_START_DATE = "samplePdStartDate";
    private static final String SAMPLE_SPECIFS = "sampleSpecifs";
    private static final String SAMPLE_SPECIFS_FK_ARRAY = "sampleSpecifs[].id";
    private static final String VALID_END_DATE = "validEndDate";
    private static final String VALID_START_DATE = "validStartDate";

    //Other constants
    private static final String INTERVAL_M = "M";
    private static final String INTERVAL_YEAR = "J";
    private static final String EXISTING_MEAS_FACIL_ID = "06010";
    private static final int EXISTING_SAMPLE_METH_ID = 1;
    private static final int EXISTING_REGULATION_ID = 1;
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
    private static final String EXAMPLE_ENV_DESCRIP_DISPLAY
        = "D: 01 59 03 01 01 02 05 01 02 00 00 00";

    @Inject
    private Validator<Mpg> validator;

    /**
     * Test mpg objects with proper valid start and end date.
     */
    public void validStartEndDate() {
        Mpg mpg = new Mpg();
        mpg.setValidStartDate(DOY_MAX - 1);
        mpg.setValidEndDate(DOY_MAX - 1);

        Violation violation = validator.validate(mpg);
        if (violation.hasErrors()) {
            Assert.assertFalse(violation.getErrors()
                .containsKey(VALID_START_DATE));
            Assert.assertFalse(violation.getErrors()
            .containsKey(VALID_END_DATE));
        }
    }

    /**
     * Test mpg with pdStartDate smaller than valid.
     */
    public void yearIntervalStartDateSmallerThanValid() {
        Mpg mpg = new Mpg();
        mpg.setSamplePd(INTERVAL_YEAR);
        mpg.setSamplePdStartDate(PD_1);
        mpg.setSamplePdEndDate(PD_3);
        mpg.setValidStartDate(PD_2);
        mpg.setValidEndDate(PD_2);

        mpg.setRegulationId(EXISTING_REGULATION_ID);
        mpg.setApprLabId(EXISTING_MEAS_FACIL_ID);
        mpg.setMeasFacilId(EXISTING_MEAS_FACIL_ID);
        mpg.setSampleMethId(EXISTING_SAMPLE_METH_ID);

        Violation violation = validator.validate(mpg);
        Assert.assertTrue(violation.hasErrors());
        Assert.assertTrue(violation.getErrors()
            .containsKey(SAMPLE_PD_START_DATE));
        Assert.assertTrue(violation.getErrors()
            .get(SAMPLE_PD_START_DATE)
            .contains(String.valueOf(StatusCodes.VALUE_OUTSIDE_RANGE)));
    }

    /**
     * Test mpg with pdEndDate greater than valid end date.
     */
    public void yearIntervalStartDateGreaterThanValid() {
        Mpg mpg = new Mpg();
        mpg.setSamplePd(INTERVAL_YEAR);
        mpg.setSamplePdStartDate(PD_3);
        mpg.setSamplePdEndDate(PD_4);
        mpg.setValidStartDate(PD_2);
        mpg.setValidEndDate(PD_2);
        mpg.setRegulationId(EXISTING_REGULATION_ID);
        mpg.setApprLabId(EXISTING_MEAS_FACIL_ID);
        mpg.setMeasFacilId(EXISTING_MEAS_FACIL_ID);
        mpg.setSampleMethId(EXISTING_SAMPLE_METH_ID);

        Violation violation = validator.validate(mpg);
        Assert.assertTrue(violation.hasErrors());
        Assert.assertTrue(violation.getErrors()
            .containsKey(SAMPLE_PD_START_DATE));
        Assert.assertTrue(violation.getErrors()
            .get(SAMPLE_PD_START_DATE)
            .contains(String.valueOf(StatusCodes.VALUE_OUTSIDE_RANGE)));
    }

    /**
     * Test mpg with pdEndDate smaller than valid start date.
     */
    public void yearIntervalEndDateSmallerThanValid() {
        Mpg mpg = new Mpg();
        mpg.setSamplePd(INTERVAL_YEAR);
        mpg.setSamplePdStartDate(PD_3);
        mpg.setSamplePdEndDate(PD_1);
        mpg.setValidStartDate(PD_2);
        mpg.setValidEndDate(PD_2);

        mpg.setRegulationId(EXISTING_REGULATION_ID);
        mpg.setApprLabId(EXISTING_MEAS_FACIL_ID);
        mpg.setMeasFacilId(EXISTING_MEAS_FACIL_ID);
        mpg.setSampleMethId(EXISTING_SAMPLE_METH_ID);

        Violation violation = validator.validate(mpg);
        Assert.assertTrue(violation.hasErrors());
        Assert.assertTrue(violation.getErrors()
            .containsKey(SAMPLE_PD_END_DATE));
        Assert.assertTrue(violation.getErrors()
            .get(SAMPLE_PD_END_DATE)
            .contains(String.valueOf((StatusCodes.VALUE_OUTSIDE_RANGE))));
    }

    /**
     * Test mpg with pdEndDate greater than valid end date.
     */
    public void yearIntervalEndDateGreaterThanValid() {
        Mpg mpg = new Mpg();
        mpg.setSamplePd(INTERVAL_YEAR);
        mpg.setSamplePdStartDate(PD_2);
        mpg.setSamplePdEndDate(PD_4);
        mpg.setValidStartDate(PD_1);
        mpg.setValidEndDate(PD_3);
        mpg.setRegulationId(EXISTING_REGULATION_ID);
        mpg.setApprLabId(EXISTING_MEAS_FACIL_ID);
        mpg.setMeasFacilId(EXISTING_MEAS_FACIL_ID);
        mpg.setSampleMethId(EXISTING_SAMPLE_METH_ID);

        Violation violation = validator.validate(mpg);
        Assert.assertTrue(violation.hasErrors());
        Assert.assertTrue(violation.getErrors()
            .containsKey(SAMPLE_PD_END_DATE));
        Assert.assertTrue(violation.getErrors()
            .get(SAMPLE_PD_END_DATE)
            .contains(String.valueOf(StatusCodes.VALUE_OUTSIDE_RANGE)));
    }

    /**
     * Test mpg with invalid sample pd offset.
     */
    public void yearIntervalInvalidSamplePdOffset() {
        Mpg mpg = new Mpg();
        mpg.setSamplePd(INTERVAL_YEAR);
        mpg.setSamplePdStartDate(PD_1);
        mpg.setSamplePdEndDate(PD_2);
        mpg.setSamplePdOffset(DOY_MAX);
        mpg.setValidStartDate(PD_2);
        mpg.setValidEndDate(PD_3);

        mpg.setRegulationId(EXISTING_REGULATION_ID);
        mpg.setApprLabId(EXISTING_MEAS_FACIL_ID);
        mpg.setMeasFacilId(EXISTING_MEAS_FACIL_ID);
        mpg.setSampleMethId(EXISTING_SAMPLE_METH_ID);

        Violation violation = validator.validate(mpg);
        Assert.assertTrue(violation.hasErrors());
        Assert.assertTrue(violation.getErrors()
            .containsKey(SAMPLE_PD_OFFSET));
        Assert.assertTrue(violation.getErrors()
            .get(SAMPLE_PD_OFFSET)
            .contains(String.valueOf(StatusCodes.VALUE_OUTSIDE_RANGE)));
    }

    /**
     * Test mpg with year interval and valid dates.
     */
    public void yearValidInterval() {
        Mpg mpg = new Mpg();
        mpg.setSamplePd(INTERVAL_YEAR);
        mpg.setValidStartDate(PD_1);
        mpg.setValidEndDate(PD_4);
        mpg.setSamplePdStartDate(PD_2);
        mpg.setSamplePdEndDate(PD_3);
        mpg.setSamplePdOffset(DOY_MAX - 1);
        Violation violation = validator.validate(mpg);
        if (violation.hasErrors()) {
            Assert.assertTrue(
                !violation.getErrors().containsKey(SAMPLE_PD_START_DATE)
                || !violation.getErrors().get(SAMPLE_PD_START_DATE)
                    .contains(String.valueOf(StatusCodes.VALUE_OUTSIDE_RANGE)));
            Assert.assertTrue(
                !violation.getErrors().containsKey(SAMPLE_PD_END_DATE)
                || !violation.getErrors().get(SAMPLE_PD_END_DATE)
                    .contains(String.valueOf(StatusCodes.VALUE_OUTSIDE_RANGE)));
            Assert.assertTrue(
                !violation.getErrors().containsKey(SAMPLE_PD_OFFSET)
                || !violation.getErrors().get(SAMPLE_PD_OFFSET)
                    .contains(String.valueOf(StatusCodes.VALUE_OUTSIDE_RANGE)));
        }
    }

    /**
     * Test mpg with samplePdStartDate below lower limit.
     */
    public void samplePdStartDateBelowLimit() {
        Mpg mpg = new Mpg();
        mpg.setSamplePd(INTERVAL_M);
        mpg.setValidStartDate(DOM_MIN);
        mpg.setValidEndDate(DOM_MAX);
        mpg.setSamplePdStartDate(0);
        mpg.setSamplePdEndDate(PD_1);
        mpg.setRegulationId(EXISTING_REGULATION_ID);
        mpg.setApprLabId(EXISTING_MEAS_FACIL_ID);
        mpg.setMeasFacilId(EXISTING_MEAS_FACIL_ID);
        mpg.setSampleMethId(EXISTING_SAMPLE_METH_ID);

        Violation violation = validator.validate(mpg);
        Assert.assertTrue(violation.hasErrors());
        Assert.assertTrue(violation.getErrors()
            .containsKey(SAMPLE_PD_START_DATE));
        Assert.assertTrue(violation.getErrors()
            .get(SAMPLE_PD_START_DATE)
            .contains(String.valueOf(StatusCodes.VALUE_OUTSIDE_RANGE)));
    }

    /**
     * Test mpg with samplePdEndDate below lower limit.
     */
    public void samplePdEndDateBelowLimit() {
        Mpg mpg = new Mpg();
        mpg.setSamplePd(INTERVAL_M);
        mpg.setValidStartDate(DOM_MIN);
        mpg.setValidEndDate(DOM_MAX);
        mpg.setSamplePdStartDate(PD_1);
        mpg.setSamplePdEndDate(0);
        mpg.setRegulationId(EXISTING_REGULATION_ID);
        mpg.setApprLabId(EXISTING_MEAS_FACIL_ID);
        mpg.setMeasFacilId(EXISTING_MEAS_FACIL_ID);
        mpg.setSampleMethId(EXISTING_SAMPLE_METH_ID);

        Violation violation = validator.validate(mpg);
        Assert.assertTrue(violation.hasErrors());
        Assert.assertTrue(violation.getErrors()
            .containsKey(SAMPLE_PD_END_DATE));
        Assert.assertTrue(violation.getErrors()
            .get(SAMPLE_PD_END_DATE)
            .contains(String.valueOf(StatusCodes.VALUE_OUTSIDE_RANGE)));
    }

    /**
     * Test mpg with samplePdOffset below lower limit.
     */
    public void samplePdOffsetBelowLimit() {
        Mpg mpg = new Mpg();
        mpg.setSamplePd(INTERVAL_M);
        mpg.setValidStartDate(DOM_MIN);
        mpg.setValidEndDate(DOM_MAX);
        mpg.setSamplePdStartDate(PD_1);
        mpg.setSamplePdEndDate(PD_1);
        mpg.setSamplePdOffset(-1);
        mpg.setRegulationId(EXISTING_REGULATION_ID);
        mpg.setApprLabId(EXISTING_MEAS_FACIL_ID);
        mpg.setMeasFacilId(EXISTING_MEAS_FACIL_ID);
        mpg.setSampleMethId(EXISTING_SAMPLE_METH_ID);


        Violation violation = validator.validate(mpg);
        Assert.assertTrue(violation.hasErrors());
        Assert.assertTrue(violation.getErrors()
            .containsKey(SAMPLE_PD_OFFSET));
        Assert.assertTrue(violation.getErrors()
            .get(SAMPLE_PD_OFFSET)
            .contains(String.valueOf(StatusCodes.VALUE_OUTSIDE_RANGE)));
    }

    /**
     * Test mpg with samplePdStartDate greater than interval max.
     */
    public void samplePdStartDateGreaterThanIntervalMax() {
        Mpg mpg = new Mpg();
        mpg.setRegulationId(EXISTING_REGULATION_ID);
        mpg.setApprLabId(EXISTING_MEAS_FACIL_ID);
        mpg.setMeasFacilId(EXISTING_MEAS_FACIL_ID);
        mpg.setSampleMethId(EXISTING_SAMPLE_METH_ID);
        mpg.setSamplePd(INTERVAL_M);
        mpg.setValidStartDate(DOM_MIN);
        mpg.setValidEndDate(DOM_MAX);
        mpg.setSamplePdStartDate(DOM_MAX + 1);
        mpg.setSamplePdEndDate(DOM_MAX);

        Violation violation = validator.validate(mpg);
        Assert.assertTrue(violation.hasErrors());
        Assert.assertTrue(violation.getErrors()
            .containsKey(SAMPLE_PD_START_DATE));
        Assert.assertTrue(violation.getErrors()
            .get(SAMPLE_PD_START_DATE)
            .contains(String.valueOf(StatusCodes.VALUE_OUTSIDE_RANGE)));
    }

    /**
     * Test mpg with samplePdEndDate greater than interval max.
     */
    public void samplePdEndDateGreaterThanIntervalMax() {
        Mpg mpg = new Mpg();
        mpg.setSamplePd(INTERVAL_M);
        mpg.setValidStartDate(DOM_MIN);
        mpg.setValidEndDate(DOM_MAX);
        mpg.setSamplePdStartDate(DOM_MIN);
        mpg.setSamplePdEndDate(DOM_MAX + 1);
        mpg.setRegulationId(EXISTING_REGULATION_ID);
        mpg.setApprLabId(EXISTING_MEAS_FACIL_ID);
        mpg.setMeasFacilId(EXISTING_MEAS_FACIL_ID);
        mpg.setSampleMethId(EXISTING_SAMPLE_METH_ID);

        Violation violation = validator.validate(mpg);
        Assert.assertTrue(violation.hasErrors());
        Assert.assertTrue(violation.getErrors()
            .containsKey(SAMPLE_PD_END_DATE));
        Assert.assertTrue(violation.getErrors()
            .get(SAMPLE_PD_END_DATE)
            .contains(String.valueOf(StatusCodes.VALUE_OUTSIDE_RANGE)));
    }

    /**
     * Test mpg with samplePdOffset greater than interval max.
     */
    public void samplePdOffsetGreaterThanIntervalMax() {
        Mpg mpg = new Mpg();
        mpg.setSamplePd(INTERVAL_M);
        mpg.setValidStartDate(DOM_MIN);
        mpg.setValidEndDate(DOM_MAX);
            mpg.setSamplePdStartDate(DOM_MIN);
        mpg.setSamplePdEndDate(DOM_MAX);
        mpg.setSamplePdOffset(DOM_MAX + 1);
        mpg.setRegulationId(EXISTING_REGULATION_ID);
        mpg.setApprLabId(EXISTING_MEAS_FACIL_ID);
        mpg.setMeasFacilId(EXISTING_MEAS_FACIL_ID);
        mpg.setSampleMethId(EXISTING_SAMPLE_METH_ID);

        Violation violation = validator.validate(mpg);
        Assert.assertTrue(violation.hasErrors());
        Assert.assertTrue(violation.getErrors()
            .containsKey(SAMPLE_PD_OFFSET));
        Assert.assertTrue(violation.getErrors()
            .get(SAMPLE_PD_OFFSET)
            .contains(String.valueOf(StatusCodes.VALUE_OUTSIDE_RANGE)));
    }

    /**
     * Test mpg with samplePdStart date greater than samplePdEnd.
     */
    public void samplePdStartGreaterThanEnd() {
        Mpg mpg = new Mpg();
        mpg.setSamplePd(INTERVAL_M);
        mpg.setValidStartDate(DOM_MIN);
        mpg.setValidEndDate(DOM_MAX);
        mpg.setSamplePdStartDate(PD_4);
        mpg.setSamplePdEndDate(PD_1);
        mpg.setRegulationId(EXISTING_REGULATION_ID);
        mpg.setApprLabId(EXISTING_MEAS_FACIL_ID);
        mpg.setMeasFacilId(EXISTING_MEAS_FACIL_ID);
        mpg.setSampleMethId(EXISTING_SAMPLE_METH_ID);

        Violation violation = validator.validate(mpg);
        Assert.assertTrue(violation.hasErrors());

        Assert.assertTrue(violation.getErrors()
            .containsKey(SAMPLE_PD_START_DATE));
        Assert.assertTrue(violation.getErrors()
            .get(SAMPLE_PD_START_DATE)
            .contains(String.valueOf(StatusCodes.DATE_BEGIN_AFTER_END)));

        Assert.assertTrue(violation.getErrors()
            .containsKey(SAMPLE_PD_END_DATE));
        Assert.assertTrue(violation.getErrors()
            .get(SAMPLE_PD_END_DATE)
            .contains(String.valueOf(StatusCodes.DATE_BEGIN_AFTER_END)));
    }

    /**
     * Test mpg with invalid sampleSpecif.
     */
    public void sampleSpecifDoesNotExist() {
        SampleSpecif spec = new SampleSpecif();
        spec.setId("A99");
        spec.setName("name");
        spec.setExtId("A");
        Mpg mpg = new Mpg();
        mpg.setSampleSpecifs(Set.of(spec));
        mpg.setRegulationId(EXISTING_REGULATION_ID);
        mpg.setApprLabId(EXISTING_MEAS_FACIL_ID);
        mpg.setMeasFacilId(EXISTING_MEAS_FACIL_ID);
        mpg.setSampleMethId(EXISTING_SAMPLE_METH_ID);

        mpg.setSamplePd(INTERVAL_M);
        mpg.setValidStartDate(DOM_MIN);
        mpg.setValidEndDate(DOM_MAX);
        mpg.setSamplePdStartDate(DOM_MIN);
        mpg.setSamplePdEndDate(DOM_MAX);

        Violation violation = validator.validate(mpg);

        Assert.assertTrue(violation.hasErrors());
        Assert.assertTrue(violation.getErrors()
            .containsKey(SAMPLE_SPECIFS_FK_ARRAY));
    }

    /**
     * Test mpg with sampleSpecif.
     */
    public void sampleSpecifDoesExist() {
        SampleSpecif spec = new SampleSpecif();
        spec.setId("A42");
        Mpg mpg = new Mpg();
        mpg.setSampleSpecifs(Set.of(spec));
        mpg.setRegulationId(EXISTING_REGULATION_ID);
        mpg.setApprLabId(EXISTING_MEAS_FACIL_ID);
        mpg.setMeasFacilId(EXISTING_MEAS_FACIL_ID);
        mpg.setSampleMethId(EXISTING_SAMPLE_METH_ID);
        mpg.setSamplePd(INTERVAL_M);
        mpg.setValidStartDate(DOM_MIN);
        mpg.setValidEndDate(DOM_MAX);
        mpg.setSamplePdStartDate(DOM_MIN);
        mpg.setSamplePdEndDate(DOM_MAX);
        Violation violation = validator.validate(mpg);
        if (violation.hasErrors()) {
            Assert.assertTrue(
                !violation.getErrors().containsKey(SAMPLE_SPECIFS)
                || !violation.getErrors().get(SAMPLE_SPECIFS)
                    .contains(String.valueOf(StatusCodes.NOT_EXISTING)));
        }
    }

    /**
     * Test mpg with invalid envDescripDisplay.
     */
    public void envDescripDisplayInvalidDisplayString() {
        Mpg mpg = new Mpg();
        mpg.setEnvMediumId("L42");
        mpg.setEnvDescripDisplay("77 88 99 00");
        mpg.setRegulationId(EXISTING_REGULATION_ID);
        mpg.setApprLabId(EXISTING_MEAS_FACIL_ID);
        mpg.setMeasFacilId(EXISTING_MEAS_FACIL_ID);
        mpg.setSampleMethId(EXISTING_SAMPLE_METH_ID);

        mpg.setSamplePd(INTERVAL_M);
        mpg.setValidStartDate(DOM_MIN);
        mpg.setValidEndDate(DOM_MAX);
        mpg.setSamplePdStartDate(DOM_MIN);
        mpg.setSamplePdEndDate(DOM_MAX);

        Violation violation = validator.validate(mpg);
        Assert.assertTrue(violation.hasErrors());
        Assert.assertTrue(violation.getErrors()
            .containsKey(ENV_DESCRIP_DISPLAY));
    }

    /**
     * Test mpg with matching envMediumId.
     */
    public void envDescripWithMatchingEnvMediumId() {
        Mpg mpg = new Mpg();
        mpg.setEnvDescripDisplay(EXAMPLE_ENV_DESCRIP_DISPLAY);
        mpg.setEnvMediumId("N71");

        mpg.setRegulationId(EXISTING_REGULATION_ID);
        mpg.setApprLabId(EXISTING_MEAS_FACIL_ID);
        mpg.setMeasFacilId(EXISTING_MEAS_FACIL_ID);
        mpg.setSampleMethId(EXISTING_SAMPLE_METH_ID);

        mpg.setSamplePd(INTERVAL_M);
        mpg.setValidStartDate(DOM_MIN);
        mpg.setValidEndDate(DOM_MAX);
        mpg.setSamplePdStartDate(DOM_MIN);
        mpg.setSamplePdEndDate(DOM_MAX);
        Violation violation = validator.validate(mpg);
        if (violation.hasWarnings()) {
            Assert.assertFalse(
                violation.getWarnings().containsKey("envMediumId#N71"));
        }
    }

    /**
     * Test mpg without matching envMediumId.
     */
    public void envDescripWithoutMatchingEnvMediumId() {
        Mpg mpg = new Mpg();
        mpg.setEnvDescripDisplay(EXAMPLE_ENV_DESCRIP_FROM_SAMPLE_DATA);
        mpg.setEnvMediumId("L54");
        mpg.setRegulationId(EXISTING_REGULATION_ID);
        mpg.setApprLabId(EXISTING_MEAS_FACIL_ID);
        mpg.setMeasFacilId(EXISTING_MEAS_FACIL_ID);
        mpg.setSampleMethId(EXISTING_SAMPLE_METH_ID);

        mpg.setSamplePd(INTERVAL_M);
        mpg.setValidStartDate(DOM_MIN);
        mpg.setValidEndDate(DOM_MAX);
        mpg.setSamplePdStartDate(DOM_MIN);
        mpg.setSamplePdEndDate(DOM_MAX);
        String warningKey = "envMediumId#L54";
        Violation violation = validator.validate(mpg);
        Assert.assertTrue(violation.hasWarnings());
        Assert.assertTrue(violation.getWarnings()
            .containsKey(warningKey));
        Assert.assertTrue(violation.getWarnings().get(warningKey)
            .contains(String.valueOf(StatusCodes.VALUE_NOT_MATCHING)));
    }

}
