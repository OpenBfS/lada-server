/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.auth;

import de.intevation.lada.model.master.Site;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;


public class SiteAuthorizer extends BaseAuthorizer {

    public SiteAuthorizer(Repository repository) {
        super(repository);
    }

    @Override
    public <T> String isAuthorizedReason(
        Object data,
        RequestMethod method,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        String netId = ((Site) data).getNetworkId();
        return (method == RequestMethod.PUT || method == RequestMethod.DELETE)
            && userInfo.getFunktionenForNetzbetreiber(netId).contains(4)
            && userInfo.getNetzbetreiber().contains(netId)
            || method == RequestMethod.POST
            && userInfo.getNetzbetreiber().contains(netId)
            ? null : I18N_KEY_FORBIDDEN;
    }

    @Override
    public <T> boolean isAuthorizedById(
        Object id,
        RequestMethod method,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        return isAuthorized(
            repository.getByIdPlain(Site.class, id), method, userInfo, clazz);
    }
}
