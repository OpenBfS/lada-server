/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.land;

import java.util.Set;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;

import de.intevation.lada.BaseTest;
import de.intevation.lada.data.Laf9Service;
import de.intevation.lada.model.lada.CommSample;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.master.Tag;
import de.intevation.lada.rest.CommSampleService;
import de.intevation.lada.rest.MeasmService;
import de.intevation.lada.rest.SampleService;
import de.intevation.lada.rest.TagService;
import de.intevation.lada.test.ServiceTest;


public class AssociationTest extends ServiceTest {

    private final String samplePath = UriBuilder
        .fromResource(SampleService.class).build() + "/";
    private final String measmPath = UriBuilder
        .fromResource(MeasmService.class).build() + "/";
    private final String tagPath = UriBuilder
        .fromResource(TagService.class).build() + "/";
    private final String laf9Path = UriBuilder
        .fromResource(Laf9Service.class).build() + "/";
    private final String commSamplePath = UriBuilder
            .fromResource(CommSampleService.class).build() + "/";

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
        final String comment = "test association with comment";

        Sample sample = getSample(measFacilId);
        CommSample commSample = getCommSample(measFacilId, comment);
        Measm measm = getMeasm(mmtId);
        Tag tag = getTag(measFacilId);

        sample.setMeasms(Set.of(measm));
        sample.setCommSamples(Set.of(commSample));
        sample.setTags(Set.of(tag));

        Sample created = create(samplePath, sample, Sample.class);
        assertSampleServiceIgnoresRelatedObjects(created);

        created = create(laf9Path, sample, Sample.class);
        Set<Measm> createdMeasms = created.getMeasms();
        Set<Tag> createdTags = created.getTags();
        Set<CommSample> createdCommSamples = created.getCommSamples();
        assertRelatedObjectsArePresent(
            createdMeasms,
            createdTags,
            createdCommSamples);

        created = get(samplePath + created.getId(), Sample.class);
        assertAssociatedObjectsAreReturned(
            created,
            createdMeasms,
            createdTags,
            createdCommSamples);

        // Created measm is correctly associated
        Measm newMeasm = new Measm();
        newMeasm.setMmtId(mmtId);
        newMeasm.setSample(created);
        Measm createdMeasm = create(measmPath, newMeasm, Measm.class);
        CommSample newCommSample = getCommSample(measFacilId, "new Comment");
        newCommSample.setSample(created);
        newCommSample = create(commSamplePath, newCommSample, CommSample.class);

        // Check OneToMany side
        createdMeasms = get(measmPath + "?sampleId=" + created.getId(),
            new GenericType<Set<Measm>>() { });
        createdCommSamples = get(commSamplePath
                + "?sampleId=" + created.getId(),
                new GenericType<Set<CommSample>>() {
                });
        Assert.assertEquals(2, createdMeasms.size());
        Assert.assertEquals(2, createdCommSamples.size());
        MatcherAssert.assertThat(
            createdMeasms.stream().map(m -> m.getId()).toList(),
            CoreMatchers.hasItem(createdMeasm.getId()));
        MatcherAssert.assertThat(
                createdCommSamples.stream().map(s -> s.getId()).toList(),
                CoreMatchers.hasItem(newCommSample.getId()));

        // Check ManyToOne side
        Assert.assertEquals(created.getId().intValue(),
            get(measmPath + createdMeasm.getId(), JsonObject.class)
                .getInt("sampleId"));

        Assert.assertEquals(created.getId().intValue(),
            get(commSamplePath + newCommSample.getId(), JsonObject.class)
                 .getInt("sampleId"));

        // Associated objects in PUT payload should be ignored
        created.setMainSampleId("X");
        Sample updated = assertAssociatedUnchanged(
            created, createdMeasms, createdTags, createdCommSamples);

        Measm measm2 = new Measm();
        measm2.setMmtId(mmtId);
        Tag tag2 = new Tag();
        tag2.setName("another tag");
        CommSample commSample2 = getCommSample(measFacilId,
            "new comment2");
        updated.getMeasms().add(measm2);
        updated.getTags().add(tag2);
        updated.getCommSamples().add(commSample2);
        updated = assertAssociatedUnchanged(
            updated, createdMeasms, createdTags, createdCommSamples);
        updated.getMeasms().clear();
        updated.getTags().clear();
        updated.getCommSamples().clear();
        updated = assertAssociatedUnchanged(
            updated, createdMeasms, createdTags, createdCommSamples);
        updated.setMeasms(null);
        updated.setTags(null);
        updated.setCommSamples(null);
        updated = assertAssociatedUnchanged(
            updated, createdMeasms, createdTags, createdCommSamples);

        // Associated measm can be updated
        update(measmPath + createdMeasm.getId(),
            "minSampleId", JsonValue.NULL, Json.createValue("XX"));

        // Associated objects can be deleted
        for (Measm m: createdMeasms) {
            delete(measmPath + m.getId());
        }
        for (Tag t: createdTags) {
            delete(tagPath + t.getId());
        }

