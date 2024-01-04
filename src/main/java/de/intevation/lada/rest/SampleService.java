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

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import de.intevation.lada.factory.ProbeFactory;
import de.intevation.lada.lock.LockConfig;
import de.intevation.lada.lock.LockType;
import de.intevation.lada.lock.ObjectLocker;
import de.intevation.lada.model.lada.Mpg;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.master.Tag;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.data.TagUtil;
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.validation.Validator;

/**
 * REST service for Sample objects.
 * <p>
 * The services produce data in the application/json media type.
 * All HTTP methods use the authorization module to determine if the user is
 * allowed to perform the requested action.
 * A typical response holds information about the action performed and the data.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("sample")
public class SampleService extends LadaService {

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
     * The validator used for Sample objects.
     */
    @Inject
    private Validator<Sample> validator;

    /**
     * The factory to create Sample objects.
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
     * Get a single Sample object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object containing a single Sample.
     */
    @GET
    @Path("{id}")
    public Response getById(
        @PathParam("id") Integer id
    ) {
        Response response = repository.getById(Sample.class, id);
        validator.validate(response.getData());
        return this.authorization.filter(response, Sample.class);
    }

    /**
     * Create a new Sample object.
     * <p>
     * The new object is embedded in the post data as JSON formatted string.
     * <p>
     *
     * @return Response object containing the new probe object.
     */
    @POST
    public Response create(
        @Valid Sample probe
    ) {
        if (!authorization.isAuthorized(
                probe,
                RequestMethod.POST,
                Sample.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }
        validator.validate(probe);
        if (probe.hasErrors()) {
            return new Response(false, StatusCodes.ERROR_VALIDATION, probe);
        }
        if (probe.getEnvMediumId() == null
            || "".equals(probe.getEnvMediumId())
        ) {
            probe = factory.findUmweltId(probe);
        } else {
            if (probe.getEnvDescripDisplay() == null
                || probe.getEnvDescripDisplay().isEmpty()
                || "D: 00 00 00 00 00 00 00 00 00 00 00 00".equals(
                    probe.getEnvDescripDisplay())
            ) {
                probe = factory.getInitialMediaDesk(probe);
            }
        }
        probe = factory.findMedia(probe);

        return authorization.filter(
            repository.create(probe),
            Sample.class);
    }

    /**
     * Create new Sample objects from a messprogramm.
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
    @Path("messprogramm")
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
            Mpg messprogramm = repository.getByIdPlain(
                Mpg.class, id);
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
                Sample testProbe = new Sample();
                testProbe.setMeasFacilId(messprogramm.getMeasFacilId());
                if (!authorization.isAuthorized(
                        testProbe,
                        RequestMethod.POST,
                        Sample.class)
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
            List<Sample> proben = factory.create(
                messprogramm,
                object.start,
                object.end,
                object.dryrun);

            for (Sample probe : proben) {
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
                responseData.put("tag", newTag.getName());
            } else {
                /* TODO: The whole request should be handled in one
                 * transaction that should be rolled back at this point. */
                responseData.put("tag", "XXX Creation of tag failed XXX");
            }
        }
        return new Response(true, StatusCodes.OK, responseData);
    }

    /**
     * Update an existing Sample object.
     * <p>
     * The object to update should come as JSON formatted string.
     *
     * @return Response object containing the updated Sample object.
     */
    @PUT
    @Path("{id}")
    public Response update(
        @PathParam("id") Integer id,
        @Valid Sample probe
    ) {
        if (!authorization.isAuthorized(
                probe,
                RequestMethod.PUT,
                Sample.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }
        if (lock.isLocked(probe)) {
            return new Response(false, StatusCodes.CHANGED_VALUE, null);
        }
        if (probe.getEnvMediumId() == null
            || probe.getEnvMediumId().isEmpty()
        ) {
            factory.findUmweltId(probe);
        } else {
            if (probe.getEnvDescripDisplay() == null
                || probe.getEnvDescripDisplay().isEmpty()
                || "D: 00 00 00 00 00 00 00 00 00 00 00 00".equals(
                    probe.getEnvDescripDisplay())
            ) {
                factory.getInitialMediaDesk(probe);
            }
        }
        probe = factory.findMedia(probe);
        validator.validate(probe);
        if (probe.hasErrors()) {
            return new Response(false, StatusCodes.ERROR_VALIDATION, null);
        }
        Response response = repository.update(probe);
        validator.validate(response.getData());
        return authorization.filter(
            response,
            Sample.class);
    }

    /**
     * Delete an existing Sample object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object.
     */
    @DELETE
    @Path("{id}")
    public Response delete(
        @PathParam("id") Integer id
    ) {
        Response probe = repository.getById(Sample.class, id);
        if (!probe.getSuccess()) {
            return probe;
        }
        Sample probeObj = (Sample) probe.getData();
        if (!authorization.isAuthorized(
                probeObj,
                RequestMethod.DELETE,
                Sample.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }
        return repository.delete(probeObj);
    }
}
