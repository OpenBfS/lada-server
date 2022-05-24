/* Copyright (C) 2018 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest.stamm;

import java.sql.Timestamp;
import java.util.Calendar;
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

    @Inject
    private TagUtil tagUtil;

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
     * Get tags for Probe or Messung instances,
     * filtered by the users messstelle id.
     * If IDs of Probe or Messung objects are given as URL parameters like
     * <pre>
     * <code>
     * ?pid=42&pid=24
     * </code>
     * </pre>
     * only those tags are returned that are associated to all of them.
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

        // Return only the tags without mstId or matching the user
        final String mstIdParam = "mstId";
        Predicate filter = builder.or(
            builder.isNull(root.get(mstIdParam)),
            builder.in(root.get(mstIdParam)).value(
                authorization.getInfo().getMessstellen()));

        // Return only tags assigned to all given Probe or Messung objects
        List<Tag> result;
        if (!pIds.isEmpty() || !mIds.isEmpty()) {
            Join<Tag, TagZuordnung> joinTagZuordnung =
                root.join("tagZuordnungs");
            // Work-around missing SQL INTERSECTION in JPA:
            final String filterBy = pIds.isEmpty() ? "messungId" : "probeId";
            final Iterator<Integer> filterIds =
                pIds.isEmpty() ? mIds.iterator() : pIds.iterator();
            Predicate idFilter = builder.and(
                filter,
                builder.equal(
                    joinTagZuordnung.get(filterBy), filterIds.next()));
            result = repository.filterPlain(criteriaQuery.where(idFilter));
            while (!result.isEmpty() && filterIds.hasNext()) {
                idFilter = builder.and(
                    filter,
                    builder.equal(
                        joinTagZuordnung.get(filterBy), filterIds.next()));
                result.retainAll(
                    repository.filterPlain(criteriaQuery.where(idFilter)));
            }
        } else {
            result = repository.filterPlain(criteriaQuery.where(filter));
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
        //Check if tag has changed and is valid
        String tagTyp = tag.getTypId();
        String origTagTyp = origTag.getTypId();
        if (!tagTyp.equals(origTagTyp)) {
            //Tags may only changed to global
            //or from messstelle to netzbetreiber
            if (!tagTyp.equals(Tag.TAG_TYPE_GLOBAL)
                && !(tagTyp.equals(Tag.TAG_TYPE_NETZBETREIBER)
                    && origTagTyp.equals(Tag.TAG_TYPE_MST))) {
                return new Response(false,
                    StatusCodes.ERROR_VALIDATION, "Invalid tag type change");
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

        tag.setGeneratedAt(new Timestamp(System.currentTimeMillis()));
        if (tag.getGueltigBis() == null) {
            tag.setGueltigBis(getGueltigBis(tag,
                new Timestamp(System.currentTimeMillis())));
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

    /**
     * Get gueltig bis timestamp for the given tag and timestamp.
     * @param tag Tag to get date for
     * @param ts Timestamp to use as base
     * @return Timestamp
     */
    public static Timestamp getGueltigBis(Tag tag, Timestamp ts) {
        Calendar now;
        Calendar tagExp;
        switch (tag.getTypId()) {
            //Global tags do not expire
            case Tag.TAG_TYPE_GLOBAL:
                return null;
            //Mst tags expire after 365 days
            case Tag.TAG_TYPE_MST:
                //Check if expiration date needs to be extended
                if (tag.getGueltigBis() != null) {
                    tagExp = Calendar.getInstance();
                    tagExp.setTime(tag.getGueltigBis());
                    now = Calendar.getInstance();
                    now.add(Calendar.DAY_OF_YEAR, Tag.MST_TAG_EXPIRATION_TIME);
                    if (tagExp.compareTo(now) > 0) {
                        return tag.getGueltigBis();
                    }
                }
                Calendar mstCal = Calendar.getInstance();
                mstCal.setTime(ts);
                mstCal.add(Calendar.DAY_OF_YEAR, Tag.MST_TAG_EXPIRATION_TIME);
                ts.setTime(mstCal.getTimeInMillis());
                return ts;
            // Generated tags expire after 548 days,
            // other Netzbetreiber tags do not expire
            case Tag.TAG_TYPE_NETZBETREIBER:
                if (!tag.getGenerated()) {
                    return null;
                }
                //Check if expiration date needs to be extended
                if (tag.getGueltigBis() != null) {
                    tagExp = Calendar.getInstance();
                    tagExp.setTime(tag.getGueltigBis());
                    now = Calendar.getInstance();
                    now.add(Calendar.DAY_OF_YEAR,
                        Tag.GENERATED_EXPIRATION_TIME);
                    if (tagExp.compareTo(now) > 0) {
                        return tag.getGueltigBis();
                    }
                }
                Calendar cal = Calendar.getInstance();
                cal.setTime(ts);
                cal.add(Calendar.DAY_OF_YEAR, Tag.GENERATED_EXPIRATION_TIME);
                ts.setTime(cal.getTimeInMillis());
                return ts;
            default: return null;
        }
    }
}
