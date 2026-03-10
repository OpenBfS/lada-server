/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.data;

import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import de.intevation.lada.data.requests.Laf8ImportParameters;
import de.intevation.lada.data.requests.Laf9ImportParameters;
import de.intevation.lada.importer.ImportJobManager;
import de.intevation.lada.importer.Report;
import de.intevation.lada.importer.laf.Laf8Report;
import de.intevation.lada.importer.laf.Laf9Report;
import de.intevation.lada.util.data.JobManager;
import de.intevation.lada.rest.AsyncLadaService;
import de.intevation.lada.rest.LadaService;


/**
 * This class produces a RESTful service to interact with probe objects.
 *
 * @author <a href = "mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_DATA + "import/async")
public class AsyncImportService extends AsyncLadaService {

    @Inject
    ImportJobManager importJobManager;

    public static class JobStatus extends AsyncLadaService.JobStatus {
        private boolean errors;
        private boolean warnings;
        private boolean notifications;

        public JobStatus() {}

        private JobStatus(Future<Map<String, Report>> future) {
            super(future);

            if (future.isDone()) {
                try {
                    for (Report report : future.get().values()) {
                        if (report.hasErrors()) {
                            this.errors = true;
                        }
                        if (report.hasWarnings()) {
                            this.warnings = true;
                        }
                        if (report.hasNotifications()) {
                            this.notifications = true;
                        }
                    }
                } catch (InterruptedException | ExecutionException e) {
                    this.status = Status.ERROR;
                    this.message = INTERNAL_SERVER_ERROR.getReasonPhrase();
                }
            }
        }

        public Boolean getErrors() {
            return errors;
        }

        public Boolean getWarnings() {
            return warnings;
        }

        public Boolean getNotifications() {
            return notifications;
        }

        public void setErrors(Boolean errors) {
            this.errors = errors;
        }

        public void setWarnings(Boolean warnings) {
            this.warnings = warnings;
        }

        public void setNotifications(Boolean notifications) {
            this.notifications = notifications;
        }
    }

    @Override
    protected JobManager<Map<String, Report>> getJobManager() {
        return importJobManager;
    }

    @POST
    @Path("laf")
    @Operation(summary = "Import LAF 8.4 documents")
    public AsyncLadaService.AsyncJobResponse createAsyncImport(
        @NotNull @Valid Laf8ImportParameters lafImportParameters
    ) throws BadRequestException {
        //Get file content strings from input object
        Map<String, String> files = HashMap.newHashMap(
            lafImportParameters.getFiles().size());
        Charset charset = lafImportParameters.getEncoding();
        try {
            for (Map.Entry<String, String> e
                     : lafImportParameters.getFiles().entrySet()
            ) {
                ByteBuffer decodedBytes = ByteBuffer.wrap(
                    Base64.getDecoder().decode(e.getValue()));
                String decodedContent = new String(
                    new StringBuffer(charset.newDecoder()
                        .decode(decodedBytes)));
                files.put(e.getKey(), decodedContent);
            }
        } catch (IllegalArgumentException | CharacterCodingException e) {
            throw new BadRequestException();
        }
        lafImportParameters.setFiles(files);

        String newJobId = importJobManager.createLafImportJob(
            authorization.getInfo(), lafImportParameters);
        return new AsyncLadaService.AsyncJobResponse(newJobId);
    }

    @POST
    @Path("laf9")
    @Operation(summary = "Import LAF 9 documents")
    public AsyncLadaService.AsyncJobResponse createAsyncLaf9Import(
        @NotNull @Valid Laf9ImportParameters importParameters
    ) throws BadRequestException {
        String newJobId = importJobManager.createLafImportJob(
            authorization.getInfo(), importParameters);
        return new AsyncLadaService.AsyncJobResponse(newJobId);
    }

    @Override
    @GET
    @Path("status/{jobId}")
    public JobStatus getStatus(@PathParam("jobId") String id) {
        return new JobStatus(
            importJobManager.getJobById(id, authorization.getInfo()));
    }

    @GET
    @Path("result/{jobId}")
    @Operation(summary = "Get import result report data")
    @APIResponse(
        description = "Import result report data",
        responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM,
            schema = @Schema(oneOf = {Laf8Report.class, Laf9Report.class})))
    public Map<String, Report> getResult(
        @PathParam("jobId") String id
    ) throws InterruptedException, ExecutionException {
        Future<Map<String, Report>> job = importJobManager.getJobById(
            id, authorization.getInfo());
        jobToRemove = id;
        try {
            return job.get();
        } catch (CancellationException e) {
            // Job already canceled and about to be removed
            throw new NotFoundException();
        }
    }
}
