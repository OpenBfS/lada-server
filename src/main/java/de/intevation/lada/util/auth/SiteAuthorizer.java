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


class SiteAuthorizer extends Authorizer<Site> {

    SiteAuthorizer(
        UserInfo userInfo,
        Repository repository
    ) {
        super(userInfo, repository);
    }

    @Override
    void authorize(
        Site site,
        RequestMethod method
    ) throws AuthorizationException {
        String netId = site.getNetworkId();
        if ((method == RequestMethod.PUT || method == RequestMethod.DELETE)
            && (!userInfo.getFunktionenForNetzbetreiber(netId).contains(4)
                || !userInfo.getNetzbetreiber().contains(netId))
            || method == RequestMethod.POST
                && !userInfo.getNetzbetreiber().contains(netId)
        ) {
            throw new AuthorizationException(I18N_KEY_FORBIDDEN);
        }
        if (method == RequestMethod.DELETE
            && (site.getReferenceCount() > 0 || site.getReferenceCountMp() > 0)
        ) {
            throw new AuthorizationException(I18N_KEY_CANNOTDELETE);
        }
    }
}
