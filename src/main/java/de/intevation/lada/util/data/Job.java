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
    protected boolean done;

    /**
     * Logger instance.
     */
    protected Logger logger;

    /**
     * Message String, used in case of an error.
     */
    protected String message;

    /**
     * Id of this export job.
     */
    protected String jobId;

    /**
     * UserInfo.
     */
    protected UserInfo userInfo;

    /**
     * Possible status values for export jobs.
     */
    public enum Status { waiting, running, finished, error }

    /**
     * The current job status.
     */
    protected Status currentStatus;

    /**
     * Cleanup method triggered when the job has finished
     * @throws JobNotFinishedException Thrown if job is still running
     */
    abstract public void cleanup() throws JobNotFinishedException;

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
