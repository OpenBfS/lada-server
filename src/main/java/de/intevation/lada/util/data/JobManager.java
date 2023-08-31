/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */

package de.intevation.lada.util.data;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;

import org.jboss.logging.Logger;

import de.intevation.lada.util.auth.UserInfo;
import de.intevation.lada.util.data.Job.JobNotFinishedException;
import de.intevation.lada.util.data.Job.JobStatus;
import de.intevation.lada.util.data.Job.Status;

/**
 * Abstract class for managing jobs.
 * @author <a href="mailto:awoestmann@intevation.de">Alexander Woestmann</a>
 */
@ApplicationScoped
public abstract class JobManager {

    protected static JobIdentifier identifier =
        new JobManager.JobIdentifier();

    protected Logger logger = Logger.getLogger(this.getClass());

    @Resource
    protected ManagedExecutorService executor;

    private ConcurrentMap<String, Job> activeJobs = new ConcurrentHashMap<>();

    /**
     * Get job by id.
     * @param id Id to look for
     * @throws JobNotFoundException Thrown if a job with the given can not
     *                              be found
     * @return Job instance with given id
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
        JobStatus statusObject = job.getStatus();
        if (statusObject.getStatus() == Status.ERROR && statusObject.isDone()) {
            removeJob(id);
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
     * Add job and return the next job identifier.
     *
     * The new identifier will be stored in lastIdentifier.
     * @param newJob A new job
     * @return New identifier as String
     */
    protected synchronized String addJob(Job newJob) {
        identifier.next();
        String id = identifier.toString();
        logger.debug(String.format("Creating new job: %s", id));
        if (activeJobs.put(id, newJob) != null) {
            // This should never happen
            throw new RuntimeException(
                String.format("Job with id %s already exists", id));
        }
        return id;
    }

    /**
     * Remove the given job from the active job list and trigger its
     * cleanup function.
     * @param jobId ID of job to remove
     */
    protected void removeJob(String jobId) {
        logger.debug(String.format("Removing job %s", jobId));
        Job job = activeJobs.get(jobId);
        if (job != null) {
            try {
                job.cleanup();
            } catch (JobNotFinishedException jfe) {
                logger.warn(String.format(
                        "Tried to remove unfinished job %s", jobId));
            }
            activeJobs.remove(jobId);
        } // else, job has already been removed by concurrent request.
    }

    /**
     * Utility class providing unique identifier values for jobs.
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
         * @return String representation
         */
        public String toString() {
            return String.format(hexFormat, timestamp, seqNo, randomPart);
        }
    }

    /**
     * Thrown if a job cannot be found.
     */
    public static class JobNotFoundException extends Exception {
        private static final long serialVersionUID = 1L;
    }
}
