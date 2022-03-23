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
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;

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
    public Response createTagReference(
        @Context HttpServletRequest request,
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
                    request, zuordnung, RequestMethod.POST, TagZuordnung.class)
            ) {
                responseList.add(new Response(
                        false, StatusCodes.NOT_ALLOWED, zuordnung));
                continue;
            }

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
            isAssigned.setParameter(
                mstIdsParam, authorization.getInfo(request).getMessstellen());
            isAssigned.setParameter(taggedIdParam,
                zuordnung.getProbeId() != null
                ? zuordnung.getProbeId()
                : zuordnung.getMessungId());
            if ((Boolean) isAssigned.getSingleResult()) {
                responseList.add(new Response(
                        true, StatusCodes.OK, zuordnung));
                continue;
            }

            Tag tag = repository.getByIdPlain(Tag.class, tagId);
            zuordnung.setTag(tag);

            //Extend tag expiring time
            Date date = new Date();
            Timestamp now = new Timestamp(date.getTime());
            tag.setGueltigBis(TagService.getGueltigBis(tag, now));

            responseList.add(repository.create(zuordnung));
        }

        return new Response(true, StatusCodes.OK, responseList);
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
        if (!authorization.isAuthorized(
                request,
                tagZuordnung,
                RequestMethod.DELETE,
                TagZuordnung.class)
        ) {
            return new Response(
                false,
                StatusCodes.NOT_ALLOWED,
                tagZuordnung);
        }

        return repository.delete(tagZuordnung);
    }
}
