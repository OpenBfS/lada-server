/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import de.intevation.lada.model.lada.TagLinkMeasm;
import de.intevation.lada.model.master.Tag;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.data.TagUtil;
import de.intevation.lada.util.rest.RequestMethod;
import jakarta.validation.Valid;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("tag/taglinkmeasm")
public class TagLinkMeasmService extends TagLinkService {

    /**
     * Create new tag measm references.
     * @param tagLinks List of new tag references
     * @return Result list.
     */
    @POST
    public List<Response<TagLinkMeasm>> createTagReference(
        @Valid List<TagLinkMeasm> tagLinks
    ) {
        //Create Response
        List<Response<TagLinkMeasm>> responseList = new ArrayList<>();

        for (TagLinkMeasm tagLinkMeasm: tagLinks) {
            Integer tagId = tagLinkMeasm.getTagId();
            if (isExisting(tagLinkMeasm)) {
                responseList.add(new Response<TagLinkMeasm>(
                        true, StatusCodes.OK, tagLinkMeasm));
                continue;
            }

            if (!authorization.isAuthorized(
                    tagLinkMeasm, RequestMethod.POST, TagLinkMeasm.class)
            ) {
                responseList.add(new Response<TagLinkMeasm>(
                        false, StatusCodes.NOT_ALLOWED, tagLinkMeasm));
                continue;
            }

            //Extend tag expiring time
            Tag tag = repository.getById(Tag.class, tagId);
            if (tag.getMeasFacilId() != null) {
                Timestamp defaultExpiration =
                    TagUtil.getMstTagDefaultExpiration();
                if (tag.getValUntil() == null
                    || defaultExpiration.after(tag.getValUntil())
                ) {
                    tag.setValUntil(defaultExpiration);
                }
            }

            responseList.add(new Response<TagLinkMeasm>(
                    true, StatusCodes.OK, repository.create(tagLinkMeasm)));
        }

        return responseList;
    }

    /**
     * Delete the given list of tag measm references.
     * @param tagLinks List to delete
     * @return Deletion result list
     */
    @POST
    @Path("delete")
    public List<Response<TagLinkMeasm>> deleteTagReference(
        @Valid List<TagLinkMeasm> tagLinks
    ) {
        List<Response<TagLinkMeasm>> responseList = new ArrayList<>();

        for (TagLinkMeasm tagLink: tagLinks) {
            if (!isExisting(tagLink)) {
                responseList.add(new Response<TagLinkMeasm>(
                        true, StatusCodes.OK, tagLink));
                continue;
            }
            if (!authorization.isAuthorized(
                    tagLink,
                    RequestMethod.DELETE,
                    TagLinkMeasm.class)
            ) {
                responseList.add(new Response<TagLinkMeasm>(
                        false,
                        StatusCodes.NOT_ALLOWED,
                        tagLink));
                continue;
            }

            // Delete existing entity
            repository.delete(
                repository.getSingle(repository
                    .queryBuilder(TagLinkMeasm.class)
                    .and("tagId", tagLink.getTagId())
                    .and("measmId", tagLink.getMeasmId())
                    .getQuery()));
            responseList.add(new Response<TagLinkMeasm>(
                true, StatusCodes.OK, tagLink));
        }
        return responseList;
    }

    private Boolean isExisting(TagLinkMeasm zuordnung) {
        return isExisting(zuordnung.getTagId(), zuordnung.getMeasmId(),
            "measm_id", "tag_link_measm");
    }
}
