/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.importer.laf;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.persistence.metamodel.ListAttribute;
import jakarta.persistence.metamodel.PluralAttribute;
import jakarta.validation.Validator;
import jakarta.validation.groups.Default;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import de.intevation.lada.importer.ObjectMerger;
import de.intevation.lada.importer.identification.Identification;
import de.intevation.lada.importer.identification.IdentificationException;
import de.intevation.lada.importer.Report;
import de.intevation.lada.importer.ReportItem;
import de.intevation.lada.model.lada.BelongsToSample;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.lada.TagLinkMeasm;
import de.intevation.lada.model.lada.TagLinkSample;
import de.intevation.lada.model.master.Tag;
import de.intevation.lada.model.master.Tag_;
import de.intevation.lada.rest.TagLinkService;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.JSONBConfig;


public class Laf9ImportJob extends ImportJob<Collection<JsonObject>> {

    @Inject
    private Identification identification;

    @Inject
    private Repository repository;

    @Inject
    private ObjectMerger merger;

    @Inject
    private Validator validator;

    @Inject
    private TagLinkService<TagLinkSample> tagLinkSampleService;

    @Inject
    private TagLinkService<TagLinkMeasm> tagLinkMeasmService;

    private Map<String, PropertyDescriptor> belongsToSampleProperties;

    @PostConstruct
    private void init() throws IntrospectionException {
        // Collect PropertyDescriptors for lists of associated child objects
        Map<String, PropertyDescriptor> collectGetters = new HashMap<>();
        Set<PluralAttribute<? super Sample, ?, ?>> attrs = repository
            .entityManager().getMetamodel().entity(Sample.class)
            .getPluralAttributes();
        for (PluralAttribute<? super Sample, ?, ?> attr : attrs) {
            if (attr instanceof ListAttribute<?, ?>
                && BelongsToSample.class.isAssignableFrom(
                    attr.getElementType().getJavaType())
            ) {
                String attrName = attr.getName();
                collectGetters.put(attrName,
                    new PropertyDescriptor(attrName, Sample.class));
            }
        }
        this.belongsToSampleProperties = Map.copyOf(collectGetters);
    }

    /**
     * Run the import job.
     */
    @Override
    public void runWithTx() {
        // IDs of all imported samples
        List<Integer> importedSampleIds = new ArrayList<>();

        // Import each file
        this.files.forEach((fileName, content) -> {
            List<Integer> sampleIds = new ArrayList<>();
            Report fileResponseData = new Report();
            for (JsonObject rawSample: content) {
                Sample sample = JSONBConfig.JSONB.fromJson(
                    rawSample.toString(), Sample.class);

                // TODO: Authorize
                try {
                    Sample persistent = identification.getExisting(sample);
                    if (persistent == null) {
                        repository.create(sample);
                        sampleIds.add(sample.getId());
                    } else {
                        merge(persistent, sample, rawSample, fileResponseData);
                        repository.update(persistent);
                        sampleIds.add(persistent.getId());
                    }
                    // Handle associated tags
                    // TODO: Handle tag links outside request scope
                    // handleSampleTags(sample);
                    // for (Measm m: sample.getMeasms()) {
                    //     handleMeasmTags(m);
                    // }

                    // TODO: validate

                    // TODO: Handle geolocat.site_id

                    // TODO: Avoid duplicating statusProt entries
                } catch (IdentificationException e) {
                    reportIdentificationException(sample, e, fileResponseData);
                }
            }
            fileResponseData.setSampleIds(sampleIds);
            importData.put(fileName, fileResponseData);
            importedSampleIds.addAll(sampleIds);
        });

        tagImportedData(importedSampleIds, this.mst);
    }

