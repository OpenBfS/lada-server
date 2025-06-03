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
import jakarta.transaction.Transactional;
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
@Transactional
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

    @AroundInvoke
    public Object intercept(InvocationContext ctx) throws Exception {
        ThreadLocale.set(request.getLocale());
        return ctx.proceed();
    }
}
