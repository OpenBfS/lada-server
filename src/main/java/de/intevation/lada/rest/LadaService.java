/* Copyright (C) 2021 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.util.List;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.jboss.logging.Logger;

import de.intevation.lada.context.ThreadLocale;
import de.intevation.lada.model.BaseModel;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.validation.Validator;
import de.intevation.lada.validation.groups.Warnings;
import de.intevation.lada.validation.groups.Notifications;


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

    private static final Logger LOG = Logger.getLogger(LadaService.class);

    /**
     * Path prefix for REST services.
     */
    public static final String PATH_REST = "rest/";

    /**
     * Path prefix for import and export services.
     */
    public static final String PATH_DATA = "data/";

    @Inject
    private HttpServletRequest request;

    @Inject
    @AuthorizationConfig(type = AuthorizationType.HEADER)
    protected Authorization authorization;

    /**
     * Authorize parameters that are model instances and validate return
     * values of business methods returning (lists of) instances
     * of class BaseModel and set authorization hints at instances.
     *
     * Only warnings and notifications are considered in validation. Validation
     * errors are expected to be handled respectively prevented by parameter
     * validation before method invocation.
     *
     * @param ctx Invocation context
     * @return The possibly validated and modified return value of the
     * business method
     * @throws Exception in case the business method throws an exception
     */
    @AroundInvoke
    @SuppressWarnings("unchecked")
    public Object intercept(InvocationContext ctx) throws Exception {
        LOG.debug("Set locale from request");
        ThreadLocale.set(request.getLocale());

        // Authorize input model instances
        for (Object param: ctx.getParameters()) {
            if (param instanceof BaseModel p) {
                authorization.authorize(
                    p, RequestMethod.valueOf(request.getMethod()));
            }
        }

        Object result = ctx.proceed();
        if (result != null) {
            // Set authorization hints and validate
            if (result instanceof List<?> listResult) {
                if (!listResult.isEmpty()
                    && listResult.get(0) instanceof BaseModel
                ) {
                    List<BaseModel> bmList = (List<BaseModel>) listResult;
                    authorization.filter(bmList);

                    new Validator().validate(
                        bmList,
                        Warnings.class,
                        Notifications.class);
                }
            } else if (result instanceof BaseModel r) {
                authorization.filter(r);

                new Validator().validate(
                    r, Warnings.class, Notifications.class);
            }
        }
        return result;
    }
}
