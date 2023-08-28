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
