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
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import de.intevation.lada.data.requests.Laf8ImportParameters;
import de.intevation.lada.data.requests.Laf9ImportParameters;
import de.intevation.lada.importer.ImportJobManager;
import de.intevation.lada.importer.Report;
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

    @Override
    protected JobManager getJobManager() {
        return importJobManager;
    }

    @POST
    @Path("laf")
    @Operation(summary = "Import LAF 8.4 documents")
    public AsyncLadaService.AsyncJobResponse createAsyncImport(
        @Valid Laf8ImportParameters lafImportParameters
    ) throws BadRequestException {
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
        lafImportParameters.setFiles(files);

        String newJobId = importJobManager.createLafImportJob(
            authorization.getInfo(), lafImportParameters);
        return new AsyncLadaService.AsyncJobResponse(newJobId);
    }

    @POST
    @Path("laf9")
    @Operation(summary = "Import LAF 9 documents")
    public AsyncLadaService.AsyncJobResponse createAsyncLaf9Import(
        @Valid Laf9ImportParameters importParameters
    ) throws BadRequestException {
        String newJobId = importJobManager.createLafImportJob(
            authorization.getInfo(), importParameters);
        return new AsyncLadaService.AsyncJobResponse(newJobId);
    }

    @GET
    @Path("result/{jobId}")
    @Operation(summary = "Get import result report data")
    public Map<String, Report> getResult(
        @PathParam("jobId") String id
    ) {
        return importJobManager.getImportResult(id, authorization.getInfo());
    }
}
