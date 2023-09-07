/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.data;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.logging.Logger;

import de.intevation.lada.importer.ImportJobManager;
import de.intevation.lada.importer.laf.LafImporter;
import de.intevation.lada.model.master.MeasFacil;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.auth.UserInfo;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.JobManager.JobNotFoundException;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.data.Job.JobStatus;
import de.intevation.lada.rest.LadaService;


/**
 * This class produces a RESTful service to interact with probe objects.
 *
 * @author <a href = "mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("import/async")
public class AsyncImportService extends LadaService {

    /**
     * The importer.
     */
    @Inject
    private LafImporter importer;

    @Inject
    private Repository repository;

    /**
     * The authorization module.
     */
    @Inject
    @AuthorizationConfig(type = AuthorizationType.HEADER)
    private Authorization authorization;

    @Inject
    private Logger logger;

    @Inject
    ImportJobManager importJobManager;

    @POST
    @Path("laf")
    public Response createAsyncImport(
        JsonObject jsonInput,
        @Context HttpServletRequest request
    ) {
        JsonObjectBuilder errBuilder = Json.createObjectBuilder()
            .add("success", false)
            .add("status", StatusCodes.NOT_ALLOWED);

        String mstId = request.getHeader("X-LADA-MST");
        if (mstId == null) {
            errBuilder.add("data", "Missing header for messtelle.");
            return Response.ok(errBuilder.build().toString()).build();
        }
        MeasFacil mst = repository.getByIdPlain(MeasFacil.class, mstId);
        if (mst == null) {
            errBuilder.add("data", "Wrong header for messtelle.");
            return Response.ok(errBuilder.build().toString()).build();
        }
        UserInfo userInfo = authorization.getInfo();
        String newJobId =
            importJobManager.createImportJob(userInfo, jsonInput, mst);
        JsonObject responseJson = Json.createObjectBuilder()
            .add("refId", newJobId)
            .build();
        return Response.ok(responseJson.toString()).build();
    }

    /**
     * Get the status of an export job.
     *
     * Output format:
     *
     * <pre>
     * {
     *    done: boolean
     *    status: 'waiting' | 'running' | 'finished' | 'error'
     *    message: string (optional)
     *    errors: boolean
     *    warnings: boolean
     *    notifications: boolean
     *  }
     * </pre>
     *
     *  Note: The 'error' status indicates errors in the server
     *        like I/O errors etc.
     *        'errors' and 'warnings' indicate errors in the import itself,
     *        like authorization issues etc.
     *
     * @param id Job id to check
     * @return Json object containing the status information, status
     *         403 if the requesting user has not created the request
     *         or status 404 if job was not found
     */
    @GET
    @Path("status/{id}")
    public Response getStatus(
        @PathParam("id") String id
    ) {
        JobStatus status;
        UserInfo originalCreator;
        UserInfo requestingUser = authorization.getInfo();
        try {
            originalCreator = importJobManager.getJobUserInfo(id);
            if (!originalCreator.getUserId().equals(
                    requestingUser.getUserId())
            ) {
                logger.warn(String.format(
                    "Rejected status request by user "
                    + "#%s for job %s created by user #%s",
                    requestingUser.getUserId(),
                    id,
                    originalCreator.getUserId()));
                return Response.status(Response.Status.FORBIDDEN).build();
            }

            status = importJobManager.getJobStatus(id);
        } catch (JobNotFoundException jnfe) {
            logger.info(String.format("Could not find status for job %s", id));
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(status, MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Path("result/{id}")
    public Response getResult(
        @PathParam("id") String id
    ) {
        UserInfo originalCreator;
        UserInfo requestingUser = authorization.getInfo();

        try {
            originalCreator = importJobManager.getJobUserInfo(id);
            if (!originalCreator.getUserId().equals(
                    requestingUser.getUserId())
            ) {
                logger.warn(String.format(
                    "Rejected download request by user %s "
                    + "for job %s created by user %s",
                    requestingUser.getUserId(),
                    id,
                    originalCreator.getUserId()));
                    return Response.status(Response.Status.FORBIDDEN).build();
            }
            return Response.ok(importJobManager.getImportResult(id)).build();
        } catch (JobNotFoundException jfe) {
            logger.info(String.format(
                "Returning 404 for download: Could not find job %s", id));
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
