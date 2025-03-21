/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import org.eclipse.microprofile.openapi.annotations.Operation;

import de.intevation.lada.factory.ProbeFactory;
import de.intevation.lada.lock.TimestampLocker;
import de.intevation.lada.model.lada.Mpg;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.master.Tag;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.data.TagUtil;
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.validation.constraints.IsValidPrimaryKey;
import de.intevation.lada.validation.constraints.BeginBeforeEnd;


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
@Path(LadaService.PATH_REST + "sample")
public class SampleService extends LadaService {

    /**
     * The data repository granting read/write access.
     */
    @Inject
    private Repository repository;

    /**
     * The object lock mechanism.
     */
    @Inject
    private TimestampLocker<Sample> lock;

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
    @BeginBeforeEnd
    public static class PostData {
        @NotNull
        private List<@NotNull @IsValidPrimaryKey(
            clazz = Mpg.class) Integer> ids;

        private boolean dryrun;

        @NotNull
        private ZonedDateTime start;

        @NotNull
        private ZonedDateTime end;

        public void setIds(List<Integer> ids) {
            this.ids = ids;
        }

        public void setDryrun(boolean dryrun) {
            this.dryrun = dryrun;
        }

        public ZonedDateTime getStart() {
            return this.start;
        }
        public void setStart(ZonedDateTime start) {
            this.start = start;
        }

        public ZonedDateTime getEnd() {
            return this.end;
        }
        public void setEnd(ZonedDateTime end) {
            this.end = end;
        }
    }

    /**
     * Get a single Sample object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return a single Sample.
     */
    @GET
    @Path("{id}")
    public Sample getById(
        @PathParam("id") Integer id
    ) {
        return repository.getById(Sample.class, id);
    }

    /**
     * Create a new Sample object.
     * <p>
     * The new object is embedded in the post data as JSON formatted string.
     * <p>
     *
     * @return the new probe object.
     * @throws BadRequestException if any constraint violations are detected.
     */
    @POST
    public Sample create(
        @Valid Sample probe
    ) throws BadRequestException {
        setEnvAttrs(probe);

        return repository.create(probe);
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
     * @return the new probe objects.
     * @throws BadRequestException if any constraint violations are detected.
     */
    @POST
    @Path("messprogramm")
    @Operation(description =
        "Create samples from measuring programs given by IDs for given period")
    public Map<String, Object> createFromMessprogramm(
        @Valid PostData object
    ) throws BadRequestException {
        Map<String, Object> responseData = new HashMap<String, Object>();
        Map<String, Object> probenData = new HashMap<String, Object>();
        List<Integer> generatedProbeIds = new ArrayList<Integer>();

        object.ids.forEach(id -> {
            HashMap<String, Object> data = new HashMap<String, Object>();
            Mpg messprogramm = repository.getById(
                Mpg.class, id);

            if (!object.dryrun) {
                // Use a dummy probe with same mstId as the messprogramm to
                // authorize the user to create probe objects.
                Sample testProbe = new Sample();
                testProbe.setMeasFacilId(messprogramm.getMeasFacilId());
                if (
                    !authorization.isAuthorized(testProbe, RequestMethod.POST)
                ) {
                    data.put("success", false);
                    data.put("message", StatusCodes.NOT_ALLOWED);
                    data.put("data", null);
                    probenData.put(messprogramm.getId().toString(), data);
                    return;
                }
            }

            List<Sample> proben = factory.create(
                messprogramm,
                GregorianCalendar.from(object.getStart()),
                GregorianCalendar.from(object.getEnd()),
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
            Tag newTag = tagUtil.generateTag(
                "PEP", List.copyOf(authorization.getInfo().getNetzbetreiber())
                    .get(0));
            tagUtil.setTagsByProbeIds(generatedProbeIds, newTag.getId());
            responseData.put("tag", newTag.getName());
        }
        return responseData;
    }

    /**
     * Update an existing Sample object.
     * <p>
     * The object to update should come as JSON formatted string.
     *
     * @return the updated Sample object.
     * @throws BadRequestException if any constraint violations are detected.
     */
    @PUT
    @Path("{id}")
    public Sample update(
        @PathParam("id") Integer id,
        @Valid Sample probe
    ) throws BadRequestException {
        lock.isLocked(probe);

        setEnvAttrs(probe);

        return repository.update(probe);
    }

    /**
     * Delete an existing Sample object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     */
    @DELETE
    @Path("{id}")
    public void delete(
        @PathParam("id") Integer id
    ) {
        Sample probeObj = repository.getById(Sample.class, id);
        authorization.authorize(probeObj, RequestMethod.DELETE);
        repository.delete(probeObj);
    }

    private void setEnvAttrs(Sample probe) {
        if (probe.getEnvMediumId() == null) {
            probe.setEnvMediumId(
                factory.findEnvMediumId(probe.getEnvDescripDisplay()));
        } else if (probe.getEnvDescripDisplay() == null
            || "D: 00 00 00 00 00 00 00 00 00 00 00 00".equals(
                probe.getEnvDescripDisplay())
        ) {
            probe.setEnvDescripDisplay(
                factory.getInitialMediaDesk(probe.getEnvMediumId()));
        }
        factory.findMedia(probe);
    }
}
