/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.auth;

import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.StatusProt;
import de.intevation.lada.model.master.StatusMp;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;


public class StatusProtAuthorizer extends MessungIdAuthorizer {

    public StatusProtAuthorizer(Repository repository) {
        super(repository);
    }

    @Override
    public String isAuthorizedReason(
        Object data,
        RequestMethod method,
        UserInfo userInfo
    ) {
        StatusProt status = (StatusProt) data;
        switch (method) {
        case PUT:
        case DELETE:
            // StatusProt instances should never be edited or deleted
            return I18N_KEY_FORBIDDEN;
        case POST:
            // Is user authorized to edit status at all?
            Measm measm = repository.getById(Measm.class, status.getMeasmId());
            messungAuthorizer.setAuthAttrs(measm, userInfo);
            if (!measm.getStatusEdit()) {
                return I18N_KEY_FORBIDDEN;
            }
            // Check if the user is allowed to change to the requested
            // status_kombi
            int lev = repository.getById(StatusMp.class, status.getStatusMpId())
                .getStatusLev().getId();
            if (userInfo.getFunktionenForMst(status.getMeasFacilId())
                    .contains(lev)
                && (lev == 1 && measm.getStatusEditMst()
                    || lev == 2 && measm.getStatusEditLand()
                    || lev == 3 && measm.getStatusEditLst())
            ) {
                return null;
            }
            return I18N_KEY_FORBIDDEN;
        default:
            return messungAuthorizer.isAuthorizedReason(
                repository.getById(Measm.class, status.getMeasmId()),
                method,
                userInfo);
        }
    }
}
