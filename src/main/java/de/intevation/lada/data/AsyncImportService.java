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

    @POST
    @Path("laf")
    @Operation(summary="Import a LAF document")
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
    @Path("result/{jobId}")
    @Operation(summary = "Get the result")
    public String getResult(
        @PathParam("jobId") String id
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
