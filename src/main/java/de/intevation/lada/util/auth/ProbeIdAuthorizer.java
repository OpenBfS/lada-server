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
    public <T> boolean isAuthorized(
        Object data,
        RequestMethod method,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        Integer id;
        Method m;
        try {
            m = clazz.getMethod("getSampleId");
        } catch (NoSuchMethodException | SecurityException e1) {
            return false;
        }
        try {
            id = (Integer) m.invoke(data);
        } catch (IllegalAccessException
            | IllegalArgumentException
            | InvocationTargetException e
        ) {
            return false;
        }
        return isAuthorizedById(id, method, userInfo, clazz);
    }
    @Override
    public <T> boolean isAuthorizedById(
        Object id,
        RequestMethod method,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        Sample probe =
            repository.getByIdPlain(Sample.class, id);
        return !isProbeReadOnly((Integer) id)
            && getAuthorization(userInfo, probe);
    }

    @Override
    public <T extends BaseModel> void setAuthAttrs(
        BaseModel data,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        if (data == null) {
            return;
        }
        try {
            Method getProbeId = clazz.getMethod("getSampleId");
            Integer id = (Integer) getProbeId.invoke(data);
            Sample probe =
                (Sample) repository.getById(
                    Sample.class, id).getData();

            boolean readOnly = true;
            boolean owner = false;
            MeasFacil mst =
                repository.getByIdPlain(
                    MeasFacil.class, probe.getMeasFacilId());
            if (!userInfo.getNetzbetreiber().contains(
                    mst.getNetworkId())) {
                owner = false;
                readOnly = true;
            } else {
                if (userInfo.belongsTo(
                    probe.getMeasFacilId(), probe.getApprLabId())
                ) {
                    owner = true;
                } else {
                    owner = false;
                }
                readOnly = this.isProbeReadOnly(id);
            }

            Method setOwner = clazz.getMethod("setOwner", boolean.class);
            Method setReadonly = clazz.getMethod("setReadonly", boolean.class);
            setOwner.invoke(data, owner);
            setReadonly.invoke(data, readOnly);
        } catch (NoSuchMethodException
            | IllegalAccessException
            | InvocationTargetException e
        ) {
            throw new RuntimeException(e);
        }
    }
}
