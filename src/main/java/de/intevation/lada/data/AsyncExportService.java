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

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import de.intevation.lada.data.requests.CsvExportParameters;
import de.intevation.lada.data.requests.LafExportParameters;
import de.intevation.lada.data.requests.QueryExportParameters;
import de.intevation.lada.exporter.ExportJobManager;
import de.intevation.lada.i18n.I18n;
import de.intevation.lada.util.auth.UserInfo;
import de.intevation.lada.util.data.JobManager;
import de.intevation.lada.rest.AsyncLadaService;
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
public class AsyncExportService extends AsyncLadaService {

    @Inject
    private ExportJobManager exportJobManager;

    @Inject
    I18n i18n;

    /**
     * getJobManager is used to retrieve the class specific JobManager
     * @return JobManager
     */
    @Override
    protected JobManager getJobManager(){
        return exportJobManager;
    }

    @POST
    @Path("csv")
    @Operation(summary = "Create a CSV export job")
    public AsyncLadaService.AsyncJobResponse createCsvExportJob(
        @Valid CsvExportParameters objects
    ) throws BadRequestException {
        UserInfo userInfo = authorization.getInfo();
        String newJobId =
            exportJobManager.createExportJob(
                objects.getEncoding(), objects,
                i18n.getResourceBundle(), userInfo);
        return new AsyncLadaService.AsyncJobResponse(newJobId);
    }

    @POST
    @Path("laf")
    @Operation(summary = "Create a LAF export job")
    public AsyncLadaService.AsyncJobResponse createLafExportJob(
        @Valid LafExportParameters objects
    ) throws BadRequestException {
        UserInfo userInfo = authorization.getInfo();
        String newJobId =
            exportJobManager.createExportJob(
                objects.getEncoding(),
                objects,
                i18n.getResourceBundle(),
                userInfo);
        return new AsyncLadaService.AsyncJobResponse(newJobId);
    }

    @POST
    @Path("json")
    @Operation(summary = "Create a JSON export job")
    public AsyncLadaService.AsyncJobResponse createJsonExportJob(
        @Valid QueryExportParameters objects
    ) throws BadRequestException {
        UserInfo userInfo = authorization.getInfo();
        String newJobId =
            exportJobManager.createExportJob(
                StandardCharsets.UTF_8,
                objects,
                i18n.getResourceBundle(),
                userInfo);
        return new AsyncLadaService.AsyncJobResponse(newJobId);
    }

    @GET
    @Path("download/{id}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Operation(summary = "Download a finished export file")
    @APIResponse(description = "A file to download",
                 content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM,
                 schema = @Schema(implementation = String.class, format = "binary")
                )
    )
    @APIResponse(responseCode = "403", description = "Forbidden Download requested")
    @APIResponse(responseCode = "404", description = "Job not found")
    @APIResponse(responseCode = "500", description = "Error while reading the result")
    public Response download(
        @Parameter(description = "The id of the job ", required = true)
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
                this.logger.warn(String.format(
                    "Rejected download request by user %s "
                    + "for job %s created by user %s",
                    requestingUser.getUserId(),
                    id,
                    originalCreator.getUserId()));
                throw new ForbiddenException();
            }

            filename = exportJobManager.getJobDownloadFilename(id);
            resultStream = exportJobManager.getResultFileAsStream(id);

        } catch (JobManager.JobNotFoundException jfe) {
            this.logger.info(String.format(
                "Returning 404 for download: Could not find job %s", id));
            throw new NotFoundException();
        } catch (FileNotFoundException fnfe) {
            this.logger.error(String.format(
                "Error on reading result file for job %s", id));
            throw new InternalServerErrorException();
        }

        return Response.ok(resultStream)
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + filename + "\"")
            .build();
    }
}
