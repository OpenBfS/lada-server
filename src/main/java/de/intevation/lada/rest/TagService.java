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
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import de.intevation.lada.model.lada.TagLinkMeasm;
import de.intevation.lada.model.lada.TagLinkMeasm_;
import de.intevation.lada.model.lada.TagLinkSample;
import de.intevation.lada.model.lada.TagLinkSample_;
import de.intevation.lada.model.master.Tag;
import de.intevation.lada.model.master.Tag_;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.TagUtil;
import de.intevation.lada.util.rest.RequestMethod;

/**
 * REST-Service for tags.
 */

@Path(LadaService.PATH_REST + "tag")
public class TagService extends LadaService {

    @Inject
    private Repository repository;

    /**
     * Get a single tag by id.
     * @param id Tag id
     * @return the tag
     */
    @GET
    @Path("{id}")
    public Tag getById(
        @PathParam("id") Integer id
    ) {
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
        CriteriaBuilder builder =
            repository.entityManager().getCriteriaBuilder();
        CriteriaQuery<Tag> criteriaQuery = builder.createQuery(Tag.class);
        Root<Tag> root = criteriaQuery.from(Tag.class);

        List<Tag> result;
        if (!sampleIds.isEmpty()) {
            // Return only tags assigned to all given Sample or Messung objects
            Join<Tag, TagLinkSample> joinTagZuordnung =
                root.join(Tag_.tagLinkSamples);
            // Work-around missing SQL INTERSECTION in JPA:
            final Iterator<Integer> filterIds = sampleIds.iterator();
            Predicate idFilter = builder.equal(
                joinTagZuordnung.get(TagLinkSample_.sampleId),
                    filterIds.next());
            result = repository.filter(criteriaQuery.where(idFilter));
            while (!result.isEmpty() && filterIds.hasNext()) {
                idFilter = builder.equal(
                    joinTagZuordnung.get(TagLinkSample_.sampleId),
                        filterIds.next());
                result.retainAll(
                    repository.filter(criteriaQuery.where(idFilter)));
            }
        } else if (!measmIds.isEmpty()) {
            // Return only tags assigned to all given Sample or Messung objects
            Join<Tag, TagLinkMeasm> joinTagZuordnung =
                root.join(Tag_.tagLinkMeasms);
            // Work-around missing SQL INTERSECTION in JPA:
            final Iterator<Integer> filterIds = measmIds.iterator();
            Predicate idFilter = builder.equal(
                joinTagZuordnung.get(TagLinkMeasm_.measmId), filterIds.next());
            result = repository.filter(criteriaQuery.where(idFilter));
            while (!result.isEmpty() && filterIds.hasNext()) {
                idFilter = builder.equal(
                    joinTagZuordnung.get(TagLinkMeasm_.measmId),
                        filterIds.next());
                result.retainAll(
                    repository.filter(criteriaQuery.where(idFilter)));
            }
        } else {
            result = repository.getAll(Tag.class);
        }

        return result;
    }

    /**
     * Update an existing tag object.
     *
     * @param tag Tag to update using payload like
     * @param id Tag id
     * @return the updated tag object
     * @throws BadRequestException if any constraint violations are detected.
     */
    @PUT
    @Path("{id}")
    public Tag update(
        @PathParam("id") String id,
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
     * @param id Tag id
     */
    @DELETE
    @Path("{id}")
    public void delete(
        @PathParam("id") Integer id
    ) {
        Tag tag = repository.getById(Tag.class, id);
        authorization.authorize(tag, RequestMethod.DELETE);
        repository.delete(tag);
    }
}
