/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada;

import javax.inject.Inject;

import org.jboss.logging.Logger;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.intevation.lada.test.validator.MessungTest;
import de.intevation.lada.test.validator.ProbeTest;
import de.intevation.lada.test.validator.StatusTest;


/**
 * Test validators.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@RunWith(Arquillian.class)
// TODO make tests independent of test data which do not exist anymore
public class ValidatorTest extends BaseTest {

    private static Logger logger = Logger.getLogger(StammdatenTest.class);

    @Inject
    private ProbeTest probeTest;

    @Inject
    private MessungTest messungTest;

    @Inject
    private StatusTest statusTest;

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
    //TODO: Fails
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
    //TODO: fails
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
}
