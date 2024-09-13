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
            return getAuthorization(
                userInfo,
                repository.getById(Measm.class, m.invoke(data)).getSample())
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
            Method getMessungsId = clazz.getMethod("getMeasmId");
            Integer id = (Integer) getMessungsId.invoke(data);
            Measm messung = repository.getById(
                Measm.class, id);
            Sample probe = repository.getById(
                Sample.class, messung.getSampleId());
            MeasFacil mst = repository.getById(
                MeasFacil.class, probe.getMeasFacilId());

            boolean owner =
                userInfo.getNetzbetreiber().contains(mst.getNetworkId())
                && userInfo.belongsTo(
                    probe.getMeasFacilId(), probe.getApprLabId());

            Method setOwner = clazz.getMethod("setOwner", boolean.class);
            setOwner.invoke(data, owner);
        } catch (NoSuchMethodException
            | IllegalAccessException
            | InvocationTargetException e
        ) {
            throw new RuntimeException(e);
        }
    }
}
