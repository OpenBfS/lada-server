/* Copyright (C) 2023 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.validator;

import jakarta.inject.Inject;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Test;

import de.intevation.lada.model.lada.MeasVal;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Validator;

public class MeasValTest extends ValidatorBaseTest {

    //Validation keys
    private static final String DETECT_LIM = "detectLim";
    private static final String ERROR = "error";
    private static final String MEASD_ID = "measdId";
    private static final String MEAS_VAL = "measVal";
    private static final String UNIT_ID = "measUnitId";

    //Other constants
    private static final int EXISTING_MEASM_ID = 1200;
    private static final int EXISTING_EMPTY_MEASM_ID = 1201;
    private static final int EXISTING_MEASD_ID = 57;
    private static final int OTHER_MEASD_ID = 56;
    private static final int UNMATCHED_MEASD_ID = 58;
    private static final String LESS_THAN_LOD_SMALLER_THAN = "<";
    private static final int EXISTING_ENV_MEDIUM_PRIMARY_UNIT = 207;
    private static final int EXISTING_ENV_MEDIUM_SECONDARY_UNIT = 208;
    private static final int EXISTING_ENV_MEDIUM_UNIT_CONVERTABLE_TO_PRIMARY
        = 209;
    private static final int EXISTING_ENV_MEDIUM_UNIT_CONVERTABLE_TO_SECONDARY
        = 210;
    private static final int UNIT_ID_NOT_CONVERTABLE = 211;

    private static final float ERROR_GT_ZERO = 0.5f;

    private static final String VALUE_MISSING = "A value must be provided";

    @Inject
    Validator<MeasVal> validator;

    /**
     * Test measVal without error and lessThanLOD set.
     */
    @Test
    public void measValWithoutErrorAndLessThanLOD() {
        MeasVal val = new MeasVal();
        val.setMeasmId(EXISTING_MEASM_ID);
        val.setMeasdId(EXISTING_MEASD_ID);
        val.setMeasUnitId(EXISTING_ENV_MEDIUM_PRIMARY_UNIT);

        assertHasWarnings(
            validator.validate(val),
            ERROR,
            VALUE_MISSING);
    }

    @Test
    public void measValWithErrorZeroAndNoLessThanLOD() {
        MeasVal val = new MeasVal();
        val.setMeasmId(EXISTING_MEASM_ID);
        val.setMeasdId(EXISTING_MEASD_ID);
        val.setMeasUnitId(EXISTING_ENV_MEDIUM_PRIMARY_UNIT);
        val.setError(0.0f);

        assertHasWarnings(
            validator.validate(val),
            ERROR,
            VALUE_MISSING);
    }

    @Test
    public void measValWithErrorZeroAndLessThanLOD() {
        MeasVal val = new MeasVal();
        val.setMeasmId(EXISTING_MEASM_ID);
        val.setMeasdId(EXISTING_MEASD_ID);
        val.setLessThanLOD(LESS_THAN_LOD_SMALLER_THAN);
        val.setDetectLim(0.0d);
        val.setError(0.0f);
        val.setMeasUnitId(EXISTING_ENV_MEDIUM_PRIMARY_UNIT);

        assertHasWarnings(
            validator.validate(val),
            ERROR,
            "Uncertainty not allowed if < LOD");
    }

    /**
     * Test measVal with error set.
     */
    @Test
    public void measValWithError() {
        MeasVal val = new MeasVal();
        val.setMeasmId(EXISTING_MEASM_ID);
        val.setMeasdId(EXISTING_MEASD_ID);
        val.setMeasVal(1.0d);
        val.setError(1.0f);
        val.setMeasUnitId(EXISTING_ENV_MEDIUM_PRIMARY_UNIT);

        validator.validate(val);
        assertNoWarningsOrErrors(val);
    }

    /**
     * Test measVal without measVal and lessThanLOD.
     */
    @Test
    public void notLessThanLODAndNoMeasVal() {
        MeasVal val = new MeasVal();
        val.setMeasmId(EXISTING_MEASM_ID);
        val.setMeasdId(EXISTING_MEASD_ID);
        val.setLessThanLOD(null);
        val.setMeasVal(null);
        val.setMeasUnitId(EXISTING_ENV_MEDIUM_PRIMARY_UNIT);

        assertHasWarnings(
            validator.validate(val),
            MEAS_VAL,
            VALUE_MISSING);
    }

    /**
     * Test measVal with lessThanLOD and measVal set.
     */
    @Test
    public void lessThanLODAndMeasVal() {
        MeasVal val = new MeasVal();
        val.setMeasmId(EXISTING_MEASM_ID);
        val.setMeasdId(EXISTING_MEASD_ID);
        val.setLessThanLOD(LESS_THAN_LOD_SMALLER_THAN);
        val.setDetectLim(0.0d);
        val.setMeasVal(1.0d);
        val.setMeasUnitId(EXISTING_ENV_MEDIUM_PRIMARY_UNIT);

        assertHasWarnings(
            validator.validate(val),
            MEAS_VAL,
            "Measured value not allowed if < LOD");
    }

    /**
     * Test measVal with lessThanLOD but not val set.
     */
    @Test
    public void lessThanLODAndNoMeasVal() {
        MeasVal val = new MeasVal();
        val.setMeasmId(EXISTING_MEASM_ID);
        val.setMeasdId(EXISTING_MEASD_ID);
        val.setLessThanLOD(LESS_THAN_LOD_SMALLER_THAN);
        val.setDetectLim(0.0d);
        val.setMeasVal(null);
        val.setMeasUnitId(EXISTING_ENV_MEDIUM_PRIMARY_UNIT);

        validator.validate(val);
        assertNoWarningsOrErrors(val);
    }

    /**
     * Test measVal with measVal set but not lessThanLOD.
     */
    @Test
    public void measValWithoutLessThanLOD() {
        MeasVal val = new MeasVal();
        val.setMeasmId(EXISTING_MEASM_ID);
        val.setMeasdId(EXISTING_MEASD_ID);
        val.setLessThanLOD(null);
        val.setError(ERROR_GT_ZERO);
        val.setMeasVal(1.0d);
        val.setMeasUnitId(EXISTING_ENV_MEDIUM_PRIMARY_UNIT);

        validator.validate(val);
        assertNoWarningsOrErrors(val);
    }

    /**
     * Test measVal with measVal set to zero.
     */
    @Test
    public void measValIsZero() {
        MeasVal val = new MeasVal();
        val.setMeasmId(EXISTING_MEASM_ID);
        val.setMeasdId(EXISTING_MEASD_ID);
        val.setMeasVal(0.0d);
        val.setMeasUnitId(EXISTING_ENV_MEDIUM_PRIMARY_UNIT);

        assertHasNotifications(
            validator.validate(val),
            MEAS_VAL,
            String.valueOf(StatusCodes.VAL_ZERO));
    }

    /**
     * Test measVal with lessThanLOD set but not detectLim.
     */
    @Test
    public void hasNoDetectLim() {
        MeasVal val = new MeasVal();
        val.setMeasmId(EXISTING_MEASM_ID);
        val.setMeasdId(EXISTING_MEASD_ID);
        val.setLessThanLOD(LESS_THAN_LOD_SMALLER_THAN);
        val.setDetectLim(null);
        val.setMeasUnitId(EXISTING_ENV_MEDIUM_PRIMARY_UNIT);

        assertHasErrors(
            validator.validate(val),
            DETECT_LIM,
            VALUE_MISSING);
    }

    /**
     * Test measVal with lessThanLOD set but not detectLim.
     */
    @Test
    public void hasDetectLim() {
        MeasVal val = new MeasVal();
        val.setMeasmId(EXISTING_MEASM_ID);
        val.setMeasdId(EXISTING_MEASD_ID);
        val.setLessThanLOD(LESS_THAN_LOD_SMALLER_THAN);
        val.setDetectLim(1.0d);
        val.setMeasUnitId(EXISTING_ENV_MEDIUM_PRIMARY_UNIT);

        validator.validate(val);
        assertNoWarningsOrErrors(val);
    }

    /**
     * Test measVal with envMediums primary unit as measUnit.
     */
    @Test
    public void measUnitIsPrimary() {
        MeasVal val = new MeasVal();
        val.setMeasmId(EXISTING_MEASM_ID);
        val.setMeasdId(EXISTING_MEASD_ID);
        val.setMeasUnitId(EXISTING_ENV_MEDIUM_PRIMARY_UNIT);
        val.setError(ERROR_GT_ZERO);
        val.setMeasVal(1.0d);

        validator.validate(val);
        assertNoWarningsOrErrors(val);
    }

    /**
     * Test measVal with envMediums secondary unit as measUnit.
     */
    @Test
    public void measUnitIsSecondary() {
        MeasVal val = new MeasVal();
        val.setMeasmId(EXISTING_MEASM_ID);
        val.setMeasdId(EXISTING_MEASD_ID);
        val.setMeasUnitId(EXISTING_ENV_MEDIUM_SECONDARY_UNIT);
        val.setError(ERROR_GT_ZERO);
        val.setMeasVal(1.0d);

        validator.validate(val);
        assertNoWarningsOrErrors(val);
    }

    /**
     * Test measVal with measUnit convertable to envMediums primary unit.
     */
    @Test
    public void measUnitIsConvertableToPrimary() {
        MeasVal val = new MeasVal();
        val.setMeasmId(EXISTING_MEASM_ID);
        val.setMeasdId(EXISTING_MEASD_ID);
        val.setMeasUnitId(EXISTING_ENV_MEDIUM_UNIT_CONVERTABLE_TO_PRIMARY);
        validator.validate(val);
        String warningKey = UNIT_ID;
        Assert.assertTrue(val.hasWarnings());
        Assert.assertTrue(val.getWarnings()
            .containsKey(warningKey));
        Assert.assertTrue(val.getWarnings().get(warningKey).contains(
                String.valueOf(StatusCodes.VAL_UNIT_NORMALIZE)));
    }

    /**
     * Test measVal with measUnit convertable to envMediums secondary unit.
     */
    @Test
    public void measUnitIsConvertableToSecondary() {
        MeasVal val = new MeasVal();
        val.setMeasmId(EXISTING_MEASM_ID);
        val.setMeasdId(EXISTING_MEASD_ID);
        val.setMeasUnitId(EXISTING_ENV_MEDIUM_UNIT_CONVERTABLE_TO_SECONDARY);
        validator.validate(val);
        String warningKey = UNIT_ID;
        Assert.assertTrue(val.hasWarnings());
        Assert.assertTrue(val.getWarnings()
            .containsKey(warningKey));
        Assert.assertTrue(val.getWarnings().get(warningKey).contains(
                String.valueOf(StatusCodes.VAL_UNIT_UMW)));
    }

    /**
     * Test measVal with measUnit that can not be converted and is not related
     * to the given envMedium.
     */
    @Test
    public void measUnitIsNotConvertable() {
        MeasVal val = new MeasVal();
        val.setMeasmId(EXISTING_MEASM_ID);
        val.setMeasdId(EXISTING_MEASD_ID);
        val.setMeasUnitId(UNIT_ID_NOT_CONVERTABLE);
        validator.validate(val);
        String warningKey = UNIT_ID;
        Assert.assertTrue(val.hasWarnings());
        Assert.assertTrue(val.getWarnings()
            .containsKey(warningKey));
        Assert.assertTrue(val.getWarnings().get(warningKey).contains(
                String.valueOf(StatusCodes.VAL_UNIT_UMW)));
    }

    /**
     * Test measVal which measd does not match the connected measm mmt.
     */
    @Test
    public void mmtDoesNotMatchMeasd() {
        MeasVal val = new MeasVal();
        val.setMeasmId(EXISTING_MEASM_ID);
        val.setMeasdId(UNMATCHED_MEASD_ID);
        val.setMeasUnitId(EXISTING_ENV_MEDIUM_PRIMARY_UNIT);

        assertHasWarnings(
            validator.validate(val),
            MEASD_ID,
            "Measurand does not match measuring method");
    }

    /**
     * Test measVal which measd does match the connected measm mmt.
     */
    @Test
    public void mmtDoesMatchMeasd() {
        MeasVal val = new MeasVal();
        val.setMeasmId(EXISTING_MEASM_ID);
        val.setMeasdId(EXISTING_MEASD_ID);
        val.setMeasUnitId(EXISTING_ENV_MEDIUM_PRIMARY_UNIT);
        val.setError(ERROR_GT_ZERO);
        val.setMeasVal(1.0d);
        validator.validate(val);
        assertNoWarningsOrErrors(val);
    }

    /**
     * Test measVal with envMediums secondary unit as measUnit.
     */
    @Test
    public void secondaryMeasUnitSelected() {
        MeasVal val = new MeasVal();
        val.setMeasmId(EXISTING_MEASM_ID);
        val.setMeasdId(EXISTING_MEASD_ID);
        val.setMeasUnitId(EXISTING_ENV_MEDIUM_SECONDARY_UNIT);

        assertHasNotifications(
            validator.validate(val),
            UNIT_ID,
            String.valueOf(StatusCodes.VAL_SEC_UNIT));
    }

    /**
     * Test measVal with measUnit convertable to envMediums secondary unit but
     * not its primary unit.
     */
    @Test
    public void measUnitIsConvertableToSecondaryButNotPrimary() {
        MeasVal val = new MeasVal();
        val.setMeasmId(EXISTING_MEASM_ID);
        val.setMeasdId(EXISTING_MEASD_ID);
        val.setMeasUnitId(EXISTING_ENV_MEDIUM_UNIT_CONVERTABLE_TO_SECONDARY);

        assertHasNotifications(
            validator.validate(val),
            UNIT_ID,
            String.valueOf(StatusCodes.VAL_SEC_UNIT));
    }

    /**
     * Test measVal with measd that already exists in the measm.
     */
    @Test
    public void measdIsNotUniqueInMeasm() {
        MeasVal val = new MeasVal();
        val.setMeasmId(EXISTING_MEASM_ID);
        val.setMeasdId(OTHER_MEASD_ID);
        val.setMeasUnitId(EXISTING_ENV_MEDIUM_PRIMARY_UNIT);
        val.setError(1.0f);
        val.setMeasVal(2.0d);

        validator.validate(val);
        Assert.assertTrue(val.hasErrors());
        MatcherAssert.assertThat(val.getErrors().keySet(),
            CoreMatchers.hasItem(MEASD_ID));
        MatcherAssert.assertThat(val.getErrors().get(MEASD_ID),
            CoreMatchers.hasItem(
                "Non-unique value combination for [measdId, measmId]"));
    }

    /**
     * Test measVal with measd that doest not already exists in the measm.
     */
    @Test
    public void measdIsUniqueInMeasm() {
        MeasVal val = new MeasVal();
        val.setMeasmId(EXISTING_EMPTY_MEASM_ID);
        val.setMeasdId(OTHER_MEASD_ID);
        val.setMeasUnitId(EXISTING_ENV_MEDIUM_PRIMARY_UNIT);

        validator.validate(val);
        Assert.assertFalse(val.hasErrors());
    }
}
