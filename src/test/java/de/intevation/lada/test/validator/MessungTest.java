/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.validator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Test;

import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Measm_;


/**
 * Test messung entities.
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class MessungTest extends ValidatorBaseTest {

    //ID constants from test dataset
    private static final int EXISTING_SAMPLE_ID = 1000;
    private static final int EXISTING_SAMPLE_ID_SAMPLE_METH_CONT = 2000;
    private static final int EXISTING_SAMPLE_ID_REGULATION_161 = 3000;
    private static final int EXISTING_MEASM_ID = 1200;
    private static final String EXISTING_MIN_SAMPLE_ID = "T100";
    private static final String EXISTING_MMT_ID = "A3";
    private static final String EXISTING_SAMPLE_START_DATE
        = "2012-05-03 13:07:00";

    //Other constants
    private static final SimpleDateFormat DB_UNIT_DATE_FORMAT
        = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final String NEW_MIN_SAMPLE_ID = "42AB";
    private static final String MIN_SAMPLE_ID_00G2 = "00G2";

    private static final String MSG_VALUE_MISSING = "A value must be provided";
    private static final String MSG_VAL_OBL_MEASURE =
        "Obligatory measurand missing";

    /**
     * Test valid measm.
     */
    @Test
    public void isValid() {
        assertNoMessages(validator.validate(createMinimalValidMeasm()));
    }

    /**
     * Test without nebenproben nr.
     */
    @Test
    public void hasNoNebenprobenNr() {
        Measm measm = createMinimalValidMeasm();
        measm.setMinSampleId(null);
        assertHasNotifications(
            validator.validate(measm),
            Measm_.MIN_SAMPLE_ID,
            "must not be blank");
    }

    /**
     * Test empty nebenproben nr.
     */
    @Test
    public void hasEmptyNebenprobenNr() {
        Measm measm = createMinimalValidMeasm();
        measm.setMinSampleId("");
        validator.validate(measm);
        Assert.assertTrue(measm.hasErrors());
        MatcherAssert.assertThat(
            measm.getErrors().keySet(),
            CoreMatchers.hasItem(Measm_.MIN_SAMPLE_ID));
        MatcherAssert.assertThat(
            measm.getErrors().get(Measm_.MIN_SAMPLE_ID),
            CoreMatchers.hasItem(
                "size must be between 1 and 2147483647"));
        MatcherAssert.assertThat(
            measm.getErrors().get(Measm_.MIN_SAMPLE_ID),
            CoreMatchers.hasItem(
                "must match \".*\\S+.*\""));
    }

    /**
     * Test new existing nebenproben nr.
     */
    @Test
    public void existingNebenprobenNrNew() {
        Measm messung = createMinimalValidMeasm();
        messung.setId(null);
        messung.setMinSampleId(EXISTING_MIN_SAMPLE_ID);
        validator.validate(messung);
        Assert.assertTrue(messung.hasErrors());
        MatcherAssert.assertThat(
            messung.getErrors().keySet(),
            CoreMatchers.hasItem(Measm_.MIN_SAMPLE_ID));
        MatcherAssert.assertThat(
            messung.getErrors().get(Measm_.MIN_SAMPLE_ID),
            CoreMatchers.hasItem(
                "Non-unique value combination for [minSampleId, sampleId]"));
    }

    /**
     * Test new unique nebenproben nr.
     */
    @Test
    public void uniqueNebenprobenNrNew() {
        Measm messung = createMinimalValidMeasm();
        messung.setId(null);
        messung.setMinSampleId(MIN_SAMPLE_ID_00G2);
        validator.validate(messung);
        Assert.assertFalse(messung.hasErrors());
    }

    /**
     * Test update unique nebenproben nr.
     */
    @Test
    public void uniqueNebenprobenNrUpdate() {
        Measm messung = createMinimalValidMeasm();
        messung.setMinSampleId(MIN_SAMPLE_ID_00G2);
        validator.validate(messung);
        Assert.assertFalse(messung.hasErrors());
    }

    /**
     * Test update existing nebenproben nr.
     */
    @Test
    public void existingNebenprobenNrUpdate() {
        Measm measm = createMinimalValidMeasm();
        measm.setMinSampleId(EXISTING_MIN_SAMPLE_ID);

        validator.validate(measm);
        Assert.assertFalse(measm.hasErrors());
    }

    /**
     * Test measm with start date in future.
     */
    @Test
    public void measmStartDateInFuture() {
        Instant tomorrow = Instant.now().plus(1, ChronoUnit.DAYS);
        Measm measm = createMinimalValidMeasm();
        measm.setMeasmStartDate(Date.from(tomorrow));

        assertHasWarnings(
            validator.validate(measm),
            Measm_.MEASM_START_DATE,
            "must be a date in the past or in the present");
    }

    /**
     * Test measm with start date before sampleStartDate.
     * @throws ParseException Thrown if date parsing fails
     */
    @Test
    public void measmStartDateBeforeSampleStartDate() throws ParseException {
        Instant sampleStartDate = DB_UNIT_DATE_FORMAT
            .parse(EXISTING_SAMPLE_START_DATE).toInstant();
        Instant measmStartDate = sampleStartDate.minus(1, ChronoUnit.DAYS);

        Measm measm = createMinimalValidMeasm();
        measm.setMinSampleId(NEW_MIN_SAMPLE_ID);
        measm.setMeasmStartDate(Date.from(measmStartDate));

        assertHasWarnings(
            validator.validate(measm),
            Measm_.MEASM_START_DATE,
            "Values do not match");
    }

    /**
     * Test measm without start date.
     */
    @Test
    public void measmWithoutStartDate() {
        Measm measm = createMinimalValidMeasm();
        measm.setMeasmStartDate(null);
        assertHasWarnings(
            validator.validate(measm),
            Measm_.MEASM_START_DATE,
            MSG_VALUE_MISSING);
    }

    /**
     * Test measm without start date connected to a sample with regulation id 1.
     */
    @Test
    public void measmWithoutStartDateRegulation161Sample() {
        Measm measm = createMinimalValidMeasm();
        measm.setSampleId(EXISTING_SAMPLE_ID_REGULATION_161);
        measm.setMeasmStartDate(null);

        assertHasNotifications(
            validator.validate(measm),
            Measm_.MEASM_START_DATE,
            MSG_VALUE_MISSING);
    }

    /**
     * Test measm without a measPd.
     */
    @Test
    public void measmWithoutMeasPD() {
        Measm measm = createMinimalValidMeasm();
        measm.setMinSampleId(NEW_MIN_SAMPLE_ID);
        measm.setMeasPd(null);

        assertHasWarnings(
            validator.validate(measm),
            Measm_.MEAS_PD,
            MSG_VALUE_MISSING);
    }

    /**
     * Test measm without a measPd connected to a sample with regulation id 1.
     */
    @Test
    public void measmWithoutMeasPDRegulation161Sample() {
        Measm measm = createMinimalValidMeasm();
        measm.setSampleId(EXISTING_SAMPLE_ID_REGULATION_161);
        measm.setMeasPd(null);
        measm.setMinSampleId(NEW_MIN_SAMPLE_ID);

        assertHasNotifications(
            validator.validate(measm),
            Measm_.MEAS_PD,
            MSG_VALUE_MISSING);
    }

    /**
     * Test measm without a measPd connected to a continuous sample.
     */
    @Test
    public void measmWithoutMeasPDRContSample() {
        Measm measm = createMinimalValidMeasm();
        measm.setMeasPd(null);
        measm.setSampleId(EXISTING_SAMPLE_ID_SAMPLE_METH_CONT);

        assertHasNotifications(
            validator.validate(measm),
            Measm_.MEAS_PD,
            MSG_VALUE_MISSING);
    }

    /**
     * Test measm missing obligatory measds.
     */
    @Test
    public void measmWithoutMeasVals() {
        Measm measm = createMinimalValidMeasm();
        measm.setId(null);

        assertHasNotifications(
            validator.validate(measm),
            Measm_.MMT_ID,
            MSG_VAL_OBL_MEASURE);
    }

    /**
     * Test measm missing obligatory measd.
     */
    @Test
    public void measmWithoutObligMeasd() {
        Measm measm = createMinimalValidMeasm();
        measm.setMmtId("A4");

        assertHasNotifications(
            validator.validate(measm),
            Measm_.MMT_ID,
            MSG_VAL_OBL_MEASURE);
    }

    /**
     * Test measm missing obligatory measd due to partly matching envMediumId.
     */
    @Test
    public void measmWithoutObligMeasdEnvMediumL6() {
        Measm measm = createMinimalValidMeasm();
        measm.setMmtId("A5");

        assertHasNotifications(
            validator.validate(measm),
            Measm_.MMT_ID,
            MSG_VAL_OBL_MEASURE);
    }

    /**
     * Test measm missing obligatory measd due to partly matching envMediumId.
     */
    @Test
    public void measmWithoutObligMeasdEnvMediumL() {
        Measm measm = createMinimalValidMeasm();
        measm.setMmtId("A6");

        assertHasNotifications(
            validator.validate(measm),
            Measm_.MMT_ID,
            MSG_VAL_OBL_MEASURE);
    }

    /**
     * Test measm with invalid mmtId.
     */
    @Test
    public void measmWithInvalidMmt() {
        Measm measm = createMinimalValidMeasm();
        final String invalidKey = "XX";
        measm.setMmtId(invalidKey);

        validator.validate(measm);
        MatcherAssert.assertThat(
            measm.getErrors().keySet(),
            CoreMatchers.hasItem(Measm_.MMT_ID));
        MatcherAssert.assertThat(
            measm.getErrors().get(Measm_.MMT_ID),
            CoreMatchers.hasItem(
                "'" + invalidKey + "' is no valid primary key"));
    }

    private Measm createMinimalValidMeasm() {
        Measm measm = new Measm();
        measm.setId(EXISTING_MEASM_ID);
        measm.setSampleId(EXISTING_SAMPLE_ID);
        measm.setMmtId(EXISTING_MMT_ID);
        measm.setMinSampleId(MIN_SAMPLE_ID_00G2);
        measm.setMeasPd(1);
        measm.setMeasmStartDate(new Date());
        return measm;
    }
}
