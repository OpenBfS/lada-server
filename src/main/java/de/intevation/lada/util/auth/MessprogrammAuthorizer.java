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


public class MessprogrammAuthorizer extends BaseAuthorizer {

    public MessprogrammAuthorizer(Repository repository) {
        super(repository);
    }

    @Override
    public String isAuthorizedReason(
        Object data,
        RequestMethod method,
        UserInfo userInfo
    ) {
        if (method == RequestMethod.GET) {
            // Allow read access to everybody
            return null;
        }
        Mpg messprogramm = (Mpg) data;
        MeasFacil mst = repository.getById(
            MeasFacil.class, messprogramm.getMeasFacilId());
        if (!userInfo.getFunktionenForNetzbetreiber(
                mst.getNetworkId()).contains(4)) {
            return I18N_KEY_FORBIDDEN;
        }

        if (method == RequestMethod.DELETE
            && messprogramm.getReferenceCount() > 0
        ) {
            return I18N_KEY_CANNOTDELETE;
        }

        return null;
    }
}
