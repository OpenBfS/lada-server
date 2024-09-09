/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.data;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.jboss.logging.Logger;

import de.intevation.lada.data.requests.CsvExportParameters;
import de.intevation.lada.data.requests.LafExportParameters;
import de.intevation.lada.data.requests.QueryExportParameters;
import de.intevation.lada.exporter.ExportJobManager;
import de.intevation.lada.i18n.I18n;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.auth.UserInfo;
import de.intevation.lada.util.data.Job.JobStatus;
import de.intevation.lada.util.data.JobManager.JobNotFoundException;
import de.intevation.lada.rest.LadaService;

/**
 * REST service to export data into files using a polling mechanism.
 *
 * Available actions are
 *
 * - Export probe objects with their child objects into .laf files.
 * - Export a query result into CSV files
 * - Export a query result into JSON files
 *
 * @author <a href="mailto:awoestmann@intevation.de">Alexander Woestmann</a>
 */
@Path(LadaService.PATH_DATA + "asyncexport")
public class AsyncExportService extends LadaService {

    @Inject
    private Logger logger;

    @Inject
    private ExportJobManager exportJobManager;

    /**
     * The authorization module.
     */
    @Inject
    @AuthorizationConfig(type = AuthorizationType.HEADER)
    private Authorization authorization;

    @Inject
    I18n i18n;

    /**
     * Export data into a CSV file.
     *
     * @param objects Export parameters object
     * @return Response containing the new export ref id
     * @throws BadRequestException if any constraint violations are detected
     */
    @POST
    @Path("csv")
    public Response createCsvExportJob(
        @Valid CsvExportParameters objects
    ) throws BadRequestException {
        UserInfo userInfo = authorization.getInfo();
        String newJobId =
            exportJobManager.createExportJob(
                "csv", objects.getEncoding(), objects,
                i18n.getResourceBundle(), userInfo);
        JsonObject responseJson = Json.createObjectBuilder()
            .add("refId", newJobId)
            .build();
        return Response.ok(responseJson.toString()).build();
    }

    /**
     * Export Sample objects into LAF files.
     *
     * @param objects    Export parameters object
     * @return The job identifier.
     * @throws BadRequestException if any constraint violations are detected
     */
    @POST
    @Path("laf")
    public Response createLafExportJob(
        @Valid LafExportParameters objects
    ) throws BadRequestException {
        UserInfo userInfo = authorization.getInfo();
        String newJobId =
            exportJobManager.createExportJob(
                "laf", objects.getEncoding(), objects, i18n.getResourceBundle(), userInfo);
        JsonObject responseJson = Json.createObjectBuilder()
            .add("refId", newJobId)
            .build();
        return Response.ok(responseJson.toString()).build();
    }

    /**
     * Export data into a JSON file.
     *
     * @param objects Export parameters object
     * @return Response containing the new export ref id
     * @throws BadRequestException if any constraint violations are detected
     */
    @POST
    @Path("json")
    public Response createJsonExportJob(
        @Valid QueryExportParameters objects
    ) throws BadRequestException {
        UserInfo userInfo = authorization.getInfo();
        String newJobId =
            exportJobManager.createExportJob(
                "json",
                StandardCharsets.UTF_8,
                objects,
                i18n.getResourceBundle(),
                userInfo);
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
    @Path("status/{id}")
    public Response getStatus(
        @PathParam("id") String id
    ) {
        JobStatus status;
        UserInfo originalCreator;
        UserInfo requestingUser = authorization.getInfo();

        try {
            originalCreator = exportJobManager.getJobUserInfo(id);
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

            status = exportJobManager.getJobStatus(id);
        } catch (JobNotFoundException jnfe) {
            logger.info(String.format("Could not find status for job %s", id));
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(status, MediaType.APPLICATION_JSON).build();
    }

    /**
     * Download a finished export file.
     * @param id Job id to download file from
     * @return Export file, status 403 if the requesting user has not created
     *         the request or status 404 if job was not found
     */
    @GET
    @Path("download/{id}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response download(
        @PathParam("id") String id
    ) {
        ByteArrayInputStream resultStream;
        String filename;
        UserInfo originalCreator;
        UserInfo requestingUser = authorization.getInfo();

        try {
            originalCreator = exportJobManager.getJobUserInfo(id);
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

            filename = exportJobManager.getJobDownloadFilename(id);
            resultStream = exportJobManager.getResultFileAsStream(id);

        } catch (JobNotFoundException jfe) {
            logger.info(String.format(
                "Returning 404 for download: Could not find job %s", id));
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (FileNotFoundException fnfe) {
            logger.error(String.format(
                "Error on reading result file for job %s", id));
            return Response.status(
                Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        return Response.ok(resultStream)
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + filename + "\"")
            .build();
    }
}
