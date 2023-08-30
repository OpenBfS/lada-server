/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada;

import java.text.ParseException;

import javax.inject.Inject;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.intevation.lada.test.validator.CommMeasmTest;
import de.intevation.lada.test.validator.CommSampleTest;
import de.intevation.lada.test.validator.MessungTest;
import de.intevation.lada.test.validator.MpgTest;
import de.intevation.lada.test.validator.ProbeTest;
import de.intevation.lada.test.validator.StatusTest;


/**
 * Test validators.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@RunWith(Arquillian.class)
public class ValidatorTest extends BaseTest {

    @Inject
    private ProbeTest probeTest;

    @Inject
    private MessungTest messungTest;

    @Inject
    private StatusTest statusTest;

    @Inject
    private MpgTest mpgTest;

    @Inject
    private CommSampleTest commSampleTest;

    @Inject
    private CommMeasmTest commMeasmTest;

    /**
     * Constructor.
     * Sets test dataset.
     */
    public ValidatorTest() {
        this.testDatasetName = "datasets/dbUnit_validator.xml";
    }

    /**
     * Test hauptprobennr.
     */
    @Test
    public void probeHasHauptprobenNr() {
        probeTest.hasHauptprobenNr();
    }

    /**
     * Test hauptprobennr missing.
     */
    @Test
    public void probeHasNoHauptprobenNr() {
        probeTest.hasNoHauptprobenNr();
    }

    /**
     * Test existing hauptprobennr new.
     */
    @Test
    public void probeExistingHauptprobenNrNew() {
        probeTest.existingHauptprobenNrNew();
    }

    /**
     * Test unique hauptprobennr new.
     */
    @Test
    public void probeUniqueHauptprobenNrNew() {
        probeTest.uniqueHauptprobenNrNew();
    }

    /**
     * Test existing hauptprobennr update.
     */
    @Test
    public void probeExistingHauptprobenNrUpdate() {
        probeTest.existingHauptprobenNrUpdate();
    }

    /**
     * Test unique hauptprobennr update.
     */
    @Test
    public void probeUniqueHauptprobenNrUpdate() {
        probeTest.uniqueHauptprobenNrUpdate();
    }

    /**
     * Test probe has entnahmeort.
     */
    @Test
    public void probeHasEntnahmeOrt() {
        probeTest.hasEntnahmeOrt();
    }

    /**
     * Test probe has no entnahmeort.
     */
    @Test
    public void probeHasNoEntnahmeOrt() {
        probeTest.hasNoEntnahmeOrt();
    }

    /**
     * Test probe has probenahmebegin.
     */
    @Test
    public void probeHasProbenahmeBegin() {
        probeTest.hasProbeentnahmeBegin();
    }

    /**
     * Test probe has no probenahmebegin.
     */
    @Test
    public void probeHasNoProbenahmeBegin() {
        probeTest.hasNoProbeentnahmeBegin();
    }

    /**
     * Test probe has no time end probenahmebegin.
     */
    @Test
    public void probeTimeNoEndProbenahmeBegin() {
        probeTest.timeNoEndProbeentnahmeBegin();
    }

    /**
     * Test probe has no time begin probenahmebegin.
     */
    @Test
    public void probeTimeNoBeginProbenahmeBegin() {
        probeTest.timeNoBeginProbeentnahmeBegin();
    }

    /**
     * Test probe time begin after end probenahmebegin.
     */
    @Test
    public void probeTimeBeginAfterEndProbenahmeBegin() {
        probeTest.timeBeginAfterEndProbeentnahmeBegin();
    }

    /**
     * Test probe begin in future probenahmebegin.
     */
    @Test
    public void probeTimeBeginFutureProbenahmeBegin() {
        probeTest.timeBeginFutureProbeentnahmeBegin();
    }

    /**
     * Test probe has umwelt.
     */
    @Test
    public void probeHasUmwelt() {
        probeTest.hasUmwelt();
    }

    /**
     * Test probe has no umwelt.
     */
    @Test
    public void probeHasNoUmwelt() {
        probeTest.hasNoUmwelt();
    }

    /**
     * Test probe has empty umwelt.
     */
    @Test
    public void probeHasEmptyUmwelt() {
        probeTest.hasEmptyUmwelt();
    }

    /**
     * Test sampleMeth validation.
     */
    @Test
    public void samplePeBeginEqualsPeEnd() {
        probeTest.peBeginEqualsPeEnd();
    }


    /**
     * Test sampleMeth validation.
     */
    @Test
    public void samplePeBeginDoesNotEqualPeEnd() {
        probeTest.peBeginDoesNotEqualPeEnd();
    }

    /**
     * Test orig date is after sample start date.
     */
    @Test
    public void sampleOrigDateAfterSampleStartDate() {
        probeTest.origDateAfterSampleStartDate();
    }

    /**
     * Test orig date is before sample start date.
     */
    @Test
    public void sampleOrigDateBeforeSampleStartDate() {
        probeTest.origDateBeforeSampleStartDate();
    }

    /**
     * Test sample with valid envDescripDisplay.
     */
    @Test
    public void sampleEnvDescripDisplay() {
        probeTest.envDescripDisplay();
    }

    /**
     * Test sample without envDescripDisplay.
     */
    @Test
    public void sampleNoEnvDescripDisplay() {
        probeTest.noEnvDescripDisplay();
    }

    /**
     * Test sample with empty envDescripDisplay.
     */
    @Test
    public void sampleEmptyEnvDescripDisplay() {
        probeTest.emptyEnvDescripDisplay();
    }

    /**
     * Test sample with envDescripDisplay S1 not set.
     */
    @Test
    public void sampleEnvDescripDisplayS1NotSet() {
        probeTest.envDescripDisplayS1NotSet();
    }

    /**
     * Test sample with inval envDescripDisplay.
     */
    @Test
    public void sampleEnvDescripDisplayInvalidDisplayString() {
        probeTest.envDescripDisplayInvalidDisplayString();
    }

    /**
     * Test sample with matching envMediumId.
     */
    @Test
    public void sampleEnvDescripWithMatchingEnvMediumId() {
        probeTest.envDescripWithMatchingEnvMediumId();
    }

    /**
     * Test sample without matching envMediumId.
     */
    @Test
    public void sampleEnvDescripWithoutMatchingEnvMediumId() {
        probeTest.envDescripWithoutMatchingEnvMediumId();
    }

    /**
     * Test sample with single U Type geolocat.
     */
    @Test
    public void sampleHasSingleUTypeGeolocat() {
        probeTest.hasSingleUTypeGeolocat();
    }

    /**
     * Test sample with sampleEndDate.
     */
    @Test
    public void sampleHasSampleEndDate() {
        probeTest.hasSampleEndDate();
    }

    /**
     * Test sample without sample end date.
     */
    @Test
    public void sampleHasNoSampleEndDate() {
        probeTest.hasNoSampleEndDate();
    }

    /**
     * Test sample without sample end date.
     */
    @Test
    public void sampleHasSampleEndDateBeforeBegin() {
        probeTest.hasSampleEndDateBeforeBegin();
    }

    /**
     * Test REI sample with R type geolocat.
     */
    @Test
    public void sampleHasRTypeGeolocat() {
        probeTest.hasRTypeGeolocat();
    }

    /**
     * Test REI sample with R type geolocat.
     */
    @Test
    public void sampleHasNoRTypeGeolocat() {
        probeTest.hasNoRTypeGeolocat();
    }

    /**
     * Test sample without REI regulation but REI fields.
     */
    @Test
    public void sampleShouldNotHaveREIData() {
        probeTest.sampleShouldNotHaveREIData();
    }

    /**
     * Test sample with REI regulation but without REI fields.
     */
    @Test
    public void sampleShouldHaveREIData() {
        probeTest.sampleShouldHaveREIData();
    }

    /**
     * Test REI sample with REI data.
     */
    @Test
    public void sampleWithREIData() {
        probeTest.sampleWithREIData();
    }

    /**
     * Test REI sample without matching envMedium.
     */
    @Test
    public void reiSampleWithoutMatchingEnvMedium() {
        probeTest.reiSampleWithoutMatchingEnvMedium();
    }

    /**
     * Test rei sample with matching envMediumId.
     */
    @Test
    public void reiSampleWithMatchingEnvMedium() {
        probeTest.reiSampleWithMatchingEnvMedium();
    }


    /**
     * Test sample with sample specif but without matching env medium.
     */
    @Test
    public void sampleSpecifMesValWithoutMatchingEnvMedium() {
        probeTest.sampleSpecifMesValWithoutMatchingEnvMedium();
    }

    /**
     * Test sample with samplespecif with matching env medium.
     */
    @Test
    public void sampleSpecifMesValWithMatchingEnvMedium() {
        probeTest.sampleSpecifMesValWithMatchingEnvMedium();
    }

    /**
     * Test sample without a unique extId.
     */
    @Test
    public void noUniqueExtId() {
        probeTest.noUniqueExtId();
    }

    /**
     * Test sample with unique ext id.
     */
    @Test
    public void uniqueExtId() {
        probeTest.uniqueExtId();
    }

    /**
     * Test messung has nebenprobennr.
     */
    @Test
    public void messungHasNebenprobenNr() {
        messungTest.hasNebenprobenNr();
    }

    /**
     * Test messung has no nebenprobennr.
     */
    @Test
    public void messungHasNoNebenprobenNr() {
        messungTest.hasNoNebenprobenNr();
    }

    /**
     * Test messung has empty nebenprobennr.
     */
    @Test
    public void messungHasEmptyNebenprobenNr() {
        messungTest.hasEmptyNebenprobenNr();
    }

    /**
     * Test messung has unique nebenprobennr.
     */
    @Test
    public void messungUniqueNebenprobenNrNew() {
        messungTest.uniqueNebenprobenNrNew();
    }

    /**
     * Test messung unique nebenprobennr update.
     */
    @Test
    public void messungUniqueNebenprobenNrUpdate() {
        messungTest.uniqueNebenprobenNrUpdate();
    }

    /**
     * Test messung existing nebenprobennr new.
     */
    @Test
    public void messungExistingNebenprobenNrNew() {
        messungTest.existingNebenprobenNrNew();
    }

    /**
     * Test messung existing nebenprobennr update.
     */
    @Test
    public void messungExistingNebenprobenNrUpdate() {
        messungTest.existingNebenprobenNrUpdate();
    }

    /**
     * Test negative status kombi.
     */
    @Test
    public final void statusKombiNegative() {
        statusTest.checkKombiNegative();
    }

    /**
     * Test status with invalid order.
     */
    @Test
    public void statusInvalidStatusOrder() {
        statusTest.invalidStatusOrder();
    }

    /**
     * Test status with valid order.
     */
    @Test
    public void statusValidStatusOrder() {
        statusTest.validStatusOrder();
    }

    /**
     * Test setting status of measm connected to invalid REI sample.
     */
    @Test
    public void statusInvalidReiSample() {
        statusTest.statusInvalidReiSample();
    }

    /**
     * Test setting status of measm connected to valid REI sample.
     */
    @Test
    public void statusReiCompleteSample() {
        statusTest.statusReiCompleteSample();
    }

    /**
     * Test measm with start date in future.
     */
    @Test
    public void measmStartDateInFuture() {
        messungTest.measmStartDateInFuture();
    }

    /**
     * Test measm with start date before sampleStartDate.
     * @throws ParseException Thrown if date parsing fails
     */
    @Test
    public void measStartDateBeforeSampleStartDate() throws ParseException {
        messungTest.measmStartDateBeforeSampleStartDate();
    }

    /**
     * Test measm with start date before sampleStartDate.
     * @throws ParseException Thrown if date parsing fails
     */
    @Test
    public void measmStartDateAfterSampleStartDate() throws ParseException {
        messungTest.measmStartDateAfterSampleStartDate();
    }

    /**
     * Test measm without start date.
     */
    @Test
    public void measmWithoutStartDate() {
        messungTest.measmWithoutStartDate();
    }

    /**
     * Test measm without start date connected to a sample with regulation id 1.
     */
    @Test
    public void measmWithoutStartDateRegulation161Sample() {
        messungTest.measmWithoutStartDateRegulation161Sample();
    }

    /**
     * Test measm without a measPd.
     */
    @Test
    public void measmWithoutMeasPD() {
        messungTest.measmWithoutMeasPD();
    }

    /**
     * Test measm without a measPd connected to a sample with regulation id 1.
     */
    @Test
    public void measmWithoutMeasPDRegulation161Sample() {
        messungTest.measmWithoutMeasPDRegulation161Sample();
    }

    /**
     * Test measm without a measPd connected to a continuous sample.
     */
    @Test
    public void measmWithoutMeasPDRContSample() {
        messungTest.measmWithoutMeasPDRContSample();
    }

    /**
     * Test measm with measPd.
     */
    @Test
    public void measmWithMeasPd() {
        messungTest.measmWithMeasPd();
    }

    /**
     * Test measm missing obligatory measds.
     */
    @Test
    public void measmWithoutObligMeasd() {
        messungTest.measmWithoutObligMeasd();
    }

    /**
     * Test measm with all obligatory measds.
     */
    @Test
    public void measmWithObligMeasd() {
        messungTest.measmWithObligMeasd();
    }

    /**
     * Test mpg objects with valid start and end date below minimum value.
     */
    @Test
    public void mpgValidStartEndDateBelowMin() {
        mpgTest.validStartEndDateBelowMin();
    }

    /**
     * Test mpg objects with valid start and end date above maximum value.
     */
    @Test
    public void mpgValidStartEndDateAboveMax() {
        mpgTest.validStartEndDateAboveMax();
    }

    /**
     * Test mpg objects with proper valid start and end date.
     */
    @Test
    public void mpgValidStartEndDate() {
        mpgTest.validStartEndDate();
    }

    /**
     * Test mpg with pdStartDate smaller than valid.
     */
    @Test
    public void mpgYearIntervalStartDateSmallerThanValid() {
        mpgTest.yearIntervalStartDateSmallerThanValid();
    }

    /**
     * Test mpg with pdEndDate greater than valid end date.
     */
    @Test
    public void mpgYearIntervalStartDateGreaterThanValid() {
        mpgTest.yearIntervalStartDateGreaterThanValid();
    }

    /**
     * Test mpg with pdEndDate smaller than valid start date.
     */
    @Test
    public void mpgYearIntervalEndDateSmallerThanValid() {
        mpgTest.yearIntervalEndDateSmallerThanValid();
    }

    /**
     * Test mpg with pdEndDate greater than valid end date.
     */
    @Test
    public void mpgYearIntervalEndDateGreaterThanValid() {
        mpgTest.yearIntervalEndDateGreaterThanValid();
    }

    /**
     * Test mpg with invalid sample pd offset.
     */
    @Test
    public void mpgYearIntervalInvalidSamplePdOffset() {
        mpgTest.yearIntervalInvalidSamplePdOffset();
    }

    /**
     * Test mpg with year interval and valid dates.
     */
    @Test
    public void mpgYearValidInterval() {
        mpgTest.yearValidInterval();
    }

    /**
     * Test mpg with samplePdStartDate below lower limit.
     */
    @Test
    public void mpgSamplePdStartDateBelowLimit() {
        mpgTest.samplePdStartDateBelowLimit();
    }

    /**
     * Test mpg with samplePdEndDate below lower limit.
     */
    @Test
    public void mpgSamplePdEndDateBelowLimit() {
        mpgTest.samplePdEndDateBelowLimit();
    }

    /**
     * Test mpg with samplePdOffset below lower limit.
     */
    @Test
    public void mpgSamplePdOffsetBelowLimit() {
        mpgTest.samplePdOffsetBelowLimit();
    }

    /**
     * Test mpg with invalid samplePd.
     */
    @Test
    public void mpgInvalidSamplePd() {
        mpgTest.invalidSamplePd();
    }

    /**
     * Test mpg with samplePdStartDate greater than interval max.
     */
    @Test
    public void mpgSamplePdStartDateGreaterThanIntervalMax() {
        mpgTest.samplePdStartDateGreaterThanIntervalMax();
    }

    /**
     * Test mpg with samplePdEndDate greater than interval max.
     */
    @Test
    public void mpgSamplePdEndDateGreaterThanIntervalMax() {
        mpgTest.samplePdEndDateGreaterThanIntervalMax();
    }

    /**
     * Test mpg with samplePdOffset greater than interval max.
     */
    @Test
    public void mpgSamplePdOffsetGreaterThanIntervalMax() {
        mpgTest.samplePdOffsetGreaterThanIntervalMax();
    }

    /**
     * Test mpg with samplePdStart date greater than samplePdEnd.
     */
    @Test
    public void mpgSamplePdStartGreaterThanEnd() {
        mpgTest.samplePdStartGreaterThanEnd();
    }

    /**
     * Test mpg with invalid sampleSpecif.
     */
    @Test
    public void mpgSampleSpecifDoesNotExist() {
        mpgTest.sampleSpecifDoesNotExist();
    }

    /**
     * Test mpg with sampleSpecif.
     */
    @Test
    public void mpgSampleSpecifDoesExist() {
        mpgTest.sampleSpecifDoesExist();
    }

    /**
     * Test mpg with invalid envDescripDisplay.
     */
    @Test
    public void mpgEnvDescripDisplayInvalidDisplayString() {
        mpgTest.envDescripDisplayInvalidDisplayString();
    }

    /**
     * Test mpg with matching envMediumId.
     */
    @Test
    public void mpgEnvDescripWithMatchingEnvMediumId() {
        mpgTest.envDescripWithMatchingEnvMediumId();
    }

    /**
     * Test mpg without matching envMediumId.
     */
    @Test
    public void mpgEnvDescripWithoutMatchingEnvMediumId() {
        mpgTest.envDescripWithoutMatchingEnvMediumId();
    }

    /**
     * Test commSample with existing text.
     */
    @Test
    public void commSampleDuplicateText() {
        commSampleTest.commentDuplicateText();
    }

    /**
     * Test commSample with new text.
     */
    @Test
    public void commSampleUniqueText() {
        commSampleTest.commentUniqueText();
    }

    /**
     * Test commMeasm with existing text.
     */
    @Test
    public void commMeasmDuplicateText() {
        commMeasmTest.commentDuplicateText();
    }

    /**
     * Test commMeasm with new text.
     */
    @Test
    public void commMeasmUniqueText() {
        commMeasmTest.commentUniqueText();
    }
}
