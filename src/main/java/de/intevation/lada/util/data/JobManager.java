/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */

package de.intevation.lada.util.data;

import static jakarta.enterprise.concurrent.ManagedTask.IDENTITY_NAME;
import static java.util.concurrent.Future.State.FAILED;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.concurrent.ManagedExecutors;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.concurrent.ManagedTask;
import jakarta.enterprise.concurrent.ManagedTaskListener;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;

import org.jboss.logging.Logger;

import de.intevation.lada.util.auth.UserInfo;

/**
 * Abstract class for managing jobs.
 *
 * @param <V> Result type of managed jobs
 * @author <a href="mailto:awoestmann@intevation.de">Alexander Woestmann</a>
 */
@ApplicationScoped
public abstract class JobManager<V> {

    private static final long KEEP_RESULT_HOURS = 8;

    protected static JobIdentifier identifier =
        new JobManager.JobIdentifier();

    protected Logger logger = Logger.getLogger(this.getClass());

    @Resource
    private ManagedScheduledExecutorService executor;

    protected ConcurrentMap<String, JobRecord> activeJobs =
        new ConcurrentHashMap<>();

    public class JobRecord {
        private Future<V> future;
        private UserInfo user;
        private ScheduledFuture<?> scheduledRemoval;

        JobRecord(Future<V> future, UserInfo user) {
            this.future = future;
            this.user = user;
        }

        public Future<V> getFuture() {
            return future;
        }

        public UserInfo getUser() {
            return user;
        }

        public ScheduledFuture<?> getScheduledRemoval() {
            return scheduledRemoval;
        }
    }

    private class JobListener implements ManagedTaskListener {
        @Override
        public void taskDone(
            Future<?> f, ManagedExecutorService mes, Object task, Throwable e
        ) {
            ManagedTask job = (ManagedTask) task;
            String jobId = job.getExecutionProperties().get(IDENTITY_NAME);
            activeJobs.get(jobId).scheduledRemoval =
                executor.schedule(
                    () -> removeJob(jobId), KEEP_RESULT_HOURS, TimeUnit.HOURS);
        }

        @Override
        public void taskSubmitted(
            Future<?> f, ManagedExecutorService mes, Object task
        ) {
            // No-op
        }

        @Override
        public void taskStarting(
            Future<?> f, ManagedExecutorService mes, Object task
        ) {
            // No-op
        }

        @Override
        public void taskAborted(
            Future<?> f, ManagedExecutorService mes, Object task, Throwable e
        ) {
            // No-op
        }
    };

    /**
     * Get job by id.
     *
     * If the job is done with an error or has been canceled, it will be
     * cleaned up.
     *
     * @param id Id to look for
     * @param userInfo for authorization
     * @throws NotFoundException if job with given ID cannot be found
     * @throws ForbiddenException if job does not belong to requesting user
     * @return {@link Future} representing the requested job
     */
    public JobRecord getJobById(String id, UserInfo userInfo) {
        JobRecord job = activeJobs.get(id);
        if (job == null) {
            throw new NotFoundException();
        }
        if (!job.user.getUserId().equals(userInfo.getUserId())) {
            throw new ForbiddenException();
        }

        // Cleanup canceled or failed job
        Future<V> statusObject = job.future;
        if ((statusObject.isCancelled() || statusObject.state() == FAILED)
            && statusObject.isDone()
        ) {
            removeJob(id);
        }

        return job;
    }

    /**
     * Try to cancel execution of jobs of a user.
     *
     * @param userInfo for authorization
     */
    public void cancelJobs(UserInfo userInfo) {
        for (String jobId: activeJobs.keySet()) {
            JobRecord job = activeJobs.get(jobId);
            if (job.user.getUserId().equals(userInfo.getUserId())) {
                if (job.future.cancel(true)) {
                    removeJob(jobId);
                };
            }
        }
    }

    /**
     * Add job and return the next job identifier.
     *
     * The new identifier will be stored in lastIdentifier.
     * @param newJob A new job
     * @return New identifier as String
     */
    protected synchronized String addJob(Job<V> newJob, UserInfo user) {
        // Create job identifier
        identifier.next();
        String id = identifier.toString();

        logger.debug(String.format("Creating new job: %s", id));
        Future<V> future = executor.submit(ManagedExecutors.managedTask(
                newJob, Map.of(IDENTITY_NAME, id), new JobListener()));
        if (activeJobs.put(id, new JobRecord(future, user)) != null) {
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
    public void removeJob(String jobId) {
        logger.debug(String.format("Removing job %s", jobId));
        activeJobs.remove(jobId);
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
}
