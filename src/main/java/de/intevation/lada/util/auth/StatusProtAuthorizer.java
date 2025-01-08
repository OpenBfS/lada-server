/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.auth;

import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.lada.StatusProt;
import de.intevation.lada.model.master.MeasFacil;
import de.intevation.lada.model.master.StatusMp;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;


class StatusProtAuthorizer extends Authorizer<StatusProt> {

    private Authorizer<Measm> messungAuthorizer;

    StatusProtAuthorizer(
        UserInfo userInfo,
        Repository repository
    ) {
        super(userInfo, repository);

        this.messungAuthorizer = new MessungAuthorizer(userInfo, repository);
    }

    @Override
    void authorize(
        StatusProt status,
        RequestMethod method
    ) throws AuthorizationException {
        switch (method) {
        case PUT:
        case DELETE:
            // StatusProt instances should never be edited or deleted
            throw new AuthorizationException(I18N_KEY_FORBIDDEN);
        case POST:
            // Is user authorized to edit status at all?
            Measm measm = repository.getById(Measm.class, status.getMeasmId());
            messungAuthorizer.setAuthAttrs(measm);
            if (!measm.getStatusEdit()) {
                throw new AuthorizationException(I18N_KEY_FORBIDDEN);
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
                return;
            }
            throw new AuthorizationException(I18N_KEY_FORBIDDEN);
        default:
            messungAuthorizer.authorize(
                repository.getById(Measm.class, status.getMeasmId()),
                method);
        }
    }

    @Override
    void setAuthAttrs(StatusProt status) {
        // Set readonly flag
        super.setAuthAttrs(status);

        Sample probe = repository.getById(Measm.class, status.getMeasmId())
            .getSample();
        MeasFacil mst = repository.getById(
            MeasFacil.class, probe.getMeasFacilId());
        status.setOwner(
            userInfo.getNetzbetreiber().contains(mst.getNetworkId())
            && userInfo.belongsTo(
                probe.getMeasFacilId(), probe.getApprLabId()));
    }
}
