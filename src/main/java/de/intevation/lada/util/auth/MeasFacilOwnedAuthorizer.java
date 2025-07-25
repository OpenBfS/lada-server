/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.auth;

import de.intevation.lada.model.lada.MeasFacilOwned;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;


class MeasFacilOwnedAuthorizer extends Authorizer<MeasFacilOwned> {

    MeasFacilOwnedAuthorizer(
        UserInfo userInfo,
        Repository repository
    ) {
        super(userInfo, repository);
    }

    @Override
    void authorizeMethod(
        MeasFacilOwned object,
        RequestMethod method
    ) throws AuthorizationException {
        if (!userInfo.getMessstellen().contains(object.getMeasFacilId())) {
            throw new AuthorizationException(I18N_KEY_FORBIDDEN);
        }
    }
}
