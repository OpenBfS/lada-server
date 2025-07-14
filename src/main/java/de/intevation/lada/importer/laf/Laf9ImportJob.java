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
import java.lang.reflect.Method;
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
import de.intevation.lada.model.lada.CommMeasm;
import de.intevation.lada.model.lada.MeasVal;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.lada.Sample_;
import de.intevation.lada.model.lada.StatusProt;
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

    private static final String MSG_KEY_PREFIX = "validation#";

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

    private Map<String, Method> belongsToSampleGetters;

    private Map<Class<?>, Method> idSetters;

    private Report fileResponseData;
    private String currentReportKey;

    @PostConstruct
    private void init() {
        /* Collect getters for lists of associated child objects
           and setters for IDs of associated child objects */
        Map<String, Method> collectGetters = new HashMap<>();
        Map<Class<?>, Method> collectSetters = new HashMap<>();
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
                        new PropertyDescriptor(attrName, Sample.class)
                        .getReadMethod());

                    Class<?> elementType = attr.getElementType().getJavaType();
                    collectSetters.put(elementType,
                        repository.idProperty(elementType).getWriteMethod());
                } catch (IntrospectionException e) {
                    // Avoids warning during startup
                    throw new RuntimeException(e);
                }
            }
        }
        this.belongsToSampleGetters = Map.copyOf(collectGetters);
        this.idSetters = Map.copyOf(collectSetters);
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
                    boolean isNewSample = finalSample == null;
                    final String msgKey = "probe";
                    if (isNewSample) {
                        /* Ignore IDs in input to prevent Hibernate from
                           considering new objects as transient */
                        inputSample.setId(null);
                        finalSample = create(inputSample, msgKey);
                    } else {
                        finalSample = merge(finalSample, rawSample, msgKey);
                    }
                    /* Merge child objects if parent has no errors,
                       i.e. was persisted */
                    if (repository.entityManager().contains(finalSample)) {
                        fileResponseData.addSampleId(finalSample.getId());
                        mergeSampleChilds(
                            finalSample,
                            isNewSample,
                            rawSample,
                            fileResponseData);
                    }

                    /* Add warnings and notifications to final state
                       with child objects merged */
                    reportValidationMessages(
                        validator.validate(
                            finalSample, Warnings.class, Notifications.class),
                        MSG_KEY_PREFIX + msgKey);

                    // Handle associated tags
                    // TODO: Handle tag links outside request scope
                    // handleSampleTags(sample);
                    // for (Measm m: sample.getMeasms()) {
                    //     handleMeasmTags(m);
                    // }

                    // TODO: Handle geolocat.site_id
                } catch (IdentificationException e) {
                    reportIdentificationException(e);
                }
            }
            importData.put(fileName, fileResponseData);
            importedSampleIds.addAll(fileResponseData.getSampleIds());
        });

        tagImportedData(importedSampleIds, this.mst);
    }

    private void mergeSampleChilds(
        Sample targetSample,
        boolean isNewSample,
        JsonObject rawSample,
        Report report
    ) {
        Sample srcSample = JSONBConfig.JSONB.fromJson(
            rawSample.toString(), Sample.class);
        Map<Measm, JsonObject> importedMeasms = new HashMap<>();
        // TODO: Merge other associations
        for (String attrName : belongsToSampleGetters.keySet()) {
            List<BelongsToSample> srcObjects =
                getChildList(attrName, srcSample);
            if (srcObjects == null) {
                continue;
            }
            for (int i = 0; i < srcObjects.size(); i++) {
                BelongsToSample srcObject = srcObjects.get(i);
                srcObject.setSample(targetSample);

                BelongsToSample finalObject = null;
                if (!isNewSample) {
                    // Identify
                    try {
                        finalObject = identification.getExisting(srcObject);
                    } catch (IdentificationException e) {
                        reportIdentificationException(e);
                        continue;
                    }
                }

                // Merge existent or add new object
                JsonObject rawObject =
                    rawSample.getJsonArray(attrName).getJsonObject(i);
                if (finalObject == null) {
                    /* Ignore IDs in input to prevent Hibernate from
                       considering new objects as transient */
                    try {
                        this.idSetters.get(srcObject.getClass()).invoke(
                            srcObject, (Object) null);
                    } catch (ReflectiveOperationException e) {
                        throw new RuntimeException(e);
                    }
                    finalObject = create(srcObject, attrName);
                } else {
                    finalObject = merge(finalObject, rawObject, attrName);
                }

                /* Merge Measm child objects if parent has no errors,
                   i.e. was persisted */
                if (repository.entityManager().contains(finalObject)
                    && finalObject instanceof Measm targetMeasm
                ) {
                    importedMeasms.put(targetMeasm, rawObject);
                } else {
                    reportValidationMessages(
                        validator.validate(
                            finalObject, Warnings.class, Notifications.class),
                        MSG_KEY_PREFIX + attrName);
                }
            }
        }

        // Merge and validate child objects and validate imported measms
        for (Measm importedMeasm : importedMeasms.keySet()) {
            mergeMeasmChilds(importedMeasm, importedMeasms.get(importedMeasm));
            reportValidationMessages(
                validator.validate(
                    importedMeasm, Warnings.class, Notifications.class),
                MSG_KEY_PREFIX + Sample_.MEASMS);
        }
    }

    private <T extends BaseModel> T create(T inputObject, String msgKey) {
        reportValidationMessages(
            validator.validate(inputObject, CreateErrors.class),
            MSG_KEY_PREFIX + msgKey);
        if (!inputObject.hasErrors()) {
            return repository.create(inputObject);
        }
        return inputObject;
    }

    private <T extends BaseModel> T merge(
        T persistent, JsonObject rawObject, String msgKey
    ) {
        merger.merge(persistent, rawObject);
        reportValidationMessages(
            validator.validate(persistent, Default.class),
            MSG_KEY_PREFIX + msgKey);
        if (persistent.hasErrors()) {
            repository.entityManager().detach(persistent);
        } else {
            persistent = repository.update(persistent);
        }
        return persistent;
    }

    private void mergeMeasmChilds(Measm targetMeasm, JsonObject rawMeasm) {
        Measm srcMeasm = JSONBConfig.JSONB.fromJson(
            rawMeasm.toString(), Measm.class);

        // measVals
        Collection<MeasVal> newMeasVals = srcMeasm.getMeasVals();
        if (newMeasVals != null) {
            merger.mergeMeasVals(targetMeasm, newMeasVals);
            for (MeasVal m : newMeasVals) {
                // Validation already done in ObjectMerger
                reportValidationMessages(m, MSG_KEY_PREFIX + "messwert");
            }
        }

        // statusProts and commMeasms can only be added, not updated
        addBelongsToMeasms(targetMeasm, srcMeasm.getCommMeasms());
        /* Put statusProts last, because validating requires
           the final state of all objects */
        addBelongsToMeasms(targetMeasm, srcMeasm.getStatusProts());
    }

    private void addBelongsToMeasms(
        Measm target,
        Collection<? extends BelongsToMeasm> newEntries
    ) {
        if (newEntries != null) {
            for (BelongsToMeasm newEntry : newEntries) {
                /* Ignore IDs in input to prevent Hibernate from
                   considering new objects as transient */
                if (newEntry instanceof CommMeasm cm) {
                    cm.setId(null);
                } else if (newEntry instanceof StatusProt sp) {
                    sp.setId(null);
                }

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
            return (List<BelongsToSample>) belongsToSampleGetters
                .get(name).invoke(sample);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private void reportIdentificationException(
        IdentificationException exception
    ) {
        Map<String, Object> failedAttrs = exception.getIdentifyingAttributes();
        ReportItem reportItem;
        if (failedAttrs != null) {
            reportItem = new ReportItem(
                failedAttrs.keySet().toString(),
                failedAttrs.values().toString(),
                StatusCodes.IMP_INVALID_VALUE);
        } else {
            reportItem = new ReportItem(
                "identification", "", StatusCodes.IMP_INVALID_VALUE);
        }
        fileResponseData.addError(currentReportKey, reportItem);
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
