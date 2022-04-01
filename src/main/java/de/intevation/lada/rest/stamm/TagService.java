/* Copyright (C) 2018 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest.stamm;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import de.intevation.lada.model.land.Messung;
import de.intevation.lada.model.land.Probe;
import de.intevation.lada.model.land.TagZuordnung;
import de.intevation.lada.model.stammdaten.Tag;
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
 * REST-Service for the probe tags.
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
    public Response getTags(
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
        List<Tag> tags = repository.filterPlain(criteriaQuery);
        return new Response(true, StatusCodes.OK, tags);
    }

    /**
     * Creates a new reference between a tag and a probe.
     * The tag can be an existing one or a new one, embedded in the request.
     * Request for creating a new tag:
     * <pre>
     * <code>
     * {
     *   "probeId": [Integer],
     *   "tag": {
     *     "tag": [String],
     *     "mstId": [String]
     *   }
     * }
     * </code>
     * </pre>
     *
     * Existing tags can be used with the following request:
     * <pre>
     * <code>
     * {
     *   "probeId": [Integer],
     *   "tagId": [Integer]
     * }
     * </code>
     * </pre>
     * Requests containing both, tag and tagId will be rejected.
     * Setting a mstId is mandatory, as only global tags have no mstId.
     */
    @POST
    @Path("/")
    public Response createTagReference(
        @Context HttpServletRequest request,
        TagZuordnung zuordnung
    ) {
        // Check if payload contains sensible information
        if (zuordnung == null) {
            return new Response(
                false, StatusCodes.ERROR_VALIDATION, "Not a valid tag");
        }
        Tag tag = zuordnung.getTag();
        Integer tagId = zuordnung.getTagId();
        if (tag != null && tagId != null
            || tag == null && tagId == null
            || zuordnung.getProbeId() != null
            && zuordnung.getMessungId() != null
        ) {
            return new Response(
                false, StatusCodes.ERROR_VALIDATION, "Not a valid tag");
        }

        List<String> messstellen =
            authorization.getInfo(request).getMessstellen();

        if (tag == null) { // Use existing tag
            //Check if tag is already assigned to the probe
            final String tagIdParam = "tagId",
                mstIdsParam = "mstIds",
                taggedIdParam = "taggedId";
            String idField = zuordnung.getProbeId() != null
                ? "probe_id" : "messung_id";
            Query isAssigned = repository.queryFromString(
                "SELECT EXISTS("
                + "SELECT 1 FROM land.tagzuordnung "
                + "JOIN stamm.tag ON tag_id=tag.id "
                + "WHERE tag_id=:" + tagIdParam
                + " AND (mst_id IS NULL OR mst_id IN (:" + mstIdsParam + "))"
                + " AND " + idField + "=:" + taggedIdParam + ")");
            isAssigned.setParameter(tagIdParam, zuordnung.getTagId());
            isAssigned.setParameter(mstIdsParam, messstellen);
            isAssigned.setParameter(taggedIdParam,
                zuordnung.getProbeId() != null
                ? zuordnung.getProbeId()
                : zuordnung.getMessungId());
            if ((Boolean) isAssigned.getSingleResult()) {
                return new Response(
                    true,
                    StatusCodes.OK,
                    "Tag is already assigned to probe");
            }

            tag = repository.getByIdPlain(Tag.class, tagId);
            String mstId = tag.getMstId();
            //If user tries to assign a global tag: authorize
            if (mstId == null) {
                Object data;
                boolean authorized = false;
                if (zuordnung.getMessungId() != null) {
                    data = repository.getByIdPlain(
                        Messung.class, zuordnung.getMessungId());
                    authorized = authorization.isAuthorized(
                        request,
                        data,
                        RequestMethod.PUT,
                        Messung.class
                    );
                } else {
                    data = repository.getByIdPlain(
                        Probe.class, zuordnung.getProbeId());
                    authorized = authorization.isAuthorized(
                        request,
                        data,
                        RequestMethod.PUT,
                        Probe.class
                    );
                }
                if (!authorized) {
                    return new Response(
                        false,
                        StatusCodes.NOT_ALLOWED,
                        "Not authorized to set global tag");
                }
            //Else check if it is the users private tag
            } else if (!messstellen.contains(mstId)) {
                return new Response(
                    false, StatusCodes.NOT_ALLOWED, "Invalid mstId");
            }

            zuordnung.setTag(tag);

        } else { // Create new tag
            String mstId = zuordnung.getTag().getMstId();
            //mstId may not be null, global tags cannot be created
            if (mstId == null || !messstellen.contains(mstId)) {
                return new Response(
                    false,
                    StatusCodes.NOT_ALLOWED,
                    "Invalid/empty mstId");
            }
            if (!repository.create(tag).getSuccess()) {
                //TODO Proper response code?
                return new Response(
                    false,
                    StatusCodes.ERROR_DB_CONNECTION,
                    "Failed to create Tag");
            }
        }
        return repository.create(zuordnung);
    }

    /**
     * Delete a reference between a tag and a probe.
     */
    @DELETE
    @Path("/")
    public Response deleteTagReference(
        @Context HttpHeaders headers,
        @Context HttpServletRequest request,
        TagZuordnung tagZuordnung
    ) {
        if ((tagZuordnung.getProbeId() == null
            && tagZuordnung.getMessungId() == null)
            || tagZuordnung.getTagId() == null
        ) {
            return new Response(
                false, StatusCodes.NOT_ALLOWED, "Invalid TagZuordnung");
        }
        boolean global = false;
        //Check if its a global tag
        Tag tag = repository.getByIdPlain(
            Tag.class, tagZuordnung.getTagId());
        if (tag.getMstId() == null) {
            Object data;
            boolean authorized = false;
            if (tagZuordnung.getMessungId() != null) {
                data = repository.getByIdPlain(
                    Messung.class, tagZuordnung.getMessungId());
                authorized = authorization.isAuthorized(
                    request,
                    data,
                    RequestMethod.PUT,
                    Messung.class
                );
            } else {
                data = repository.getByIdPlain(
                    Probe.class, tagZuordnung.getProbeId());
                authorized = authorization.isAuthorized(
                    request,
                    data,
                    RequestMethod.PUT,
                    Probe.class
                );
            }
            if (!authorized) {
                return new Response(
                    false,
                    StatusCodes.NOT_ALLOWED,
                    "Not authorized to delete global tag");
            } else {
                global = true;
            }
        }

        UserInfo userInfo = authorization.getInfo(request);
        EntityManager em = repository.entityManager();
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<TagZuordnung> criteriaQuery =
            builder.createQuery(TagZuordnung.class);
        Root<TagZuordnung> root = criteriaQuery.from(TagZuordnung.class);
        Join<TagZuordnung, Tag> joinTagZuordnung =
            root.join("tag", javax.persistence.criteria.JoinType.LEFT);
        Predicate tagFilter =
            builder.equal(root.get("tag").get("id"), tagZuordnung.getTagId());
        Predicate mstFilter;
        if (global) {
            mstFilter = builder.isNull(joinTagZuordnung.get("mstId"));
        } else {
            mstFilter = builder.in(
                joinTagZuordnung.get("mstId")).value(userInfo.getMessstellen());
        }
        Predicate filter = builder.and(tagFilter, mstFilter);

        if (tagZuordnung.getProbeId() != null) {
            Predicate probeFilter =
                builder.equal(root.get("probeId"), tagZuordnung.getProbeId());
            filter = builder.and(filter, probeFilter);
        } else {
            Predicate messungFilter =
                builder.equal(
                    root.get("messungId"), tagZuordnung.getMessungId());
            filter = builder.and(filter, messungFilter);
        }

        criteriaQuery.where(filter);
        List<TagZuordnung> zuordnungs =
            repository.filterPlain(criteriaQuery);

        // TODO Error code if no zuordnung is found?
        if (zuordnungs.size() == 0) {
            return new Response(
                false, StatusCodes.NOT_ALLOWED, "No valid Tags found");
        } else {
            return repository.delete(zuordnungs.get(0));
        }
    }
}
