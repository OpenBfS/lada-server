/* Copyright (C) 2026 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.rest;

import java.io.IOException;

import jakarta.inject.Inject;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

/**
 * Rollback transactions in response pipeline.
 *
 * Assumes not yet committed transactions have been begun in
 * {@link ReaderTransactionWrapper} and are still open because
 * an error occurred during processing of the request.
 */
@Provider
public class TxRollbackFilter implements ContainerResponseFilter {

    @Inject
    TransactionManager txManager;

    @Override
    public void filter(
        ContainerRequestContext requestContext,
        ContainerResponseContext responseContext
    ) throws IOException {
        try {
            if (txManager.getTransaction() != null) {
                txManager.rollback();
            }
        } catch (SystemException e) {
            throw new RuntimeException(e);
        }
    }
}
