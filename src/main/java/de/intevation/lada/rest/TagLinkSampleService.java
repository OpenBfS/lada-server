/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
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

import de.intevation.lada.model.lada.TagLinkSample;
import de.intevation.lada.model.master.Tag;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.data.TagUtil;
import de.intevation.lada.util.rest.RequestMethod;
import jakarta.validation.Valid;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("tag/taglinksample")
public class TagLinkSampleService extends TagLinkService {

    /**
     * Create new tag sample references.
     * @param tagLinks List of new tag references
     * @return Result list.
     */
    @POST
    public List<Response<TagLinkSample>> createTagReference(
        @Valid List<TagLinkSample> tagLinks
    ) {
        //Create Response
        List<Response<TagLinkSample>> responseList = new ArrayList<>();

        for (TagLinkSample tagLinkSample: tagLinks) {
            Integer tagId = tagLinkSample.getTagId();
            if (isExisting(tagLinkSample)) {
                responseList.add(new Response<TagLinkSample>(
                        true, StatusCodes.OK, tagLinkSample));
                continue;
            }

            if (!authorization.isAuthorized(
                    tagLinkSample, RequestMethod.POST, TagLinkSample.class)
            ) {
                responseList.add(new Response<TagLinkSample>(
                        false, StatusCodes.NOT_ALLOWED, tagLinkSample));
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

            responseList.add(new Response<TagLinkSample>(
                    true, StatusCodes.OK, repository.create(tagLinkSample)));
        }

        return responseList;
    }

    /**
     * Delete the given list of tag sample references.
     * @param tagLinks List to delete
     * @return Deletion result list
     */
    @POST
    @Path("delete")
    public List<Response<TagLinkSample>> deleteTagReference(
        @Valid List<TagLinkSample> tagLinks
    ) {
        List<Response<TagLinkSample>> responseList = new ArrayList<>();

        for (TagLinkSample tagLink: tagLinks) {
            if (!isExisting(tagLink)) {
                responseList.add(new Response<TagLinkSample>(
                        true, StatusCodes.OK, tagLink));
                continue;
            }
            if (!authorization.isAuthorized(
                    tagLink,
                    RequestMethod.DELETE,
                    TagLinkSample.class)
            ) {
                responseList.add(new Response<TagLinkSample>(
                        false,
                        StatusCodes.NOT_ALLOWED,
                        tagLink));
                continue;
            }

            // Delete existing entity
            repository.delete(
                repository.getSingle(repository
                    .queryBuilder(TagLinkSample.class)
                    .and("tagId", tagLink.getTagId())
                    .and("sampleId", tagLink.getSampleId())
                    .getQuery()));
            responseList.add(new Response<TagLinkSample>(
                true, StatusCodes.OK, tagLink));
        }
        return responseList;
    }

    private Boolean isExisting(TagLinkSample zuordnung) {
        return isExisting(zuordnung.getTagId(), zuordnung.getSampleId(),
            "sample_id", "tag_link_sample");
    }
}
