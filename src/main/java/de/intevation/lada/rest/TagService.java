/* Copyright (C) 2018 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.TypedQuery;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import de.intevation.lada.model.lada.TagLinkMeasm_;
import de.intevation.lada.model.lada.TagLinkSample_;
import de.intevation.lada.model.lada.TagLink_;
import de.intevation.lada.model.master.Tag;
import de.intevation.lada.model.master.Tag_;
import de.intevation.lada.util.data.TagUtil;
import de.intevation.lada.util.rest.RequestMethod;


/**
 * REST-Service for tags.
 */
@Path(LadaService.PATH_REST + "tag")
public class TagService extends LadaIntegerIdEntityService {

    private static final String TAGS_PER_OBJECT_QUERY = String.format(
        "select t from %s t where ", Tag_.class_.getName());

    private static final String ID_PARAM = "id%s";

    private static final String IN_PREDICATE = String.format(
        "t.%s in (select %s from %%s where %%s = :%s)",
        Tag_.ID, TagLink_.TAG_ID, ID_PARAM);

    /**
     * Get a single tag by id.
     *
     * @return the tag
     */
    @GET
    @Path("{id}")
    public Tag getById() {
        return repository.getById(Tag.class, id);
    }

    /**
     * Get tags.
     *
     * @param sampleIds filter by IDs of Sample objects.
     * @param measmIds filter by IDs of Measm objects.
     * Ignored if sampleIds is given.
     *
     * @return List of Tag objects.
     */
    @GET
    public List<Tag> get(
        @QueryParam("sampleId") Set<Integer> sampleIds,
        @QueryParam("measmId") Set<Integer> measmIds
    ) {
        if (sampleIds.isEmpty() && measmIds.isEmpty()) {
            return repository.getAll(Tag.class);
        }

        Set<Integer> ids;
        String tagLinkEntityName;
        String taggedObjectKey;
        if (!sampleIds.isEmpty()) {
            ids = sampleIds;
            tagLinkEntityName = TagLinkSample_.class_.getName();
            taggedObjectKey = TagLinkSample_.SAMPLE_ID;
        } else {
            ids = measmIds;
            tagLinkEntityName = TagLinkMeasm_.class_.getName();
            taggedObjectKey = TagLinkMeasm_.MEASM_ID;
        }
        // Return only tags assigned to all objects given by IDs
        String queryString = TAGS_PER_OBJECT_QUERY + ids.stream()
            .map(id -> String.format(
                    IN_PREDICATE,
                    tagLinkEntityName,
                    taggedObjectKey,
                    id))
            .collect(Collectors.joining(" and "));
        TypedQuery<Tag> query = repository.entityManager().createQuery(
            queryString, Tag.class);
        ids.forEach(
            id -> query.setParameter(String.format(ID_PARAM, id), id));
        return query.getResultList();
    }

    /**
     * Update an existing tag object.
     *
     * @param tag Tag to update using payload like
     * @return the updated tag object
     * @throws BadRequestException if any constraint violations are detected.
     */
    @PUT
    @Path("{id}")
    public Tag update(
        @Valid Tag tag
    ) throws BadRequestException {
        // Drop validity for network-tags
        if (tag.getNetworkId() != null) {
            tag.setValUntil(null);
        }

        // Set default validity for measFacil-tags
        if (tag.getMeasFacilId() != null && tag.getValUntil() == null) {
            tag.setValUntil(TagUtil.getMstTagDefaultExpiration());
        }

        return repository.update(tag);
    }

    /**
     * Creates a new tag.
     *
     * @param tag Tag to create.
     * @return Created Tag object
     * @throws BadRequestException if any constraint violations are detected.
     */
    @POST
    public Tag create(
        @Valid Tag tag
    ) throws BadRequestException {
        tag.setLadaUserId(authorization.getInfo().getUserId());

        if (tag.getValUntil() == null
            && tag.getMeasFacilId() != null
        ) {
            tag.setValUntil(TagUtil.getMstTagDefaultExpiration());
        }
        return repository.create(tag);
    }

    /**
     * Delete a tag.
     */
    @DELETE
    @Path("{id}")
    public void delete() {
        Tag tag = repository.getById(Tag.class, id);
        authorization.authorize(tag, RequestMethod.DELETE);
        repository.delete(tag);
    }
}
