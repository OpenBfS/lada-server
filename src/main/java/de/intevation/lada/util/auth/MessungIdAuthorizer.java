/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.auth;

import de.intevation.lada.model.lada.BelongsToMeasm;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.master.MeasFacil;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;


class MessungIdAuthorizer extends Authorizer<BelongsToMeasm> {

    protected MessungAuthorizer messungAuthorizer;

    MessungIdAuthorizer(
        UserInfo userInfo,
        Repository repository
    ) {
        super(userInfo, repository);

        this.messungAuthorizer = new MessungAuthorizer(
            this.userInfo, this.repository);
    }

    @Override
    void authorize(
        BelongsToMeasm data,
        RequestMethod method
    ) throws AuthorizationException {
        messungAuthorizer.authorize(
            repository.getById(Measm.class, data.getMeasmId()),
            // Allow reading if measm is readable, everything else corresponds
            // to editing the measm
            method == RequestMethod.GET
            ? RequestMethod.GET
            : RequestMethod.PUT);
    }

    @Override
    void setAuthAttrs(BelongsToMeasm object) {
        // Set readonly flag
        super.setAuthAttrs(object);

        Sample probe = repository.getById(Measm.class, object.getMeasmId())
            .getSample();
        MeasFacil mst = repository.getById(
            MeasFacil.class, probe.getMeasFacilId());
        object.setOwner(
            userInfo.getNetzbetreiber().contains(mst.getNetworkId())
            && userInfo.belongsTo(
                probe.getMeasFacilId(), probe.getApprLabId()));
    }
}
