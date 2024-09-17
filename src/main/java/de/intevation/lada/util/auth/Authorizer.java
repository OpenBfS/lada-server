/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.auth;

import java.util.List;

import de.intevation.lada.model.BaseModel;
import de.intevation.lada.util.rest.RequestMethod;

public interface Authorizer {

    String isAuthorizedReason(
        Object data,
        RequestMethod method,
        UserInfo userInfo);

    default boolean isAuthorized(
        Object data,
        RequestMethod method,
        UserInfo userInfo
    ) {
        return isAuthorizedReason(data, method, userInfo) == null;
    }

    default void setAuthAttrs(
        List<BaseModel> data,
        UserInfo userInfo
    ) {
        for (BaseModel object: data) {
            setAuthAttrs(object, userInfo);
        }
    }

    default void setAuthAttrs(
        BaseModel data,
        UserInfo userInfo
    ) {
        data.setReadonly(!isAuthorized(data, RequestMethod.PUT, userInfo));
    }
}