        for (CommSample cm: createdCommSamples){
            delete(commSamplePath + cm.getId());
        }

        // ... and are not restored by updating the associated sample
        Assert.assertFalse(updated.getMeasms().isEmpty());
        Assert.assertFalse(updated.getTags().isEmpty());
        Assert.assertFalse(updated.getCommSamples().isEmpty());
        updated.setMainSampleId("Y");
        updated = target.path(samplePath)
            .path(String.valueOf(updated.getId()))
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .put(Entity.entity(updated, MediaType.APPLICATION_JSON),
                Sample.class);
        Assert.assertTrue(updated.getMeasms().isEmpty());
        Assert.assertTrue(updated.getTags().isEmpty());
        Assert.assertTrue(updated.getCommSamples().isEmpty());

        // Sample with associated objects can be deleted
        tag.setName("another association test");
        created = create(laf9Path, sample, Sample.class);
        delete(samplePath + created.getId());

        // Associated measms are deleted as well
        Assert.assertFalse(created.getMeasms().isEmpty());
        Assert.assertFalse(created.getCommSamples().isEmpty());

        for (Measm m: created.getMeasms()) {
            get(measmPath + m.getId(), Response.Status.NOT_FOUND);
        }

        for (CommSample cs : created.getCommSamples()) {
            get(commSamplePath + cs.getId(), Response.Status.NOT_FOUND);
        }

        // Previously associated tags still exist
        Assert.assertFalse(created.getTags().isEmpty());
        for (Tag t: created.getTags()) {
            get(tagPath + t.getId());
        }
    }

    private void assertAssociatedObjectsAreReturned(
        Sample created,
        Set<Measm> createdMeasms,
        Set<Tag> createdTags,
        Set<CommSample> createdCommSamples
    ) {
        assertAssociatedUnchanged(createdMeasms, created.getMeasms());
        assertAssociatedUnchanged(createdTags, created.getTags());
        assertAssociatedUnchanged(createdCommSamples, created.getCommSamples());
    }

    private void assertRelatedObjectsArePresent(
        Set<Measm> createdMeasms,
        Set<Tag> createdTags,
        Set<CommSample> createdCommSamples
    ) {
        Assert.assertNotNull(createdMeasms);
        Assert.assertEquals(1, createdMeasms.size());
        Assert.assertNotNull(createdTags);
        Assert.assertEquals(1, createdTags.size());
        Assert.assertNotNull(createdCommSamples);
        Assert.assertEquals(1, createdCommSamples.size());
    }

    private void assertSampleServiceIgnoresRelatedObjects(Sample created) {
        Assert.assertTrue(created.getMeasms().isEmpty());
        Assert.assertTrue(created.getTags().isEmpty());
        Assert.assertTrue(created.getCommSamples().isEmpty());
    }

    private Tag getTag(final String measFacilId) {
        Tag tag = new Tag();
        tag.setName("association test");
        tag.setMeasFacilId(measFacilId);
        return tag;
    }

    private Measm getMeasm(final String mmtId) {
        Measm measm = new Measm();
        measm.setMmtId(mmtId);
        return measm;
    }

    private CommSample getCommSample(
        final String measFacilId,
        final String comment
    ) {
        CommSample commSample = new CommSample();
        commSample.setText(comment);
        commSample.setMeasFacilId(measFacilId);
        return commSample;
    }

    private Sample getSample(final String measFacilId) {
        Sample sample = new Sample();
        sample.setOprModeId(1);
        sample.setRegulationId(2);
        sample.setApprLabId(measFacilId);
        sample.setMeasFacilId(measFacilId);
        sample.setSampleMethId(1);
        sample.setIsTest(true);
        return sample;
    }

    private Sample assertAssociatedUnchanged(
        Sample payload,
        Set<Measm> expectedMeasms,
        Set<Tag> expectedTags,
        Set<CommSample> expectedCommSamples
    ) {
        Sample updated = target.path(samplePath)
            .path(String.valueOf(payload.getId()))
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .put(Entity.entity(payload, MediaType.APPLICATION_JSON),
                Sample.class);
        assertAssociatedUnchanged(expectedMeasms, updated.getMeasms());
        assertAssociatedUnchanged(expectedTags, updated.getTags());
        assertAssociatedUnchanged(expectedCommSamples,
            updated.getCommSamples());
        return updated;
    }

    private <T> void assertAssociatedUnchanged(
        Set<T> expected, Set<T> actual
    ) {
        Assert.assertEquals(expected.size(), actual.size());
        MatcherAssert.assertThat(
            actual.stream().map(m -> getId(m)).toList(),
            CoreMatchers.hasItems(
                expected.stream().map(m -> getId(m)).toArray(Object[]::new)));
    }

    private Object getId(Object instance) {
        try {
            return instance.getClass().getMethod("getId").invoke(instance);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
