/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.auth;

import de.intevation.lada.model.master.Sampler;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;


public class SamplerAuthorizer extends BaseAuthorizer {

    public SamplerAuthorizer(Repository repository) {
        super(repository);
    }

    @Override
    public <T> String isAuthorizedReason(
        Object data,
        RequestMethod method,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        Sampler sampler = (Sampler) data;
        if (!userInfo.getFunktionenForNetzbetreiber(
                sampler.getNetworkId()).contains(4)
        ) {
            return I18N_KEY_FORBIDDEN;
        }
        if (method == RequestMethod.DELETE
            && sampler.getReferenceCount() > 0
        ) {
            return I18N_KEY_CANNOTDELETE;
        }
        return null;
    }

    @Override
    public <T> boolean isAuthorizedById(
        Object id,
        RequestMethod method,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        return isAuthorized(repository.getById(
                Sampler.class, id), method, userInfo, clazz);
    }
}
