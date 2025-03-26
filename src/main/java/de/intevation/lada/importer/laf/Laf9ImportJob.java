/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.importer.laf;

import jakarta.inject.Inject;
import jakarta.validation.Validator;
import jakarta.validation.groups.Default;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.lada.TagLinkMeasm;
import de.intevation.lada.model.lada.TagLinkSample;
import de.intevation.lada.model.master.Tag;
import de.intevation.lada.model.master.Tag_;
import de.intevation.lada.rest.TagLinkService;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;


public class Laf9ImportJob extends ImportJob<List<Sample>> {

    @Inject
    private Repository repository;

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
        Map<String, Object> fileResponseData = new HashMap<>();
        this.files.forEach((fileName, content) -> {
            // TODO: Authorize
            for (Sample sample: content) {
                repository.create(sample);

                // Handle associated tags
                // TODO: Handle tag links outside request scope
                // handleSampleTags(sample);
                // for (Measm m: sample.getMeasms()) {
                //     handleMeasmTags(m);
                // }

                // TODO: Handle geolocat.site_id

                // TODO: Avoid duplicating statusProt entries

                importedSampleIds.add(sample.getId());
            }
            fileResponseData.put(SUCCESS_KEY, !currentStatus.getErrors());
            fileResponseData.put(SAMPLE_IDS_KEY, importedSampleIds);
            importData.put(fileName, fileResponseData);
        });

        tagImportedData(importedSampleIds, this.mst);
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
