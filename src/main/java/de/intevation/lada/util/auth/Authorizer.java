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

    <T> String isAuthorizedReason(
        Object data,
        RequestMethod method,
        UserInfo userInfo,
        Class<T> clazz);

    default <T> boolean isAuthorized(
        Object data,
        RequestMethod method,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        return isAuthorizedReason(data, method, userInfo, clazz) == null;
    }

    <T> boolean isAuthorizedById(
        Object id,
        RequestMethod method,
        UserInfo userInfo,
        Class<T> clazz);

    default <T extends BaseModel> void setAuthAttrs(
        List<BaseModel> data,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        for (BaseModel object: data) {
            setAuthAttrs(object, userInfo, clazz);
        }
    }

    <T extends BaseModel> void setAuthAttrs(
        BaseModel data,
        UserInfo userInfo,
        Class<T> clazz);

    boolean isProbeReadOnly(Integer probeId);

    boolean isMessungReadOnly(Integer messungsId);

}
