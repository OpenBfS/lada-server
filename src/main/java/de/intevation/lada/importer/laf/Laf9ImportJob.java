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
import de.intevation.lada.model.BaseModel;
import de.intevation.lada.model.lada.BelongsToMeasm;
import de.intevation.lada.model.lada.BelongsToSample;
import de.intevation.lada.model.lada.MeasVal;
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
import de.intevation.lada.validation.Validator;
import de.intevation.lada.validation.groups.CreateErrors;
import de.intevation.lada.validation.groups.Notifications;
import de.intevation.lada.validation.groups.Warnings;


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

    private Report fileResponseData;
    private String currentReportKey;

    @PostConstruct
    private void init() {
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
                try {
                    collectGetters.put(attrName,
                        new PropertyDescriptor(attrName, Sample.class));
                } catch (IntrospectionException e) {
                    // Avoids warning during startup
                    throw new RuntimeException(e);
                }
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
            this.fileResponseData = new Report();
            for (JsonObject rawSample: content) {
                Sample inputSample = JSONBConfig.JSONB.fromJson(
                    rawSample.toString(), Sample.class);
                this.currentReportKey = inputSample.getExtId() != null
                    ? inputSample.getExtId() : inputSample.getMainSampleId();

                // TODO: Authorize
                try {
                    Sample finalSample =
                        identification.getExisting(inputSample);
                    if (finalSample == null) {
                        reportValidationMessages(
                            validator.validate(inputSample, CreateErrors.class),
                            "validation#probe");
                        if (!inputSample.hasErrors()) {
                            finalSample = repository.create(inputSample);
                            fileResponseData.addSampleId(inputSample.getId());
                        } else {
                            // Only for further validation
                            finalSample = inputSample;
                        }
                    } else {
                        merge(
                            finalSample,
                            inputSample,
                            rawSample,
                            fileResponseData);
                        reportValidationMessages(
                            validator.validate(finalSample, Default.class),
                            "validation#probe");
                        if (!finalSample.hasErrors()) {
                            finalSample = repository.update(finalSample);
                            fileResponseData.addSampleId(finalSample.getId());
                        }
                    }
                    // Add warnings and notifications to final state
                    reportValidationMessages(
                        validator.validate(
                            finalSample, Warnings.class, Notifications.class),
                        "validation#probe");

                    // Handle associated tags
                    // TODO: Handle tag links outside request scope
                    // handleSampleTags(sample);
                    // for (Measm m: sample.getMeasms()) {
                    //     handleMeasmTags(m);
                    // }

                    // TODO: Handle geolocat.site_id

                    // TODO: Avoid duplicating statusProt entries
                } catch (IdentificationException e) {
                    reportIdentificationException(e);
                }
            }
            importData.put(fileName, fileResponseData);
            importedSampleIds.addAll(fileResponseData.getSampleIds());
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
        // TODO: validate
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
                    reportIdentificationException(e);
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
        // measVals
        Collection<MeasVal> newMeasVals = srcMeasm.getMeasVals();
        if (newMeasVals != null) {
            merger.mergeMeasVals(targetMeasm, newMeasVals);
            for (MeasVal m : newMeasVals) {
                // Validation already done in ObjectMerger
                reportValidationMessages(m, "validation#messwert");
            }
        }

        // statusProts and commMeasms can only be added, not updated
        addBelongsToMeasms(targetMeasm, srcMeasm.getStatusProts());
        addBelongsToMeasms(targetMeasm, srcMeasm.getCommMeasms());
    }

    private void addBelongsToMeasms(
        Measm target,
        Collection<? extends BelongsToMeasm> newEntries
    ) {
        if (newEntries != null) {
            for (BelongsToMeasm newEntry : newEntries) {
                newEntry.setMeasm(target);
                reportValidationMessages(
                    validator.validate(newEntry), "Status ");
                if (!newEntry.hasErrors()) {
                    repository.create(newEntry);
                }
            }
        }
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
        IdentificationException exception
    ) {
        Map<String, Object> failedAttrs = exception.getIdentifyingAttributes();
        fileResponseData.addError(currentReportKey,
            new ReportItem(
                failedAttrs.keySet().toString(),
                failedAttrs.values().toString(),
                StatusCodes.IMP_INVALID_VALUE));
    }

    private void reportValidationMessages(
        BaseModel validatedObject, String key
    ) {
        fileResponseData.addValidationMessages(
            currentReportKey, key, validatedObject);
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
        if (!validator.validate(tag, Default.class).hasErrors()) {
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
