/* Copyright (C) 2021 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import de.intevation.lada.context.ThreadLocale;
import de.intevation.lada.util.auth.Authorization;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.TransactionManager;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;


/**
 * Abstract base class for LADA REST-services.
 *
 * Most LADA REST-services consume and produce JSON data. A method that
 * consumes or produces another media type can override the class level
 * annotations at this class.
 */
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public abstract class LadaService {

    /**
     * Path prefix for REST services.
     */
    public static final String PATH_REST = "rest/";

    /**
     * Path prefix for import and export services.
     */
    public static final String PATH_DATA = "data/";

    @Inject
    protected HttpServletRequest request;

    @Inject
    protected Authorization authorization;

    @Inject
    private TransactionManager txManager;

    /**
     * Set {@link ThreadLocale} before execution of business methods.
     * Also start a transaction, if not already done in
     * {@link de.intevation.lada.util.rest.ReaderTransactionWrapper},
     * and commit transaction after business method invocation.
     *
     * @param ctx Invocation context
     * @return The return value of the business method
     * @throws Exception in case the business method throws an exception
     */
    @AroundInvoke
    public Object interceptService(InvocationContext ctx) throws Exception {
        ThreadLocale.set(request.getLocale());
        if (txManager.getTransaction() == null) {
            txManager.begin();
        }
        Object result = ctx.proceed();
        txManager.commit();
        return result;
    }
}
