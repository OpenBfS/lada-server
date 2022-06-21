/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.auth;

import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.util.rest.Response;

/**
 * Interface for authorization in the lada application.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public interface Authorization {
    UserInfo getInfo();

    <T> Response filter(
        Response data, Class<T> clazz);

    <T> boolean isAuthorized(
        Object data,
        RequestMethod method,
        Class<T> clazz);

    <T> boolean isAuthorizedById(
        Object id,
        RequestMethod method,
        Class<T> clazz);

    boolean isProbeReadOnly(Integer probeId);

    boolean isMessungReadOnly(Integer messungId);
}
