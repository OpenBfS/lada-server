/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.auth;

import de.intevation.lada.model.master.BelongsToNetwork;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;


public class NetzbetreiberAuthorizer extends BaseAuthorizer {

    public NetzbetreiberAuthorizer(Repository repository) {
        super(repository);
    }

    @Override
    public <T> String isAuthorizedReason(
        Object data,
        RequestMethod method,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        return userInfo.getFunktionenForNetzbetreiber(
            ((BelongsToNetwork) data).getNetworkId()).contains(4)
            ? null : I18N_KEY_FORBIDDEN;
    }
}
