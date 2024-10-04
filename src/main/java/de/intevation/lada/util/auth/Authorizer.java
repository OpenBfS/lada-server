/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.auth;

import de.intevation.lada.model.BaseModel;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;


abstract class Authorizer<T extends BaseModel> {

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
     * Authorize applying a method to data.
     *
     * @param data object to authorize
     * @param method method to authorize
     * @throws AuthorizationException if authorization does not succeed
     */
    abstract void authorize(
        T data,
        RequestMethod method
    ) throws AuthorizationException;

    /**
     * Authorize applying a method to data.
     *
     * @param data object to authorize
     * @param method method to authorize
     * @return true if authorization succeeds
     */
    boolean isAuthorized(
        T data,
        RequestMethod method
    ) {
        try {
            authorize(data, method);
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
        data.setReadonly(!isAuthorized(data, RequestMethod.PUT));
    }
}
