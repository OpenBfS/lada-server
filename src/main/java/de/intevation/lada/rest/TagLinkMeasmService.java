/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import de.intevation.lada.model.lada.TagLinkMeasm;
import jakarta.ws.rs.Path;

@Path("tag/taglinkmeasm")
public class TagLinkMeasmService extends TagLinkService<TagLinkMeasm> {
    @Override
    protected Boolean isExisting(TagLinkMeasm zuordnung) {
        return isExisting(zuordnung.getTagId(), zuordnung.getMeasmId(),
            "measm_id", "tag_link_measm");
    }

    @Override
    protected String getTaggedObjectIdField() {
        return "measmId";
    }

    @Override
    protected Object getTaggegObjectId(TagLinkMeasm link) {
        return link.getMeasmId();
    }
}
