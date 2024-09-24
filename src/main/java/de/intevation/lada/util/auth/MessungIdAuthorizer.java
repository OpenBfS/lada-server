/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.auth;

import de.intevation.lada.model.BaseModel;
import de.intevation.lada.model.lada.BelongsToMeasm;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.master.MeasFacil;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;


public class MessungIdAuthorizer extends BaseAuthorizer {

    protected MessungAuthorizer messungAuthorizer;

    public MessungIdAuthorizer(Repository repository) {
        super(repository);

        this.messungAuthorizer = new MessungAuthorizer(repository);
    }

    @Override
    public String isAuthorizedReason(
        Object data,
        RequestMethod method,
        UserInfo userInfo
    ) {
        return messungAuthorizer.isAuthorizedReason(
            repository.getById(
                Measm.class, ((BelongsToMeasm) data).getMeasmId()),
            // Allow reading if measm is readable, everything else corresponds
            // to editing the measm
            method == RequestMethod.GET
            ? RequestMethod.GET
            : RequestMethod.PUT,
            userInfo);
    }

    @Override
    public void setAuthAttrs(
        BaseModel data,
        UserInfo userInfo
    ) {
        // Set readonly flag
        super.setAuthAttrs(data, userInfo);

        BelongsToMeasm object = (BelongsToMeasm) data;
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
