/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.data;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import de.intevation.lada.data.requests.LafImportParameters;
import de.intevation.lada.importer.ImportJobManager;
import de.intevation.lada.model.master.MeasFacil;
import de.intevation.lada.util.auth.UserInfo;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.JobManager;
import de.intevation.lada.util.data.JobManager.JobNotFoundException;
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
    private Repository repository;

    @Inject
    ImportJobManager importJobManager;

    /**
     * getJobManager is used to retrieve the class specific JobManager
     * @return JobManager
     */
    @Override
    protected JobManager getJobManager() {
        return importJobManager;
    }

    /**
     * Import a given list of files, generate a tag and set it to all
     * imported records.
     *
     * @param lafImportParameters LafImportParameters
     * @return Object containing ID of new job
     * @throws BadRequestException if any constraint violations are detected,
     * file content is not in valid Base64 scheme or decoding using the encoding
     * given in lafImportParameters fails.
     */
    @POST
    @Path("laf")
    @Operation(summary="Import a LAF document")
    @APIResponse(description = "An Object containing an ID of the job (refId)",
                 content = @Content(mediaType = "application/json",
                 schema = @Schema(implementation = AsyncJobResponse.class)
                )
    )
    @APIResponse(responseCode = "400", description = "Bad request")
    public AsyncLadaService.AsyncJobResponse createAsyncImport(
        @Valid LafImportParameters lafImportParameters
    ) throws BadRequestException {
        MeasFacil mst = repository.getById(
                MeasFacil.class, lafImportParameters.getMeasFacilId());

        //Get file content strings from input object
        Map<String, String> files = new HashMap<String, String>();
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

        String newJobId = importJobManager.createImportJob(
            authorization.getInfo(), lafImportParameters, mst, files);
        return new AsyncLadaService.AsyncJobResponse(newJobId);
    }

    @GET
    @Path("result/{id}")
    @Operation(summary = "Get the result")
    @APIResponse(description = "An ID of the job")
    @APIResponse(responseCode = "403", description = "Forbidden Download requested")
    @APIResponse(responseCode = "404", description = "Job not found")
    public String getResult(
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
                    throw new ForbiddenException();
            }
            return importJobManager.getImportResult(id);
        } catch (JobNotFoundException jfe) {
            logger.info(String.format(
                "Returning 404 for download: Could not find job %s", id));
            throw new NotFoundException();
        }
    }
}
