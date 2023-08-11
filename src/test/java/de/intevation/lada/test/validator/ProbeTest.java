/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.validator;

import java.sql.Timestamp;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.junit.Assert;

import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Validator;
import de.intevation.lada.validation.Violation;

/**
 * Test probe validations.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Transactional
public class ProbeTest {

    private static final long TS1 = 1376287046510L;
    private static final long TS2 = 1376287046511L;
    private static final long TS3 = 2376287046511L;
    private static final int ID710 = 710;

    @Inject
    private Validator<Sample> validator;

    /**
     * Test hauptprobennr.
     */
    public void hasHauptprobenNr() {
        Sample probe = new Sample();
        probe.setMainSampleId("4554567890");
        Violation violation = validator.validate(probe);
        if (violation.hasErrors()) {
            Assert.assertFalse(
                violation.getErrors().containsKey("mainSampleId"));
        }
    }

    /**
     * Test no hauptprobennr.
     */
    public void hasNoHauptprobenNr() {
        Sample probe = new Sample();
        Violation violation = validator.validate(probe);
        Assert.assertTrue(violation.hasErrors());
        Assert.assertTrue(violation.getErrors().containsKey("mainSampleId"));
        Assert.assertTrue(
            violation.getErrors().get("mainSampleId").contains(
                StatusCodes.VALUE_MISSING));
    }

    /**
     * Test new existing hpnr.
     */
    public void existingHauptprobenNrNew() {
        Sample probe = new Sample();
        probe.setMainSampleId("120510002");
        Violation violation = validator.validate(probe);
        Assert.assertTrue(violation.hasErrors());
        Assert.assertTrue(violation.getErrors().containsKey("mainSampleId"));
        Assert.assertTrue(
            violation.getErrors().get("mainSampleId").contains(
                StatusCodes.VALUE_AMBIGOUS));
    }

    /**
     * Test new unique hpnr.
     */
    public void uniqueHauptprobenNrNew() {
        Sample probe = new Sample();
        probe.setMainSampleId("4564567890");
        Violation violation = validator.validate(probe);
        if (violation.hasErrors()) {
            Assert.assertFalse(
                violation.getErrors().containsKey("mainSampleId"));
        }
    }

    /**
     * Test update unique hpnr.
     */
    public void uniqueHauptprobenNrUpdate() {
        Sample probe = new Sample();
        probe.setId(1);
        probe.setMainSampleId("4564567890");
        Violation violation = validator.validate(probe);
        if (violation.hasErrors()) {
            Assert.assertFalse(
                violation.getErrors().containsKey("mainSampleId"));
        }
    }

    /**
     * Test update of existing hpnr..
     */
    public void existingHauptprobenNrUpdate() {
        Sample probe = new Sample();
        probe.setId(1);
        probe.setMainSampleId("120224003");
        Violation violation = validator.validate(probe);
        Assert.assertTrue(violation.hasErrors());
        Assert.assertTrue(violation.getErrors().containsKey("mainSampleId"));
        Assert.assertTrue(
            violation.getErrors().get("mainSampleId").contains(
                StatusCodes.VALUE_AMBIGOUS));
    }

    /**
     * Test entnahmeort.
     */
    public void hasEntnahmeOrt() {
        Sample probe = new Sample();
        probe.setId(1);
        Violation violation = validator.validate(probe);
        if (violation.hasWarnings()) {
            Assert.assertFalse(
                violation.getWarnings().containsKey("entnahmeOrt"));
        }
    }

    /**
     * Test no entnahmeort.
     */
    public void hasNoEntnahmeOrt() {
        Sample probe = new Sample();
        probe.setId(ID710);
        Violation violation = validator.validate(probe);
        Assert.assertTrue(violation.hasWarnings());
        Assert.assertTrue(violation.getWarnings().containsKey("entnahmeOrt"));
        Assert.assertTrue(
            violation.getWarnings().get("entnahmeOrt").contains(
                StatusCodes.VALUE_MISSING));
    }

    /**
     * Test probenahmebegin.
     */
    public void hasProbeentnahmeBegin() {
        Sample probe = new Sample();
        probe.setSampleStartDate(new Timestamp(TS1));
        probe.setSampleEndDate(new Timestamp(TS2));
        Violation violation = validator.validate(probe);
        if (violation.hasWarnings()) {
            Assert.assertFalse(
                violation.getWarnings().containsKey("probeentnahmeBeginn"));
        }
    }

    /**
     * Test no probenahme begin.
     */
    public void hasNoProbeentnahmeBegin() {
        Sample probe = new Sample();
        Violation violation = validator.validate(probe);
        Assert.assertTrue(violation.hasWarnings());
        Assert.assertTrue(
            violation.getWarnings().containsKey("probeentnahmeBeginn"));
        Assert.assertTrue(
            violation.getWarnings().get("probeentnahmeBeginn").contains(
                StatusCodes.VALUE_MISSING));
    }

    /**
     * Test probenahme begin without end.
     */
    public void timeNoEndProbeentnahmeBegin() {
        Sample probe = new Sample();
        probe.setSampleStartDate(new Timestamp(TS1));
        Violation violation = validator.validate(probe);
        if (violation.hasWarnings()) {
            Assert.assertFalse(
                violation.getWarnings().containsKey("probeentnahmeBeginn"));
        }
    }

    /**
     * Test probenahme begin without begin.
     */
    public void timeNoBeginProbeentnahmeBegin() {
        Sample probe = new Sample();
        probe.setSampleEndDate(new Timestamp(TS1));
        Violation violation = validator.validate(probe);
        Assert.assertTrue(
            violation.getWarnings().get("probeentnahmeBeginn").contains(
                StatusCodes.VALUE_MISSING));
        Assert.assertTrue(
            violation.getWarnings().get("probeentnahmeBeginn").contains(
                StatusCodes.DATE_BEGIN_AFTER_END));
    }

    /**
     * Test probenahme begin after end.
     */
    public void timeBeginAfterEndProbeentnahmeBegin(
    ) {
        Sample probe = new Sample();
        probe.setSampleStartDate(new Timestamp(TS2));
        probe.setSampleEndDate(new Timestamp(TS1));
        Violation violation = validator.validate(probe);
        Assert.assertTrue(
            violation.getWarnings().get("probeentnahmeBeginn").contains(
                StatusCodes.DATE_BEGIN_AFTER_END));
    }

    /**
     * Test probenahmebegin in future.
     */
    public void timeBeginFutureProbeentnahmeBegin() {
        Sample probe = new Sample();
        probe.setSampleStartDate(new Timestamp(TS3));
        Violation violation = validator.validate(probe);
        Assert.assertTrue(
            violation.getWarnings().get("probeentnahmeBeginn").contains(
            StatusCodes.DATE_IN_FUTURE));
    }

    /**
     * Test umwelt.
     * @param protocol the test protocol.
     */
    public void hasUmwelt() {
        Sample probe = new Sample();
        probe.setEnvMediumId("A4");
        Violation violation = validator.validate(probe);
        if (violation.hasWarnings()) {
            Assert.assertFalse(violation.getWarnings().containsKey("umwId"));
        }
    }

    /**
     * Test no umwelt.
     * @param protocol the test protocol.
     */
    public void hasNoUmwelt() {
        Sample probe = new Sample();
        Violation violation = validator.validate(probe);
        Assert.assertTrue(violation.hasWarnings());
        Assert.assertTrue(violation.getWarnings().containsKey("umwId"));
        Assert.assertTrue(violation.getWarnings().get("umwId").contains(
                StatusCodes.VALUE_MISSING));
    }

    /**
     * Test empty umwelt.
     * @param protocol the test protocol.
     */
    public void hasEmptyUmwelt() {
        Sample probe = new Sample();
        probe.setEnvMediumId("");
        Violation violation = validator.validate(probe);
        Assert.assertTrue(violation.hasWarnings());
        Assert.assertTrue(violation.getWarnings().containsKey("umwId"));
        Assert.assertTrue(violation.getWarnings().get("umwId").contains(
                StatusCodes.VALUE_MISSING));
    }
}
