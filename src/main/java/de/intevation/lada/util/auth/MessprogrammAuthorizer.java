/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.auth;

import de.intevation.lada.model.lada.Mpg;
import de.intevation.lada.model.master.MeasFacil;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;


class MessprogrammAuthorizer extends Authorizer<Mpg> {

    MessprogrammAuthorizer(
        UserInfo userInfo,
        Repository repository
    ) {
        super(userInfo, repository);
    }

    @Override
    void authorize(
        Mpg messprogramm,
        RequestMethod method
    ) throws AuthorizationException {
        if (method == RequestMethod.GET) {
            // Allow read access to everybody
            return;
        }
        MeasFacil mst = repository.getById(
            MeasFacil.class, messprogramm.getMeasFacilId());
        if (!userInfo.getFunktionenForNetzbetreiber(
                mst.getNetworkId()).contains(4)) {
            throw new AuthorizationException(I18N_KEY_FORBIDDEN);
        }

        if (method == RequestMethod.DELETE
            && messprogramm.getReferenceCount() > 0
        ) {
            throw new AuthorizationException(I18N_KEY_CANNOTDELETE);
        }
    }
}
