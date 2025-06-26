/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.importer.laf;

import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.validation.Validator;
import jakarta.validation.groups.Default;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import de.intevation.lada.importer.ObjectMerger;
import de.intevation.lada.importer.identification.Identification;
import de.intevation.lada.importer.identification.IdentificationException;
import de.intevation.lada.importer.Report;
import de.intevation.lada.importer.ReportItem;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Measm_;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.lada.Sample_;
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
                    reportIdentificationException(
                        Sample_.EXT_ID,
                        Sample_.MAIN_SAMPLE_ID,
                        rawSample,
                        fileResponseData);
                }
            }
            fileResponseData.setSuccess(fileResponseData.getErrors().isEmpty());
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
        for (int i = 0; i < srcSample.getMeasms().size(); i++) {
            Measm srcMeasm = srcSample.getMeasms().get(i);
            JsonObject rawMeasm =
                rawSample.getJsonArray(Sample_.MEASMS).getJsonObject(i);

            // Identify
            srcMeasm.setSample(targetSample);
            Measm persistentMeasm;
            try {
                persistentMeasm = identification.getExisting(srcMeasm);
            } catch (IdentificationException e) {
                reportIdentificationException(
                    Measm_.EXT_ID, Measm_.MIN_SAMPLE_ID, rawMeasm, report);
                continue;
            }

            // Merge existent or add new object
            if (persistentMeasm != null) {
                merger.merge(persistentMeasm, rawMeasm);
            } else {
                targetSample.getMeasms().add(srcMeasm);
            }
        }
    }

    private void reportIdentificationException(
        String primaryIdField,
        String secondaryIdField,
        JsonObject failedObject,
        Report report
    ) {
        String idField = failedObject.isNull(primaryIdField)
            ? secondaryIdField
            : primaryIdField;
        JsonValue jsonId = failedObject.get(idField);
        // Convert to String without quote signs
        String id = JsonValue.ValueType.STRING.equals(jsonId.getValueType())
            ? failedObject.getString(idField)
            : jsonId.toString();
        report.addError(id, new ReportItem(
                idField, id, StatusCodes.IMP_INVALID_VALUE));
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
