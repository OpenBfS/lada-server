/* Copyright (C) 2018 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import jakarta.inject.Inject;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.validation.Valid;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

import de.intevation.lada.model.lada.TagLink;
import de.intevation.lada.model.master.Tag;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.TagUtil;
import de.intevation.lada.util.rest.RequestMethod;

/**
 * REST-Service for tags.
 */

@Path("tag")
public class TagService extends LadaService {

    @Inject
    private Repository repository;

    @Inject
    @AuthorizationConfig(type = AuthorizationType.HEADER)
    private Authorization authorization;

    /**
     * Get a single tag by id.
     * @param id Tag id
     * @return the tag
     */
    @GET
    @Path("{id}")
    public Tag getById(
        @PathParam("id") String id
    ) {
        return authorization.filter(
            repository.getById(Tag.class, Integer.valueOf(id)),
            Tag.class);
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
        CriteriaBuilder builder =
            repository.entityManager().getCriteriaBuilder();
        CriteriaQuery<Tag> criteriaQuery = builder.createQuery(Tag.class);
        Root<Tag> root = criteriaQuery.from(Tag.class);

        List<Tag> result;
        if (!sampleIds.isEmpty() || !measmIds.isEmpty()) {
            // Return only tags assigned to all given Sample or Messung objects
            Join<Tag, TagLink> joinTagZuordnung =
                root.join("tagZuordnungs");
            // Work-around missing SQL INTERSECTION in JPA:
            final String filterBy = sampleIds.isEmpty()
                ? "measmId" : "sampleId";
            final Iterator<Integer> filterIds = sampleIds.isEmpty()
                ? measmIds.iterator() : sampleIds.iterator();
            Predicate idFilter = builder.equal(
                joinTagZuordnung.get(filterBy), filterIds.next());
            result = repository.filter(criteriaQuery.where(idFilter));
            while (!result.isEmpty() && filterIds.hasNext()) {
                idFilter = builder.equal(
                    joinTagZuordnung.get(filterBy), filterIds.next());
                result.retainAll(
                    repository.filter(criteriaQuery.where(idFilter)));
            }
        } else {
            result = repository.getAll(Tag.class);
        }

        return authorization.filter(result, Tag.class);
    }

    /**
     * Update an existing tag object.
     *
     * @param tag Tag to update using payload like
     * @param id Tag id
     * @return the updated tag object
     */
    @PUT
    @Path("{id}")
    public Tag update(
        @PathParam("id") String id,
        @Valid Tag tag
    ) {
        authorization.authorize(tag, RequestMethod.PUT, Tag.class);

        // Drop validity for network-tags
        if (tag.getNetworkId() != null) {
            tag.setValUntil(null);
        }

        // Set default validity for measFacil-tags
        if (tag.getMeasFacilId() != null && tag.getValUntil() == null) {
            tag.setValUntil(TagUtil.getMstTagDefaultExpiration());
        }

        return authorization.filter(
            repository.update(tag),
            Tag.class);
    }

    /**
     * Creates a new tag.
     *
     * @param tag Tag to create.
     * @return Created Tag object
     */
    @POST
    public Tag create(
        @Valid Tag tag
    ) {
        authorization.authorize(
            tag, RequestMethod.POST, Tag.class);

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
     * @param id Tag id
     */
    @DELETE
    @Path("{id}")
    public void delete(
        @PathParam("id") Integer id
    ) {
        Tag tag = repository.getById(Tag.class, id);
        authorization.authorize(
            tag, RequestMethod.DELETE, Tag.class);
        repository.delete(tag);
    }
}
