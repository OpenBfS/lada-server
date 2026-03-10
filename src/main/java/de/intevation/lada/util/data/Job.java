/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */

package de.intevation.lada.util.data;

import static jakarta.transaction.Status.STATUS_NO_TRANSACTION;

import java.util.concurrent.Future;

import jakarta.inject.Inject;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;
import jakarta.transaction.UserTransaction;

import org.jboss.logging.Logger;

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
     * Information about the user who started the job.
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

    public Future<?> getFuture() {
        return future;
    }

    void setFuture(Future<?> future) {
        this.future = future;
    }

    void cancel() {
        this.future.cancel(true);
    }

    /**
     * Cleanup method triggered when the job has finished.
     * @throws JobNotFinishedException Thrown if job is still running
     */
    public abstract void cleanup() throws JobNotFinishedException;

    public UserInfo getUserInfo() {
        return userInfo;
    }

    /**
     * Child classes have to override this method to have the job run within
     * a transactional context.
     */
    protected abstract void runWithTx();

    /**
     * Call {@link Job#runWithTx()} in transaction context.
     */
    @Override
    public final void run() {
        try {
            this.tx.begin();
            this.runWithTx();
        } catch (NotSupportedException | SystemException e) {
            throw new RuntimeException(e.getMessage());
        } catch (RuntimeException e) {
            logger.error("Exception occured in transaction context", e);
            throw e;
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
                throw new RuntimeException(e);
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
     * Exception thrown if an unfished Job is about to be removed
     * while still running.
     */
    public static class JobNotFinishedException extends Exception {
        private static final long serialVersionUID = 1L;
    }
}
