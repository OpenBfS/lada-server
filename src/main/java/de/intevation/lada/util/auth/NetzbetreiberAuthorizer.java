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
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;


public class NetzbetreiberAuthorizer extends BaseAuthorizer {

    public NetzbetreiberAuthorizer(Repository repository) {
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
            Method m = clazz.getMethod("getNetworkId");
            String id = (String) m.invoke(data);
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
        String netId = (String) id;
        /* If model is an ort instance check:
           - If user tries to edit: Has user function 4 and the same mstId
             as the ort? OR
           - If user tries to create: Can user edit probe objects?
        */
        if (clazz.isAssignableFrom(
                de.intevation.lada.model.master.Site.class)
        ) {
            return
            ((method == RequestMethod.PUT
              || method == RequestMethod.DELETE
            ) && userInfo.getFunktionenForNetzbetreiber(netId).contains(4)
              && userInfo.getNetzbetreiber().contains(netId))
            || method == RequestMethod.POST
            && userInfo.getNetzbetreiber().contains(netId);
        } else {
            return
            ((method == RequestMethod.PUT
            || method == RequestMethod.POST
            || method == RequestMethod.DELETE
            ) && userInfo.getFunktionenForNetzbetreiber(netId).contains(4));
        }
    }

    @Override
    public <T extends BaseModel> void setAuthAttrs(
        BaseModel data,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        data.setReadonly(
            !isAuthorized(data, RequestMethod.PUT, userInfo, clazz));
    }
}
