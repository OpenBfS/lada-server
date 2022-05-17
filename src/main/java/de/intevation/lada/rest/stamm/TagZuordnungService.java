/* Copyright (C) 2018 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest.stamm;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.Query;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import de.intevation.lada.model.land.TagZuordnung;
import de.intevation.lada.model.stammdaten.Tag;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
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

    private static final String EXISTS_QUERY_TEMPLATE =
        "SELECT EXISTS("
        + "SELECT 1 FROM land.tagzuordnung "
        + "JOIN stamm.tag ON tag_id=tag.id "
        + "WHERE tag_id=:%s"
        + " AND (mst_id IS NULL OR mst_id IN (:%s))"
        + " AND %s=:%s)";

    /**
     * Create new references between tags and Probe or Messung objects.
     *
     * @param zuordnungs A list of references like
     * <pre>
     * <code>
     * [{
     *   "probeId": [Integer],
     *   "tagId": [Integer]
     * }, {
     *   "messungId": [Integer],
     *   "tagId": [Integer]
     * }, {
     *    ...
     * }]
     * </code>
     * </pre>
     *
     * @return Response with list of Response objects for each reference.
     */
    @POST
    @Path("/")
    public Response createTagReference(
        List<TagZuordnung> zuordnungs
    ) {
        //Create Response
        List<Response> responseList = new ArrayList<>();

        for (TagZuordnung zuordnung: zuordnungs) {
            // Check if payload contains sensible information
            Integer tagId = zuordnung.getTagId();
            if (tagId == null
                || zuordnung.getProbeId() != null
                && zuordnung.getMessungId() != null
            ) {
                responseList.add(new Response(
                        false, StatusCodes.ERROR_VALIDATION, zuordnung));
                continue;
            }

            if (!authorization.isAuthorized(
                    zuordnung, RequestMethod.POST, TagZuordnung.class)
            ) {
                responseList.add(new Response(
                        false, StatusCodes.NOT_ALLOWED, zuordnung));
                continue;
            }

            if (isExisting(zuordnung)) {
                responseList.add(new Response(
                        true, StatusCodes.OK, zuordnung));
                continue;
            }

            //Extend tag expiring time
            Tag tag = repository.getByIdPlain(Tag.class, tagId);
            Date date = new Date();
            Timestamp now = new Timestamp(date.getTime());
            tag.setGueltigBis(TagService.getGueltigBis(tag, now));

            responseList.add(repository.create(zuordnung));
        }

        return new Response(true, StatusCodes.OK, responseList);
    }

    /**
     * Delete references between tags and Probe or Messung objects.
     *
     * @param zuordnungs A list of references like
     * <pre>
     * <code>
     * [{
     *   "probeId": [Integer],
     *   "tagId": [Integer]
     * }, {
     *   "messungId": [Integer],
     *   "tagId": [Integer]
     * }, {
     *    ...
     * }]
     * </code>
     * </pre>
     *
     * @return Response with list of Response objects for each reference.
     */
    @POST
    @Path("/delete")
    public Response deleteTagReference(
        List<TagZuordnung> zuordnungs
    ) {
        List<Response> responseList = new ArrayList<>();

        for (TagZuordnung zuordnung: zuordnungs) {
            if (!authorization.isAuthorized(
                    zuordnung,
                    RequestMethod.DELETE,
                    TagZuordnung.class)
            ) {
                responseList.add(new Response(
                        false,
                        StatusCodes.NOT_ALLOWED,
                        zuordnung));
                continue;
            }

            if (!isExisting(zuordnung)) {
                responseList.add(new Response(
                        true, StatusCodes.OK, zuordnung));
                continue;
            }

            // Delete existing entity
            responseList.add(
                repository.delete(
                    repository.getSinglePlain(
                        repository.queryBuilder(TagZuordnung.class)
                        .and("tagId", zuordnung.getTagId())
                        .and("probeId", zuordnung.getProbeId())
                        .and("messungId", zuordnung.getMessungId())
                        .getQuery())));
        }
        return new Response(true, StatusCodes.OK, responseList);
    }

    private Boolean isExisting(TagZuordnung zuordnung) {
        // Check if tag is already assigned
        final String tagIdParam = "tagId",
            mstIdsParam = "mstIds",
            taggedIdParam = "taggedId";
        String idField = zuordnung.getProbeId() != null
            ? "probe_id" : "messung_id";
        Query isAssigned = repository.queryFromString(
            String.format(EXISTS_QUERY_TEMPLATE,
                tagIdParam, mstIdsParam, idField, taggedIdParam));
        isAssigned.setParameter(tagIdParam, zuordnung.getTagId());
        isAssigned.setParameter(
            mstIdsParam, authorization.getInfo().getMessstellen());
        isAssigned.setParameter(taggedIdParam,
            zuordnung.getProbeId() != null
            ? zuordnung.getProbeId()
            : zuordnung.getMessungId());
        return (Boolean) isAssigned.getSingleResult();
    }
}
