/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.util.Collection;

import org.jboss.logging.Logger;

import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;
import jakarta.persistence.NoResultException;
import jakarta.validation.groups.Default;
import jakarta.ws.rs.BadRequestException;

import de.intevation.lada.context.ThreadLocale;
import de.intevation.lada.model.BaseModel;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.validation.Validator;
import de.intevation.lada.validation.groups.Warnings;
import de.intevation.lada.validation.groups.Notifications;
import de.intevation.lada.validation.groups.PostAuthorization;


/**
 * Abstract base class for LADA REST-services serving model entities.
 *
 * @param <I> Type of resource identifier provided in URL paths
 */
abstract class LadaEntityService<I> extends LadaService {

    private static final Logger LOG = Logger.getLogger(LadaEntityService.class);

    @Inject
    protected Repository repository;

    /**
     * Get resource identifier from URL path.
     *
     * Can provide the value of a field annotated with
     * {@link jakarta.ws.rs.PathParam}.
     *
     * @return ID from URL path or null
     */
    abstract I getPathId();

    /**
     * Authorize and validate parameters that are model instances
     * and validate return values of business methods returning (lists of)
     * instances of class BaseModel and set authorization hints at instances.
     *
     * Only warnings ({@link Warnings}) and notifications
     * ({@link Notifications}) are considered in return value validation.
     * Validation errors are handled respectively prevented by parameter
     * validation before method invocation and before ({@link Default})
     * respectively after ({@link PostAuthorization}) authorization.
     *
     * @param ctx Invocation context
     * @return The possibly validated and modified return value of the
     * business method
     * @throws Exception in case the business method throws an exception
     */
    @Override
    @AroundInvoke
    @SuppressWarnings("unchecked")
    public Object intercept(InvocationContext ctx) throws Exception {
        if (this instanceof SamplerService) {
            LOG.debug("Start processing request");
        }
        ThreadLocale.set(request.getLocale());

        // Create validator with request/thread locale
        Validator validator = new Validator();

        // Authorize and validate input model instances
        for (Object param: ctx.getParameters()) {
            if (param instanceof BaseModel p) {
                I pathId = getPathId();
                if (pathId != null) {
                    // Check path and payload identity match
                    try {
                        Object persistent = repository.getSingle(repository
                            .queryBuilder(p.getClass())
                            .identity(p)
                            .getQuery());
                        Object payloadId = repository.getIdentifier(persistent);
                        if (!pathId.equals(payloadId)) {
                            throw new BadRequestException();
                        }
                    } catch(IllegalStateException e) {
                        // ID in payload is null
                        throw new BadRequestException();
                    } catch(NoResultException e) {
                        // 404 if resource does not exist at all
                        repository.getById(p.getClass(), pathId);

                        // ID in payload does not match path
                        throw new BadRequestException();
                    }
                }
                authorization.authorize(
                    p, RequestMethod.valueOf(request.getMethod()));

                validator.validateAndThrow(p, PostAuthorization.class);
            }
        }

        Object result = ctx.proceed();
        if (result != null) {
            // Set authorization hints and validate
            if (result instanceof Collection<?> listResult) {
                if (!listResult.isEmpty()
                    && listResult.toArray()[0] instanceof BaseModel
                ) {
                    Collection<BaseModel> bmList =
                        (Collection<BaseModel>) listResult;
                    authorization.filter(bmList);

                    validator.validate(
                        bmList,
                        Warnings.class,
                        Notifications.class);
                }
            } else if (result instanceof BaseModel r) {
                authorization.filter(r);

                validator.validate(
                    r, Warnings.class, Notifications.class);
            }
        }
        if (this instanceof SamplerService) {
            LOG.debug("Finished processing request");
        }
        return result;
    }
}
