/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.auth;

import de.intevation.lada.model.master.Sampler;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;


class SamplerAuthorizer extends Authorizer<Sampler> {

    SamplerAuthorizer(
        UserInfo userInfo,
        Repository repository
    ) {
        super(userInfo, repository);
    }

    @Override
    void authorize(
        Sampler sampler,
        RequestMethod method
    ) throws AuthorizationException {
        if (!userInfo.getFunktionenForNetzbetreiber(
                sampler.getNetworkId()).contains(4)
        ) {
            throw new AuthorizationException(I18N_KEY_FORBIDDEN);
        }
        if (method == RequestMethod.DELETE
            && sampler.getReferenceCount() > 0
        ) {
            throw new AuthorizationException(I18N_KEY_CANNOTDELETE);
        }
    }
}
