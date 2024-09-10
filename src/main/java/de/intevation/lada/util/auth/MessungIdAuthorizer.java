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
        try {
            Method m = clazz.getMethod("getMeasmId");
            Integer id = (Integer) m.invoke(data);
            return isAuthorizedById(id, method, userInfo, clazz)
                ? null : I18N_KEY_FORBIDDEN;
        } catch (NoSuchMethodException
            | IllegalAccessException
            | InvocationTargetException e
        ) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> boolean isAuthorizedById(
        Object id,
        RequestMethod method,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        Measm messung = repository.getById(Measm.class, id);
        Sample probe = repository.getById(
            Sample.class, messung.getSampleId());
        if (messung.getStatusProt() == null) {
            return false;
        }
        return ((method == RequestMethod.POST
            || method == RequestMethod.PUT
            || method == RequestMethod.DELETE)
            && getAuthorization(userInfo, probe));
    }

    @Override
    public <T extends BaseModel> void setAuthAttrs(
        BaseModel data,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        try {
            Method getMessungsId = clazz.getMethod("getMeasmId");
            Integer id = (Integer) getMessungsId.invoke(data);
            Measm messung = repository.getById(
                Measm.class, id);
            Sample probe = repository.getById(
                Sample.class, messung.getSampleId());

            boolean readOnly = true;
            boolean owner = false;
            MeasFacil mst =
                repository.getById(
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
                readOnly = this.isMessungReadOnly(messung.getId());
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
