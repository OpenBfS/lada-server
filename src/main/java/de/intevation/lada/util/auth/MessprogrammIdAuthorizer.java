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
import de.intevation.lada.model.lada.Mpg;
import de.intevation.lada.model.master.MeasFacil;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;


public class MessprogrammIdAuthorizer extends BaseAuthorizer {

    public MessprogrammIdAuthorizer(Repository repository) {
        super(repository);
    }

    @Override
    public <T> boolean isAuthorized(
        Object data,
        RequestMethod method,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        Method m;
        try {
            m = clazz.getMethod("getMpgId");
        } catch (NoSuchMethodException | SecurityException e1) {
            return false;
        }
        Integer id;
        try {
            id = (Integer) m.invoke(data);
        } catch (IllegalAccessException
            | IllegalArgumentException
            | InvocationTargetException e
        ) {
            return false;
        }
        Mpg messprogramm =
            repository.getByIdPlain(Mpg.class, id);
        String mstId = messprogramm.getMeasFacilId();
        if (mstId != null) {
            MeasFacil mst = repository.getByIdPlain(
                MeasFacil.class, mstId);
            if (userInfo.getFunktionenForNetzbetreiber(
                    mst.getNetworkId()).contains(4)
            ) {
                return true;
            }
        }
        return false;
    }

    @Override
    public <T> boolean isAuthorizedById(
        Object id,
        RequestMethod method,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        //TODO implement
        return false;
    }

    @Override
    public <T extends BaseModel> void setAuthAttrs(
        BaseModel data,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        try {
            Method getMessprogrammId = clazz.getMethod("getMpgId");
            Integer id = (Integer) getMessprogrammId.invoke(data);
            Mpg messprogramm = repository.getByIdPlain(
                Mpg.class, id);
            String mstId = messprogramm.getMeasFacilId();
            boolean owner = false;
            if (mstId != null) {
                MeasFacil mst = repository.getByIdPlain(
                    MeasFacil.class, mstId);
                if (userInfo.getFunktionenForNetzbetreiber(
                        mst.getNetworkId()).contains(4)
                ) {
                    owner = true;
                }
            }
            boolean readOnly = !owner;

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
