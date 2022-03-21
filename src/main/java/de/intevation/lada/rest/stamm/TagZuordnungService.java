/* Copyright (C) 2018 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest.stamm;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;

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
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.rest.LadaService;

/**
 * REST-Service for associations of tags to objects.
 */

@Path("rest/tag/zuordnung")
public class TagZuordnungService extends LadaService {

    @Inject
    private Repository repository;

    @Inject
    @AuthorizationConfig(type = AuthorizationType.HEADER)
    private Authorization authorization;

    /**
     * Creates a new reference between a tag and a probe.
     *
     * Existing tags can be used with the following request:
     * <pre>
     * <code>
     * [{
     *   "probeId": [Integer],
     *   "tagId": [Integer]
     * }, {
     *    ...
     * }]
     * </code>
     * </pre>
     *
     * Setting a mstId is mandatory, as only global tags have no mstId.
     */
    @POST
    @Path("/")
    public javax.ws.rs.core.Response createTagReference(
        @Context HttpServletRequest request,
        List<TagZuordnung> zuordnungs
    ) {
        //Create Response
        JsonObjectBuilder builder = Json.createObjectBuilder();
        JsonObjectBuilder dataBuilder = Json.createObjectBuilder();

        for (int i = 0; i < zuordnungs.size(); i++) {
            JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
            TagZuordnung zuordnung = zuordnungs.get(i);

            // Check if payload contains sensible information
            if (zuordnung == null) {
                responseBuilder.add("success", false);
                responseBuilder.add("status", StatusCodes.ERROR_VALIDATION);
                responseBuilder.add("message", "Not a valid tag");
                responseBuilder.add("data", "");
                dataBuilder.add("newZuordnung" + i, responseBuilder);
                continue;
            }
            Integer tagId = zuordnung.getTagId();
            if (tagId == null
                || zuordnung.getProbeId() != null
                && zuordnung.getMessungId() != null
            ) {
                responseBuilder.add("success", false);
                responseBuilder.add("status", StatusCodes.ERROR_VALIDATION);
                responseBuilder.add("message", "Not a valid tag");
                responseBuilder.add("data", zuordnung.toJson());
                dataBuilder.add("newZuordnung" + i, responseBuilder);
                continue;
            }

            List<String> messstellen =
                authorization.getInfo(request).getMessstellen();

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
                responseBuilder.add("success", true);
                responseBuilder.add("status", StatusCodes.OK);
                responseBuilder.add("message",
                    "Tag is already assigned to probe");
                responseBuilder.add("data", "");
                dataBuilder.add(
                    zuordnung.getTagId().toString(), responseBuilder);
                continue;
            }

            Tag tag = repository.getByIdPlain(Tag.class, tagId);
            if (tag == null) {
                responseBuilder.add("success", false);
                responseBuilder.add("status", StatusCodes.ERROR_VALIDATION);
                responseBuilder.add("message", "Tag not found");
                responseBuilder.add("data", "");
                dataBuilder.add(
                    zuordnung.getTagId().toString(), responseBuilder);
                continue;
            }
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
                    responseBuilder.add("success", true);
                    responseBuilder.add("status", StatusCodes.NOT_ALLOWED);
                    responseBuilder.add("message", "Unathorized");
                    responseBuilder.add("data", zuordnung.toJson());
                    dataBuilder.add("newZuordnung" + i, responseBuilder);
                    continue;
                }
            } else if (!messstellen.contains(mstId)) {
                //Else check if it is the users private tag
                responseBuilder.add("success", true);
                responseBuilder.add("status", StatusCodes.ERROR_VALIDATION);
                responseBuilder.add("message", "Invalid mstId");
                responseBuilder.add("data", zuordnung.toJson());
                dataBuilder.add("newZuordnung" + i, responseBuilder);
                continue;
            }

            zuordnung.setTag(tag);

            Response createResponse = repository.create(zuordnung);

            //Extend tag expiring time
            Date date = new Date();
            Timestamp now = new Timestamp(date.getTime());
            tag.setGueltigBis(TagService.getGueltigBis(tag, now));

            TagZuordnung newZuordnung = (TagZuordnung) createResponse.getData();
            responseBuilder.add("success", createResponse.getSuccess());
            responseBuilder.add("status", StatusCodes.OK);
            responseBuilder.add("data", newZuordnung.toJson());
            dataBuilder.add(
                newZuordnung.getId().toString(), responseBuilder);
        }

        builder.add("success", true);
        builder.add("data", dataBuilder);
        return javax.ws.rs.core.Response.ok(builder.build().toString()).build();
    }

    /**
     * Delete a reference between a tag and a probe.
     * @return Response object
     */
    @DELETE
    @Path("/{id}")
    public Response deleteTagReference(
        @Context HttpServletRequest request,
        @PathParam("id") Integer id
    ) {

        TagZuordnung tagZuordnung
            = repository.getByIdPlain(TagZuordnung.class, id);
        if (tagZuordnung == null) {
            return new Response(
                    false,
                    StatusCodes.NOT_EXISTING,
                    "Tagzuordnung not found");
        }
        boolean global = false;
        //Check if its a global tag
        Tag tag = tagZuordnung.getTag();
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
