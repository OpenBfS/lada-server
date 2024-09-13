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


/**
 * Interface for authorization in the lada application.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public interface Authorization {
    UserInfo getInfo();

    default <T extends BaseModel> List<T> filter(List<T> data, Class<T> clazz) {
        for (T object: data) {
            filter(object, clazz);
        }
        return data;
    }

    <T extends BaseModel> T filter(
        T data, Class<T> clazz);

    <T> void authorize(
        Object data,
        RequestMethod method,
        Class<T> clazz);

    <T> boolean isAuthorized(
        Object data,
        RequestMethod method,
        Class<T> clazz);

    boolean isProbeReadOnly(Integer probeId);

    boolean isMessungReadOnly(Integer messungId);
}
