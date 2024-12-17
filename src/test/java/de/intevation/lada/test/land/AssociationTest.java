/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.land;

import static de.intevation.lada.rest.LadaService.PATH_DATA;
import static de.intevation.lada.rest.LadaService.PATH_REST;

import java.util.Set;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;

import de.intevation.lada.BaseTest;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.test.ServiceTest;


public class AssociationTest extends ServiceTest {

    private final String samplePath = PATH_REST + "sample/";
    private final String measmPath = PATH_REST + "measm/";
    private final String laf9Path = PATH_DATA + "laf9/";

    @Override
    public void init(WebTarget t) {
        super.init(t);
    }

    /**
     * Execute the tests.
     */
    public final void execute() {
        final String measFacilId = "06010";
        final String mmtId = "A3";
        Sample sample = new Sample();
        sample.setOprModeId(1);
        sample.setRegulationId(2);
        sample.setApprLabId(measFacilId);
        sample.setMeasFacilId(measFacilId);
        sample.setSampleMethId(1);
        sample.setIsTest(true);
        Measm measm = new Measm();
        measm.setMmtId(mmtId);
        sample.setMeasms(Set.of(measm));

        // Create via SampleService should ignore related Measm
        Sample created = create(samplePath, sample, Sample.class);
        Assert.assertTrue(created.getMeasms().isEmpty());

        // Create Sample with related Measm
        created = create(laf9Path, sample, Sample.class);
        Set<Measm> createdMeasms = created.getMeasms();
        Assert.assertNotNull(createdMeasms);
        Assert.assertEquals(1, createdMeasms.size());

        // Associated measms are returned as part of sample
        created = get(samplePath + created.getId(), Sample.class);
        assertMeasmsUnchanged(createdMeasms, created.getMeasms());

        // Created measm is correctly associated
        Measm newMeasm = new Measm();
        newMeasm.setMmtId(mmtId);
        newMeasm.setSample(created);
        Measm createdMeasm = create(measmPath, newMeasm, Measm.class);
        // Check OneToMany side
        createdMeasms = get(measmPath + "?sampleId=" + created.getId(),
            new GenericType<Set<Measm>>() { });
        Assert.assertEquals(2, createdMeasms.size());
        MatcherAssert.assertThat(
            createdMeasms.stream().map(m -> m.getId()).toList(),
            CoreMatchers.hasItem(createdMeasm.getId()));
        // Check ManyToOne side
        Assert.assertEquals(created.getId().intValue(),
            get(measmPath + createdMeasm.getId(), JsonObject.class)
            .getInt("sampleId"));

        // Sample.measms in PUT payload should be ignored
        created.setMainSampleId("X");
        Sample updated = assertMeasmsUnchanged(createdMeasms, created);
        Measm measm2 = new Measm();
        measm2.setMmtId(mmtId);
        updated.getMeasms().add(measm2);
        updated = assertMeasmsUnchanged(createdMeasms, updated);
        updated.getMeasms().clear();
        updated = assertMeasmsUnchanged(createdMeasms, updated);
        updated.setMeasms(null);
        updated = assertMeasmsUnchanged(createdMeasms, updated);

        // Associated measm can be updated
        update(measmPath + createdMeasm.getId(),
            "minSampleId", JsonValue.NULL, Json.createValue("XX"));

        // Associated measms can be deleted
        for (Measm m: createdMeasms) {
            delete(measmPath + m.getId());
        }
        // ... and are not restored by updating the associated sample
        Assert.assertFalse(updated.getMeasms().isEmpty());
        updated.setMainSampleId("Y");
        updated = target.path(samplePath)
            .path(String.valueOf(updated.getId()))
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .put(Entity.entity(updated, MediaType.APPLICATION_JSON),
                Sample.class);
        Assert.assertTrue(updated.getMeasms().isEmpty());

        // Sample with associated measms can be deleted
        created = create(laf9Path, sample, Sample.class);
        Assert.assertFalse(created.getMeasms().isEmpty());
        delete(samplePath + created.getId());
        for (Measm m: created.getMeasms()) {
            get(measmPath + m.getId(), Response.Status.NOT_FOUND);
        }
    }

    private Sample assertMeasmsUnchanged(Set<Measm> expected, Sample payload) {
        Sample updated = target.path(samplePath)
            .path(String.valueOf(payload.getId()))
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .put(Entity.entity(payload, MediaType.APPLICATION_JSON),
                Sample.class);
        assertMeasmsUnchanged(expected, updated.getMeasms());
        return updated;
    }

    private void assertMeasmsUnchanged(
        Set<Measm> expected, Set<Measm> actual
    ) {
        Assert.assertEquals(expected.size(), actual.size());
        MatcherAssert.assertThat(
            actual.stream().map(m -> m.getId()).toList(),
            CoreMatchers.hasItems(
                expected.stream().map(m -> m.getId()).toArray(Integer[]::new)));
    }
}
