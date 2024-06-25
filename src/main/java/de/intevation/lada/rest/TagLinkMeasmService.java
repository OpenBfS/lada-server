/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import de.intevation.lada.model.lada.TagLinkMeasm;
import de.intevation.lada.model.lada.TagLinkMeasm_;
import de.intevation.lada.model.lada.TagLink_;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.ws.rs.Path;

@Path(LadaService.PATH_REST + "tag/taglinkmeasm")
public class TagLinkMeasmService extends TagLinkService<TagLinkMeasm> {
    @Override
    protected Boolean isExisting(TagLinkMeasm zuordnung) {
        return isExisting(zuordnung.getTagId(), zuordnung.getMeasmId(),
            "measm_id", "tag_link_measm");
    }

    @Override
    protected SingularAttribute<TagLinkMeasm, Integer>
            getTaggedObjectIdField() {
        return TagLinkMeasm_.measmId;
    }

    @Override
    protected Integer getTaggegObjectId(TagLinkMeasm link) {
        return link.getMeasmId();
    }

    @Override
    protected void deleteTagLink(TagLinkMeasm tagLink) {
        repository.delete(
            repository.getSingle(repository
                .queryBuilder(TagLinkMeasm.class)
                .and(TagLink_.tagId, tagLink.getTagId())
                .and(getTaggedObjectIdField(), getTaggegObjectId(tagLink))
                .getQuery()));
    }
}
