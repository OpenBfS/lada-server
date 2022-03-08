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

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import de.intevation.lada.model.land.TagZuordnung;
import de.intevation.lada.model.stammdaten.Tag;
import de.intevation.lada.model.stammdaten.TagTyp;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.auth.UserInfo;
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
     * Get all tags for a Probe or Messung instance,
     * filtered by the users messstelle id.
     * If a pid is set in the url, the tags are filter by the given probe id.
     * If a mid is set in the url, the tags are filter by the given messung id.
     */
    @GET
    @Path("/")
    public Response get(
        @Context HttpServletRequest request,
        @Context UriInfo info
    ) {
        MultivaluedMap<String, String> params = info.getQueryParameters();
        Integer probeId = null;
        Integer messungId = null;

        if (!params.isEmpty()
            && params.containsKey("pid")
            && params.containsKey("mid")
        ) {
            return new Response(
                false,
                StatusCodes.ERROR_DB_CONNECTION,
                "Filtering by both pid and mid not allowed");
        }

        if (!params.isEmpty() && params.containsKey("pid")) {
            try {
                probeId = Integer.valueOf(params.getFirst("pid"));
            } catch (NumberFormatException e) {
                return new Response(
                    false,
                    StatusCodes.ERROR_DB_CONNECTION,
                    "Not a valid probe id");
            }
        }

        if (!params.isEmpty() && params.containsKey("mid")) {
            try {
                messungId = Integer.valueOf(params.getFirst("mid"));
            } catch (NumberFormatException nfe) {
                return new Response(
                    false,
                    StatusCodes.ERROR_DB_CONNECTION,
                    "Not a valid messung id");
            }
        }

        UserInfo userInfo = authorization.getInfo(request);
        EntityManager em = repository.entityManager();
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Tag> criteriaQuery = builder.createQuery(Tag.class);
        Root<Tag> root = criteriaQuery.from(Tag.class);
        Predicate zeroMstfilter = builder.isNull(root.get("mstId"));
        Predicate filter;
        if (userInfo.getMessstellen().isEmpty()) {
            filter = zeroMstfilter;
        } else {
            Predicate userMstFilter =
            builder.in(root.get("mstId")).value(userInfo.getMessstellen());
            filter = builder.or(zeroMstfilter, userMstFilter);
        }
        if (probeId != null) {
            Join<Tag, TagZuordnung> joinTagZuordnung =
                root.join(
                    "tagZuordnungs",
                    javax.persistence.criteria.JoinType.LEFT);
            Predicate probeFilter =
                builder.equal(joinTagZuordnung.get("probeId"), probeId);
            filter = builder.and(filter, probeFilter);
        } else if (messungId != null) {
            Join<Tag, TagZuordnung> joinTagZuordnung =
                root.join(
                    "tagZuordnungs",
                    javax.persistence.criteria.JoinType.LEFT);
            Predicate messungFilter =
                builder.equal(joinTagZuordnung.get("messungId"), messungId);
            filter = builder.and(filter, messungFilter);
        }
        criteriaQuery.where(filter);
        return authorization.filter(
            request, repository.filter(criteriaQuery), Tag.class);
    }

    /**
     * Update an existing tag object.
     * Request:
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
     * @return Response object containing the updated tag object
     */
    @PUT
    @Path("/{id}")
    public Response update(
        @Context HttpHeaders headers,
        @Context HttpServletRequest request,
        @PathParam("id") String id,
        Tag tag
    ) {
        if (!authorization.isAuthorized(
            request, tag, RequestMethod.PUT, Tag.class)) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }
        //Check if tag has changed and is valid
        Tag origTag = repository.getByIdPlain(Tag.class, tag.getId());
        String tagTyp = tag.getTypId();
        String origTagTyp = origTag.getTyp().getId();
        if (!tagTyp.equals(origTagTyp)) {
            //Tags may only changed to global
            //or from messstelle to netzbetreiber
            if (!tagTyp.equals(TagTyp.TAG_TYPE_GLOBAL)
                || !tagTyp.equals(TagTyp.TAG_TYPE_NETZBETREIBER) && !origTagTyp.equals(TagTyp.TAG_TYPE_MST)) {
                return new Response(false,
                    StatusCodes.ERROR_VALIDATION, "Invalid tag type change");
            }
        }
        Response response = repository.update(tag);
        return authorization.filter(
            request,
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
     * @param request Request object
     * @param tags List
     * @return Response object
     */
    @POST
    @Path("/")
    public Response create(
        @Context HttpServletRequest request,
        Tag tag
    ) {
        if (!authorization.isAuthorized(
                request, tag, RequestMethod.POST, Tag.class)) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }
        tag.setGeneratedAt(new Timestamp(System.currentTimeMillis()));
        if (tag.getGueltigBis() == null) {
            tag.setGueltigBis(getGueltigBis(tag,
                new Timestamp(System.currentTimeMillis())));
        }
        return repository.create(tag);
    }

    /**
     * Delete a tag.
     * @param headers Headers
     * @param request Request
     * @param id Tag id
     * @return Response object
     */
    @DELETE
    @Path("/{id}")
    public Response delete(
        @Context HttpHeaders headers,
        @Context HttpServletRequest request,
        @PathParam("id") Integer id
        ) {
            Tag tag = repository.getByIdPlain(Tag.class, id);
            if (!authorization.isAuthorized(
                request, tag, RequestMethod.DELETE, Tag.class)) {
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
        String typ = tag.getTypId() != null
            ? tag.getTypId() : tag.getTyp().getId();
        switch (typ) {
            //Global tags do not expire
            case TagTyp.TAG_TYPE_GLOBAL: return null;
            //Netzbetreiber tags do not expire
            case TagTyp.TAG_TYPE_NETZBETREIBER: return null;
            //Mst tags expire after 365 days
            case TagTyp.TAG_TYPE_MST:
                //Check if expiration date needs to be extended
                if (tag.getGueltigBis() != null) {
                    tagExp = Calendar.getInstance();
                    tagExp.setTime(tag.getGueltigBis());
                    now = Calendar.getInstance();
                    now.add(Calendar.DAY_OF_YEAR, TagTyp.MST_TAG_EXPIRATION_TIME);
                    if (tagExp.compareTo(now) > 0) {
                        return tag.getGueltigBis();
                    }
                }
                Calendar mstCal = Calendar.getInstance();
                mstCal.setTime(ts);
                mstCal.add(Calendar.DAY_OF_YEAR, TagTyp.MST_TAG_EXPIRATION_TIME);
                ts.setTime(mstCal.getTimeInMillis());
                return ts;
            //Auto tags expire after 548 days
            case TagTyp.TAG_TYPE_AUTO:
                //Check if expiration date needs to be extended
                if (tag.getGueltigBis() != null) {
                    tagExp = Calendar.getInstance();
                    tagExp.setTime(tag.getGueltigBis());
                    now = Calendar.getInstance();
                    now.add(Calendar.DAY_OF_YEAR, TagTyp.AUTO_TAG_EXPIRATION_TIME);
                    if (tagExp.compareTo(now) > 0) {
                        return tag.getGueltigBis();
                    }
                }
                Calendar cal = Calendar.getInstance();
                cal.setTime(ts);
                cal.add(Calendar.DAY_OF_YEAR, TagTyp.AUTO_TAG_EXPIRATION_TIME);
                ts.setTime(cal.getTimeInMillis());
                return ts;
            default: return null;
        }
    }
}
