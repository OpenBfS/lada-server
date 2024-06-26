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

import jakarta.inject.Inject;
import jakarta.persistence.Query;
import jakarta.validation.Valid;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import de.intevation.lada.model.lada.TagLink;
import de.intevation.lada.model.master.Tag;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.data.TagUtil;
import de.intevation.lada.util.rest.RequestMethod;


/**
 * REST-Service for associations of tags to objects.
 */

@Path(LadaService.PATH_REST + "tag/taglink")
public class TagLinkService extends LadaService {

    @Inject
    private Repository repository;

    @Inject
    @AuthorizationConfig(type = AuthorizationType.HEADER)
    private Authorization authorization;

    private static final String EXISTS_QUERY_TEMPLATE =
        "SELECT EXISTS("
        + "SELECT 1 FROM lada.tag_link "
        + "WHERE tag_id=:%s"
        + " AND %s=:%s)";

    public static class Response {
        private boolean success;
        private String message;
        private TagLink data;

        private Response(boolean success, int code, TagLink data) {
            this.success = success;
            this.message = Integer.toString(code);
            this.data = data;
        }

        public boolean getSuccess() {
            return this.success;
        }

        public String getMessage() {
            return this.message;
        }

        public TagLink getData() {
            return this.data;
        }
    }

    /**
     * Create new references between tags and Sample or Measm objects.
     *
     * @param tagLinks A list of references like
     * @return Response with list of Response objects for each reference.
     */
    @POST
    public List<Response> createTagReference(
        @Valid List<TagLink> tagLinks
    ) {
        //Create Response
        List<Response> responseList = new ArrayList<>();

        for (TagLink zuordnung: tagLinks) {
            // Check if payload contains sensible information
            Integer tagId = zuordnung.getTagId();
            if (tagId == null
                || zuordnung.getSampleId() != null
                && zuordnung.getMeasmId() != null
            ) {
                responseList.add(new Response(
                        false, StatusCodes.ERROR_VALIDATION, zuordnung));
                continue;
            }

            if (isExisting(zuordnung)) {
                responseList.add(new Response(
                        true, StatusCodes.OK, zuordnung));
                continue;
            }

            if (!authorization.isAuthorized(
                    zuordnung, RequestMethod.POST, TagLink.class)
            ) {
                responseList.add(new Response(
                        false, StatusCodes.NOT_ALLOWED, zuordnung));
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

            responseList.add(new Response(
                    true, StatusCodes.OK, repository.create(zuordnung)));
        }

        return responseList;
    }

    /**
     * Delete references between tags and Sample or Messung objects.
     *
     * @param tagLinks A list of references like
     *
     * @return Response with list of Response objects for each reference.
     */
    @POST
    @Path("delete")
    public List<Response> deleteTagReference(
        @Valid List<TagLink> tagLinks
    ) {
        List<Response> responseList = new ArrayList<>();

        for (TagLink zuordnung: tagLinks) {
            if (!isExisting(zuordnung)) {
                responseList.add(new Response(
                        true, StatusCodes.OK, zuordnung));
                continue;
            }
            if (!authorization.isAuthorized(
                    zuordnung,
                    RequestMethod.DELETE,
                    TagLink.class)
            ) {
                responseList.add(new Response(
                        false,
                        StatusCodes.NOT_ALLOWED,
                        zuordnung));
                continue;
            }

            // Delete existing entity
            repository.delete(
                repository.getSingle(repository
                    .queryBuilder(TagLink.class)
                    .and("tagId", zuordnung.getTagId())
                    .and("sampleId", zuordnung.getSampleId())
                    .and("measmId", zuordnung.getMeasmId())
                    .getQuery()));
            responseList.add(new Response(true, StatusCodes.OK, zuordnung));
        }
        return responseList;
    }

    private Boolean isExisting(TagLink zuordnung) {
        // Check if tag is already assigned
        final String tagIdParam = "tagId",
            taggedIdParam = "taggedId";
        String idField = zuordnung.getSampleId() != null
            ? "sample_id" : "measm_id";
        Query isAssigned = repository.queryFromString(
            String.format(EXISTS_QUERY_TEMPLATE,
                tagIdParam, idField, taggedIdParam));
        isAssigned.setParameter(tagIdParam, zuordnung.getTagId());
        isAssigned.setParameter(taggedIdParam,
            zuordnung.getSampleId() != null
            ? zuordnung.getSampleId()
            : zuordnung.getMeasmId());
        return (Boolean) isAssigned.getSingleResult();
    }
}
