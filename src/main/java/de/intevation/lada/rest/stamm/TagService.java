/* Copyright (C) 2018 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest.stamm;

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
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import de.intevation.lada.model.land.TagZuordnung;
import de.intevation.lada.model.stammdaten.MessStelle;
import de.intevation.lada.model.stammdaten.Tag;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.data.TagUtil;
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.rest.LadaService;

/**
 * REST-Service for tags.
 */

@Path("rest/tag")
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
    @Path("/{id}")
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
     * If IDs of Probe or Messung objects are given as URL parameters like
     * <pre>
     * <code>
     * ?pid=42&pid=24
     * </code>
     * </pre>
     * only those tags are returned that are associated to all of them.
     * Otherwise, return only tags the user is allowed to assign.
     *
     * @param pIds filter by IDs of Probe objects.
     * @param mIds filter by IDs of Messung objects. Ignored if pid is given.
     *
     * @return Response with list of Tag objects.
     */
    @GET
    @Path("/")
    public Response get(
        @QueryParam("pid") Set<Integer> pIds,
        @QueryParam("mid") Set<Integer> mIds
    ) {
        CriteriaBuilder builder =
            repository.entityManager().getCriteriaBuilder();
        CriteriaQuery<Tag> criteriaQuery = builder.createQuery(Tag.class);
        Root<Tag> root = criteriaQuery.from(Tag.class);

        List<Tag> result;
        if (!pIds.isEmpty() || !mIds.isEmpty()) {
            // Return only tags assigned to all given Probe or Messung objects
            Join<Tag, TagZuordnung> joinTagZuordnung =
                root.join("tagZuordnungs");
            // Work-around missing SQL INTERSECTION in JPA:
            final String filterBy = pIds.isEmpty() ? "messungId" : "probeId";
            final Iterator<Integer> filterIds =
                pIds.isEmpty() ? mIds.iterator() : pIds.iterator();
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
     * <pre>
     * <code>
     * {
     *   id: [int], //Tag id
     *   tag: [string], //Tag text
     *   netzbetreiberId: [string], //Tag netzbetreiber
     *   mstId: [string], //Owner mst id
     *   userId: [integer], //Creator user id
     *   typId: [string], //Tag type id
     *   gueltigBis: [integer] //Optional: Expiration date
     * }
     * </code>
     * </pre>
     *
     * @param id Tag id
     * @return Response object containing the updated tag object
     */
    @PUT
    @Path("/{id}")
    public Response update(
        @PathParam("id") String id,
        Tag tag
    ) {
        Tag origTag = repository.getByIdPlain(Tag.class, tag.getId());
        if (!authorization.isAuthorized(
                origTag, RequestMethod.PUT, Tag.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }

        String tagTyp = tag.getTypId();
        String origTagTyp = origTag.getTypId();
        Timestamp gueltigBis = tag.getGueltigBis();

        if (tag.getMstId() != null) {
            MessStelle mst = repository.getByIdPlain(
                MessStelle.class, tag.getMstId());
            if (tag.getNetzbetreiberId() == null) {
                tag.setNetzbetreiberId(mst.getNetzbetreiberId());
            } else if (!tag.getNetzbetreiberId().equals(mst.getNetzbetreiberId())) {
                return new Response(false, StatusCodes.VALUE_NOT_MATCHING, "mst");
            }
        }

        if (!tagTyp.equals(origTagTyp)
        ) {
            // User changed type but not gueltigBis
            switch (tagTyp) {
            // Remove expiration timestamp for 'advanced' tags
            case Tag.TAG_TYPE_GLOBAL:
                tag.setGueltigBis(null);
                break;
            case Tag.TAG_TYPE_NETZBETREIBER:
                if (!Tag.TAG_TYPE_GLOBAL.equals(origTagTyp)) {
                    tag.setGueltigBis(null);
                }
                break;
            case Tag.TAG_TYPE_MST:
                // Set default expiration for tags downgraded to 'mst'
                tag.setGueltigBis(TagUtil.getMstTagDefaultExpiration());
                break;
            default:
                throw new IllegalArgumentException("Unknown tag type");
            }
        } else {
            // tagType messstelle never without gueltigBis
            if (tagTyp.equals(Tag.TAG_TYPE_MST) && gueltigBis == null) {
                tag.setGueltigBis(TagUtil.getMstTagDefaultExpiration());
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
     * Request:
     * <pre>
     * <code>
     * {
     *   tag: [string], //Tag text
     *   netzbetreiberId: [string], //Tag netzbetreiber
     *   mstId: [string], //Owner mst id
     *   userId: [integer], //Creator user id
     *   typId: [string], //Tag type id
     *   gueltigBis: [integer] //Optional: Expiration date
     * }
     * </code>
     * </pre>
     * @param tag Tag to create.
     * @return Response object
     */
    @POST
    @Path("/")
    public Response create(
        Tag tag
    ) {
        if (!authorization.isAuthorized(
                tag, RequestMethod.POST, Tag.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }

        tag.setUserId(authorization.getInfo().getUserId());

        if (tag.getMstId() != null) {
            MessStelle mst = repository.getByIdPlain(
                MessStelle.class, tag.getMstId());
            if (tag.getNetzbetreiberId() == null) {
                tag.setNetzbetreiberId(mst.getNetzbetreiberId());
            } else if (!tag.getNetzbetreiberId().equals(mst.getNetzbetreiberId())) {
                return new Response(false, StatusCodes.VALUE_NOT_MATCHING, "mst");
            }
        }

        if (tag.getGueltigBis() == null
            && Tag.TAG_TYPE_MST.equals(tag.getTypId())
        ) {
            tag.setGueltigBis(TagUtil.getMstTagDefaultExpiration());
        }
        return repository.create(tag);
    }

    /**
     * Delete a tag.
     * @param id Tag id
     * @return Response object
     */
    @DELETE
    @Path("/{id}")
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
