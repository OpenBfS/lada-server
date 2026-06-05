/* Copyright (C) 2026 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.io.IOException;

import jakarta.inject.Inject;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;

/**
 * {@link WriterInterceptor} for testing internal state.
 */
@Provider
public class WriterTestWrapper implements WriterInterceptor {

    @Inject
    private TransactionManager txManager;

    /**
     * Ensure there is no active transaction, i.e. transaction is
     * committed before serialization.
     */
    @Override
    public void aroundWriteTo(WriterInterceptorContext context)
        throws IOException, WebApplicationException {
        try {
            if (txManager.getTransaction() != null) {
                throw new IllegalStateException(
                    "Serialization should not happen in transaction context");
            }
        } catch (SystemException e) {
            throw new RuntimeException(e);
        }
        context.proceed();
    }
}
