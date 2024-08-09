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
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import de.intevation.lada.model.BaseModel;
import de.intevation.lada.validation.Validator;


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
    private Validator validator;

    /**
     * Validate return values of business methods returning (lists of) instances
     * of class BaseModel.
     *
     * @param ctx Invocation context
     * @return The (validated) return value of the business method
     * @throws Exception in case the business method throws an exception
     */
    @AroundInvoke
    @SuppressWarnings("unchecked")
    public Object intercept(InvocationContext ctx) throws Exception {
        Object result = ctx.proceed();
        if (result != null) {
            if (result instanceof List) {
                List listResult = (List) result;
                if (!listResult.isEmpty()
                    && listResult.get(0) instanceof BaseModel
                ) {
                    validator.validate(listResult);
                }
            } else if (result instanceof BaseModel) {
                validator.validate((BaseModel) result);
            }
        }
        return result;
    }
}
