/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.importer.identification;

import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import jakarta.transaction.UserTransaction;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.intevation.lada.BaseTest;
import de.intevation.lada.model.lada.Sample;


/**
 * Unit tests for Identifier implementations.
 */
@RunWith(Arquillian.class)
public class SampleIdentifierTest extends BaseTest {

    @Resource
    private UserTransaction transaction;

    private final String mstId = "06010";

    @Inject
    private Identifier<Sample> probeIdentifier;

    public SampleIdentifierTest() {
        testDatasetName = "datasets/dbUnit_identifier_sample.xml";
    }

    /**
     * Identify probe objects.
     *
     * @throws Exception that can occur during the test.
     */
    @Test
    @Transactional
    public final void identifyProbeByHPNrMST() throws Exception {
        Sample probe = new Sample();
        probe.setMainSampleId("120510002");
        probe.setMeasFacilId(mstId);
        probe.setIsTest(false);

        Assert.assertNotNull(probeIdentifier.getExisting(probe));
    }

    /**
     * Identify probject by HP-Nr and MST.
     * @throws Exception that can occur during the test.
     */
    @Test
    @Transactional
    public final void identifyProbeByHPNrMSTNew() throws Exception {
        Sample probe = new Sample();
        probe.setMainSampleId("120510003");
        probe.setMeasFacilId(mstId);

        Assert.assertNull(probeIdentifier.getExisting(probe));
    }

    /**
     * Identify probe object by external probe id.
     *
     * @throws Exception that can occur during the test.
     */
    @Test
    @Transactional
    public final void identifyProbeByExterneProbeId() throws Exception {
        Sample probe = new Sample();
        probe.setExtId("T001");

        Assert.assertNotNull(probeIdentifier.getExisting(probe));
    }

    /**
     * Identify probe object by external id as new.
     * @throws Exception that can occur during test.
     */
    @Test
    @Transactional
    public final void identifyProbeByExterneProbeIdNew() throws Exception {
        Sample probe = new Sample();
        probe.setExtId("T002");

        Assert.assertNull(probeIdentifier.getExisting(probe));
    }

    /**
     * Identify probe object by external id as reject.
     * @throws Exception that can occur during the test.
     */
    @Test
    @Transactional
    public final void identifyProbeByExterneProbeIdReject() throws Exception {
        Sample probe = new Sample();
        probe.setExtId("T001");
        probe.setMainSampleId("120510003");
        probe.setMeasFacilId(mstId);

        Assert.assertThrows(
            IdentificationException.class,
            () -> probeIdentifier.getExisting(probe));
    }

    /**
     * Identify probe object by external id as update.
     * @throws Exception that ca occur during the test.
     */
    @Test
    @Transactional
    public final void identifyProbeByExterneProbeIdUpdate() throws Exception {
        Sample probe = new Sample();
        probe.setExtId("T001");
        probe.setMainSampleId("");
        probe.setMeasFacilId(mstId);

        Assert.assertNotNull(probeIdentifier.getExisting(probe));
    }
}
