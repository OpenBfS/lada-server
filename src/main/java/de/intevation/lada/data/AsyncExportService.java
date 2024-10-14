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

import de.intevation.lada.data.requests.CsvExportParameters;
import de.intevation.lada.data.requests.LafExportParameters;
import de.intevation.lada.data.requests.QueryExportParameters;
import de.intevation.lada.exporter.ExportJobManager;
import de.intevation.lada.i18n.I18n;
import de.intevation.lada.util.auth.UserInfo;
import de.intevation.lada.util.data.JobManager;
import de.intevation.lada.rest.AsyncJobResponse;
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

    /**
     * Export data into a CSV file.
     *
     * @param objects Export parameters object
     * @return Response containing the new export ref id
     * @throws BadRequestException if any constraint violations are detected
     */
    @POST
    @Path("csv")
    public AsyncJobResponse createCsvExportJob(
        @Valid CsvExportParameters objects
    ) throws BadRequestException {
        UserInfo userInfo = authorization.getInfo();
        String newJobId =
            exportJobManager.createExportJob(
                objects.getEncoding(), objects,
                i18n.getResourceBundle(), userInfo);
        return new AsyncJobResponse(newJobId);
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
    public AsyncJobResponse createLafExportJob(
        @Valid LafExportParameters objects
    ) throws BadRequestException {
        UserInfo userInfo = authorization.getInfo();
        String newJobId =
            exportJobManager.createExportJob(
                objects.getEncoding(),
                objects,
                i18n.getResourceBundle(),
                userInfo);
        return new AsyncJobResponse(newJobId);
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
    public AsyncJobResponse createJsonExportJob(
        @Valid QueryExportParameters objects
    ) throws BadRequestException {
        UserInfo userInfo = authorization.getInfo();
        String newJobId =
            exportJobManager.createExportJob(
                StandardCharsets.UTF_8,
                objects,
                i18n.getResourceBundle(),
                userInfo);
        return new AsyncJobResponse(newJobId);
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
                this.logger.warn(String.format(
                    "Rejected download request by user %s "
                    + "for job %s created by user %s",
                    requestingUser.getUserId(),
                    id,
                    originalCreator.getUserId()));
                return Response.status(Response.Status.FORBIDDEN).build();
            }

            filename = exportJobManager.getJobDownloadFilename(id);
            resultStream = exportJobManager.getResultFileAsStream(id);

        } catch (JobManager.JobNotFoundException jfe) {
            this.logger.info(String.format(
                "Returning 404 for download: Could not find job %s", id));
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (FileNotFoundException fnfe) {
            this.logger.error(String.format(
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
