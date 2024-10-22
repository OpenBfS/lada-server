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

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.jboss.logging.Logger;

import de.intevation.lada.util.data.JobManager;
import de.intevation.lada.util.data.JobManager.JobNotFoundException;
import de.intevation.lada.util.data.Job.JobStatus;
import de.intevation.lada.util.auth.UserInfo;

public abstract class AsyncLadaService extends LadaService {

    protected abstract JobManager getJobManager();

    public static final class AsyncJobResponse {
        private final String jobId;

        public AsyncJobResponse(String jobId) {
            this.jobId = jobId;
        }

        public String getJobId() {
            return jobId;
        }
    }


    @Inject
    protected Logger logger;

    @GET
    @Path("status/{jobId}")
    @Operation(summary = "Retrieve status of an async job")
    public JobStatus getStatus(
            @PathParam("jobId") String id) {
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
