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

    /**
     * Validate return values of business methods returning (lists of) instances
     * of class BaseModel.
     *
     * Only warnings and notifications are considered in validation. Validation
     * errors are expected to be handled respectively prevented by parameter
     * validation before method invocation.
     *
     * @param ctx Invocation context
     * @return The (validated) return value of the business method
     * @throws Exception in case the business method throws an exception
     */
    @AroundInvoke
    @SuppressWarnings("unchecked")
    public Object intercept(InvocationContext ctx) throws Exception {
        LOG.debug("Set locale from request");
        ThreadLocale.set(request.getLocale());

        Object result = ctx.proceed();
        if (result != null) {
            if (result instanceof List) {
                List listResult = (List) result;
                if (!listResult.isEmpty()
                    && listResult.get(0) instanceof BaseModel
                ) {
                    new Validator().validate(
                        listResult, Warnings.class, Notifications.class);
                }
            } else if (result instanceof BaseModel) {
                new Validator().validate(
                    (BaseModel) result, Warnings.class, Notifications.class);
            }
        }
        return result;
    }
}
