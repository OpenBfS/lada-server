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
    public String isAuthorizedReason(
        Object data,
        RequestMethod method,
        UserInfo userInfo
    ) {
        Site site = (Site) data;
        String netId = site.getNetworkId();
        if ((method == RequestMethod.PUT || method == RequestMethod.DELETE)
            && (!userInfo.getFunktionenForNetzbetreiber(netId).contains(4)
                || !userInfo.getNetzbetreiber().contains(netId))
            || method == RequestMethod.POST
                && !userInfo.getNetzbetreiber().contains(netId)
        ) {
            return I18N_KEY_FORBIDDEN;
        }
        if (method == RequestMethod.DELETE
            && (site.getReferenceCount() > 0 || site.getReferenceCountMp() > 0)
        ) {
            return I18N_KEY_CANNOTDELETE;
        }
        return null;
    }
}
