/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.auth;

import de.intevation.lada.model.BaseModel;
import de.intevation.lada.model.lada.BelongsToSample;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.master.MeasFacil;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;


public class ProbeIdAuthorizer extends BaseAuthorizer {

    public ProbeIdAuthorizer(Repository repository) {
        super(repository);
    }

    @Override
    public <T> String isAuthorizedReason(
        Object data,
        RequestMethod method,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        Integer id = ((BelongsToSample) data).getSampleId();
        Sample probe = repository.getById(Sample.class, id);
        return !isProbeReadOnly(id) && getAuthorization(userInfo, probe)
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

        BelongsToSample object = (BelongsToSample) data;
        Sample sample = repository.getById(Sample.class, object.getSampleId());

        String mfId = sample.getMeasFacilId();
        MeasFacil mst = repository.getById(MeasFacil.class, mfId);

        object.setOwner(
            userInfo.getNetzbetreiber().contains(mst.getNetworkId())
            && userInfo.belongsTo(mfId, sample.getApprLabId()));
    }
}
