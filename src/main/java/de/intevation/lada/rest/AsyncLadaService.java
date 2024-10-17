/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import java.io.Serializable;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.jboss.logging.Logger;

import de.intevation.lada.util.data.JobManager;
import de.intevation.lada.util.data.JobManager.JobNotFoundException;
import de.intevation.lada.util.data.Job.JobStatus;
import de.intevation.lada.util.auth.UserInfo;

public abstract class AsyncLadaService extends LadaService {

    protected abstract JobManager getJobManager();

    @Schema(name="AsyncJobResponse", description = "DTO carryin refID of Jobs")
    public static final class AsyncJobResponse implements Serializable {
        private final String refId;

        public AsyncJobResponse(String refId) {
            this.refId = refId;
        }

        public String getRefId() {
            return refId;
        }
    }


    @Inject
    protected Logger logger;

    /**
     * Retrieve the status of an export job.
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
     * Note: The 'error' status indicates errors in the server
     * like I/O errors etc.
     * 'errors' and 'warnings' indicate errors in the import itself,
     * like authorization issues etc.
     *
     * @param id Job id to check
     * @return Json object containing the status information, status
     *         403 if the requesting user has not created the request
     *         or status 404 if job was not found
     */
    @GET
    @Path("status/{id}")
    @Operation(summary="Retrieve status of an async Job")
    @APIResponse(description = "A JobStatus",
                 content = @Content(mediaType = "application/json",
                 schema = @Schema(implementation = JobStatus.class)
                )
    )
    @APIResponse(responseCode = "403", description = "Forbidden Job requested")
    @APIResponse(responseCode = "404", description = "Job not found")
    public JobStatus getStatus(
            @Parameter(description = "The id of the job ", required = true)
            @PathParam("id") String id) {
        JobStatus status;
        UserInfo originalCreator;
        UserInfo requestingUser = authorization.getInfo();

        try {
            originalCreator = getJobManager().getJobUserInfo(id);
            if (!originalCreator.getUserId().equals(
                    requestingUser.getUserId())) {
                logger.warn(String.format(
                        "Rejected status request by user "
                                + "#%s for job %s created by user #%s",
                        requestingUser.getUserId(),
                        id,
                        originalCreator.getUserId()));
                throw new ForbiddenException();
            }

            status = getJobManager().getJobStatus(id);
        } catch (JobNotFoundException jnfe) {
            logger.info(String.format("Could not find status for job %s", id));
            throw new NotFoundException();
        }
        return status;
    }

}
