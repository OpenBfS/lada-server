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
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.junit.Ignore;
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
    @Transactional
    public void probeHasHauptprobenNr() {
        probeTest.hasHauptprobenNr(testProtocol);
    }

    /**
     * Test hauptprobennr missing.
     */
    @Ignore
    @Test
    @Transactional
    public void probeHasNoHauptprobenNr() {
        probeTest.hasNoHauptprobenNr(testProtocol);
    }

    /**
     * Test existing hauptprobennr new.
     */
    @Ignore
    @Test
    @Transactional
    public void probeExistingHauptprobenNrNew() {
        probeTest.existingHauptprobenNrNew(testProtocol);
    }

    /**
     * Test unique hauptprobennr new.
     */
    @Ignore
    @Test
    @Transactional
    public void probeUniqueHauptprobenNrNew() {
        probeTest.uniqueHauptprobenNrNew(testProtocol);
    }

    /**
     * Test existing hauptprobennr update.
     */
    @Ignore
    @Test
    @Transactional
    public void probeExistingHauptprobenNrUpdate() {
        probeTest.existingHauptprobenNrUpdate(testProtocol);
    }

    /**
     * Test unique hauptprobennr update.
     */
    @Ignore
    @Test
    @Transactional
    public void probeUniqueHauptprobenNrUpdate() {
        probeTest.uniqueHauptprobenNrUpdate(testProtocol);
    }

    /**
     * Test probe has entnahmeort.
     */
    @Ignore
    @Test
    @Transactional
    public void probeHasEntnahmeOrt() {
        probeTest.hasEntnahmeOrt(testProtocol);
    }

    /**
     * Test probe has no entnahmeort.
     */
    @Ignore
    @Test
    @Transactional
    public void probeHasNoEntnahmeOrt() {
        probeTest.hasNoEntnahmeOrt(testProtocol);
    }

    /**
     * Test probe has probenahmebegin.
     */
    @Ignore
    @Test
    @Transactional
    public void probeHasProbenahmeBegin() {
        probeTest.hasProbeentnahmeBegin(testProtocol);
    }

    /**
     * Test probe has no probenahmebegin.
     */
    @Ignore
    @Test
    @Transactional
    public void probeHasNoProbenahmeBegin() {
        probeTest.hasNoProbeentnahmeBegin(testProtocol);
    }

    /**
     * Test probe has no time end probenahmebegin.
     */
    @Ignore
    @Test
    @Transactional
    public void probeTimeNoEndProbenahmeBegin() {
        probeTest.timeNoEndProbeentnahmeBegin(testProtocol);
    }

    /**
     * Test probe has no time begin probenahmebegin.
     */
    @Ignore
    @Test
    @Transactional
    public void probeTimeNoBeginProbenahmeBegin() {
        probeTest.timeNoBeginProbeentnahmeBegin(testProtocol);
    }

    /**
     * Test probe time begin after end probenahmebegin.
     */
    @Ignore
    @Test
    @Transactional
    public void probeTimeBeginAfterEndProbenahmeBegin() {
        probeTest.timeBeginAfterEndProbeentnahmeBegin(testProtocol);
    }

    /**
     * Test probe begin in future probenahmebegin.
     */
    @Ignore
    @Test
    @Transactional
    public void probeTimeBeginFutureProbenahmeBegin() {
        probeTest.timeBeginFutureProbeentnahmeBegin(testProtocol);
    }

    /**
     * Test probe has umwelt.
     */
    @Ignore
    @Test
    @Transactional
    public void probeHasUmwelt() {
        probeTest.hasUmwelt(testProtocol);
    }

    /**
     * Test probe has no umwelt.
     */
    @Ignore
    @Test
    @Transactional
    public void probeHasNoUmwelt() {
        probeTest.hasNoUmwelt(testProtocol);
    }

    /**
     * Test probe has empty umwelt.
     */
    @Ignore
    @Test
    @Transactional
    public void probeHasEmptyUmwelt() {
        probeTest.hasEmptyUmwelt(testProtocol);
    }

    /**
     * Test messung has nebenprobennr.
     */
    @Ignore
    @Test
    @Transactional
    public void messungHasNebenprobenNr() {
        messungTest.hasNebenprobenNr(testProtocol);
    }

    /**
     * Test messung has no nebenprobennr.
     */
    @Ignore
    @Test
    @Transactional
    public void messungHasNoNebenprobenNr() {
        messungTest.hasNoNebenprobenNr(testProtocol);
    }

    /**
     * Test messung has empty nebenprobennr.
     */
    @Ignore
    @Test
    @Transactional
    public void messungHasEmptyNebenprobenNr() {
        messungTest.hasEmptyNebenprobenNr(testProtocol);
    }

    /**
     * Test messung has unique nebenprobennr.
     */
    @Ignore
    @Test
    @Transactional
    public void messungUniqueNebenprobenNrNew() {
        messungTest.uniqueNebenprobenNrNew(testProtocol);
    }

    /**
     * Test messung unique nebenprobennr update.
     */
    @Ignore
    @Test
    @Transactional
    public void messungUniqueNebenprobenNrUpdate() {
        messungTest.uniqueNebenprobenNrUpdate(testProtocol);
    }

    /**
     * Test messung existing nebenprobennr new.
     */
    @Test
    @Transactional
    public void messungExistingNebenprobenNrNew() {
        messungTest.existingNebenprobenNrNew(testProtocol);
    }

    /**
     * Test messung existing nebenprobennr update.
     */
    @Ignore
    @Test
    @Transactional
    public void messungExistingNebenprobenNrUpdate() {
        messungTest.existingNebenprobenNrUpdate(testProtocol);
    }

    /**
     * Test messung has messwert.
     */
    @Ignore
    @Test
    @Transactional
    public void messungHasMesswert() {
        messungTest.hasMesswert(testProtocol);
    }

    /**
     * Test messung has no messwert.
     */
    @Ignore
    @Test
    @Transactional
    public void messungHasNoMesswert() {
        messungTest.hasNoMesswert(testProtocol);
    }

    /**
     * Test negative status kombi.
     */
    @Test
    @Transactional
    public final void statusKombiNegative() {
        statusTest.checkKombiNegative(testProtocol);
    }
}