    private void merge(
        Sample targetSample,
        Sample srcSample,
        JsonObject rawSample,
        Report report
    ) {
        merger.merge(targetSample, rawSample);
        // TODO: Merge other associations
        for (String attrName : belongsToSampleProperties.keySet()) {
            List<BelongsToSample> srcObjects =
                getChildList(attrName, srcSample);
            if (srcObjects == null) {
                continue;
            }
            for (int i = 0; i < srcObjects.size(); i++) {
                BelongsToSample srcObject = srcObjects.get(i);
                JsonObject rawObject =
                    rawSample.getJsonArray(attrName).getJsonObject(i);

                // Identify
                srcObject.setSample(targetSample);
                BelongsToSample persistentObject;
                try {
                    persistentObject = identification.getExisting(srcObject);
                } catch (IdentificationException e) {
                    reportIdentificationException(targetSample, e, report);
                    continue;
                }

                // Merge existent or add new object
                if (persistentObject != null) {
                    merger.merge(persistentObject, rawObject);
                    if (persistentObject instanceof Measm targetMeasm
                        && srcObject instanceof Measm srcMeasm
                    ) {
                        mergeMeasmChilds(targetMeasm, srcMeasm);
                    }
                } else {
                    List<BelongsToSample> targetObjects =
                        getChildList(attrName, targetSample);
                    if (targetObjects == null) {
                        targetObjects = new ArrayList<>();
                        try {
                            belongsToSampleProperties.get(attrName)
                                .getWriteMethod()
                                .invoke(targetSample, targetObjects);
                        } catch (ReflectiveOperationException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    targetObjects.add(srcObject);
                }
            }
        }
    }

    private void mergeMeasmChilds(Measm targetMeasm, Measm srcMeasm) {
        merger.mergeMeasVals(targetMeasm, srcMeasm.getMeasVals());
    }

    @SuppressWarnings("unchecked")
    private List<BelongsToSample> getChildList(String name, Sample sample) {
        try {
            return (List<BelongsToSample>) belongsToSampleProperties
                .get(name).getReadMethod().invoke(sample);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private void reportIdentificationException(
        Sample sample,
        IdentificationException exception,
        Report report
    ) {
        Map<String, Object> failedAttrs = exception.getIdentifyingAttributes();
        report.addError(sample.getExtId() != null
            ? sample.getExtId() : sample.getMainSampleId(),
            new ReportItem(
                failedAttrs.keySet().toString(),
                failedAttrs.values().toString(),
                StatusCodes.IMP_INVALID_VALUE));
    }

    private void handleMeasmTags(Measm measm) {
        Set<Tag> tags = measm.getTags();
        List<TagLinkMeasm> tagLinks = new ArrayList<>();
        for (Tag tag : tags) {
            Optional<Tag> currentTag = upsertTag(tag);
            if (currentTag.isPresent()) {
                TagLinkMeasm tagLink = new TagLinkMeasm();
                tagLink.setMeasmId(measm.getId());
                tagLink.setTagId(currentTag.get().getId());
                tagLinks.add(tagLink);
            } else {
                tags.remove(tag);
            }
        }
        tagLinkMeasmService.createTagReference(tagLinks);
    }

    private void handleSampleTags(Sample sample) {
        Set<Tag> tags = sample.getTags();
        List<TagLinkSample> tagLinks = new ArrayList<>();
        for (Tag tag : tags) {
            Optional<Tag> currentTag = upsertTag(tag);
            if (currentTag.isPresent()) {
                TagLinkSample tagLink = new TagLinkSample();
                tagLink.setSampleId(sample.getId());
                tagLink.setTagId(currentTag.get().getId());
                tagLinks.add(tagLink);
            } else {
                tags.remove(tag);
            }
        }
        tagLinkSampleService.createTagReference(tagLinks);
    }

    private Optional<Tag> upsertTag(Tag tag) {
        Optional<Tag> currentTag = findInDB(tag);
        if (currentTag.isPresent()) {
            return currentTag;
        }
        if (validator.validate(tag, Default.class).isEmpty()) {
            currentTag = Optional.of(repository.create(tag));
        }
        return currentTag;
    }

     private Optional<Tag> findInDB(Tag tag) {
        Optional<Tag> currentTag = Optional.empty();
        QueryBuilder<Tag> builderTag = repository.queryBuilder(Tag.class)
            .and(Tag_.measFacilId, tag.getMeasFacilId())
            .and(Tag_.name, tag.getName());
        List<Tag> foundTag = repository.filter(builderTag.getQuery());
        if (foundTag.size() == 1) {
            currentTag = Optional.of(foundTag.get(0));
        }
        return currentTag;
    }
}
