/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada;

import jakarta.inject.Inject;

import org.jboss.arquillian.persistence.ApplyScriptBefore;
import org.jboss.arquillian.persistence.Cleanup;
import org.jboss.arquillian.persistence.TestExecutionPhase;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.logging.Logger;
import org.jboss.arquillian.junit.Arquillian;
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
@Ignore
@RunWith(Arquillian.class)
@ApplyScriptBefore("datasets/clean_and_seed.sql")
@Cleanup(phase = TestExecutionPhase.NONE)
// TODO make tests independent of test data which do not exist anymore
public class ValidatorTest extends BaseTest {

    private static Logger logger = Logger.getLogger(StammdatenTest.class);

    @Inject
    private ProbeTest probeTest;

    @Inject
    private MessungTest messungTest;

    @Inject
    private StatusTest statusTest;

    /**
     * Test hauptprobennr.
     */
    @Test
    public void probeHasHauptprobenNr() {
        probeTest.hasHauptprobenNr(testProtocol);
    }

    /**
     * Test hauptprobennr missing.
     */
    @Ignore
    @Test
    public void probeHasNoHauptprobenNr() {
        probeTest.hasNoHauptprobenNr(testProtocol);
    }

    /**
     * Test existing hauptprobennr new.
     */
    @Ignore
    @Test
    public void probeExistingHauptprobenNrNew() {
        probeTest.existingHauptprobenNrNew(testProtocol);
    }

    /**
     * Test unique hauptprobennr new.
     */
    @Ignore
    @Test
    public void probeUniqueHauptprobenNrNew() {
        probeTest.uniqueHauptprobenNrNew(testProtocol);
    }

    /**
     * Test existing hauptprobennr update.
     */
    @Ignore
    @Test
    public void probeExistingHauptprobenNrUpdate() {
        probeTest.existingHauptprobenNrUpdate(testProtocol);
    }

    /**
     * Test unique hauptprobennr update.
     */
    @Ignore
    @Test
    public void probeUniqueHauptprobenNrUpdate() {
        probeTest.uniqueHauptprobenNrUpdate(testProtocol);
    }

    /**
     * Test probe has entnahmeort.
     */
    @Ignore
    @Test
    public void probeHasEntnahmeOrt() {
        probeTest.hasEntnahmeOrt(testProtocol);
    }

    /**
     * Test probe has no entnahmeort.
     */
    @Ignore
    @Test
    public void probeHasNoEntnahmeOrt() {
        probeTest.hasNoEntnahmeOrt(testProtocol);
    }

    /**
     * Test probe has probenahmebegin.
     */
    @Ignore
    @Test
    public void probeHasProbenahmeBegin() {
        probeTest.hasProbeentnahmeBegin(testProtocol);
    }

    /**
     * Test probe has no probenahmebegin.
     */
    @Ignore
    @Test
    public void probeHasNoProbenahmeBegin() {
        probeTest.hasNoProbeentnahmeBegin(testProtocol);
    }

    /**
     * Test probe has no time end probenahmebegin.
     */
    @Ignore
    @Test
    public void probeTimeNoEndProbenahmeBegin() {
        probeTest.timeNoEndProbeentnahmeBegin(testProtocol);
    }

    /**
     * Test probe has no time begin probenahmebegin.
     */
    @Ignore
    @Test
    public void probeTimeNoBeginProbenahmeBegin() {
        probeTest.timeNoBeginProbeentnahmeBegin(testProtocol);
    }

    /**
     * Test probe time begin after end probenahmebegin.
     */
    @Ignore
    @Test
    public void probeTimeBeginAfterEndProbenahmeBegin() {
        probeTest.timeBeginAfterEndProbeentnahmeBegin(testProtocol);
    }

    /**
     * Test probe begin in future probenahmebegin.
     */
    @Ignore
    @Test
    public void probeTimeBeginFutureProbenahmeBegin() {
        probeTest.timeBeginFutureProbeentnahmeBegin(testProtocol);
    }

    /**
     * Test probe has umwelt.
     */
    @Ignore
    @Test
    public void probeHasUmwelt() {
        probeTest.hasUmwelt(testProtocol);
    }

    /**
     * Test probe has no umwelt.
     */
    @Ignore
    @Test
    public void probeHasNoUmwelt() {
        probeTest.hasNoUmwelt(testProtocol);
    }

    /**
     * Test probe has empty umwelt.
     */
    @Ignore
    @Test
    public void probeHasEmptyUmwelt() {
        probeTest.hasEmptyUmwelt(testProtocol);
    }

    /**
     * Test messung has nebenprobennr.
     */
    @Ignore
    @Test
    public void messungHasNebenprobenNr() {
        messungTest.hasNebenprobenNr(testProtocol);
    }

    /**
     * Test messung has no nebenprobennr.
     */
    @Ignore
    @Test
    public void messungHasNoNebenprobenNr() {
        messungTest.hasNoNebenprobenNr(testProtocol);
    }

    /**
     * Test messung has empty nebenprobennr.
     */
    @Ignore
    @Test
    public void messungHasEmptyNebenprobenNr() {
        messungTest.hasEmptyNebenprobenNr(testProtocol);
    }

    /**
     * Test messung has unique nebenprobennr.
     */
    @Ignore
    @Test
    public void messungUniqueNebenprobenNrNew() {
        messungTest.uniqueNebenprobenNrNew(testProtocol);
    }

    /**
     * Test messung unique nebenprobennr update.
     */
    @Ignore
    @Test
    public void messungUniqueNebenprobenNrUpdate() {
        messungTest.uniqueNebenprobenNrUpdate(testProtocol);
    }

    /**
     * Test messung existing nebenprobennr new.
     */
    @Test
    @UsingDataSet("datasets/dbUnit_probe.json")
    public void messungExistingNebenprobenNrNew() {
        messungTest.existingNebenprobenNrNew(testProtocol);
    }

    /**
     * Test messung existing nebenprobennr update.
     */
    @Ignore
    @Test
    public void messungExistingNebenprobenNrUpdate() {
        messungTest.existingNebenprobenNrUpdate(testProtocol);
    }

    /**
     * Test messung has messwert.
     */
    @Ignore
    @Test
    public void messungHasMesswert() {
        messungTest.hasMesswert(testProtocol);
    }

    /**
     * Test messung has no messwert.
     */
    @Ignore
    @Test
    public void messungHasNoMesswert() {
        messungTest.hasNoMesswert(testProtocol);
    }

    /**
     * Test negative status kombi.
     */
    @Test
    @UsingDataSet("datasets/dbUnit_probe.json")
    public final void statusKombiNegative() {
        statusTest.checkKombiNegative(testProtocol);
    }
}
