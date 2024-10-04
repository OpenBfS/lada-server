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


class NetzbetreiberAuthorizer extends Authorizer<BelongsToNetwork> {

    NetzbetreiberAuthorizer(
        UserInfo userInfo,
        Repository repository
    ) {
        super(userInfo, repository);
    }

    @Override
    void authorize(
        BelongsToNetwork data,
        RequestMethod method
    ) throws AuthorizationException {
        if (userInfo.getFunktionenForNetzbetreiber(data.getNetworkId())
                .contains(4)
        ) {
            return;
        }
        throw new AuthorizationException(I18N_KEY_FORBIDDEN);
    }
}
