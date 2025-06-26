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
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Sample;


/**
 * Unit tests for Identifier implementations.
 */
@RunWith(Arquillian.class)
public class MeasmIdentifierTest extends BaseTest {

    private static final int PID1000 = 1000;

    @Resource
    private UserTransaction transaction;

    @Inject
    private Identifier<Measm> messungIdentifier;

    private String mmt42 = "42";
    private int existingMeasmIdMmtA3 = 1200;
    private int existingMeasmIdMmt42 = 1201;
    private final String existingMinSampleId = "06A0";

    public MeasmIdentifierTest() {
        testDatasetName = "datasets/dbUnit_identifier_measm.xml";
    }

    /**
     * Identify messung object by np nr.
     * @throws Exception that can occur during the test.
     */
    @Test
    @Transactional
    public final void identifyMessungByNpNr() throws Exception {
        Measm messung = new Measm();
        Sample sample = new Sample();
        sample.setId(PID1000);
        messung.setSample(sample);
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
        Sample sample = new Sample();
        sample.setId(PID1000);
        messung.setSample(sample);
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
        Sample sample = new Sample();
        sample.setId(PID1000);
        messung.setSample(sample);
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
        Sample sample = new Sample();
        sample.setId(PID1000);
        messung.setSample(sample);
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
        Sample sample = new Sample();
        sample.setId(PID1000);
        messung.setSample(sample);

        Assert.assertThrows(
            IdentificationException.class,
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
        Sample sample = new Sample();
        sample.setId(PID1000);
        messung.setSample(sample);
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
        Sample sample = new Sample();
        sample.setId(PID1000);
        measm.setSample(sample);
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
        Sample sample = new Sample();
        sample.setId(PID1000);
        measm.setSample(sample);
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
        Sample sample = new Sample();
        sample.setId(PID1000);
        measm.setSample(sample);
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
        Sample sample = new Sample();
        sample.setId(PID1000);
        measm.setSample(sample);
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
        Sample sample = new Sample();
        sample.setId(PID1000);
        measm.setSample(sample);
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
        Sample sample = new Sample();
        sample.setId(PID1000);
        measm.setSample(sample);
        measm.setMmtId("43");

        Assert.assertNull(messungIdentifier.getExisting(measm));
    }
}
