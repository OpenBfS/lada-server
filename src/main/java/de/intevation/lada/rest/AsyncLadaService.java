/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;


import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.jboss.logging.Logger;

import de.intevation.lada.i18n.I18n;
import de.intevation.lada.util.data.JobManager;


public abstract class AsyncLadaService extends LadaService {

    private static final Logger LOG = Logger.getLogger(AsyncLadaService.class);

    @Inject
    private I18n i18n;

    protected String jobToRemove;

    /**
     * Retrieve the class specific JobManager.
     * @return JobManager
     */
    protected abstract JobManager<?> getJobManager();

    public static final class AsyncJobResponse {
        private String jobId;

        /**
         * Default constructor for JSON binding.
         */
        public AsyncJobResponse() { }

        public AsyncJobResponse(String jobId) {
            this.jobId = jobId;
        }

        public String getJobId() {
            return jobId;
        }

        public void setJobId(String jobId) {
            this.jobId = jobId;
        }
    }

    /**
     * Possible status values for jobs.
     */
    public enum Status {
        WAITING, FINISHED, ERROR;
    }

    /**
     * Class modeling a job status.
     * Stores job status and message
     */
    public class JobStatus {
        protected Status status = Status.WAITING;
        protected String message = "";
        private boolean done;

        protected JobStatus(JobManager<?>.JobRecord jobRecord) {
            ScheduledFuture<?> scheduledRemoval =
                jobRecord.getScheduledRemoval();
            if (scheduledRemoval != null) {
                this.done = true;
                try {
                    jobRecord.getFuture().get();
                    this.status = Status.FINISHED;
                    this.message = i18n.getString(I18n.KEY_DOWNLOAD_UNTIL,
                        Date.from(Instant.now().plus(
                                scheduledRemoval.getDelay(TimeUnit.SECONDS),
                                ChronoUnit.SECONDS)));
                } catch (CancellationException | InterruptedException e) {
                    this.status = Status.ERROR;
                    this.message = INTERNAL_SERVER_ERROR.getReasonPhrase();
                } catch (ExecutionException ee) {
                    Throwable cause = ee.getCause();
                    LOG.error(cause.getMessage());
                    cause.printStackTrace();
                    this.status = Status.ERROR;
                    this.message = INTERNAL_SERVER_ERROR.getReasonPhrase();
                }
            }
        }

        public boolean isDone() {
            return done;
        }

        public Status getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }
    }

    @GET
    @Path("status/{jobId}")
    @Operation(summary = "Retrieve status of an async job")
    public JobStatus getStatus(
        @PathParam("jobId") String id
    ) {
        return new JobStatus(
            getJobManager().getJobById(id, authorization.getInfo()));
    }

    @GET
    @Path("cancel")
    @Operation(
        summary = "Try to cancel execution of all jobs of requesting user")
    public void cancel() {
        getJobManager().cancelJobs(authorization.getInfo());
    }

    @PreDestroy
    public void cleanup() {
        if (jobToRemove != null) {
            getJobManager().removeJob(jobToRemove);
        }
    }
}
