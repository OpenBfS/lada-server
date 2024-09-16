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

    public MessungIdAuthorizer(Repository repository) {
        super(repository);
    }

    @Override
    public <T> String isAuthorizedReason(
        Object data,
        RequestMethod method,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        return getAuthorization(
            userInfo,
            repository.getById(
                Measm.class, ((BelongsToMeasm) data).getMeasmId()).getSample())
            ? null : I18N_KEY_FORBIDDEN;
    }

    @Override
    public <T extends BaseModel> void setAuthAttrs(
        BaseModel data,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        // Set readonly flag
        super.setAuthAttrs(data, userInfo, clazz);

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
