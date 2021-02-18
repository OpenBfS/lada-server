/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJBTransactionRolledbackException;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.persistence.TransactionRequiredException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import de.intevation.lada.factory.ProbeFactory;
import de.intevation.lada.lock.LockConfig;
import de.intevation.lada.lock.LockType;
import de.intevation.lada.lock.ObjectLocker;
import de.intevation.lada.model.land.Messprogramm;
import de.intevation.lada.model.land.Probe;
import de.intevation.lada.model.stammdaten.Tag;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.annotation.RepositoryConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.RepositoryType;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.data.Strings;
import de.intevation.lada.util.data.TagUtil;
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.validation.Validator;
import de.intevation.lada.validation.Violation;
import de.intevation.lada.validation.annotation.ValidationConfig;

/**
 * REST service for Probe objects.
 * <p>
 * The services produce data in the application/json media type.
 * All HTTP methods use the authorization module to determine if the user is
 * allowed to perform the requested action.
 * A typical response holds information about the action performed and the data.
 * <pre>
 * <code>
 * {
 *  "success": [boolean];
 *  "message": [string],
 *  "data":[{
 *      "id":[number],
 *      "baId": [string],
 *      "datenbasisId": [number],
 *      "letzteAenderung": [timestamp],
 *      "media": [string],
 *      "mediaDesk": [string],
 *      "mittelungsdauer": [number],
 *      "mstId": [string],
 *      "netzbetreiberId":[string],
 *      "probeentnahmeBeginn": [timestamp],
 *      "probeentnahmeEnde": [timestamp],
 *      "probenartId": [number],
 *      "test": [boolean],
 *      "umwId": [string],
 *      "hauptprobenNr": [string],
 *      "erzeugerId": [string],
 *      "mpKat": [string],
 *      "mplId": [number],
 *      "mprId": [number],
 *      "probeNehmerId": [number],
 *      "solldatumBeginn": [timestamp],
 *      "solldatumEnde": [timestamp],
 *      "treeModified": [timestamp],
 *      "readonly": [boolean],
 *      "owner": [boolean],
 *      "externeProbeId": [string]
 *  }],
 *  "errors": [object],
 *  "warnings": [object],
 *  "readonly": [boolean],
 *  "totalCount": [number]
 * }
 * </code>
 * </pre>
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("rest/probe")
@RequestScoped
public class ProbeService {

    /**
     * The data repository granting read/write access.
     */
    @Inject
    @RepositoryConfig(type = RepositoryType.RW)
    private Repository repository;

    /**
     * The authorization module.
     */
    @Inject
    @AuthorizationConfig(type = AuthorizationType.HEADER)
    private Authorization authorization;

    /**
     * The object lock mechanism.
     */
    @Inject
    @LockConfig(type = LockType.TIMESTAMP)
    private ObjectLocker lock;

    /**
     * The validator used for Probe objects.
     */
    @Inject
    @ValidationConfig(type = "Probe")
    private Validator validator;

    /**
     * The factory to create Probe objects.
     * Used for messprogramm.
     */
    @Inject
    private ProbeFactory factory;

    /**
     * Get all Probe objects.
     * <p>
     * The requested objects can be filtered using the following URL
     * parameters:<br>
     *  * page: The page to display in a paginated result grid.<br>
     *  * start: The first Probe item.<br>
     *  * limit: The count of Probe items.<br>
     *  <br>
     *  The response data contains a stripped set of Probe objects.
     *  The returned fields are defined in the query used in the request.
     * <p>
     * Example:
     * http://example.com/probe?page=[PAGE]&start=[START]&limit=[LIMIT]
     *
     * @return Response object containing all Probe objects.
     */
    @GET
    @Path("/")
    @Produces("application/json")
    public Response get(
        @Context HttpHeaders headers,
        @Context UriInfo info,
        @Context HttpServletRequest request
    ) {
        MultivaluedMap<String, String> params = info.getQueryParameters();
        List<Probe> probes = repository.getAllPlain(Probe.class, Strings.LAND);

        int size = probes.size();
        if (params.containsKey("start") && params.containsKey("limit")) {
            int start = Integer.valueOf(params.getFirst("start"));
            int limit = Integer.valueOf(params.getFirst("limit"));
            int end = limit + start;
            if (start + limit > size) {
                end = size;
            }
            probes = probes.subList(start, end);
        }

        for (Probe probe: probes) {
            Violation violation = validator.validate(probe);
            if (violation.hasWarnings()
                || violation.hasErrors()
                || violation.hasNotifications()
            ) {
                probe.setWarnings(violation.getWarnings());
                probe.setErrors(violation.getErrors());
                probe.setNotifications(violation.getNotifications());
            }
            probe.setReadonly(authorization.isReadOnly(probe.getId()));
        }
        return new Response(true, StatusCodes.OK, probes);
    }

    /**
     * Get a single Probe object by id.
     * <p>
     * The id is appended to the URL as a path parameter.
     * <p>
     * Example: http://example.com/probe/{id}
     *
     * @return Response object containing a single Probe.
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(
        @Context HttpHeaders headers,
        @PathParam("id") Integer id,
        @Context HttpServletRequest request
    ) {
        Response response =
            repository.getById(Probe.class, id, Strings.LAND);
        Violation violation = validator.validate(response.getData());
        if (violation.hasWarnings()) {
            response.setWarnings(violation.getWarnings());
        }
        if (violation.hasNotifications()) {
            response.setNotifications(violation.getNotifications());
        }
        return this.authorization.filter(request, response, Probe.class);
    }

    /**
     * Create a new Probe object.
     * <p>
     * The new object is embedded in the post data as JSON formatted string.
     * <p>
     * <pre>
     * <code>
     * {
     *  "externeProbeId": [string],
     *  "hauptprobenNr": [string],
     *  "test": [boolean],
     *  "netzbetreiberId": [string],
     *  "mstId": [string],
     *  "datenbasisId": [number],
     *  "baId": [string],
     *  "probenartId": [number],
     *  "mediaDesk": [string],
     *  "media": [string],
     *  "umwId": [string],
     *  "mittelungsdauer": [number],
     *  "erzeugerId":[string],
     *  "probeNehmerId": [number],
     *  "mpKat": [string],
     *  "mplId": [number],
     *  "mprId": [number],
     *  "treeModified":null,
     *  "probeentnahmeBeginn": [date],
     *  "probeentnahmeEnde": [date],
     *  "letzteAenderung": [date],
     *  "solldatumBeginn": [date],
     *  "solldatumEnde": [date]
     * }
     * </code>
     * </pre>
     *
     * @return Response object containing the new probe object.
     */
    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(
        @Context HttpHeaders headers,
        @Context HttpServletRequest request,
        Probe probe
    ) {
        if (!authorization.isAuthorized(
                request,
                probe,
                RequestMethod.POST,
                Probe.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }
        Violation violation = validator.validate(probe);
        if (violation.hasErrors()) {
            Response response =
                new Response(false, StatusCodes.ERROR_VALIDATION, probe);
            response.setErrors(violation.getErrors());
            response.setWarnings(violation.getWarnings());
            response.setNotifications(violation.getNotifications());
            return response;
        }
        if (probe.getUmwId() == null || probe.getUmwId().equals("")) {
            probe = factory.findUmweltId(probe);
        }
        probe = factory.findMediaDesk(probe);

        /* Persist the new probe object*/
        Response newProbe = repository.create(probe, Strings.LAND);

        if (violation.hasWarnings()) {
            newProbe.setWarnings(violation.getWarnings());
        }
        if (violation.hasNotifications()) {
            newProbe.setNotifications(violation.getNotifications());
        }

        return authorization.filter(
            request,
            newProbe,
            Probe.class);
    }

    /**
     * Create new Probe objects from a messprogramm.
     * <p>
     * <p>
     * <pre>
     * <code>
     * {
     *  "ids": [[number]],
     *  "start": [timestamp],
     *  "end": [timestamp]
     * }
     * </code>
     * </pre>
     *
     * @return Response object containing the new probe objects.
     */
    @POST
    @Path("/messprogramm")
    @Produces(MediaType.APPLICATION_JSON)
    public Response createFromMessprogramm(
        @Context HttpHeaders headers,
        @Context HttpServletRequest request,
        JsonObject object
    ) {

        JsonArray ids = object.getJsonArray("ids");
        if (ids == null) {
            return new Response(false, StatusCodes.NOT_EXISTING, null);
        }

        // This is due to the requiremment that the dryrun variable has to be
        // effectively final.
        boolean dryrun;
        if (object.containsKey("dryrun")) {
            dryrun = object.getBoolean("dryrun");
        }
        else {
            dryrun = false;
        }

        Map<String, Object> responseData = new HashMap<String, Object>();
        Map<String, Object> probenData = new HashMap<String, Object>();
        List<Integer> generatedProbeIds = new ArrayList<Integer>();

        ids.forEach(mpId -> {
            int id = Integer.parseInt(mpId.toString());
            HashMap<String, Object> data = new HashMap<String, Object>();
            Messprogramm messprogramm = repository.getByIdPlain(
                Messprogramm.class, id, Strings.LAND);
            if (messprogramm == null) {
                data.put("success", false);
                data.put("message", StatusCodes.NOT_EXISTING);
                data.put("data", "Invalid mst id");
                probenData.put("" + id, data);
                return;
            }

            if (!dryrun) {
                // Use a dummy probe with same mstId as the messprogramm to
                // authorize the user to create probe objects.
                Probe testProbe = new Probe();
                testProbe.setMstId(messprogramm.getMstId());
                if (!authorization.isAuthorized(
                        request,
                        testProbe,
                        RequestMethod.POST,
                        Probe.class)
                ) {
                    data.put("success", false);
                    data.put("message", StatusCodes.NOT_ALLOWED);
                    data.put("data", null);
                    probenData.put(messprogramm.getId().toString(), data);
                    return;
                }
            }

            long start = 0;
            long end = 0;
            try {
                start = object.getJsonNumber("start").longValue();
                end = object.getJsonNumber("end").longValue();
            } catch (ClassCastException e) {
                // Catch invalid (i.e. too high) time values
                data.put("success", false);
                data.put("message", StatusCodes.VALUE_OUTSIDE_RANGE);
                data.put("data", null);
                probenData.put(messprogramm.getId().toString(), data);
                return;
            }
            if (start > end) {
                data.put("success", false);
                data.put("message", StatusCodes.DATE_BEGIN_AFTER_END);
                data.put("data", null);
                probenData.put(messprogramm.getId().toString(), data);
                return;
            }
            List<Probe> proben = factory.create(
                messprogramm,
                start,
                end,
                dryrun);

            for (Probe probe : proben) {
                if (!probe.isFound()) {
                    generatedProbeIds.add(probe.getId());
                }
            }
            List<Map<String, Object>> returnValue = factory.getProtocol();
            data.put("success", true);
            data.put("message", StatusCodes.OK);
            data.put("data", returnValue);
            probenData.put(messprogramm.getId().toString(), data);
        });
        responseData.put("proben", probenData);

        // Generate and associate tag
        if (!dryrun && generatedProbeIds.size() > 0) {
            // Assume the user is associated to at least one Messstelle,
            // because authorization should ensure this.
            // TODO: Pick the correct instead of the first Messstelle
            String mstId = authorization.getInfo(request)
                .getMessstellen().get(0);
            Response tagCreation =
                TagUtil.generateTag("PEP", mstId, repository);
            if (tagCreation.getSuccess()) {
                Tag newTag = (Tag) tagCreation.getData();
                TagUtil.setTagsByProbeIds(
                    generatedProbeIds, newTag.getId(), repository);
                responseData.put("tag", newTag.getTag());
            } else {
                /* TODO: The whole request should be handled in one
                 * transaction that should be rolled back at this point. */
                responseData.put("tag", "XXX Creation of tag failed XXX");
            }
        }
        return new Response(true, StatusCodes.OK, responseData);
    }

    /**
     * Update an existing Probe object.
     * <p>
     * The object to update should come as JSON formatted string.
     * <pre>
     * <code>
     * {
     *  "id": [number],
     *  "externeProbeId": [string],
     *  "hauptprobenNr": [string],
     *  "test": [boolean],
     *  "netzbetreiberId": [string],
     *  "mstId": [string],
     *  "datenbasisId": [number],
     *  "baId": [string],
     *  "probenartId": [number],
     *  "mediaDesk": [string],
     *  "media": [string],
     *  "umwId": [string],
     *  "mittelungsdauer": [number],
     *  "erzeugerId": [number],
     *  "probeNehmerId": [number],
     *  "mpKat": [string],
     *  "mplId": [number],
     *  "mprId": [number],
     *  "treeModified": [timestamp],
     *  "probeentnahmeBeginn": [date],
     *  "probeentnahmeEnde": [date],
     *  "letzteAenderung": [date],
     *  "solldatumBeginn": [date],
     *  "solldatumEnde":[date]
     * }
     * </code>
     * </pre>
     *
     * @return Response object containing the updated Probe object.
     */
    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(
        @Context HttpHeaders headers,
        @Context HttpServletRequest request,
        @PathParam("id") String id,
        Probe probe
    ) {
        if (!authorization.isAuthorized(
                request,
                probe,
                RequestMethod.PUT,
                Probe.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }
        if (lock.isLocked(probe)) {
            return new Response(false, StatusCodes.CHANGED_VALUE, null);
        }
        if (probe.getMediaDesk() == null || probe.getMediaDesk().isEmpty()) {
            probe = factory.findMediaDesk(probe);
        }
        if (probe.getUmwId() == null || probe.getUmwId().isEmpty()) {
            factory.findUmweltId(probe);
        }
        Violation violation = validator.validate(probe);
        if (violation.hasErrors()) {
            Response response =
                new Response(false, StatusCodes.ERROR_VALIDATION, null);
            response.setErrors(violation.getErrors());
            response.setWarnings(violation.getWarnings());
            response.setNotifications(violation.getNotifications());
            return response;
        }
        Response response = repository.update(probe, Strings.LAND);
        if (!response.getSuccess()) {
            return response;
        }
        Response updated = repository.getById(
            Probe.class,
            ((Probe) response.getData()).getId(), Strings.LAND);
        if (violation.hasWarnings()) {
            updated.setWarnings(violation.getWarnings());
        }
        if (violation.hasNotifications()) {
           updated.setNotifications(violation.getNotifications());
        }
        return authorization.filter(
            request,
            updated,
            Probe.class);
    }

    /**
     * Delete an existing Probe object by id.
     * <p>
     * The id is appended to the URL as a path parameter.
     * <p>
     * Example: http://example.com/probe/{id}
     *
     * @return Response object.
     */
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(
        @Context HttpHeaders headers,
        @Context HttpServletRequest request,
        @PathParam("id") String id
    ) {
        /* Get the probe object by id*/
        Response probe =
            repository.getById(Probe.class, Integer.valueOf(id), Strings.LAND);
        if (!probe.getSuccess()) {
            return probe;
        }
        Probe probeObj = (Probe) probe.getData();
        if (!authorization.isAuthorized(
                request,
                probeObj,
                RequestMethod.DELETE,
                Probe.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }
        /* Delete the probe object*/
        try {
            Response response = repository.delete(probeObj, Strings.LAND);
            return response;
        } catch (IllegalArgumentException
            | EJBTransactionRolledbackException
            | TransactionRequiredException e
        ) {
            return new Response(false, StatusCodes.NOT_EXISTING, "");
        }
    }
}
