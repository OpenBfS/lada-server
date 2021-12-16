/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */

package de.intevation.lada.util.data;

import com.fasterxml.jackson.annotation.JsonValue;

import org.apache.log4j.Logger;

import de.intevation.lada.util.auth.UserInfo;

/**
 * Abstract job class.
 * @author <a href="mailto:awoestmann@intevation.de">Alexander Woestmann</a>
 */
public abstract class Job extends Thread {

    /**
     * Logger instance.
     */
    protected Logger logger = Logger.getLogger(this.getClass());

    /**
     * UserInfo.
     */
    protected UserInfo userInfo;

    /**
     * Possible status values for jobs.
     */
    public enum Status {
        WAITING, RUNNING, FINISHED, ERROR;

        @JsonValue
        public String getName() {
            return this.name().toLowerCase();
        }
    }

    /**
     * The current job status.
     */
    protected JobStatus currentStatus = new JobStatus(Status.WAITING);

    /**
     * Cleanup method triggered when the job has finished.
     * @throws JobNotFinishedException Thrown if job is still running
     */
    public abstract void cleanup() throws JobNotFinishedException;

    /**
     * Set this job to failed state.
     * @param m Optional message
     */
    public void fail(String m) {
        try {
            this.setCurrentStatus(Status.ERROR);
            this.setDone(true);
            this.setMessage(m);
        } catch (IllegalStatusTransitionException iste) {
            this.currentStatus.setStatus(Status.ERROR);
            this.currentStatus.setMessage("Internal server errror");
            this.currentStatus.setDone(true);
        } finally {
            logger.error(
                String.format("Job failed with message: %s", getMessage()));
        }
    }

    /**
     * Set this job to finished state.
     */
    protected void finish() {
        try {
            this.setCurrentStatus(Status.FINISHED);
            this.setDone(true);
        } catch (IllegalStatusTransitionException iste) {
            this.currentStatus.setStatus(Status.ERROR);
            this.currentStatus.setMessage("Internal server errror");
            this.currentStatus.setDone(true);
        }
    }

    /**
     * Return the message String.
     * @return message as String
     */
    public String getMessage() {
        return currentStatus.getMessage();
    }

    /**
     * Return the current job status.
     * @return Job status
     */
    public JobStatus getStatus() {
        return currentStatus;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    /**
    * Check if job is done and will no longer change its status.
    * @return True if done, else false
    */
   public boolean isDone() {
       return currentStatus.isDone();
   }

    /**
     * Run the Job.
     * Should be overwritten in child classes.
     */
    public void run() {
        currentStatus.setStatus(Status.RUNNING);
    }

    /**
     * Set the current status.
     *
     * @param status New status
     * @throws IllegalStatusTransitionException Thrown if job is already done
     */
    protected void setCurrentStatus(
        Status status
    ) throws IllegalStatusTransitionException {
        if (isDone()) {
            throw new IllegalStatusTransitionException(
                "Invalid job status transition: Job is already done");
        }
        this.currentStatus.setStatus(status);
    }

    /**
     * Set the done state.
     * @param done New done status
     * @throws IllegalArgumentException Thrown if argument is false and
     *                                  job is already done
     */
    protected void setDone(boolean done) throws IllegalArgumentException {
        if (!done && this.isDone()) {
            throw new IllegalArgumentException(
                "Job is already done, can not reset done to false");
        }
        this.currentStatus.setDone(done);
    }

    /**
     * Set status message.
     * @param message Message
     */
    protected void setMessage(String message) {
        this.currentStatus.setMessage(message);
    }

    /**
     * Set user info.
     * @param userInfo New userInfo
     */
    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    /**
     * Exception thrown if an unfished ExportJob is about to be removed
     * while still runnning.
     */
    public static class JobNotFinishedException extends Exception {
        private static final long serialVersionUID = 1L;
    }

    /**
     * Exception thrown if an illegal status transition was done.
     */
    public static class IllegalStatusTransitionException extends Exception {
        private static final long serialVersionUID = 2L;
        public IllegalStatusTransitionException(String msg) {
            super(msg);
        }
    }

    /**
     * Class modeling a job status.
     * Stores job status and message
     */
    public static class JobStatus {
        private Status status;
        private String message;
        private boolean done;
        private Boolean warnings;
        private Boolean errors;

        public JobStatus(Status s) {
            this(s, "", false);
        }

        public JobStatus(Status s, String m, boolean d) {
            this.status = s;
            this.message = m != null ? m : "";
            this.done = d;
            warnings = null;
            errors = null;
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

        public Boolean getErrors() {
            return errors;
        }

        public Boolean getWarnings() {
            return warnings;
        }

        public void setDone(boolean done) {
            this.done = done;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public void setErrors(Boolean errors) {
            this.errors = errors;
        }

        public void setWarnings(Boolean warnings) {
            this.warnings = warnings;
        }

        public void setStatus(Status status) {
            this.status = status;
        }
    }
}
