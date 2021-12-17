/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */

package de.intevation.lada.util.data;

import static javax.transaction.Status.STATUS_NO_TRANSACTION;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.inject.Inject;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import com.fasterxml.jackson.annotation.JsonValue;

import org.apache.log4j.Logger;

import de.intevation.lada.util.auth.UserInfo;

/**
 * Abstract job class.
 * @author <a href="mailto:awoestmann@intevation.de">Alexander Woestmann</a>
 */
public abstract class Job implements Runnable {

    /**
     * Logger instance.
     */
    protected Logger logger = Logger.getLogger(this.getClass());

    /**
     * UserInfo.
     */
    protected UserInfo userInfo;

    /**
     * The Future corresponding to this job
     * after being submitted to an ExecutorService.
     */
    protected Future<?> future;

    /**
     * Repository used for loading data.
     */
    @Inject
    protected Repository repository;

    @Inject
    private UserTransaction tx;

    /**
     * Possible status values for jobs.
     */
    public enum Status {
        WAITING, FINISHED, ERROR;

        @JsonValue
        public String getName() {
            return this.name().toLowerCase();
        }
    }

    /**
     * The current job status.
     */
    protected JobStatus currentStatus = new JobStatus(Status.WAITING);

    public void setFuture(Future<?> future) {
        this.future = future;
    }

    /**
     * Cleanup method triggered when the job has finished.
     * @throws JobNotFinishedException Thrown if job is still running
     */
    public abstract void cleanup() throws JobNotFinishedException;

    /**
     * Return the current job status.
     * @return Job status
     */
    public JobStatus getStatus() {
        if (this.future != null) {
            if (this.future.isDone()) {
                this.currentStatus.setDone(true);
                try {
                    this.future.get();
                    this.currentStatus.setStatus(Status.FINISHED);
                } catch (CancellationException | InterruptedException e) {
                    this.currentStatus.setStatus(Status.ERROR);
                    this.currentStatus.setMessage(e.getMessage());
                } catch (ExecutionException ee) {
                    Throwable cause = ee.getCause();
                    logger.error(cause.getMessage());
                    cause.printStackTrace();
                    this.currentStatus.setStatus(Status.ERROR);
                    this.currentStatus.setMessage(cause.getMessage());
                }
            }
        }
        return currentStatus;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    /**
     * Child classes have to override this method to have the job run within
     * a transactional context.
     */
    protected abstract void runWithTx();

    /**
     * Should not be overwritten in child classes unless the transaction
     * handling has to be changed or no transaction is needed.
     */
    @Override
    public void run() {
        try {
            this.tx.begin();
            this.runWithTx();
        } catch (NotSupportedException | SystemException e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            try {
                if (this.tx.getStatus() != STATUS_NO_TRANSACTION) {
                    this.tx.commit();
                }
            } catch (SystemException
                | RollbackException
                | HeuristicMixedException
                | HeuristicRollbackException e
            ) {
                throw new RuntimeException(e.getMessage());
            }
        }
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
