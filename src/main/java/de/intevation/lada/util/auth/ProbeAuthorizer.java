/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.auth;

import de.intevation.lada.model.BaseModel;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.master.MeasFacil;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;


public class ProbeAuthorizer extends BaseAuthorizer {

    public ProbeAuthorizer(Repository repository) {
        super(repository);
    }

    @Override
    public <T> String isAuthorizedReason(
        Object data,
        RequestMethod method,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        Sample probe = (Sample) data;
        if (method == RequestMethod.PUT
            || method == RequestMethod.DELETE) {
            return !isProbeReadOnly(probe.getId())
                && getAuthorization(userInfo, probe)
                ? null : I18N_KEY_FORBIDDEN;
        }
        return getAuthorization(userInfo, probe)
            ? null : I18N_KEY_FORBIDDEN;
    }

    @Override
    public <T extends BaseModel> void setAuthAttrs(
        BaseModel data,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        if (data instanceof Sample) {
            setAuthData(userInfo, (Sample) data);
        }
    }

    /**
     * Set authorization data for the current probe object.
     *
     * @param userInfo  The user information.
     * @param probe     The probe object.
     */
    private void setAuthData(UserInfo userInfo, Sample probe) {
        MeasFacil mst =
            repository.getById(
                MeasFacil.class, probe.getMeasFacilId());
        if (!userInfo.getNetzbetreiber().contains(mst.getNetworkId())) {
            probe.setOwner(false);
            probe.setReadonly(true);
            return;
        }
        probe.setOwner(
            userInfo.belongsTo(probe.getMeasFacilId(), probe.getApprLabId()));
        probe.setReadonly(this.isProbeReadOnly(probe.getId()));
    }
}
