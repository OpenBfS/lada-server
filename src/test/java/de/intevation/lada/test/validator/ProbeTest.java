/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.validator;

import java.sql.Timestamp;
import java.util.List;

import javax.inject.Inject;

import org.junit.Assert;

import de.intevation.lada.Protocol;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Validator;
import de.intevation.lada.validation.Violation;

/**
 * Test probe validations.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class ProbeTest {

    private static final long TS1 = 1376287046510L;
    private static final long TS2 = 1376287046511L;
    private static final long TS3 = 2376287046511L;
    private static final int ID710 = 710;

    @Inject
    private Validator<Sample> validator;

    /**
     * Test hauptprobennr.
     * @param protocol the test protocol.
     */
    public final void hasHauptprobenNr(List<Protocol> protocol) {
        Protocol prot = new Protocol();
        prot.setName("ProbeValidator");
        prot.setType("has mainSampleId");
        prot.setPassed(false);
        protocol.add(prot);
        Sample probe = new Sample();
        probe.setMainSampleId("4554567890");
        Violation violation = validator.validate(probe);
        if (violation.hasErrors()) {
            Assert.assertFalse(
                violation.getErrors().containsKey("mainSampleId"));
        }
        prot.setPassed(true);
    }

    /**
     * Test no hauptprobennr.
     * @param protocol the test protocol.
     */
    public final void hasNoHauptprobenNr(List<Protocol> protocol) {
        Protocol prot = new Protocol();
        prot.setName("ProbeValidator");
        prot.setType("has no mainSampleId");
        prot.setPassed(false);
        protocol.add(prot);
        Sample probe = new Sample();
        Violation violation = validator.validate(probe);
        Assert.assertTrue(violation.hasErrors());
        Assert.assertTrue(violation.getErrors().containsKey("mainSampleId"));
        Assert.assertTrue(
            violation.getErrors().get("mainSampleId").contains(
                StatusCodes.VALUE_MISSING));
        prot.setPassed(true);
    }

    /**
     * Test new existing hpnr.
     * @param protocol the test protocol.
     */
    public final void existingHauptprobenNrNew(List<Protocol> protocol) {
        Protocol prot = new Protocol();
        prot.setName("ProbeValidator");
        prot.setType("existing mainSampleId (new)");
        prot.setPassed(false);
        protocol.add(prot);
        Sample probe = new Sample();
        probe.setMainSampleId("120510002");
        prot.addInfo("mainSampleId", "120510002");
        Violation violation = validator.validate(probe);
        Assert.assertTrue(violation.hasErrors());
        Assert.assertTrue(violation.getErrors().containsKey("mainSampleId"));
        Assert.assertTrue(
            violation.getErrors().get("mainSampleId").contains(
                StatusCodes.VALUE_AMBIGOUS));
        prot.setPassed(true);
    }

    /**
     * Test new unique hpnr.
     * @param protocol the test protocol.
     */
    public final void uniqueHauptprobenNrNew(List<Protocol> protocol) {
        Protocol prot = new Protocol();
        prot.setName("ProbeValidator");
        prot.setType("unique mainSampleId (new)");
        prot.setPassed(false);
        protocol.add(prot);
        Sample probe = new Sample();
        probe.setMainSampleId("4564567890");
        prot.addInfo("mainSampleId", "4564567890");
        Violation violation = validator.validate(probe);
        if (violation.hasErrors()) {
            Assert.assertFalse(
                violation.getErrors().containsKey("mainSampleId"));
        }
        prot.setPassed(true);
    }

    /**
     * Test update unique hpnr.
     * @param protocol the test protocol.
     */
    public final void uniqueHauptprobenNrUpdate(List<Protocol> protocol) {
        Protocol prot = new Protocol();
        prot.setName("ProbeValidator");
        prot.setType("unique mainSampleId (update)");
        prot.setPassed(false);
        protocol.add(prot);
        Sample probe = new Sample();
        probe.setId(1);
        probe.setMainSampleId("4564567890");
        prot.addInfo("mainSampleId", "4564567890");
        Violation violation = validator.validate(probe);
        if (violation.hasErrors()) {
            Assert.assertFalse(
                violation.getErrors().containsKey("mainSampleId"));
        }
        prot.setPassed(true);
    }

    /**
     * Test update of existing hpnr..
     * @param protocol the test protocol.
     */
    public final void existingHauptprobenNrUpdate(List<Protocol> protocol) {
        Protocol prot = new Protocol();
        prot.setName("ProbeValidator");
        prot.setType("existing mainSampleId (update)");
        prot.setPassed(false);
        protocol.add(prot);
        Sample probe = new Sample();
        probe.setId(1);
        probe.setMainSampleId("120224003");
        prot.addInfo("mainSampleId", "120224003");
        Violation violation = validator.validate(probe);
        Assert.assertTrue(violation.hasErrors());
        Assert.assertTrue(violation.getErrors().containsKey("mainSampleId"));
        Assert.assertTrue(
            violation.getErrors().get("mainSampleId").contains(
                StatusCodes.VALUE_AMBIGOUS));
        prot.setPassed(true);
    }

    /**
     * Test entnahmeort.
     * @param protocol the test protocol.
     */
    public final void hasEntnahmeOrt(List<Protocol> protocol) {
        Protocol prot = new Protocol();
        prot.setName("ProbeValidator");
        prot.setType("has entnahmeOrt");
        prot.setPassed(false);
        protocol.add(prot);
        Sample probe = new Sample();
        probe.setId(1);
        Violation violation = validator.validate(probe);
        if (violation.hasWarnings()) {
            Assert.assertFalse(
                violation.getWarnings().containsKey("entnahmeOrt"));
        }
        prot.setPassed(true);
    }

    /**
     * Test no entnahmeort.
     * @param protocol the test protocol.
     */
    public final void hasNoEntnahmeOrt(List<Protocol> protocol) {
        Protocol prot = new Protocol();
        prot.setName("ProbeValidator");
        prot.setType("has no entnahmeOrt");
        prot.setPassed(false);
        protocol.add(prot);
        Sample probe = new Sample();
        probe.setId(ID710);
        Violation violation = validator.validate(probe);
        Assert.assertTrue(violation.hasWarnings());
        Assert.assertTrue(violation.getWarnings().containsKey("entnahmeOrt"));
        Assert.assertTrue(
            violation.getWarnings().get("entnahmeOrt").contains(
                StatusCodes.VALUE_MISSING));
        prot.setPassed(true);
    }

    /**
     * Test probenahmebegin.
     * @param protocol the test protocol.
     */
    public final void hasProbeentnahmeBegin(List<Protocol> protocol) {
        Protocol prot = new Protocol();
        prot.setName("ProbeValidator");
        prot.setType("has probeentnahmeBegin");
        prot.setPassed(false);
        protocol.add(prot);
        Sample probe = new Sample();
        probe.setSampleStartDate(new Timestamp(TS1));
        probe.setSampleEndDate(new Timestamp(TS2));
        Violation violation = validator.validate(probe);
        if (violation.hasWarnings()) {
            Assert.assertFalse(
                violation.getWarnings().containsKey("probeentnahmeBeginn"));
        }
        prot.setPassed(true);
    }

    /**
     * Test no probenahme begin.
     * @param protocol the test protocol.
     */
    public final void hasNoProbeentnahmeBegin(List<Protocol> protocol) {
        Protocol prot = new Protocol();
        prot.setName("ProbeValidator");
        prot.setType("has no probeentnahmeBegin");
        prot.setPassed(false);
        protocol.add(prot);
        Sample probe = new Sample();
        Violation violation = validator.validate(probe);
        Assert.assertTrue(violation.hasWarnings());
        Assert.assertTrue(
            violation.getWarnings().containsKey("probeentnahmeBeginn"));
        Assert.assertTrue(
            violation.getWarnings().get("probeentnahmeBeginn").contains(
                StatusCodes.VALUE_MISSING));
        prot.setPassed(true);
    }

    /**
     * Test probenahme begin without end.
     * @param protocol the test protocol.
     */
    public final void timeNoEndProbeentnahmeBegin(List<Protocol> protocol) {
        Protocol prot = new Protocol();
        prot.setName("ProbeValidator");
        prot.setType("time no end probeentnahmeBegin");
        prot.setPassed(false);
        protocol.add(prot);
        Sample probe = new Sample();
        probe.setSampleStartDate(new Timestamp(TS1));
        Violation violation = validator.validate(probe);
        if (violation.hasWarnings()) {
            Assert.assertFalse(
                violation.getWarnings().containsKey("probeentnahmeBeginn"));
        }
        prot.setPassed(true);
    }

    /**
     * Test probenahme begin without begin.
     * @param protocol the test protocol.
     */
    public final void timeNoBeginProbeentnahmeBegin(List<Protocol> protocol) {
        Protocol prot = new Protocol();
        prot.setName("ProbeValidator");
        prot.setType("time no begin probeentnahmeBegin");
        prot.setPassed(false);
        protocol.add(prot);
        Sample probe = new Sample();
        probe.setSampleEndDate(new Timestamp(TS1));
        Violation violation = validator.validate(probe);
        Assert.assertTrue(
            violation.getWarnings().get("probeentnahmeBeginn").contains(
                StatusCodes.VALUE_MISSING));
        Assert.assertTrue(
            violation.getWarnings().get("probeentnahmeBeginn").contains(
                StatusCodes.DATE_BEGIN_AFTER_END));
        prot.setPassed(true);
    }

    /**
     * Test probenahme begin after end.
     * @param protocol the test protocol.
     */
    public final void timeBeginAfterEndProbeentnahmeBegin(
        List<Protocol> protocol
    ) {
        Protocol prot = new Protocol();
        prot.setName("ProbeValidator");
        prot.setType("time begin after end probeentnahmeBegin");
        prot.setPassed(false);
        protocol.add(prot);
        Sample probe = new Sample();
        probe.setSampleStartDate(new Timestamp(TS2));
        probe.setSampleEndDate(new Timestamp(TS1));
        Violation violation = validator.validate(probe);
        Assert.assertTrue(
            violation.getWarnings().get("probeentnahmeBeginn").contains(
                StatusCodes.DATE_BEGIN_AFTER_END));
        prot.setPassed(true);
    }

    /**
     * Test probenahmebegin in future.
     * @param protocol the test protocol.
     */
    public final void timeBeginFutureProbeentnahmeBegin(
        List<Protocol> protocol
    ) {
        Protocol prot = new Protocol();
        prot.setName("ProbeValidator");
        prot.setType("time begin in future probeentnahmeBegin");
        prot.setPassed(false);
        protocol.add(prot);
        Sample probe = new Sample();
        probe.setSampleStartDate(new Timestamp(TS3));
        Violation violation = validator.validate(probe);
        Assert.assertTrue(
            violation.getWarnings().get("probeentnahmeBeginn").contains(
            StatusCodes.DATE_IN_FUTURE));
        prot.setPassed(true);
    }

    /**
     * Test umwelt.
     * @param protocol the test protocol.
     */
    public final void hasUmwelt(List<Protocol> protocol) {
        Protocol prot = new Protocol();
        prot.setName("ProbeValidator");
        prot.setType("has Umwelt");
        prot.setPassed(false);
        protocol.add(prot);
        Sample probe = new Sample();
        probe.setEnvMediumId("A4");
        Violation violation = validator.validate(probe);
        if (violation.hasWarnings()) {
            Assert.assertFalse(violation.getWarnings().containsKey("umwId"));
        }
        prot.setPassed(true);
    }

    /**
     * Test no umwelt.
     * @param protocol the test protocol.
     */
    public final void hasNoUmwelt(List<Protocol> protocol) {
        Protocol prot = new Protocol();
        prot.setName("ProbeValidator");
        prot.setType("has no Umwelt");
        prot.setPassed(false);
        protocol.add(prot);
        Sample probe = new Sample();
        Violation violation = validator.validate(probe);
        Assert.assertTrue(violation.hasWarnings());
        Assert.assertTrue(violation.getWarnings().containsKey("umwId"));
        Assert.assertTrue(violation.getWarnings().get("umwId").contains(
                StatusCodes.VALUE_MISSING));
        prot.setPassed(true);
    }

    /**
     * Test empty umwelt.
     * @param protocol the test protocol.
     */
    public final void hasEmptyUmwelt(List<Protocol> protocol) {
        Protocol prot = new Protocol();
        prot.setName("ProbeValidator");
        prot.setType("has empty Umwelt");
        prot.setPassed(false);
        protocol.add(prot);
        Sample probe = new Sample();
        probe.setEnvMediumId("");
        Violation violation = validator.validate(probe);
        Assert.assertTrue(violation.hasWarnings());
        Assert.assertTrue(violation.getWarnings().containsKey("umwId"));
        Assert.assertTrue(violation.getWarnings().get("umwId").contains(
                StatusCodes.VALUE_MISSING));
        prot.setPassed(true);
    }
}
