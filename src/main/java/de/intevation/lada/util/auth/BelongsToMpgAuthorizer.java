/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.auth;

import de.intevation.lada.model.lada.BelongsToMpg;
import de.intevation.lada.model.lada.Mpg;
import de.intevation.lada.model.master.MeasFacil;
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.util.data.Repository;


class BelongsToMpgAuthorizer extends Authorizer<BelongsToMpg> {

    BelongsToMpgAuthorizer(
        UserInfo userInfo,
        Repository repository
    ) {
        super(userInfo, repository);
    }

    @Override
    void authorizeMethod(
        BelongsToMpg object,
        RequestMethod method
    ) throws AuthorizationException {
        MeasFacil mst = repository.getById(
            MeasFacil.class,
            repository.getById(Mpg.class, object.getMpgId()).getMeasFacilId());
        if (userInfo.getFunktionenForNetzbetreiber(
                mst.getNetworkId()).contains(4)
        ) {
            return;
        }
        throw new AuthorizationException(I18N_KEY_FORBIDDEN);
    }

    @Override
    void setAuthAttrs(BelongsToMpg object) {
        // Set readonly flag
        super.setAuthAttrs(object);

        object.setOwner(!object.isReadonly());
    }
}
