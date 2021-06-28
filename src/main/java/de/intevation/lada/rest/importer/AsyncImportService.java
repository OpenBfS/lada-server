/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest.importer;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import de.intevation.lada.importer.ImportConfig;
import de.intevation.lada.importer.ImportFormat;
import de.intevation.lada.importer.ImportJobManager;
import de.intevation.lada.importer.Importer;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.annotation.RepositoryConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.auth.UserInfo;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.RepositoryType;
import de.intevation.lada.util.data.JobManager.JobNotFoundException;
import de.intevation.lada.util.data.JobManager.JobStatus;
import de.intevation.lada.util.data.StatusCodes;

/**
 * This class produces a RESTful service to interact with probe objects.
 *
 * @author <a href = "mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("data/import/async")
@RequestScoped
public class AsyncImportService {

    /**
     * The importer
     */
    @Inject
    @ImportConfig(format = ImportFormat.LAF)
    private Importer importer;

    @Inject
    @RepositoryConfig(type = RepositoryType.RW)
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
    @Path("/laf")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createAsyncImport(
        JsonObject jsonInput,
        @Context HttpServletRequest request
    ) {
        String mstId = request.getHeader("X-LADA-MST");
        if (mstId == null) {
            JsonObjectBuilder builder = Json.createObjectBuilder();
            builder.add("success", false)
            .add("status", StatusCodes.NOT_ALLOWED)
            .add("data", "Missing header for messtelle.");
            return Response.ok(builder.build().toString()).build();
        }
        UserInfo userInfo = authorization.getInfo(request);
        String newJobId = importJobManager.createImportJob(userInfo, jsonInput, mstId);
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
     *  }
     * </pre>
     *
     * @param id Job id to check
     * @return Json object containing the status information, status
     *         403 if the requesting user has not created the request
     *         or status 404 if job was not found
     */
    @GET
    @Path("/status/{id}")
    @Produces("application/json")
    public Response getStatus(
        @PathParam("id") String id,
        @Context HttpServletRequest request) {

        JobStatus status;
        UserInfo originalCreator;
        UserInfo requestingUser = authorization.getInfo(request);
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
        JsonObject responseJson = Json.createObjectBuilder()
            .add("status", status.getStatus())
            .add("message", status.getMessage())
            .add("done", status.isDone())
            .build();

        return Response.ok(responseJson.toString()).build();
    }

    @GET
    @Path("/result/{id}")
    @Produces("application/json")
    public Response getResult(
        @PathParam("id") String id,
        @Context HttpServletRequest request) {

        UserInfo originalCreator;
        UserInfo requestingUser = authorization.getInfo(request);

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
