/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.importer;

import static de.intevation.lada.model.NamingStrategy.camelToSnake;

import java.sql.Timestamp;
import java.util.List;

import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import jakarta.transaction.UserTransaction;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.intevation.lada.BaseTest;
import de.intevation.lada.model.lada.MeasVal;
import de.intevation.lada.model.lada.MeasVal_;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.lada.SampleSpecifMeasVal;
import de.intevation.lada.model.lada.SampleSpecifMeasVal_;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;


/**
 * Unit tests for ObjectMerger.
 */
@RunWith(Arquillian.class)
public class ObjectMergerTest extends BaseTest {

    private static final double MESS15D = 1.5d;
    private static final int MGID56 = 56;
    private static final int MEHID207 = 207;
    private static final int MDAUER1000 = 1000;
    private static final int MID1200 = 1200;
    private static final int PNID = 726;
    private static final int MPRID1000 = 1000;
    private static final int PID1000 = 1000;
    private static final Integer DID9 = 9;

    @Resource
    private UserTransaction transaction;

    private final String mstId = "06010";

    @Inject
    private Repository repository;

    @Inject
    private ObjectMerger merger;

    public ObjectMergerTest() {
        testDatasetName = "datasets/dbUnit_objectmerger.xml";
    }

    /**
     * Merge probe objects.
     * @throws Exception that can occur during the test.
     */
    @Test
    public final void mergeProbe() throws Exception {
        transaction.begin();
        Sample probe = new Sample();
        probe.setExtId("T001");
        probe.setMainSampleId("120510002");
        probe.setMeasFacilId(mstId);
        probe.setOprModeId(1);
        probe.setRegulationId(DID9);
        probe.setEnvDescripName(
            "Trinkwasser Zentralversorgung Oberflächenwasser aufbereitet");
        probe.setEnvDescripDisplay("D: 59 04 01 00 05 05 01 02 00 00 00 00");
        probe.setMpgId(MPRID1000);
        probe.setSamplerId(PNID);
        probe.setIsTest(false);
        probe.setApprLabId(mstId);
        probe.setSampleMethId(2);
        probe.setEnvMediumId("A6");
        probe.setSchedStartDate(Timestamp.valueOf("2013-05-01 16:00:00"));
        probe.setSchedEndDate(Timestamp.valueOf("2013-05-05 16:00:00"));
        probe.setSampleStartDate(Timestamp.valueOf("2012-05-03 13:07:00"));
        Sample dbProbe = repository.getById(Sample.class, PID1000);
        merger.merge(dbProbe, probe);
        transaction.commit();

        shouldMatchDataSet("datasets/dbUnit_import_merge_match.xml",
            "lada.sample", new String[]{"last_mod", "tree_mod", "mid_coll_pd"});
    }

    /**
     * Merge messung objects.
     * @throws Exception that can occur during the test
     */
    @Test
    public final void mergeMessung() throws Exception {
        transaction.begin();
        Measm messung = new Measm();
        messung.setMinSampleId("06A0");
        messung.setIsScheduled(true);
        messung.setIsCompleted(false);
        messung.setMeasPd(MDAUER1000);
        messung.setMmtId("A3");
        messung.setMeasmStartDate(Timestamp.valueOf("2012-05-06 14:00:00"));
        Measm dbMessung =
            repository.getById(Measm.class, MID1200);
        merger.mergeMessung(dbMessung, messung);
        transaction.commit();

        shouldMatchDataSet(
            "datasets/dbUnit_import_merge_match_messung.xml",
            "lada.measm",
            new String[]{"status", "last_mod", "tree_mod"});
    }

    /**
     * Merge zusatzwert objects.
     * @throws Exception that can occur during the test.
     */
    @Test
    public final void mergeZusatzwert() throws Exception {
        transaction.begin();
        Sample probe = repository.getById(Sample.class, PID1000);

        // Update existing entry
        SampleSpecifMeasVal wert1 = new SampleSpecifMeasVal();
        wert1.setSampleId(PID1000);
        wert1.setError(1.2f);
        // TODO: Update only implemented for measVal and error
        // wert1.setSmallerThan("<");
        wert1.setSampleSpecifId("A74");

        // Create new entry
        SampleSpecifMeasVal wert2 = new SampleSpecifMeasVal();
        wert2.setSampleId(PID1000);
        wert2.setError(0.1f);
        wert2.setMeasVal(2d);
        wert2.setSampleSpecifId("A75");
        wert2.setSmallerThan("<");

        merger.mergeSampleSpecifMeasVals(probe, List.of(wert1, wert2));
        transaction.commit();

        shouldMatchDataSet(
            "datasets/dbUnit_import_merge_match_zusatzwert.xml",
            "lada.sample_specif_meas_val",
            new String[]{camelToSnake(SampleSpecifMeasVal_.LAST_MOD),
                camelToSnake(SampleSpecifMeasVal_.TREE_MOD)});
    }

    /**
     * Merge messwert objects.
     * @throws Exception that can occur during the test.
     */
    @Test
    public final void mergeMesswerte() throws Exception {
        transaction.begin();
        Measm messung =
            repository.getById(Measm.class, MID1200);
        QueryBuilder<MeasVal> builder = repository
            .queryBuilder(MeasVal.class)
            .and(MeasVal_.measmId, messung.getId());
        // Measm has two measVals before
        Assert.assertEquals(2, repository.filter(builder.getQuery()).size());

        MeasVal wert1 = new MeasVal();
        wert1.setMeasmId(MID1200);
        wert1.setMeasUnitId(MEHID207);
        wert1.setMeasdId(MGID56);
        wert1.setMeasVal(MESS15D);
        merger.mergeMesswerte(messung, List.of(wert1));
        List<MeasVal> dbWerte = repository.filter(builder.getQuery());
        // Only the "merged" measVal is kept
        Assert.assertEquals(1, dbWerte.size());
        transaction.commit();

        shouldMatchDataSet(
            "datasets/dbUnit_import_merge_match_messwert.xml",
            "lada.meas_val",
            new String[]{"id", "last_mod", "tree_mod"});
    }
}
