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

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;

import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Validator;

/**
 * Test messung entities.
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Transactional
public class MessungTest {

    //Validation keys
    private static final String MEAS_PD = "measPd";
    private static final String MEASD_ID = "measdId";
    private static final String MEASM_START_DATE = "measmStartDate";
    private static final String MIN_SAMPLE_ID = "minSampleId";

    //ID constants from test dataset
    private static final int EXISTING_SAMPLE_ID = 1000;
    private static final int EXISTING_SAMPLE_ID_SAMPLE_METH_CONT = 2000;
    private static final int EXISTING_SAMPLE_ID_REGULATION_161 = 3000;
    private static final String EXISTING_MEASD_NAME = "Mangan";
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

    private static final String VALIDATION_KEY_SEPARATOR = "#";

    @Inject
    private Validator<Measm> validator;

    /**
     * Test nebenproben nr.
     */
    public void hasNebenprobenNr() {
        Measm measm = new Measm();
        measm.setMinSampleId("10R1");
        measm.setSampleId(EXISTING_SAMPLE_ID);
        validator.validate(measm);
        if (measm.hasWarnings()) {
            Assert.assertFalse(
                measm.getWarnings().containsKey(MIN_SAMPLE_ID));
        }
    }

    /**
     * Test without nebenproben nr.
     */
    public void hasNoNebenprobenNr() {
        Measm measm = new Measm();
        measm.setSampleId(EXISTING_SAMPLE_ID);
        measm.setMmtId(EXISTING_MMT_ID);
        validator.validate(measm);
        Assert.assertTrue(measm.hasNotifications());
        MatcherAssert.assertThat(measm.getNotifications().keySet(),
            CoreMatchers.hasItem(MIN_SAMPLE_ID));
        MatcherAssert.assertThat(
            measm.getNotifications().get(MIN_SAMPLE_ID),
            CoreMatchers.hasItem(StatusCodes.VALUE_MISSING));
    }

    /**
     * Test empty nebenproben nr.
     */
    public void hasEmptyNebenprobenNr() {
        Measm measm = new Measm();
        measm.setMinSampleId("");
        measm.setSampleId(EXISTING_SAMPLE_ID);
        measm.setMmtId(EXISTING_MMT_ID);

        validator.validate(measm);
        Assert.assertTrue(measm.hasNotifications());
        Assert.assertTrue(measm.getNotifications()
            .containsKey(MIN_SAMPLE_ID));
        Assert.assertTrue(
            measm.getNotifications().get(MIN_SAMPLE_ID)
                .contains(StatusCodes.VALUE_MISSING));
    }

    /**
     * Test new existing nebenproben nr.
     */
    public void existingNebenprobenNrNew() {
        Measm messung = new Measm();
        messung.setMinSampleId(EXISTING_MIN_SAMPLE_ID);
        messung.setSampleId(EXISTING_SAMPLE_ID);
        messung.setMmtId(EXISTING_MMT_ID);
        validator.validate(messung);
        Assert.assertTrue(messung.hasErrors());
        MatcherAssert.assertThat(
            messung.getErrors().keySet(),
            CoreMatchers.hasItem(MIN_SAMPLE_ID));
        MatcherAssert.assertThat(
            messung.getErrors().get(MIN_SAMPLE_ID),
            CoreMatchers.hasItem(
                "Non-unique value combination for [minSampleId, sampleId]"));
    }

    /**
     * Test new unique nebenproben nr.
     */
    public void uniqueNebenprobenNrNew() {
        Measm messung = new Measm();
        messung.setMinSampleId(MIN_SAMPLE_ID_00G2);
        messung.setSampleId(EXISTING_SAMPLE_ID);
        messung.setMmtId(EXISTING_MMT_ID);
        validator.validate(messung);
        Assert.assertFalse(messung.hasErrors());
    }

    /**
     * Test update unique nebenproben nr.
     */
    public void uniqueNebenprobenNrUpdate() {
        Measm messung = new Measm();
        messung.setId(EXISTING_MEASM_ID);
        messung.setSampleId(EXISTING_SAMPLE_ID);
        messung.setMinSampleId(MIN_SAMPLE_ID_00G2);
        messung.setMmtId(EXISTING_MMT_ID);
        validator.validate(messung);
        Assert.assertFalse(messung.hasErrors());
    }

    /**
     * Test update existing nebenproben nr.
     */
    public void existingNebenprobenNrUpdate() {
        Measm measm = new Measm();
        measm.setId(EXISTING_MEASM_ID);
        measm.setSampleId(EXISTING_SAMPLE_ID);
        measm.setMinSampleId(EXISTING_MIN_SAMPLE_ID);
        measm.setMmtId(EXISTING_MMT_ID);

        validator.validate(measm);
        Assert.assertFalse(measm.hasErrors());
    }

    /**
     * Test measm with start date in future.
     */
    public void measmStartDateInFuture() {
        Instant tomorrow = Instant.now().plus(1, ChronoUnit.DAYS);
        Measm measm = new Measm();
        measm.setSampleId(EXISTING_SAMPLE_ID);
        measm.setMeasmStartDate(Date.from(tomorrow));
        measm.setMmtId(EXISTING_MMT_ID);


        validator.validate(measm);
        Assert.assertTrue(measm.hasWarnings());
        Assert.assertTrue(measm.getWarnings()
            .containsKey(MEASM_START_DATE));
        Assert.assertTrue(
            measm.getWarnings().get(MEASM_START_DATE).contains(
                String.valueOf(StatusCodes.DATE_IN_FUTURE)));
    }

    /**
     * Test measm with start date before sampleStartDate.
     * @throws ParseException Thrown if date parsing fails
     */
    public void measmStartDateBeforeSampleStartDate() throws ParseException {
        Instant sampleStartDate = DB_UNIT_DATE_FORMAT
            .parse(EXISTING_SAMPLE_START_DATE).toInstant();
        Instant measmStartDate = sampleStartDate.minus(1, ChronoUnit.DAYS);
        Measm measm = new Measm();
        measm.setSampleId(EXISTING_SAMPLE_ID);
        measm.setMinSampleId(NEW_MIN_SAMPLE_ID);
        measm.setMeasmStartDate(Date.from(measmStartDate));
        measm.setMmtId(EXISTING_MMT_ID);
        String warnKey = MEASM_START_DATE
            + VALIDATION_KEY_SEPARATOR + measm.getMinSampleId();
        validator.validate(measm);
        Assert.assertTrue(measm.hasWarnings());
        Assert.assertTrue(measm.getWarnings()
            .containsKey(warnKey));
        Assert.assertTrue(
            measm.getWarnings().get(warnKey).contains(
                String.valueOf(StatusCodes.VALUE_NOT_MATCHING)));
    }

    /**
     * Test measm with start date before sampleStartDate.
     * @throws ParseException Thrown if date parsing fails
     */
    public void measmStartDateAfterSampleStartDate() throws ParseException {
        Instant sampleStartDate = DB_UNIT_DATE_FORMAT
            .parse(EXISTING_SAMPLE_START_DATE).toInstant();
        Instant measmStartDate = sampleStartDate.plus(1, ChronoUnit.DAYS);
        Measm measm = new Measm();
        measm.setSampleId(EXISTING_SAMPLE_ID);
        measm.setMinSampleId(NEW_MIN_SAMPLE_ID);
        measm.setMeasmStartDate(Date.from(measmStartDate));
        validator.validate(measm);
        if (measm.hasWarnings()) {
            Assert.assertFalse(measm.getWarnings().containsKey(
                MEASM_START_DATE + VALIDATION_KEY_SEPARATOR
                + measm.getMinSampleId()));
        }
    }

    /**
     * Test measm without start date.
     */
    public void measmWithoutStartDate() {
        Measm measm = new Measm();
        measm.setSampleId(EXISTING_SAMPLE_ID);
        measm.setMmtId(EXISTING_MMT_ID);
        validator.validate(measm);
        Assert.assertTrue(measm.hasWarnings());
        Assert.assertTrue(measm.getWarnings()
            .containsKey(MEASM_START_DATE));
        Assert.assertTrue(
            measm.getWarnings().get(MEASM_START_DATE).contains(
                String.valueOf(StatusCodes.VALUE_MISSING)));
    }

    /**
     * Test measm without start date connected to a sample with regulation id 1.
     */
    public void measmWithoutStartDateRegulation161Sample() {
        Measm measm = new Measm();
        measm.setSampleId(EXISTING_SAMPLE_ID_REGULATION_161);
        measm.setMmtId(EXISTING_MMT_ID);

        validator.validate(measm);
        Assert.assertTrue(measm.hasNotifications());
        Assert.assertTrue(measm.getNotifications()
            .containsKey(MEASM_START_DATE));
        Assert.assertTrue(
            measm.getNotifications().get(MEASM_START_DATE).contains(
                StatusCodes.VALUE_MISSING));
    }

    /**
     * Test measm without a measPd.
     */
    public void measmWithoutMeasPD() {
        Measm measm = new Measm();
        measm.setSampleId(EXISTING_SAMPLE_ID);
        measm.setMinSampleId(NEW_MIN_SAMPLE_ID);
        measm.setMmtId(EXISTING_MMT_ID);

        validator.validate(measm);
        String warnKey = MEAS_PD + VALIDATION_KEY_SEPARATOR
            + measm.getMinSampleId();
        Assert.assertTrue(measm.hasWarnings());
        Assert.assertTrue(measm.getWarnings()
            .containsKey(warnKey));
        Assert.assertTrue(
            measm.getWarnings().get(warnKey).contains(
                String.valueOf(StatusCodes.VALUE_MISSING)));
    }

    /**
     * Test measm without a measPd connected to a sample with regulation id 1.
     */
    public void measmWithoutMeasPDRegulation161Sample() {
        Measm measm = new Measm();
        measm.setSampleId(EXISTING_SAMPLE_ID_REGULATION_161);
        measm.setMinSampleId(NEW_MIN_SAMPLE_ID);
        measm.setMmtId(EXISTING_MMT_ID);

        validator.validate(measm);
        Assert.assertTrue(measm.hasNotifications());
        Assert.assertTrue(measm.getNotifications()
            .containsKey(MEAS_PD));
        Assert.assertTrue(
            measm.getNotifications().get(MEAS_PD).contains(
                StatusCodes.VALUE_MISSING));
    }

    /**
     * Test measm without a measPd connected to a continuous sample.
     */
    public void measmWithoutMeasPDRContSample() {
        Measm measm = new Measm();
        measm.setSampleId(EXISTING_SAMPLE_ID_SAMPLE_METH_CONT);
        measm.setMmtId(EXISTING_MMT_ID);


        validator.validate(measm);
        Assert.assertTrue(measm.hasNotifications());
        Assert.assertTrue(measm.getNotifications()
            .containsKey(MEAS_PD));
        Assert.assertTrue(
            measm.getNotifications().get(MEAS_PD).contains(
                StatusCodes.VALUE_MISSING));
    }

    /**
     * Test measm with measPd.
     */
    public void measmWithMeasPd() {
        Measm measm = new Measm();
        measm.setSampleId(EXISTING_SAMPLE_ID);
        measm.setMeasPd(1);
        validator.validate(measm);
        if (measm.hasWarnings()) {
            Assert.assertFalse(measm
                .getWarnings().containsKey(MEAS_PD));
        }
        if (measm.hasNotifications()) {
            Assert.assertFalse(measm.
                getNotifications().containsKey(MEAS_PD));
        }
    }

    /**
     * Test measm missing obligatory measds.
     */
    public void measmWithoutObligMeasd() {
        Measm measm = new Measm();
        measm.setSampleId(EXISTING_SAMPLE_ID);
        measm.setMmtId(EXISTING_MMT_ID);

        validator.validate(measm);
        String notficationKey = MEASD_ID + VALIDATION_KEY_SEPARATOR
            + EXISTING_MEASD_NAME;
        Assert.assertTrue(measm.hasNotifications());
        Assert.assertTrue(measm.getNotifications()
            .containsKey(notficationKey));
        Assert.assertTrue(measm.getNotifications()
            .get(notficationKey).contains(StatusCodes.VAL_OBL_MEASURE));
    }

    /**
     * Test measm with all obligatory measds.
     */
    public void measmWithObligMeasd() {
        Measm measm = new Measm();
        measm.setId(EXISTING_MEASM_ID);
        measm.setSampleId(EXISTING_SAMPLE_ID);
        measm.setMmtId(EXISTING_MMT_ID);

        validator.validate(measm);
        if (measm.hasNotifications()) {
            String notficationKey = MEASD_ID
                + VALIDATION_KEY_SEPARATOR + EXISTING_MEASD_NAME;
            Assert.assertFalse(measm
                .getNotifications().containsKey(notficationKey));
        }
    }

    /**
     * Test measm with invalid mmtId.
     */
    public void measmWithInvalidMmt() {
        Measm measm = new Measm();
        measm.setId(EXISTING_MEASM_ID);
        measm.setSampleId(EXISTING_SAMPLE_ID);
        final String invalidKey = "XX";
        measm.setMmtId(invalidKey);

        validator.validate(measm);
        final String mmtIdKey = "mmtId";
        MatcherAssert.assertThat(
            measm.getErrors().keySet(),
            CoreMatchers.hasItem(mmtIdKey));
        MatcherAssert.assertThat(
            measm.getErrors().get(mmtIdKey),
            CoreMatchers.hasItem(
                "'" + invalidKey + "' is no valid primary key"));
    }
}
