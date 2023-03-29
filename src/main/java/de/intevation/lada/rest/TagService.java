/* Copyright (C) 2018 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.Valid;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import de.intevation.lada.model.lada.TagLink;
import de.intevation.lada.model.master.MeasFacil;
import de.intevation.lada.model.master.Tag;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.data.TagUtil;
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.util.rest.Response;

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
     * @return Response containing the tag
     */
    @GET
    @Path("{id}")
    public Response getById(
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
     * @param measmIds filter by IDs of Measm objects. Ignored if sampleId is given.
     *
     * @return Response with list of Tag objects.
     */
    @GET
    public Response get(
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
            final String filterBy = sampleIds.isEmpty() ? "measmId" : "sampleId";
            final Iterator<Integer> filterIds =
                sampleIds.isEmpty() ? measmIds.iterator() : sampleIds.iterator();
            Predicate idFilter = builder.equal(
                joinTagZuordnung.get(filterBy), filterIds.next());
            result = repository.filterPlain(criteriaQuery.where(idFilter));
            while (!result.isEmpty() && filterIds.hasNext()) {
                idFilter = builder.equal(
                    joinTagZuordnung.get(filterBy), filterIds.next());
                result.retainAll(
                    repository.filterPlain(criteriaQuery.where(idFilter)));
            }
        } else {
            result = repository.getAllPlain(Tag.class);
        }

        return authorization.filter(
            new Response(true, StatusCodes.OK, result), Tag.class);
    }

    /**
     * Update an existing tag object.
     *
     * @param tag Tag to update using payload like
     * @param id Tag id
     * @return Response object containing the updated tag object
     */
    @PUT
    @Path("{id}")
    public Response update(
        @PathParam("id") String id,
        @Valid Tag tag
    ) {
        Tag origTag = repository.getByIdPlain(Tag.class, tag.getId());
        if (!authorization.isAuthorized(
                origTag, RequestMethod.PUT, Tag.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }

        String tagTyp = tag.getTagType();
        String origTagTyp = origTag.getTagType();
        Timestamp gueltigBis = tag.getValUntil();

        if (tag.getMeasFacilId() != null) {
            MeasFacil mst = repository.getByIdPlain(
                MeasFacil.class, tag.getMeasFacilId());
            if (tag.getNetworkId() == null) {
                tag.setNetworkId(mst.getNetworkId());
            } else if (!tag.getNetworkId().equals(mst.getNetworkId())) {
                return new Response(false, StatusCodes.VALUE_NOT_MATCHING, "mst");
            }
        }

        if (!tagTyp.equals(origTagTyp)
        ) {
            // User changed type but not gueltigBis
            switch (tagTyp) {
            // Remove expiration timestamp for 'advanced' tags
            case Tag.TAG_TYPE_GLOBAL:
                tag.setValUntil(null);
                break;
            case Tag.TAG_TYPE_NETZBETREIBER:
                if (!Tag.TAG_TYPE_GLOBAL.equals(origTagTyp)) {
                    tag.setValUntil(null);
                }
                break;
            case Tag.TAG_TYPE_MST:
                // Set default expiration for tags downgraded to 'mst'
                tag.setValUntil(TagUtil.getMstTagDefaultExpiration());
                break;
            default:
                throw new IllegalArgumentException("Unknown tag type");
            }
        } else {
            // tagType messstelle never without gueltigBis
            if (tagTyp.equals(Tag.TAG_TYPE_MST) && gueltigBis == null) {
                tag.setValUntil(TagUtil.getMstTagDefaultExpiration());
            }
        }

        Response response = repository.update(tag);
        return authorization.filter(
            response,
            Tag.class);
    }

    /**
     * Creates a new tag.
     *
     * @param tag Tag to create.
     * @return Response object
     */
    @POST
    public Response create(
        @Valid Tag tag
    ) {
        if (!authorization.isAuthorized(
                tag, RequestMethod.POST, Tag.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }

        tag.setLadaUserId(authorization.getInfo().getUserId());

        if (tag.getMeasFacilId() != null) {
            MeasFacil mst = repository.getByIdPlain(
                MeasFacil.class, tag.getMeasFacilId());
            if (tag.getNetworkId() == null) {
                tag.setNetworkId(mst.getNetworkId());
            } else if (!tag.getNetworkId().equals(mst.getNetworkId())) {
                return new Response(false, StatusCodes.VALUE_NOT_MATCHING, "mst");
            }
        }

        if (tag.getValUntil() == null
            && Tag.TAG_TYPE_MST.equals(tag.getTagType())
        ) {
            tag.setValUntil(TagUtil.getMstTagDefaultExpiration());
        }
        return repository.create(tag);
    }

    /**
     * Delete a tag.
     * @param id Tag id
     * @return Response object
     */
    @DELETE
    @Path("{id}")
    public Response delete(
        @PathParam("id") Integer id
    ) {
        Tag tag = repository.getByIdPlain(Tag.class, id);
        if (!authorization.isAuthorized(
                tag, RequestMethod.DELETE, Tag.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }
        return repository.delete(tag);
    }
}
