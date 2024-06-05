/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import jakarta.inject.Inject;
import jakarta.persistence.Query;
import jakarta.validation.Valid;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import de.intevation.lada.model.lada.TagLink;
import de.intevation.lada.model.master.Tag;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.data.TagUtil;
import de.intevation.lada.util.rest.RequestMethod;

public abstract class TagLinkService<T extends TagLink> extends LadaService {

    @Inject
    protected Repository repository;

    @Inject
    @AuthorizationConfig(type = AuthorizationType.HEADER)
    protected Authorization authorization;

    private static final String EXISTS_QUERY_TEMPLATE =
        "SELECT EXISTS("
        + "SELECT 1 FROM lada.%s "
        + "WHERE tag_id=:tagId"
        + " AND %s=:taggedId)";

    public static class Response<T> {
        private boolean success;
        private String message;
        private T data;

        public Response(boolean success, int code, T data) {
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

        public T getData() {
            return this.data;
        }
    }

    protected abstract Boolean isExisting(T link);
    protected abstract Object getTaggegObjectId(T link);
    protected abstract String getTaggedObjectIdField();

    /**
     * Check if a tag link already exists.
     * @param tagId Tag id
     * @param taggedId Id of the tagged object
     * @param idField Tagged object id field
     * @param linkTable Tag link table name
     * @return True if link already exists
     */
    protected Boolean isExisting(Integer tagId, Integer taggedId,
            String idField, String linkTable) {
        // Check if tag is already assigned
        final String tagIdParam = "tagId";
        final String taggedIdParam = "taggedId";
        Query isAssigned = repository.queryFromString(
            String.format(EXISTS_QUERY_TEMPLATE, linkTable, idField));
        isAssigned.setParameter(tagIdParam, tagId);
        isAssigned.setParameter(taggedIdParam, taggedId);
        return (Boolean) isAssigned.getSingleResult();
    }

    /**
     * Create new tag measm references.
     * @param tagLinks List of new tag references
     * @return Result list.
     */
    @POST
    public List<Response<T>> createTagReference(
        @Valid List<T> tagLinks
    ) {
        //Create Response
        List<Response<T>> responseList = new ArrayList<>();

        for (T tagLink: tagLinks) {
            Integer tagId = tagLink.getTagId();
            if (isExisting(tagLink)) {
                responseList.add(new Response<T>(
                        true, StatusCodes.OK, tagLink));
                continue;
            }

            if (!authorization.isAuthorized(
                    tagLink, RequestMethod.POST, tagLink.getClass())
            ) {
                responseList.add(new Response<T>(
                        false, StatusCodes.NOT_ALLOWED, tagLink));
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
            responseList.add(new Response<T>(
                    true, StatusCodes.OK, repository.create(tagLink)));
        }
        return responseList;
    }

    @POST
    @Path("delete")
    public List<Response<T>> deleteTagReference(
        @Valid List<T> tagLinks
    ) {
        List<Response<T>> responseList = new ArrayList<>();

        for (T tagLink: tagLinks) {
            if (!isExisting(tagLink)) {
                responseList.add(new Response<T>(
                        true, StatusCodes.OK, tagLink));
                continue;
            }
            if (!authorization.isAuthorized(
                    tagLink,
                    RequestMethod.DELETE,
                    tagLink.getClass())
            ) {
                responseList.add(new Response<T>(
                        false,
                        StatusCodes.NOT_ALLOWED,
                        tagLink));
                continue;
            }

            String taggedObjectIdField = getTaggedObjectIdField();
            Object taggedObjectId = getTaggegObjectId(tagLink);

            // Delete existing entity
            repository.delete(
                repository.getSingle(repository
                    .queryBuilder(tagLink.getClass())
                    .and("tagId", tagLink.getTagId())
                    .and(taggedObjectIdField, taggedObjectId)
                    .getQuery()));
            responseList.add(new Response<T>(
                true, StatusCodes.OK, tagLink));
        }
        return responseList;
    }
}
