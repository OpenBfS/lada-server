/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.importer;

import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import jakarta.transaction.UserTransaction;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.intevation.lada.BaseTest;
import de.intevation.lada.importer.Identifier.IdentificationException;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Sample;


/**
 * Unit tests for Identifier implementations.
 */
@RunWith(Arquillian.class)
public class IdentifierTest extends BaseTest {

    private static final int PID1000 = 1000;

    @Resource
    private UserTransaction transaction;

    private final String mstId = "06010";

    @Inject
    private Identifier<Sample> probeIdentifier;

    @Inject
    private Identifier<Measm> messungIdentifier;

    private String mmt42 = "42";
    private int existingMeasmIdMmtA3 = 1200;
    private int existingMeasmIdMmt42 = 1201;
    private final String existingMinSampleId = "06A0";

    public IdentifierTest() {
        testDatasetName = "datasets/dbUnit_identifier.xml";
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
            Identifier.IdentificationException.class,
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

    /**
     * Identify messung object by np nr.
     * @throws Exception that can occur during the test.
     */
    @Test
    @Transactional
    public final void identifyMessungByNpNr() throws Exception {
        Measm messung = new Measm();
        messung.setSampleId(PID1000);
        messung.setMinSampleId(existingMinSampleId);

        Assert.assertEquals(existingMeasmIdMmtA3,
            messungIdentifier.getExisting(messung).getId().intValue());
    }

    /**
     * Identify messung object by np nr. as new.
     * @throws Exception that can occur during the test.
     */
    @Test
    @Transactional
    public final void identifyMessungByNpNrNew() throws Exception {
        Measm messung = new Measm();
        messung.setSampleId(PID1000);
        messung.setMinSampleId("06A1");

        Assert.assertNull(messungIdentifier.getExisting(messung));
    }

    /**
     * Identify messung object by external id.
     * @throws Exception that can occur during the test.
     */
    @Test
    @Transactional
    public final void identifyMessungByExterneMessungsId() throws Exception {
        Measm messung = new Measm();
        messung.setSampleId(PID1000);
        messung.setExtId(1);

        Assert.assertEquals(existingMeasmIdMmtA3,
            messungIdentifier.getExisting(messung).getId().intValue());
    }

    /**
     * Identify messung object by external id as new.
     * @throws Exception that can occur during the test.
     */
    @Test
    @Transactional
    public final void identifyMessungByExterneMessungsIdNew() throws Exception {
        Measm messung = new Measm();
        messung.setSampleId(PID1000);
        messung.setExtId(2);

        Assert.assertNull(messungIdentifier.getExisting(messung));
    }

    /**
     * Unidentifiable Measm object.
     * @throws Exception that can occur during the test.
     */
    @Test
    @Transactional
    public final void identifyMessungReject()
    throws Exception {
        Measm messung = new Measm();
        messung.setSampleId(PID1000);

        Assert.assertThrows(
            Identifier.IdentificationException.class,
            () -> messungIdentifier.getExisting(messung));
    }

    /**
     * Identify messung object by external id as update.
     * @throws Exception that can occur during the test.
     */
    @Test
    @Transactional
    public final void identifyMessungByExterneMessungsIdUpdate()
    throws Exception {
        Measm messung = new Measm();
        messung.setSampleId(PID1000);
        messung.setExtId(1);
        messung.setMinSampleId("");

        Assert.assertEquals(existingMeasmIdMmtA3,
            messungIdentifier.getExisting(messung).getId().intValue());
    }

    /**
     * Primarily identify Measm by extId: Identify as new, even if minSampleId
     * exists.
     */
    @Test
    @Transactional
    public final void identifyMeasmPrimarilyByExtId()
        throws IdentificationException {
        Measm measm = new Measm();
        measm.setSampleId(PID1000);
        measm.setExtId(2);
        measm.setMinSampleId(existingMinSampleId);

        Assert.assertNull(messungIdentifier.getExisting(measm));
    }

    /**
     * Identify as Measm with unique mmtId and without minSampleId.
     */
    @Test
    @Transactional
    public final void identifyMeasmWithMinSampleIdFallBackToMmt()
        throws IdentificationException {
        Measm measm = new Measm();
        measm.setSampleId(PID1000);
        measm.setMinSampleId("XXX");
        measm.setMmtId(mmt42);

        Assert.assertEquals(existingMeasmIdMmt42,
            messungIdentifier.getExisting(measm).getId().intValue());
    }

    /**
     * Do not identify as Measm with unique mmtId if it has minSampleId.
     */
    @Test
    @Transactional
    public final void identifyMeasmWithMinSampleIdFallBackToMmtNoResult()
        throws IdentificationException {
        Measm measm = new Measm();
        measm.setSampleId(PID1000);
        measm.setMinSampleId("XXX");
        measm.setMmtId("A3");

        Assert.assertNull(messungIdentifier.getExisting(measm));
    }

    /**
     * Do not identify Measm with new minSampleId as Measm with non-unique mmtId.
     */
    @Test
    @Transactional
    public final void identifyMeasmWithMinSampleIdFallBackToMmtNonUnique()
        throws IdentificationException {
        Measm measm = new Measm();
        measm.setSampleId(PID1000);
        measm.setMinSampleId("XXX");
        measm.setMmtId("43");

        Assert.assertNull(messungIdentifier.getExisting(measm));
    }

    /**
     * Identify as Measm with unique mmtId.
     */
    @Test
    @Transactional
    public final void identifyMeasmFallBackToMmt()
        throws IdentificationException {
        Measm measm = new Measm();
        measm.setSampleId(PID1000);
        measm.setMmtId(mmt42);

        Assert.assertEquals(existingMeasmIdMmt42,
            messungIdentifier.getExisting(measm).getId().intValue());
    }

    /**
     * Do not identify as Measm with non-unique mmtId.
     */
    @Test
    @Transactional
    public final void identifyMeasmFallBackToMmtNoResult()
        throws IdentificationException {
        Measm measm = new Measm();
        measm.setSampleId(PID1000);
        measm.setMmtId("43");

        Assert.assertNull(messungIdentifier.getExisting(measm));
    }
}
