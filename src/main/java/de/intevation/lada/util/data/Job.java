/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */

package de.intevation.lada.util.data;

import org.apache.log4j.Logger;

import de.intevation.lada.util.auth.UserInfo;

/**
 * Abstract job class
 * @author <a href="mailto:awoestmann@intevation.de">Alexander Woestmann</a>
 */
abstract public class Job extends Thread {

    /**
     * True if job has finished and will not change it's status anymore.
     */
    protected boolean done = false;

    /**
     * Logger instance.
     */
    protected Logger logger;

    /**
     * Message String, used in case of an error.
     */
    protected String message;

    /**
     * Id of this job.
     */
    protected String jobId;

    /**
     * UserInfo.
     */
    protected UserInfo userInfo;

    /**
     * Possible status values for jobs.
     */
    public enum Status { waiting, running, finished, error }

    /**
     * The current job status.
     */
    protected Status currentStatus = Status.waiting;

    /**
     * Cleanup method triggered when the job has finished
     * @throws JobNotFinishedException Thrown if job is still running
     */
    abstract public void cleanup() throws JobNotFinishedException;

    /**
     * Set this job to failed state.
     * @param m Optional message
     */
    protected void fail(String m) {
        try {
            this.setCurrentStatus(Status.error);
            this.setDone(true);
            this.message = m;
        } catch (IllegalStatusTransitionException iste) {
            this.currentStatus = Status.error;
            this.message = "Internal server errror";
            this.done = true;
        } finally {
            logger.error(
                String.format("Export failed with message: %s", message));
        }
    }

    /**
     * Set this job to finished state.
     */
    protected void finish() {
        try {
            this.setCurrentStatus(Status.finished);
            this.setDone(true);
        } catch (IllegalStatusTransitionException iste) {
            this.currentStatus = Status.error;
            this.message = "Internal server errror";
            this.done = true;
        }
    }

    /**
     * Return the job identifier.
     * @return Identifier as String
     */
    public String getJobId() {
        return jobId;
    }

    /**
     * Return the message String.
     * @return message as String
     */
    public String getMessage() {
        return message;
    }

    /**
     * Return the current job status.
     * @return Job status
     */
    public Status getStatus() {
        return currentStatus;
    }

    /**
     * Return the current status as String.
     * @return Status as String
     */
    public String getStatusName() {
        return currentStatus.name();
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    /**
    * Check if job is done and will no longer change its status.
    * @return True if done, else false
    */
   public boolean isDone() {
       return done;
   }

    /**
     * Run the Job.
     * Should be overwritten in child classes.
     */
    public void run() {
        currentStatus = Status.running;
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
        this.currentStatus = status;
    }

    /**
     * Set the done state.
     * @param done New done status
     * @throws IllegalArgumentException Thrown if argument is false and
     *                                  job is already done
     */
    protected void setDone(boolean done) throws IllegalArgumentException {
        if (!done && this.done) {
            throw new IllegalArgumentException(
                "Job is already done, can not reset done to false");
        }
        this.done = done;
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

}
