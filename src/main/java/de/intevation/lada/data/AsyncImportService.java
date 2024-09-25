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

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.jboss.logging.Logger;

import de.intevation.lada.data.requests.LafImportParameters;
import de.intevation.lada.importer.ImportJobManager;
import de.intevation.lada.model.master.MeasFacil;
import de.intevation.lada.util.auth.UserInfo;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.JobManager.JobNotFoundException;
import de.intevation.lada.util.data.Job.JobStatus;
import de.intevation.lada.rest.LadaService;


/**
 * This class produces a RESTful service to interact with probe objects.
 *
 * @author <a href = "mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_DATA + "import/async")
public class AsyncImportService extends LadaService {

    @Inject
    private Repository repository;

    @Inject
    private Logger logger;

    @Inject
    ImportJobManager importJobManager;

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
    public Response createAsyncImport(
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
            throw new BadRequestException(
                Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage()).build());
        }

        String newJobId = importJobManager.createImportJob(
            authorization.getInfo(), lafImportParameters, mst, files);
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
