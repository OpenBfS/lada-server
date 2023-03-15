/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import de.intevation.lada.factory.ProbeFactory;
import de.intevation.lada.lock.LockConfig;
import de.intevation.lada.lock.LockType;
import de.intevation.lada.lock.ObjectLocker;
import de.intevation.lada.model.land.Messprogramm;
import de.intevation.lada.model.land.Probe;
import de.intevation.lada.model.stammdaten.Tag;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
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
public class ProbeService extends LadaService {

    /**
     * The data repository granting read/write access.
     */
    @Inject
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

    @Inject
    private TagUtil tagUtil;

    /**
     * Expected format for payload in POST request to createFromMessprogramm().
     */
    public static class PostData {
        private List<Integer> ids;
        private boolean dryrun;
        private Calendar start;
        private Calendar end;

        public void setIds(List<Integer> ids) {
            this.ids = ids;
        }

        public void setDryrun(boolean dryrun) {
            this.dryrun = dryrun;
        }

        public void setStart(Calendar start) {
            this.start = start;
        }

        public void setEnd(Calendar end) {
            this.end = end;
        }
    }

    /**
     * Get a single Probe object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object containing a single Probe.
     */
    @GET
    @Path("/{id}")
    public Response getById(
        @PathParam("id") Integer id
    ) {
        Response response = repository.getById(Probe.class, id);
        Violation violation = validator.validate(response.getData());
        if (violation.hasWarnings()) {
            response.setWarnings(violation.getWarnings());
        }
        if (violation.hasNotifications()) {
            response.setNotifications(violation.getNotifications());
        }
        return this.authorization.filter(response, Probe.class);
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
    public Response create(
        @Valid Probe probe
    ) {
        if (!authorization.isAuthorized(
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
        } else {
            if (probe.getMediaDesk() == null ||
                probe.getMediaDesk().isEmpty() ||
                probe.getMediaDesk().equals("D: 00 00 00 00 00 00 00 00 00 00 00 00")) {
                probe = factory.getInitialMediaDesk(probe);
            }
        }
        probe = factory.findMedia(probe);

        /* Persist the new probe object*/
        Response newProbe = repository.create(probe);

        if (violation.hasWarnings()) {
            newProbe.setWarnings(violation.getWarnings());
        }
        if (violation.hasNotifications()) {
            newProbe.setNotifications(violation.getNotifications());
        }

        return authorization.filter(
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
     *  "dryrun": [boolean],
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
    public Response createFromMessprogramm(
        PostData object
    ) {
        if (object.ids == null) {
            return new Response(false, StatusCodes.NOT_EXISTING, null);
        }

        Map<String, Object> responseData = new HashMap<String, Object>();
        Map<String, Object> probenData = new HashMap<String, Object>();
        List<Integer> generatedProbeIds = new ArrayList<Integer>();

        object.ids.forEach(id -> {
            HashMap<String, Object> data = new HashMap<String, Object>();
            Messprogramm messprogramm = repository.getByIdPlain(
                Messprogramm.class, id);
            if (messprogramm == null) {
                data.put("success", false);
                data.put("message", StatusCodes.NOT_EXISTING);
                data.put("data", "Invalid mst id");
                probenData.put("" + id, data);
                return;
            }

            if (!object.dryrun) {
                // Use a dummy probe with same mstId as the messprogramm to
                // authorize the user to create probe objects.
                Probe testProbe = new Probe();
                testProbe.setMstId(messprogramm.getMstId());
                if (!authorization.isAuthorized(
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

            if (object.start.after(object.end)) {
                data.put("success", false);
                data.put("message", StatusCodes.DATE_BEGIN_AFTER_END);
                data.put("data", null);
                probenData.put(messprogramm.getId().toString(), data);
                return;
            }
            List<Probe> proben = factory.create(
                messprogramm,
                object.start,
                object.end,
                object.dryrun);

            for (Probe probe : proben) {
                if (!probe.isFound()) {
                    generatedProbeIds.add(probe.getId());
                }
            }
            data.put("success", true);
            data.put("message", StatusCodes.OK);
            data.put("data", proben);
            probenData.put(messprogramm.getId().toString(), data);
        });
        responseData.put("proben", probenData);

        // Generate and associate tag
        if (!object.dryrun && generatedProbeIds.size() > 0) {
            // Assume the user is associated to at least one Messstelle,
            // because authorization should ensure this.
            // TODO: Pick the correct instead of the first Netzbetreiber
            Response tagCreation = tagUtil.generateTag(
                "PEP", List.copyOf(authorization.getInfo().getNetzbetreiber())
                    .get(0));
            if (tagCreation.getSuccess()) {
                Tag newTag = (Tag) tagCreation.getData();
                tagUtil.setTagsByProbeIds(generatedProbeIds, newTag.getId());
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
    public Response update(
        @PathParam("id") Integer id,
        @Valid Probe probe
    ) {
        if (!authorization.isAuthorized(
                probe,
                RequestMethod.PUT,
                Probe.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }
        if (lock.isLocked(probe)) {
            return new Response(false, StatusCodes.CHANGED_VALUE, null);
        }
        if (probe.getUmwId() == null || probe.getUmwId().isEmpty()) {
            factory.findUmweltId(probe);
        } else {
            if (probe.getMediaDesk() == null ||
                probe.getMediaDesk().isEmpty() ||
                probe.getMediaDesk().equals("D: 00 00 00 00 00 00 00 00 00 00 00 00")) {
                factory.getInitialMediaDesk(probe);
            }
        }
        probe = factory.findMedia(probe);
        Violation violation = validator.validate(probe);
        if (violation.hasErrors()) {
            Response response =
                new Response(false, StatusCodes.ERROR_VALIDATION, null);
            response.setErrors(violation.getErrors());
            response.setWarnings(violation.getWarnings());
            response.setNotifications(violation.getNotifications());
            return response;
        }
        Response response = repository.update(probe);
        if (!response.getSuccess()) {
            return response;
        }
        if (violation.hasWarnings()) {
            response.setWarnings(violation.getWarnings());
        }
        if (violation.hasNotifications()) {
           response.setNotifications(violation.getNotifications());
        }
        return authorization.filter(
            response,
            Probe.class);
    }

    /**
     * Delete an existing Probe object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object.
     */
    @DELETE
    @Path("/{id}")
    public Response delete(
        @PathParam("id") Integer id
    ) {
        Response probe = repository.getById(Probe.class, id);
        if (!probe.getSuccess()) {
            return probe;
        }
        Probe probeObj = (Probe) probe.getData();
        if (!authorization.isAuthorized(
                probeObj,
                RequestMethod.DELETE,
                Probe.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }
        return repository.delete(probeObj);
    }
}
