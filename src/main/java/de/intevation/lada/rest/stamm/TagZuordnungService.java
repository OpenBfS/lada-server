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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

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
     * [{
     *   "probeId": [Integer],
     *   "tagId": [Integer]
     * }, {
     *    ...
     * }]
     * </code>
     * </pre>
     * Requests containing both, tag and tagId will be rejected.
     * Setting a mstId is mandatory, as only global tags have no mstId.
     */
    @POST
    @Path("/")
    public javax.ws.rs.core.Response createTagReference(
        @Context HttpServletRequest request,
        List<TagZuordnung> zuordnungs
    ) {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        ObjectMapper mapper = new ObjectMapper();

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
            Tag tag = zuordnung.getTag();
            Integer tagId = zuordnung.getTagId();
            if (tag != null && tagId != null
                || tag == null && tagId == null
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
                TagZuordnung exisitingZuordnung
                        = (TagZuordnung) isAssigned.getSingleResult();
                if ((Boolean) isAssigned.getSingleResult()) {
                    responseBuilder.add("success", true);
                    responseBuilder.add("status", StatusCodes.OK);
                    responseBuilder.add("message",
                        "Tag is already assigned to probe");
                    responseBuilder.add("data", "");
                    dataBuilder.add(
                        exisitingZuordnung.getId().toString(), responseBuilder);
                    continue;
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
                        responseBuilder.add("success", true);
                        responseBuilder.add("status", StatusCodes.NOT_ALLOWED);
                        responseBuilder.add("message",
                            "Unathorized");
                        responseBuilder.add("data", zuordnung.toJson());
                        dataBuilder.add("newZuordnung" + i, responseBuilder);
                        continue;
                    }
                //Else check if it is the users private tag
                } else if (!messstellen.contains(mstId)) {
                        responseBuilder.add("success", true);
                        responseBuilder.add("status", StatusCodes.ERROR_VALIDATION);
                        responseBuilder.add("message",
                            "Invalid mstId");
                        responseBuilder.add("data", zuordnung.toJson());
                        dataBuilder.add("newZuordnung" + i, responseBuilder);
                        continue;
                }

                zuordnung.setTag(tag);

            } else { // Create new tag
                // TODO: Why does this code exist? Creating tags should be done
                // via TagService.
                String mstId = zuordnung.getTag().getMstId();
                //mstId may not be null, global tags cannot be created
                if (mstId == null || !messstellen.contains(mstId)) {
                    responseBuilder.add("success", true);
                        responseBuilder.add("status", StatusCodes.NOT_ALLOWED);
                        responseBuilder.add("message",
                            "Invalid/empty mstId");
                        responseBuilder.add("data", zuordnung.toJson());
                        dataBuilder.add("newZuordnung" + i, responseBuilder);
                        continue;
                }
                if (!repository.create(tag).getSuccess()) {
                    //TODO Proper response code?
                    responseBuilder.add("success", true);
                        responseBuilder.add("status", StatusCodes.ERROR_DB_CONNECTION);
                        responseBuilder.add("message",
                            "Failed to create tag");
                        responseBuilder.add("data", zuordnung.toJson());
                        dataBuilder.add("newZuordnung" + i, responseBuilder);
                        continue;
                }
            }
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
        return javax.ws.rs.core.Response.ok(builder.toString()).build();
    }

    /**
     * Delete a reference between a tag and a probe.
     * @return Response object
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
<<<<<<< HEAD

    /**
     * Get gueltig bis timestamp for the given tag and timestamp.
     * @param tag Tag to get date for
     * @param ts Timestamp to use as base
     * @return Timestamp
     */
    // TODO: Duplicates a fair amount of code in TagService
    private Timestamp getGueltigBis(Tag tag, Timestamp ts) {
        Calendar now;
        Calendar tagExp;
        switch (tag.getTyp().getId()) {
            //Global tags do not expire
            case "global": return null;
            //Netzbetreiber tags do not expire
            case "netzbetreiber": return null;
            //Mst tags expire after 365 days
            case "mst":
                //Check if expiration date needs to be extended
                tagExp = Calendar.getInstance();
                tagExp.setTime(tag.getGueltigBis());
                now = Calendar.getInstance();
                now.add(Calendar.DAY_OF_YEAR, 365);
                if (tagExp.compareTo(now) > 0) {
                    return tag.getGueltigBis();
                }

                Calendar mstCal = Calendar.getInstance();
                mstCal.setTime(ts);
                mstCal.add(Calendar.DAY_OF_YEAR, 365);
                ts.setTime(mstCal.getTimeInMillis());
                return ts;
            //Auto tags expire after 548 days
            case "auto":
                //Check if expiration date needs to be extended
                tagExp = Calendar.getInstance();
                tagExp.setTime(tag.getGueltigBis());
                now = Calendar.getInstance();
                now.add(Calendar.DAY_OF_YEAR, 548);
                if (tagExp.compareTo(now) > 0) {
                    return tag.getGueltigBis();
                }
                Calendar cal = Calendar.getInstance();
                cal.setTime(ts);
                cal.add(Calendar.DAY_OF_YEAR, 548);
                ts.setTime(cal.getTimeInMillis());
                return ts;
            default: return null;
        }
    }
=======
>>>>>>> Remove redundant functions and classes in Tag and TagZuordnungService
}
