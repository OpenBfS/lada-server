/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.auth;

import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;


abstract class Authorizer<T extends Authorizable> {

    static final String I18N_KEY_FORBIDDEN = "forbidden";
    static final String I18N_KEY_CANNOTDELETE = "cannot_delete";

    protected UserInfo userInfo;

    protected Repository repository;

    /**
     * Call this in implementations extending this abstract class.
     */
    Authorizer(
        UserInfo userInfo,
        Repository repository
    ) {
        this.userInfo = userInfo;
        this.repository = repository;
    }

    /**
     * Authorize whether current user is allowed to
     * create (RequestMethod.POST), read (RequestMethod.GET),
     * update (RequestMethod.PUT) or delete (RequestMethod.DELETE)
     * the given entity instance.
     *
     * If given {@link RequestMethod} is PUT, requested changes have to be
     * applied to the given instance, before. The implementation ensures
     * to check whether the persistent state of the instance is allowed to be
     * changed as well as whether the requested changes are allowed to be
     * persisted.
     * The caller is responsible for providing identifiable data.
     *
     * @param data entity instance to authorize
     * @param method method to authorize
     * @throws AuthorizationException if authorization does not succeed
     */
    final void authorize(
        T data,
        RequestMethod method
    ) throws AuthorizationException {
        if (RequestMethod.PUT.equals(method)) {
            /* Avoid confusion in case entity identity is already present
               in persistence context */
            repository.entityManager().detach(data);

            // Authorize that persistent state can be changed
            @SuppressWarnings("unchecked")
            Class<T> entityClass = (Class<T>) data.getClass();
            T persistent = repository.getSingle(repository
                .queryBuilder(entityClass)
                .identity(data)
                .getQuery());
            authorizeMethod(persistent, RequestMethod.PUT);

            // Authorize requested changes
            authorizeMethod(data, RequestMethod.POST);
        } else {
            authorizeMethod(data, method);
        }
    }

    /**
     * Authorize whether the given entity instance in its given state
     * is allowed to be read (RequestMethod.GET), changed (RequestMethod.PUT),
     * persisted (RequestMethod.POST) or deleted (RequestMethod.DELETE).
     *
     * @param data entity instance to authorize
     * @param method method to authorize
     * @throws AuthorizationException if authorization does not succeed
     */
    abstract void authorizeMethod(
        T data,
        RequestMethod method
    ) throws AuthorizationException;

    /**
     * Authorize whether the given entity instance in its given state
     * is allowed to be read (RequestMethod.GET), changed (RequestMethod.PUT),
     * persisted (RequestMethod.POST) or deleted (RequestMethod.DELETE).
     *
     * @param data entity instance to authorize
     * @param method method to authorize
     * @return true if authorization succeeds
     */
    boolean isMethodAuthorized(
        T data,
        RequestMethod method
    ) {
        try {
            authorizeMethod(data, method);
            return true;
        } catch (AuthorizationException ae) {
            return false;
        }
    }

    /**
     * Set attributes providing hints about authorization.
     *
     * @param data object at which attributes should be set
     */
    void setAuthAttrs(T data) {
        data.setReadonly(!isMethodAuthorized(data, RequestMethod.PUT));
    }
}
