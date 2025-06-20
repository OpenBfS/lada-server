/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.importer;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.transaction.UserTransaction;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.intevation.lada.BaseTest;
import de.intevation.lada.model.lada.MeasVal;
import de.intevation.lada.model.lada.MeasVal_;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Measm_;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.lada.SampleSpecifMeasVal;
import de.intevation.lada.model.lada.Sample_;
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
    private static final double MESS18D = 1.8d;
    private static final float MESSFEHLER02F = 0.2f;
    private static final float MESSFEHLER12F = 1.2f;
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

    @Test
    public final void skipsId() {
        Sample target = new Sample();
        JsonObject src = Json.createObjectBuilder()
            .add(Sample_.ID, 1).build();
        merger.merge(target, src);
        Assert.assertNull(target.getId());
    }

    @Test
    public final void skipsReadOnlyAttribute() {
        Sample target = new Sample();
        JsonObject src = Json.createObjectBuilder()
            .add(Sample_.TAG_LINKS, JsonValue.EMPTY_JSON_ARRAY).build();
        merger.merge(target, src);
        Assert.assertNull(target.getTagLinks());
    }

    @Test
    public final void mergeSingularAttribute() {
        Sample target = new Sample();
        final String extId = "XXX";
        JsonObject src = Json.createObjectBuilder()
            .add(Sample_.EXT_ID, extId).build();
        merger.merge(target, src);
        Assert.assertEquals(extId, target.getExtId());
    }

    @Test
    public final void mergeNullValuedAttribute() {
        Sample target = new Sample();
        target.setMainSampleId("XXX");
        JsonObject src = Json.createObjectBuilder()
            .addNull(Sample_.MAIN_SAMPLE_ID).build();
        merger.merge(target, src);
        Assert.assertNull(target.getMainSampleId());
    }

    @Test
    public final void throwsIfNotEntity() {
        Assert.assertThrows(
            IllegalArgumentException.class,
            () -> merger.merge(
                new Object(), JsonValue.EMPTY_JSON_OBJECT));
    }

    @Test
    public final void mergeAssociation() {
        Sample target = new Sample();
        final String minSampleId = "XXX";
        JsonObject src = Json.createObjectBuilder()
            .add(Sample_.MEASMS, Json.createArrayBuilder()
                .add(Json.createObjectBuilder()
                    .add(Measm_.MIN_SAMPLE_ID, minSampleId)))
            .build();
        merger.merge(target, src);
        Assert.assertNotNull("Missing measms", target.getMeasms());
        Assert.assertEquals(1, target.getMeasms().size());
        Measm measm = target.getMeasms().stream().findFirst().get();
        Assert.assertEquals(minSampleId, measm.getMinSampleId());
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

    // TODO Record order can get mixed up here which cause the test to fail as
    //       different records get compared to each other (e.g. A74 <-> A76)
    /**
     * Merge zusatzwert objects.
     * @throws Exception that can occur during the test.
     */
    @Test
    @Ignore
    public final void mergeZusatzwert() throws Exception {
        transaction.begin();
        Sample probe = repository.getById(Sample.class, PID1000);
        List<SampleSpecifMeasVal> zusatzwerte = new ArrayList<SampleSpecifMeasVal>();
        SampleSpecifMeasVal wert1 = new SampleSpecifMeasVal();
        wert1.setSample(probe);
        wert1.setError(MESSFEHLER12F);
        wert1.setSmallerThan("<");
        wert1.setSampleSpecifId("A74");

        SampleSpecifMeasVal wert2 = new SampleSpecifMeasVal();
        wert2.setSample(probe);
        wert2.setError(MESSFEHLER02F);
        wert2.setMeasVal(MESS18D);
        wert1.setSmallerThan(null);
        wert2.setSampleSpecifId("A75");

        SampleSpecifMeasVal wert3 = new SampleSpecifMeasVal();
        wert3.setSample(probe);
        wert3.setError(MESSFEHLER02F);
        wert3.setMeasVal(MESS18D);
        wert1.setSmallerThan(null);
        wert3.setSampleSpecifId("A76");

        zusatzwerte.add(wert1);
        zusatzwerte.add(wert2);
        zusatzwerte.add(wert3);
        merger.mergeZusatzwerte(probe, zusatzwerte);
        transaction.commit();

        shouldMatchDataSet(
            "datasets/dbUnit_import_merge_match_zusatzwert.xml",
            "lada.sample_specif_meas_val",
            new String[]{"id", "last_mod", "tree_mod"});
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
            .and(MeasVal_.measm, messung);
        // Measm has two measVals before
        Assert.assertEquals(2, repository.filter(builder.getQuery()).size());

        MeasVal wert1 = new MeasVal();
        wert1.setMeasm(messung);
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
