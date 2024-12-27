/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.data;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.groups.Default;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.POST;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.microprofile.openapi.annotations.Operation;

import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.lada.TagLinkSample;
import de.intevation.lada.model.master.Tag;
import de.intevation.lada.rest.LadaService;
import de.intevation.lada.rest.TagLinkService;
import de.intevation.lada.util.data.Repository;


@Path(LadaService.PATH_DATA + "laf9")
public class Laf9Service extends LadaService {

    @Inject
    private Repository repository;

    @Inject
    private Validator validator;

    @Inject
    private TagLinkService<TagLinkSample> tagLinkService;

    /**
     * @param sample sample to create
     * @return created sample
     * @throws BadRequestException if any constraint violations are detected.
     */
    @Operation(description =
        "Provisional service for testing upload of samples "
        + "including associated objects.")
    @POST
    public Sample upload(
        @Valid Sample sample
    ) throws BadRequestException {
        repository.create(sample);

        // Handle associated tags
        // TODO: Authorize
        Set<Tag> tags = sample.getTags();
        List<TagLinkSample> tagLinks = new ArrayList<>();
        for (Tag tag: tags) {
            if (validator.validate(tag, Default.class).isEmpty()) {
                if (tag.getId() != null
                    && repository.entityManager().find(Tag.class, tag.getId())
                    != null
                ) {
                    repository.update(tag);
                } else {
                    repository.create(tag);
                }
                TagLinkSample tagLink = new TagLinkSample();
                tagLink.setSampleId(sample.getId());
                tagLink.setTagId(tag.getId());
                tagLinks.add(tagLink);
            } else {
                // TODO: report errors
                tags.remove(tag);
            }
        }
        tagLinkService.createTagReference(tagLinks);

        return sample;
    }
}
