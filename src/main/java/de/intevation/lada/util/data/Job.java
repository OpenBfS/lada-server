/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */

package de.intevation.lada.util.data;

import static jakarta.transaction.Status.STATUS_NO_TRANSACTION;

import java.util.concurrent.Callable;

import jakarta.inject.Inject;
import jakarta.transaction.UserTransaction;

import org.jboss.logging.Logger;

/**
 * Abstract job class.
 * @author <a href="mailto:awoestmann@intevation.de">Alexander Woestmann</a>
 *
 * @param <V> Type of job result
 */
public abstract class Job<V> implements Callable<V> {

    /**
     * Logger instance.
     */
    protected Logger logger = Logger.getLogger(this.getClass());

    /**
     * Repository used for loading data.
     */
    @Inject
    protected Repository repository;

    @Inject
    private UserTransaction tx;

    /**
     * Child classes have to override this method to have the job run within
     * a transactional context.
     */
    protected abstract V callWithTx() throws Exception;

    /**
     * Call {@link Job#callWithTx()} in transaction context.
     */
    @Override
    public final V call() throws Exception {
        try {
            this.tx.begin();
            return this.callWithTx();
        } catch (RuntimeException e) {
            logger.error("Exception occured in transaction context", e);
            throw e;
        } finally {
            if (this.tx.getStatus() != STATUS_NO_TRANSACTION) {
                this.tx.commit();
            }
        }
    }
}
