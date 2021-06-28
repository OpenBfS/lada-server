/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */

package de.intevation.lada.util.data;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.log4j.Logger;

import de.intevation.lada.util.auth.UserInfo;
import de.intevation.lada.util.data.Job.JobNotFinishedException;
import de.intevation.lada.util.data.Job.Status;

/**
 * Abstract class for managing jobs
 * @author <a href="mailto:awoestmann@intevation.de">Alexander Woestmann</a>
 */
abstract public class JobManager {

    protected static JobIdentifier identifier =
        new JobManager.JobIdentifier();

    protected Logger logger;


    protected Map<String, Job> activeJobs;

    /**
     * Get job by id.
     * @param id Id to look for
     * @throws JobNotFoundException Thrown if a job with the given can not
     *                              be found
     */
    protected Job getJobById(
        String id
    ) throws JobNotFoundException {
        Job job = activeJobs.get(id);
        if (job == null) {
            logger.debug(String.format("No active job found: %s", id));
            throw new JobNotFoundException();
        }
        return job;
    }

    /**
     * Get the status of a job by identifier.
     *
     * If the job is done with an error, it will be removed after return
     * the failure status.
     * @param id Id to look for
     * @return Job status
     * @throws JobNotFoundException Thrown if a job with the given can not
     *                              be found
     */
    public JobStatus getJobStatus(
        String id
    ) throws JobNotFoundException {
        Job job = getJobById(id);
        String jobStatus = job.getStatusName();
        String message = job.getMessage();
        boolean done = job.isDone();
        JobStatus statusObject = new JobStatus(jobStatus, message, done);
        if (jobStatus.equals(Status.error.name()) && done) {
            removeJob(job);
        }
        return statusObject;
    }

    /**
     * Get the user informations for the current job by identifier.
     * @param id Id to look for.
     * @return The user info
     * @throws JobNotFoundException Thrown if a job with the given can not
     *                              be found
     */
    public UserInfo getJobUserInfo(
        String id
    ) throws JobNotFoundException {
        Job job = getJobById(id);
        return job.getUserInfo();
    }

    /**
     * Calculates and returns the next job identifier.
     *
     * The new identifier will be stored in lastIdentifier.
     * @return New identifier as String
     */
    protected synchronized String getNextIdentifier() {
        identifier.next();
        return identifier.toString();
    }

    /**
     * Remove the given job from the active job list and trigger its
     * cleanup function.
     * @param job Job to remove
     */
    protected void removeJob(Job job) {
        try {
            logger.debug(String.format("Removing job %s", job.getJobId()));
            job.cleanup();
        } catch (JobNotFinishedException jfe) {
            logger.warn(String.format(
                "Tried to remove unfinished job %s", job.getJobId()));
        }
        activeJobs.remove(job.getJobId());
    }

    /**
     * Utility class providing unique identifier values for imnport/export jobs.
     *
     * The identifier can be set to the next value by using the next() method
     * and obtained as hex String by using the toString() method.
     *
     * Identifier format:
     * [timestamp]-[sequenceNumber]-[randomPart]
     * timestamp: Timestamp in seconds the identifier was set to the next
     *            value (64 bits)
     * sequenceNumber: Sequence number, will be reset for each
     *                 timestamp (16 bits)
     * randomPart: Random number (32 bits)
     *
     * The hexadecimal representation will contain leading zeroes.
     */
    protected static class JobIdentifier {

        /**
         * Format string for the hexadecimal representation.
         */
        private final String hexFormat;

        private static final short INITIAL_SEQ_NO = 1;

        private short seqNo;

        private long timestamp;

        private int randomPart;

        /**
         * Create the identifier with an initial value.
         */
        public JobIdentifier() {
            seqNo = INITIAL_SEQ_NO;
            timestamp = System.currentTimeMillis();
            randomPart = 0;
            String longMaxValueHex = Long.toHexString(Long.MAX_VALUE);
            String intMaxValueHex = Integer.toHexString(Integer.MAX_VALUE);
            String shortMaxValueHex = "7fff";
            int longHexWidth = longMaxValueHex.length();
            int intHexWidth = intMaxValueHex.length();
            int shortHexWidth = shortMaxValueHex.length();
            StringBuilder formatBuilder = new StringBuilder("%1$0")
                .append(longHexWidth)
                .append("x-")
                .append("%2$0")
                .append(shortHexWidth)
                .append("x-")
                .append("%3$0")
                .append(intHexWidth)
                .append("x");
            hexFormat = formatBuilder.toString();
        }

        /**
         * Set the identifier to the next value.
         */
        public void next() {
            long currentTime = System.currentTimeMillis();
            if (currentTime == timestamp) {
                seqNo++;
            } else {
                timestamp = currentTime;
                seqNo = INITIAL_SEQ_NO;
            }
            randomPart = ThreadLocalRandom.current().nextInt();
        }

        /**
         * Return the hexadecimal string representation of this identifier.
         * The string will include padding zeroes.
         */
        public String toString() {
            return String.format(hexFormat, timestamp, seqNo, randomPart);
        }
    }

    /**
     * Thrown if a job with the given can not be found.
     */
    public static class JobNotFoundException extends Exception {
        private static final long serialVersionUID = 1L;
    }

    /**
     * Class modeling a job status.
     * Stores job status and message
     */
    public static class JobStatus {
        private String status;
        private String message;
        private boolean done;

        public JobStatus(String s, String m, boolean d) {
            this.status = s;
            this.message = m != null? m: "";
            this.done = d;
        }

        public boolean isDone() {
            return done;
        }

        public String getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }
    }
}
