/* Copyright (C) 2023 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.validator;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.junit.Assert;

import de.intevation.lada.model.lada.MeasVal;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Validator;

@Transactional
public class MeasValTest {

    //Validation keys
    private static final String DETECT_LIM = "detectLim";
    private static final String ERROR = "error";
    private static final String MEASD_ID = "measdId";
    private static final String MEAS_VAL = "measVal";
    private static final String UNIT_ID = "measUnitId";
    private static final String VALIDATION_KEY_SEPARATOR = "#";

    //Other constants
    private static final int EXISTING_MEASM_ID = 1200;
    private static final int EXISTING_MEASD_ID = 56;
    private static final int OTHER_MEASD_ID = 57;
    private static final String LESS_THAN_LOD_SMALLER_THAN = "<";
    private static final String EXISTING_MEASD_NAME = "Mangan";
    private static final String OTHER_MEASD_NAME = "Other";
    private static final int EXISTING_ENV_MEDIUM_PRIMARY_UNIT = 207;
    private static final int EXISTING_ENV_MEDIUM_SECONDARY_UNIT = 208;
    private static final int EXISTING_ENV_MEDIUM_UNIT_CONVERTABLE_TO_PRIMARY
        = 209;
    private static final int EXISTING_ENV_MEDIUM_UNIT_CONVERTABLE_TO_SECONDARY
        = 210;
    private static final int UNIT_ID_NOT_CONVERTABLE = 211;

    @Inject
    Validator<MeasVal> validator;

    /**
     * Test measVal withour errror and lessThanLOD set.
     */
    public void measValWithoutErrorAndLessThanLOD() {
        MeasVal val = new MeasVal();
        val.setMeasmId(EXISTING_MEASM_ID);
        val.setMeasdId(EXISTING_MEASD_ID);
        val.setLessThanLOD(null);
        val.setError(null);

        validator.validate(val);
        Assert.assertTrue(val.hasWarnings());
        Assert.assertTrue(val.getWarnings().containsKey(ERROR));
        Assert.assertTrue(val.getWarnings()
            .get(ERROR).contains(StatusCodes.VALUE_MISSING));
    }

    /**
     * Test measVal withour errror but lessThanLOD set.
     */
    public void measValWithoutError() {
        MeasVal val = new MeasVal();
        val.setMeasmId(EXISTING_MEASM_ID);
        val.setMeasdId(EXISTING_MEASD_ID);
        val.setLessThanLOD(LESS_THAN_LOD_SMALLER_THAN);
        val.setError(null);

        validator.validate(val);
        Assert.assertTrue(val.hasWarnings());
        Assert.assertTrue(val.getWarnings().containsKey(ERROR));
        Assert.assertTrue(val.getWarnings()
            .get(ERROR).contains(StatusCodes.VAL_UNCERT));
    }

    /**
     * Test measVal with error set.
     */
    public void measValWithError() {
        MeasVal val = new MeasVal();
        val.setMeasmId(EXISTING_MEASM_ID);
        val.setMeasdId(EXISTING_MEASD_ID);
        val.setLessThanLOD(null);
        val.setError(1.0f);

        validator.validate(val);
        if (val.hasWarnings()
                && val.getWarnings().containsKey(ERROR)) {
            Assert.assertFalse(
                val.getWarnings().get(ERROR)
                    .contains(StatusCodes.VALUE_MISSING)
                || val.getWarnings().get(ERROR)
                    .contains(StatusCodes.VAL_UNCERT));
        }
    }

    /**
     * Test measVal without error set but lessThanLOD.
     */
    public void measValWithOutErrorAndLessThanLOD() {
        MeasVal val = new MeasVal();
        val.setMeasmId(EXISTING_MEASM_ID);
        val.setMeasdId(EXISTING_MEASD_ID);
        val.setLessThanLOD(LESS_THAN_LOD_SMALLER_THAN);
        val.setError(null);

        validator.validate(val);
        if (val.hasWarnings()
                && val.getWarnings().containsKey(ERROR)) {
            Assert.assertFalse(
                val.getWarnings().get(ERROR)
                    .contains(StatusCodes.VALUE_MISSING)
                || val.getWarnings().get(ERROR)
                    .contains(StatusCodes.VAL_UNCERT));
        }
    }

    /**
     * Test measVal without measVal and lessThanLOD.
     */
    public void notLessThanLODAndNoMeasVal() {
        MeasVal val = new MeasVal();
        val.setMeasmId(EXISTING_MEASM_ID);
        val.setMeasdId(EXISTING_MEASD_ID);
        val.setLessThanLOD(null);
        val.setMeasVal(null);

        validator.validate(val);
        Assert.assertTrue(val.hasWarnings());
        Assert.assertTrue(val.getWarnings().containsKey(MEAS_VAL));
        Assert.assertTrue(val.getWarnings()
            .get(MEAS_VAL).contains(StatusCodes.VALUE_MISSING));
    }

    /**
     * Test measVal with lessThanLOD and measVal set.
     */
    public void lessThanLODAndMeasVal() {
        MeasVal val = new MeasVal();
        val.setMeasmId(EXISTING_MEASM_ID);
        val.setMeasdId(EXISTING_MEASD_ID);
        val.setLessThanLOD(LESS_THAN_LOD_SMALLER_THAN);
        val.setMeasVal(1.0d);

        validator.validate(val);
        Assert.assertTrue(val.hasWarnings());
        Assert.assertTrue(val.getWarnings().containsKey(MEAS_VAL));
        Assert.assertTrue(val.getWarnings()
            .get(MEAS_VAL).contains(StatusCodes.VAL_MEASURE));
    }

    /**
     * Test measVal with lessThanLOD but not val set.
     */
    public void lessThanLODAndNoMeasVal() {
        MeasVal val = new MeasVal();
        val.setMeasmId(EXISTING_MEASM_ID);
        val.setMeasdId(EXISTING_MEASD_ID);
        val.setLessThanLOD(LESS_THAN_LOD_SMALLER_THAN);
        val.setMeasVal(null);

        validator.validate(val);
        if (val.hasWarnings()
                && val.getWarnings().containsKey(MEAS_VAL)) {
            Assert.assertFalse(val.getWarnings().get(MEAS_VAL)
                    .contains(StatusCodes.VALUE_MISSING));
        }
        if (val.hasErrors()
                && val.getErrors().containsKey(MEAS_VAL)) {
            Assert.assertFalse(val.getErrors().get(MEAS_VAL)
                    .contains(StatusCodes.VAL_MEASURE));
        }
    }

    /**
     * Test measVal with measVal set but not lessThanLOD.
     */
    public void measValWithoutLessThanLOD() {
        MeasVal val = new MeasVal();
        val.setMeasmId(EXISTING_MEASM_ID);
        val.setMeasdId(EXISTING_MEASD_ID);
        val.setLessThanLOD(null);
        val.setMeasVal(1.0d);

        validator.validate(val);
        if (val.hasWarnings()
                && val.getWarnings().containsKey(MEAS_VAL)) {
            Assert.assertFalse(val.getWarnings().get(MEAS_VAL)
                    .contains(StatusCodes.VALUE_MISSING));
        }
        if (val.hasErrors()
                && val.getErrors().containsKey(MEAS_VAL)) {
            Assert.assertFalse(val.getErrors().get(MEAS_VAL)
                    .contains(StatusCodes.VAL_MEASURE));
        }
    }

    /**
     * Test measVal with measVal set to zero.
     */
    public void measValIsZero() {
        MeasVal val = new MeasVal();
        val.setMeasmId(EXISTING_MEASM_ID);
        val.setMeasdId(EXISTING_MEASD_ID);
        val.setMeasVal(0.0d);

        validator.validate(val);
        Assert.assertTrue(val.hasNotifications());
        Assert.assertTrue(val.getNotifications().containsKey(MEAS_VAL));
        Assert.assertTrue(val.getNotifications()
            .get(MEAS_VAL).contains(StatusCodes.VAL_ZERO));
    }

    /**
     * Test measVal with lessThanLOD set but not detectLim.
     */
    public void hasNoDetectLim() {
        MeasVal val = new MeasVal();
        val.setMeasmId(EXISTING_MEASM_ID);
        val.setMeasdId(EXISTING_MEASD_ID);
        val.setLessThanLOD(LESS_THAN_LOD_SMALLER_THAN);
        val.setDetectLim(null);

        validator.validate(val);
        Assert.assertTrue(val.hasErrors());
        Assert.assertTrue(val.getErrors()
            .containsKey(DETECT_LIM));
        Assert.assertTrue(val.getErrors()
            .get(DETECT_LIM).contains(StatusCodes.VALUE_MISSING));
    }

    /**
     * Test measVal with lessThanLOD set but not detectLim.
     */
    public void hasDetectLim() {
        MeasVal val = new MeasVal();
        val.setMeasmId(EXISTING_MEASM_ID);
        val.setMeasdId(EXISTING_MEASD_ID);
        val.setLessThanLOD(LESS_THAN_LOD_SMALLER_THAN);
        val.setDetectLim(1.0d);

        validator.validate(val);
        if (val.hasErrors()
                && val.getErrors().containsKey(DETECT_LIM)) {
            Assert.assertFalse(val.getErrors().get(DETECT_LIM)
                    .contains(StatusCodes.VALUE_MISSING));
        }
    }

    /**
     * Test measVal with envMediums primary unit as measUnit.
     */
    public void measUnitIsPrimary() {
        MeasVal val = new MeasVal();
        val.setMeasmId(EXISTING_MEASM_ID);
        val.setMeasdId(EXISTING_MEASD_ID);
        val.setMeasUnitId(EXISTING_ENV_MEDIUM_PRIMARY_UNIT);

        validator.validate(val);
        String warningKey = UNIT_ID
            + VALIDATION_KEY_SEPARATOR + EXISTING_MEASD_NAME;
        if (val.hasWarnings()
                && val.getWarnings().containsKey(warningKey)) {
            Assert.assertFalse(val.getWarnings().get(warningKey)
                    .contains(StatusCodes.VAL_UNIT_NORMALIZE));
            Assert.assertFalse(val.getWarnings().get(warningKey)
                    .contains(StatusCodes.VAL_UNIT_UMW));
        }
    }

    /**
     * Test measVal with envMediums secondary unit as measUnit.
     */
    public void measUnitIsSecondary() {
        MeasVal val = new MeasVal();
        val.setMeasmId(EXISTING_MEASM_ID);
        val.setMeasdId(EXISTING_MEASD_ID);
        val.setMeasUnitId(EXISTING_ENV_MEDIUM_SECONDARY_UNIT);

        validator.validate(val);
        String warningKey = UNIT_ID
            + VALIDATION_KEY_SEPARATOR + EXISTING_MEASD_NAME;
        if (val.hasWarnings()
                && val.getWarnings().containsKey(warningKey)) {
            Assert.assertFalse(val.getWarnings().get(warningKey)
                    .contains(StatusCodes.VAL_UNIT_NORMALIZE));
            Assert.assertFalse(val.getWarnings().get(warningKey)
                    .contains(StatusCodes.VAL_UNIT_UMW));
        }
    }

    /**
     * Test measVal with measUnit convertable to envMediums primary unit.
     */
    public void measUnitIsConvertableToPrimary() {
        MeasVal val = new MeasVal();
        val.setMeasmId(EXISTING_MEASM_ID);
        val.setMeasdId(EXISTING_MEASD_ID);
        val.setMeasUnitId(EXISTING_ENV_MEDIUM_UNIT_CONVERTABLE_TO_PRIMARY);
        validator.validate(val);
        String warningKey = UNIT_ID
            + VALIDATION_KEY_SEPARATOR + EXISTING_MEASD_NAME;
        Assert.assertTrue(val.hasWarnings());
        Assert.assertTrue(val.getWarnings()
            .containsKey(warningKey));
        Assert.assertTrue(val.getWarnings()
            .get(warningKey).contains(StatusCodes.VAL_UNIT_NORMALIZE));
    }

    /**
     * Test measVal with measUnit convertable to envMediums secondary unit.
     */
    public void measUnitIsConvertableToSecondary() {
        MeasVal val = new MeasVal();
        val.setMeasmId(EXISTING_MEASM_ID);
        val.setMeasdId(EXISTING_MEASD_ID);
        val.setMeasUnitId(EXISTING_ENV_MEDIUM_UNIT_CONVERTABLE_TO_SECONDARY);
        validator.validate(val);
        String warningKey = UNIT_ID
            + VALIDATION_KEY_SEPARATOR + EXISTING_MEASD_NAME;
        Assert.assertTrue(val.hasWarnings());
        Assert.assertTrue(val.getWarnings()
            .containsKey(warningKey));
        Assert.assertTrue(val.getWarnings()
            .get(warningKey).contains(StatusCodes.VAL_UNIT_UMW));
    }

    /**
     * Test measVal with measUnit that can not be converted and is not related
     * to the given envMedium.
     */
    public void measUnitIsNotConvertable() {
        MeasVal val = new MeasVal();
        val.setMeasmId(EXISTING_MEASM_ID);
        val.setMeasdId(EXISTING_MEASD_ID);
        val.setMeasUnitId(UNIT_ID_NOT_CONVERTABLE);
        validator.validate(val);
        String warningKey = UNIT_ID
            + VALIDATION_KEY_SEPARATOR + EXISTING_MEASD_NAME;
        Assert.assertTrue(val.hasWarnings());
        Assert.assertTrue(val.getWarnings()
            .containsKey(warningKey));
        Assert.assertTrue(val.getWarnings()
            .get(warningKey).contains(StatusCodes.VAL_UNIT_UMW));
    }

    /**
     * Test measVal which measd does not match the connected measm mmt.
     */
    public void mmtDoesNotMatchMeasd() {
        MeasVal val = new MeasVal();
        val.setMeasmId(EXISTING_MEASM_ID);
        val.setMeasdId(OTHER_MEASD_ID);
        validator.validate(val);
        String warningKey = MEASD_ID
            + VALIDATION_KEY_SEPARATOR + OTHER_MEASD_NAME;
        Assert.assertTrue(val.hasWarnings());
        Assert.assertTrue(val.getWarnings()
            .containsKey(warningKey));
        Assert.assertTrue(val.getWarnings()
            .get(warningKey)
            .contains(StatusCodes.VAL_MESSGROESSE_NOT_MATCHING_MMT));
    }

    /**
     * Test measVal which measd does match the connected measm mmt.
     */
    public void mmtDoesMatchMeasd() {
        MeasVal val = new MeasVal();
        val.setMeasmId(EXISTING_MEASM_ID);
        val.setMeasdId(EXISTING_MEASD_ID);
        validator.validate(val);
        String warningKey = MEASD_ID
            + VALIDATION_KEY_SEPARATOR + EXISTING_MEASD_NAME;
        if (val.hasWarnings()
                && val.getWarnings().containsKey(warningKey)) {
            Assert.assertFalse(val.getWarnings().get(warningKey)
                .contains(StatusCodes.VAL_MESSGROESSE_NOT_MATCHING_MMT));
        }
    }

    /**
     * Test measVal with envMediums secondary unit as measUnit.
     */
    public void secondaryMeasUnitSelected() {
        MeasVal val = new MeasVal();
        val.setMeasmId(EXISTING_MEASM_ID);
        val.setMeasdId(EXISTING_MEASD_ID);
        val.setMeasUnitId(EXISTING_ENV_MEDIUM_SECONDARY_UNIT);

        validator.validate(val);
        Assert.assertTrue(val.hasNotifications());
        Assert.assertTrue(val.getNotifications().containsKey(UNIT_ID));
        Assert.assertTrue(val.getNotifications()
            .get(UNIT_ID).contains(StatusCodes.VAL_SEC_UNIT));
    }

    /**
     * Test measVal with measUnit convertable to envMediums secondary unit but
     * not its primary unit.
     */
    public void measUnitIsConvertableToSecondaryButNotPrimary() {
        MeasVal val = new MeasVal();
        val.setMeasmId(EXISTING_MEASM_ID);
        val.setMeasdId(EXISTING_MEASD_ID);
        val.setMeasUnitId(EXISTING_ENV_MEDIUM_UNIT_CONVERTABLE_TO_SECONDARY);

        validator.validate(val);
        Assert.assertTrue(val.hasNotifications());
        Assert.assertTrue(val.getNotifications().containsKey(UNIT_ID));
        Assert.assertTrue(val.getNotifications()
            .get(UNIT_ID).contains(StatusCodes.VAL_SEC_UNIT));
    }

    /**
     * Test measVal with measd that already exists in the measm.
     */
    public void measdIsNotUniqueInMeasm() {
        MeasVal val = new MeasVal();
        val.setMeasmId(EXISTING_MEASM_ID);
        val.setMeasdId(EXISTING_MEASD_ID);

        validator.validate(val);
        Assert.assertTrue(val.hasErrors());
        Assert.assertTrue(val.getErrors()
            .containsKey(MEASD_ID));
        Assert.assertTrue(val.getErrors()
            .get(MEASD_ID).contains(StatusCodes.VALUE_AMBIGOUS));
    }

    /**
     * Test measVal with measd that doest not already exists in the measm.
     */
    public void measdIsUniqueInMeasm() {
        MeasVal val = new MeasVal();
        val.setMeasmId(EXISTING_MEASM_ID);
        val.setMeasdId(OTHER_MEASD_ID);

        validator.validate(val);
        if (val.hasErrors()
                && val.getWarnings().containsKey(MEASD_ID)) {
            Assert.assertFalse(val.getErrors().get(MEASD_ID)
                .contains(StatusCodes.VALUE_AMBIGOUS));
        }
    }
}
