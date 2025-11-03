/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.importer;

import static org.junit.Assert.assertEquals;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.List;

import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.transaction.UserTransaction;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.intevation.lada.BaseTest;
import de.intevation.lada.model.lada.MeasVal;
import de.intevation.lada.model.lada.MeasVal_;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Measm_;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.lada.Sample_;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;


/**
 * Unit tests for ObjectMerger.
 */
@RunWith(Arquillian.class)
public class ObjectMergerTest extends BaseTest {

    private static final double MESS15D = 1.5d;
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
            .add(Sample_.TAG_LINKS, JsonValue.NULL).build();
        merger.merge(target, src);
        Assert.assertNotNull(target.getTagLinks());
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
    public final void skipsAssociation() {
        Sample target = new Sample();
        final String minSampleId = "XXX";
        JsonObject src = Json.createObjectBuilder()
            .add(Sample_.MEASMS, Json.createArrayBuilder()
                .add(Json.createObjectBuilder()
                    .add(Measm_.MIN_SAMPLE_ID, minSampleId)))
            .build();
        merger.merge(target, src);
        Assert.assertNull("Unexpected measms", target.getMeasms());
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
     */
    @Test
    public final void mergeMessung()
        throws IntrospectionException, ReflectiveOperationException {
        Measm messung = new Measm();
        messung.setMinSampleId("06A0");
        messung.setIsScheduled(true);
        messung.setIsCompleted(false);
        messung.setMeasPd(MDAUER1000);
        messung.setMmtId("A3");
        messung.setMeasmStartDate(Timestamp.valueOf("2012-05-06 14:00:00"));
        Measm dbMessung = repository.entityManager().find(Measm.class, MID1200);
        merger.mergeMessung(dbMessung, messung);

        for (SingularAttribute<? super Measm, ?> attr : repository
                 .entityManager().getMetamodel().entity(Measm.class)
                 .getSingularAttributes()) {
            if (!attr.isAssociation()) {
                Method getter = new PropertyDescriptor(
                    attr.getName(), Measm.class).getReadMethod();
                Object src = getter.invoke(messung);
                if (src != null) {
                    assertEquals(src, getter.invoke(dbMessung));
                }
            }
        }
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
        wert1.setMeasdId("Mangan");
        wert1.setMeasVal(MESS15D);
        merger.mergeMeasVals(messung, List.of(wert1));
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
