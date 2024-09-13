/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.auth;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import de.intevation.lada.model.BaseModel;
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
        try {
            Method m = clazz.getMethod("getSampleId");
            Integer id = (Integer) m.invoke(data);
            Sample probe = repository.getById(Sample.class, id);
            return !isProbeReadOnly(id) && getAuthorization(userInfo, probe)
                ? null : I18N_KEY_FORBIDDEN;
        } catch (NoSuchMethodException
            | IllegalAccessException
            | InvocationTargetException e
        ) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T extends BaseModel> void setAuthAttrs(
        BaseModel data,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        // Set readonly flag
        super.setAuthAttrs(data, userInfo, clazz);

        try {
            Method getProbeId = clazz.getMethod("getSampleId");
            Sample sample = repository.getById(
                Sample.class, getProbeId.invoke(data));

            String mfId = sample.getMeasFacilId();
            MeasFacil mst = repository.getById(MeasFacil.class, mfId);

            Method setOwner = clazz.getMethod("setOwner", boolean.class);
            setOwner.invoke(
                data,
                userInfo.getNetzbetreiber().contains(mst.getNetworkId())
                && userInfo.belongsTo(mfId, sample.getApprLabId()));
        } catch (NoSuchMethodException
            | IllegalAccessException
            | InvocationTargetException e
        ) {
            throw new RuntimeException(e);
        }
    }
}
