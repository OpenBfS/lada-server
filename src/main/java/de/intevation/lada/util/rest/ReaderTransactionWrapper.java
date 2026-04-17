/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.rest;

import java.io.IOException;

import org.jboss.logging.Logger;

import jakarta.inject.Inject;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.ReaderInterceptor;
import jakarta.ws.rs.ext.ReaderInterceptorContext;


/**
 * Start transaction before deserialization.
 *
 * This allows interaction with database contents during deserialization,
 * e.g. for auto-completion purposes, and using the same transaction within
 * business method invocation of service classes.
 */
@Provider
public class ReaderTransactionWrapper implements ReaderInterceptor {

    private static final Logger LOG =
        Logger.getLogger(ReaderTransactionWrapper.class);

    @Inject
    private TransactionManager txManager;

    @Override
    public Object aroundReadFrom(ReaderInterceptorContext context)
        throws IOException, WebApplicationException {
        try {
            txManager.begin();
        } catch (NotSupportedException | SystemException e) {
            LOG.error("Could not start transaction", e);
            throw new InternalServerErrorException();
        }
        return context.proceed();
    }
}
