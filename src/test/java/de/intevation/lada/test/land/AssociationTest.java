/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.land;

import java.util.Optional;
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
import de.intevation.lada.model.lada.CommMeasm;
import de.intevation.lada.model.lada.CommSample;
import de.intevation.lada.model.lada.Geolocat;
import de.intevation.lada.model.lada.MeasVal;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.lada.SampleSpecifMeasVal;
import de.intevation.lada.model.master.Site;
import de.intevation.lada.model.master.Tag;
import de.intevation.lada.rest.CommMeasmService;
import de.intevation.lada.rest.CommSampleService;
import de.intevation.lada.rest.GeolocatService;
import de.intevation.lada.rest.MeasmService;
import de.intevation.lada.rest.MeasValService;
import de.intevation.lada.rest.SampleService;
import de.intevation.lada.rest.SampleSpecifMeasValService;
import de.intevation.lada.rest.SiteService;
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
    private final String sampleSpecificMeasValPath = UriBuilder
            .fromResource(SampleSpecifMeasValService.class).build() + "/";
    private final String commMeasmPath = UriBuilder
            .fromResource(CommMeasmService.class).build() + "/";
    private final String sitePath = UriBuilder
            .fromResource(SiteService.class).build() + "/";
    private final String geolocatPath = UriBuilder
            .fromResource(GeolocatService.class).build() + "/";
    private final String measValPath = UriBuilder
            .fromResource(MeasValService.class).build() + "/";

    @Override
    public void init(WebTarget t) {
        super.init(t);
    }

    /**
     * Execute the tests.
     */
    public final void execute() {
        String measFacilId = "06010";
        String mmtId = "A3";
        String comment = "test association with comment";
        String commMeasmText = "Testkommentar";
        String TYPE_REGULATION_E = "E";
        String TYPE_REGULATION_U = "U";

        // Setup Sample
        Sample sample = getSample(measFacilId);
        CommSample commSample = getCommSample(measFacilId, comment);
        Site site = createMinimalSite();
        site = create(sitePath, site, Site.class);
        Geolocat geolocat = getGeolocat(TYPE_REGULATION_E, site);
        Measm measm = getMeasm(mmtId);
        MeasVal measValSample = getMeasVal(56, 207);
        measm.setMeasVals(Set.of(measValSample));
        Tag tag = getTag(measFacilId);
        SampleSpecifMeasVal sampleSpecifMeasVal =
            getSampleSpecificMeasVal("A74");

        sample.setMeasms(Set.of(measm));
        sample.setCommSamples(Set.of(commSample));
        sample.setTags(Set.of(tag));
        sample.setSampleSpecifMeasVals(Set.of(sampleSpecifMeasVal));
        sample.setGeolocats(Set.of(geolocat));

        // Create Sample via $samplePath
        Sample created = create(samplePath, sample, Sample.class);
        assertSampleServiceIgnoresRelatedObjects(created);

        // Create Sample via $laf9Path
        created = create(laf9Path, sample, Sample.class);

        // Extract properties to test for
        Set<Measm> createdMeasms = created.getMeasms();
        Set<Tag> createdTags = created.getTags();
        Set<CommSample> createdCommSamples = created.getCommSamples();
        Set<SampleSpecifMeasVal> createdSampleSpecifMeasVals = created
            .getSampleSpecifMeasVals();
        Set<Geolocat> createdGeolocats = created.getGeolocats();
        Optional<Measm> ms = createdMeasms.stream().findFirst();
        Measm createdMeasm = ms.get();
        Assert.assertEquals(1, createdMeasm.getMeasVals().size());
        assertRelatedObjectsArePresent(
            createdMeasms,
            createdTags,
            createdCommSamples,
            createdSampleSpecifMeasVals,
            createdGeolocats);

        // Retrieve Sample via $samplePath
        created = get(samplePath + created.getId(), Sample.class);
        assertAssociatedObjectsAreReturned(
            created,
            createdMeasms,
            createdTags,
            createdCommSamples,
            createdSampleSpecifMeasVals,
            createdGeolocats
            );

        // Created measm is correctly associated
        Measm newMeasm = new Measm();
        MeasVal measVal = getMeasVal(56, 208);
        CommMeasm commMeasm = getCommMeasm(commMeasmText, measFacilId);
        newMeasm.setMmtId(mmtId);
        newMeasm.setSample(created);
        newMeasm.setMeasVals(Set.of(measVal));
        newMeasm.setCommMeasms(Set.of(commMeasm));
        createdMeasm = create(measmPath, newMeasm, Measm.class);

        testMeasmAssociationsAreCleared(createdMeasm);

        // Add associations to newly created Measm
        measVal.setMeasm(createdMeasm);
        measVal = create(measValPath, measVal, MeasVal.class);
        MeasVal measVal2 = getMeasVal(57, 207);
        measVal2.setMeasm(createdMeasm);
        measVal2 = create(measValPath, measVal2, MeasVal.class);

        commMeasm.setMeasm(createdMeasm);
        commMeasm = create(commMeasmPath, commMeasm, CommMeasm.class);
        CommMeasm commMeasm2 = getCommMeasm("new Comment", measFacilId);
        commMeasm2.setMeasm(createdMeasm);
        commMeasm2 = create(commMeasmPath, commMeasm2, CommMeasm.class);
        createdMeasm = get(measmPath + createdMeasm.getId(),
            Measm.class);

        // Add associations to created Sample
        CommSample newCommSample = getCommSample(measFacilId, "new Comment");
        SampleSpecifMeasVal newSampleSpecifMeasVal =
            getSampleSpecificMeasVal("A75");
        newCommSample.setSample(created);
        newCommSample = create(commSamplePath, newCommSample, CommSample.class);
        newSampleSpecifMeasVal.setSample(created);
        newSampleSpecifMeasVal = create(sampleSpecificMeasValPath,
            newSampleSpecifMeasVal,
            SampleSpecifMeasVal.class);
        Geolocat newGeolocat = getGeolocat(TYPE_REGULATION_U, site);
        newGeolocat.setSample(created);
        newGeolocat = create(geolocatPath, newGeolocat, Geolocat.class);

        // Check OneToMany side
        createdMeasms = get(measmPath + "?sampleId=" + created.getId(),
            new GenericType<Set<Measm>>() { });
        createdCommSamples = get(commSamplePath
                + "?sampleId=" + created.getId(),
                new GenericType<Set<CommSample>>() {
                });
        createdSampleSpecifMeasVals = get(sampleSpecificMeasValPath
                + "?sampleId=" + created.getId(),
                new GenericType<Set<SampleSpecifMeasVal>>() {
                });
        Set<CommMeasm> createdCommMeasms = get(commMeasmPath
                + "?measmId=" + createdMeasm.getId(),
                new GenericType<Set<CommMeasm>>() {
                });
        createdGeolocats = get(geolocatPath
                + "?sampleId=" + created.getId(),
                new GenericType<Set<Geolocat>>() {
                });
        Set<MeasVal> createdMeasVals = get(measValPath
                + "?measmId=" + createdMeasm.getId(),
                new GenericType<Set<MeasVal>>() {
                });

        testOneToMany(
            createdMeasms,
            createdCommSamples,
            createdSampleSpecifMeasVals,
            createdGeolocats, createdMeasm,
            measVal,
            measVal2,
            commMeasm,
            commMeasm2,
            newCommSample,
            newSampleSpecifMeasVal,
            newGeolocat,
            createdCommMeasms,
            createdMeasVals);

        testManyToOne(
            created,
            createdMeasm,
            measVal,
            measVal2,
            commMeasm,
            commMeasm2,
            newCommSample,
            newSampleSpecifMeasVal,
            newGeolocat);

        // Associated objects in PUT payload should be ignored
        created.setMainSampleId("X");
        Sample updated = assertAssociatedUnchanged(
            created,
            createdMeasms,
            createdTags,
            createdCommSamples,
            createdSampleSpecifMeasVals,
            createdGeolocats);

        addSecondMeasm(measFacilId, mmtId, site, updated);

        updated = assertAssociatedUnchanged(
            updated,
            createdMeasms,
            createdTags,
            createdCommSamples,
            createdSampleSpecifMeasVals,
            createdGeolocats);

        emptyingAssociations(updated);

            updated = assertAssociatedUnchanged(
            updated,
            createdMeasms,
            createdTags,
            createdCommSamples,
            createdSampleSpecifMeasVals,
            createdGeolocats);

        clearingAssociations(updated);

        updated = assertAssociatedUnchanged(
            updated,
            createdMeasms,
            createdTags,
            createdCommSamples,
            createdSampleSpecifMeasVals,
            createdGeolocats);

        // Associated measm can be updated
        update(measmPath + createdMeasm.getId(),
            "minSampleId", JsonValue.NULL, Json.createValue("XX"));

        deleteAssociatedObjects(
            createdMeasms,
            createdTags,
            createdCommSamples,
            createdSampleSpecifMeasVals,
            createdGeolocats,
            createdMeasVals);

        testAssociationsAreNotrestoredByUpdate(updated);

        created = testSampleCouldBeDeleted(sample, tag);

        testAssociationsAreDeleted(
            created,
            createdCommMeasms,
            createdMeasVals);

        testAssociatedTagsExist(created);
    }

    private void testMeasmAssociationsAreCleared(Measm createdMeasm) {
        Assert.assertTrue(createdMeasm.getCommMeasms().isEmpty());
        Assert.assertTrue(createdMeasm.getMeasVals().isEmpty());
    }

    private void testAssociatedTagsExist(Sample created) {
        Assert.assertFalse(created.getTags().isEmpty());
        for (Tag t: created.getTags()) {
            get(tagPath + t.getId());
        }
    }

    private Sample testSampleCouldBeDeleted(Sample sample, Tag tag) {
        Sample created;
        tag.setName("another association test");
        created = create(laf9Path, sample, Sample.class);
        delete(samplePath + created.getId());
        return created;
    }

    private void testAssociationsAreNotrestoredByUpdate(Sample updated) {
        Assert.assertFalse(updated.getMeasms().isEmpty());
        Assert.assertFalse(updated.getTags().isEmpty());
        Assert.assertFalse(updated.getCommSamples().isEmpty());
        Assert.assertFalse(updated.getSampleSpecifMeasVals().isEmpty());
        Assert.assertFalse(updated.getGeolocats().isEmpty());
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
        Assert.assertTrue(updated.getSampleSpecifMeasVals().isEmpty());
        Assert.assertTrue(updated.getGeolocats().isEmpty());
    }

    private void testAssociationsAreDeleted(
            Sample created,
            Set<CommMeasm> createdCommMeasms,
            Set<MeasVal> createdMeasVals) {
        Assert.assertFalse(created.getMeasms().isEmpty());
        Assert.assertFalse(created.getCommSamples().isEmpty());
        Assert.assertFalse(created.getSampleSpecifMeasVals().isEmpty());
        Assert.assertFalse(created.getGeolocats().isEmpty());

        for (Measm m: created.getMeasms()) {
            get(measmPath + m.getId(), Response.Status.NOT_FOUND);
        }

        for (CommSample cs : created.getCommSamples()) {
            get(commSamplePath + cs.getId(), Response.Status.NOT_FOUND);
        }

        for (SampleSpecifMeasVal spm : created.getSampleSpecifMeasVals()) {
            get(sampleSpecificMeasValPath + spm.getId(),
                Response.Status.NOT_FOUND);
        }

        for (CommMeasm cm : createdCommMeasms) {
            get(commMeasmPath + cm.getId(),
                    Response.Status.NOT_FOUND);
        }

        for (Geolocat loc : created.getGeolocats()) {
            get(geolocatPath + loc.getId(), Response.Status.NOT_FOUND);
        }

        for (MeasVal m : createdMeasVals) {
            get(measValPath + m.getId(),
                    Response.Status.NOT_FOUND);
        }
    }

    private void deleteAssociatedObjects(
            Set<Measm> createdMeasms,
            Set<Tag> createdTags,
            Set<CommSample> createdCommSamples,
            Set<SampleSpecifMeasVal> createdSampleSpecifMeasVals,
            Set<Geolocat> createdGeolocats,
            Set<MeasVal> createdMeasVals) {
        for (MeasVal mv : createdMeasVals) {
            delete(measValPath + mv.getId());
        }

        for (Measm m: createdMeasms) {
            delete(measmPath + m.getId());
        }

        for (Tag t: createdTags) {
            delete(tagPath + t.getId());
        }

        for (CommSample cm: createdCommSamples){
            delete(commSamplePath + cm.getId());
        }

        for (SampleSpecifMeasVal smsmv: createdSampleSpecifMeasVals){
            delete(sampleSpecificMeasValPath + smsmv.getId());
        }

        for (Geolocat loc: createdGeolocats) {
            delete(geolocatPath + loc.getId());
        }
    }

    private void clearingAssociations(Sample updated) {
        updated.setMeasms(null);
        updated.setTags(null);
        updated.setCommSamples(null);
        updated.setSampleSpecifMeasVals(null);
    }

    private void emptyingAssociations(Sample updated) {
        updated.getMeasms().clear();
        updated.getTags().clear();
        updated.getCommSamples().clear();
        updated.getSampleSpecifMeasVals().clear();
    }

    private void addSecondMeasm(String measFacilId,
            String mmtId,
            Site site,
            Sample updated) {
        String TYPE_REGULATION_R = "R";
        Measm measm2 = new Measm();
        measm2.setMmtId(mmtId);
        Tag tag2 = new Tag();
        tag2.setName("another tag");
        CommMeasm commMeasm3 = getCommMeasm("new Comment2", measFacilId);
        MeasVal measVal3 = getMeasVal(56, 209);
        measm2.setMeasVals(Set.of(measVal3));
        CommSample commSample2 = getCommSample(measFacilId,
            "new comment2");
        SampleSpecifMeasVal sampleSpecifMeasVal2 =
                getSampleSpecificMeasVal("A76");
        Geolocat loc3 = getGeolocat(TYPE_REGULATION_R, site);
        measm2.setCommMeasms(Set.of(commMeasm3));
        updated.getMeasms().add(measm2);
        updated.getTags().add(tag2);
        updated.getCommSamples().add(commSample2);
        updated.getSampleSpecifMeasVals().add(sampleSpecifMeasVal2);
        updated.getGeolocats().add(loc3);
    }

    private void testManyToOne(
            Sample created,
            Measm createdMeasm,
            MeasVal measVal,
            MeasVal measVal2,
            CommMeasm commMeasm,
            CommMeasm commMeasm2,
            CommSample newCommSample,
            SampleSpecifMeasVal newSampleSpecifMeasVal,
            Geolocat newGeolocat) {
        Assert.assertEquals(created.getId().intValue(),
            get(measmPath + createdMeasm.getId(), JsonObject.class)
                .getInt("sampleId"));

        Assert.assertEquals(created.getId().intValue(),
            get(commSamplePath + newCommSample.getId(), JsonObject.class)
                 .getInt("sampleId"));

        String url = sampleSpecificMeasValPath + newSampleSpecifMeasVal.getId();
        Assert.assertEquals(created.getId().intValue(),
                get(url, JsonObject.class)
                        .getInt("sampleId"));

        Assert.assertEquals(createdMeasm.getId().intValue(),
                get(commMeasmPath + commMeasm.getId(), JsonObject.class)
                        .getInt("measmId"));
        Assert.assertEquals(createdMeasm.getId().intValue(),
                get(commMeasmPath + commMeasm2.getId(), JsonObject.class)
                        .getInt("measmId"));
        Assert.assertEquals(createdMeasm.getId().intValue(),
                get(measValPath + measVal.getId(), JsonObject.class)
                        .getInt("measmId"));
        Assert.assertEquals(createdMeasm.getId().intValue(),
                get(measValPath + measVal2.getId(), JsonObject.class)
                        .getInt("measmId"));
        Assert.assertEquals(created.getId().intValue(),
                get(geolocatPath + newGeolocat.getId(), JsonObject.class)
                        .getInt("sampleId"));
    }

    private void testOneToMany(
            Set<Measm> createdMeasms,
            Set<CommSample> createdCommSamples,
            Set<SampleSpecifMeasVal> createdSampleSpecifMeasVals,
            Set<Geolocat> createdGeolocats, Measm createdMeasm,
            MeasVal measVal, MeasVal measVal2,
            CommMeasm commMeasm, CommMeasm commMeasm2,
            CommSample newCommSample,
            SampleSpecifMeasVal newSampleSpecifMeasVal,
            Geolocat newGeolocat,
            Set<CommMeasm> createdCommMeasms,
            Set<MeasVal> createdMeasVals) {
        Assert.assertEquals(2, createdCommMeasms.size());
        Assert.assertEquals(2, createdMeasVals.size());
        Assert.assertEquals(2, createdMeasms.size());
        Assert.assertEquals(2, createdCommSamples.size());
        Assert.assertEquals(2, createdSampleSpecifMeasVals.size());
        Assert.assertEquals(2, createdGeolocats.size());
        MatcherAssert.assertThat(
            createdMeasms.stream().map(m -> m.getId()).toList(),
            CoreMatchers.hasItem(createdMeasm.getId()));
        MatcherAssert.assertThat(
                createdCommSamples.stream().map(s -> s.getId()).toList(),
                CoreMatchers.hasItem(newCommSample.getId()));
        MatcherAssert.assertThat(
                createdSampleSpecifMeasVals.stream()
                    .map(s -> s.getId()).toList(),
                CoreMatchers.hasItem(newSampleSpecifMeasVal.getId()));
        MatcherAssert.assertThat(
                createdMeasm.getCommMeasms().stream()
                        .map(s -> s.getId()).toList(),
                CoreMatchers.hasItem(commMeasm.getId()));
        MatcherAssert.assertThat(
                createdMeasm.getCommMeasms().stream()
                        .map(s -> s.getId()).toList(),
                CoreMatchers.hasItem(commMeasm2.getId()));
        MatcherAssert.assertThat(
                createdMeasm.getMeasVals().stream()
                        .map(s -> s.getId()).toList(),
                CoreMatchers.hasItem(measVal.getId()));
        MatcherAssert.assertThat(
                createdMeasm.getMeasVals().stream()
                        .map(s -> s.getId()).toList(),
                CoreMatchers.hasItem(measVal2.getId()));
        MatcherAssert.assertThat(
                createdGeolocats.stream().map(m -> m.getId()).toList(),
                CoreMatchers.hasItem(newGeolocat.getId()));
    }

    private void assertAssociatedObjectsAreReturned(
        Sample created,
        Set<Measm> createdMeasms,
        Set<Tag> createdTags,
        Set<CommSample> createdCommSamples,
        Set<SampleSpecifMeasVal> createdSampleSpecifMeasVals,
        Set<Geolocat> createdGeolocats
    ) {
        assertAssociatedUnchanged(createdMeasms, created.getMeasms());
        assertAssociatedUnchanged(createdTags, created.getTags());
        assertAssociatedUnchanged(createdCommSamples, created.getCommSamples());
        assertAssociatedUnchanged(
            createdSampleSpecifMeasVals,
            created.getSampleSpecifMeasVals());
        assertAssociatedUnchanged(createdGeolocats, created.getGeolocats());
    }

    private void assertRelatedObjectsArePresent(
        Set<Measm> createdMeasms,
        Set<Tag> createdTags,
        Set<CommSample> createdCommSamples,
        Set<SampleSpecifMeasVal> createdSampleSpecifMeasVals,
        Set<Geolocat> createdGeoLocats
    ) {
        Assert.assertNotNull(createdMeasms);
        Assert.assertEquals(1, createdMeasms.size());
        Assert.assertNotNull(createdTags);
        Assert.assertEquals(1, createdTags.size());
        Assert.assertNotNull(createdCommSamples);
        Assert.assertEquals(1, createdCommSamples.size());
        Assert.assertNotNull(createdSampleSpecifMeasVals);
        Assert.assertEquals(1, createdSampleSpecifMeasVals.size());
        Assert.assertNotNull(createdGeoLocats);
        Assert.assertEquals(1, createdGeoLocats.size());
    }

    private void assertSampleServiceIgnoresRelatedObjects(Sample created) {
        Assert.assertTrue(created.getMeasms().isEmpty());
        Assert.assertTrue(created.getTags().isEmpty());
        Assert.assertTrue(created.getCommSamples().isEmpty());
        Assert.assertTrue(created.getSampleSpecifMeasVals().isEmpty());
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

    private SampleSpecifMeasVal getSampleSpecificMeasVal(
            String sampleSpecificId
    ) {
        SampleSpecifMeasVal value = new SampleSpecifMeasVal();
        value.setSampleSpecifId(sampleSpecificId);
        return value;
    }

    private CommMeasm getCommMeasm (
            String text,
            String measFacilId
    ) {
        CommMeasm commMeasm = new CommMeasm();
        commMeasm.setText(text);
        commMeasm.setMeasFacilId(measFacilId);
        return commMeasm;
    }

    private Geolocat getGeolocat(String typeRegulation, Site site) {
        Geolocat loc = new Geolocat();
        loc.setTypeRegulation(typeRegulation);
        loc.setSite(site);
        return loc;
    }

    private Site createMinimalSite() {
        Site site = new Site();
        site.setNetworkId("06");
        site.setStateId(0);
        site.setSiteClassId(Site.SiteClassId.DYN);
        return site;
    }

    private MeasVal getMeasVal(int measId, int measUnitId){
        MeasVal m = new MeasVal();
        m.setMeasdId(measId);
        m.setMeasUnitId(measUnitId);
        return m;
    }

    private Sample assertAssociatedUnchanged(
        Sample payload,
        Set<Measm> expectedMeasms,
        Set<Tag> expectedTags,
        Set<CommSample> expectedCommSamples,
        Set<SampleSpecifMeasVal> expectedSampleSpecifMeasVals,
        Set<Geolocat> expectedGeolocats
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
        assertAssociatedUnchanged(expectedSampleSpecifMeasVals,
            updated.getSampleSpecifMeasVals());
        assertAssociatedUnchanged(expectedGeolocats, updated.getGeolocats());
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
