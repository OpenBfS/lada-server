/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import de.intevation.lada.model.lada.TagLinkSample;
import de.intevation.lada.model.lada.TagLinkSample_;
import de.intevation.lada.model.lada.TagLink_;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.ws.rs.Path;

@Path(LadaService.PATH_REST + "tag/taglinksample")
public class TagLinkSampleService extends TagLinkService<TagLinkSample> {

    @Override
    protected Boolean isExisting(TagLinkSample zuordnung) {
        return isExisting(zuordnung.getTagId(), zuordnung.getSampleId(),
            "sample_id", "tag_link_sample");
    }

    @Override
    protected Integer getTaggegObjectId(TagLinkSample link) {
        return link.getSampleId();
    }

    @Override
    protected SingularAttribute<TagLinkSample, Integer>
            getTaggedObjectIdField() {
        return TagLinkSample_.sampleId;
    }

    @Override
    protected void deleteTagLink(TagLinkSample tagLink) {
        repository.delete(
            repository.getSingle(repository
                .queryBuilder(TagLinkSample.class)
                .and(TagLink_.tagId, tagLink.getTagId())
                .and(getTaggedObjectIdField(), getTaggegObjectId(tagLink))
                .getQuery()));
    }
}
