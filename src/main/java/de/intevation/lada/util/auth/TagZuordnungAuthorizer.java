/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */

package de.intevation.lada.util.auth;

import javax.inject.Inject;

import de.intevation.lada.model.land.Messung;
import de.intevation.lada.model.land.Probe;
import de.intevation.lada.model.land.TagZuordnung;
import de.intevation.lada.model.stammdaten.Tag;
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.util.rest.Response;

/**
 * Authorizer class for TagZuordnung objects.
 *
 */
public class TagZuordnungAuthorizer extends BaseAuthorizer {

    @Inject
    ProbeAuthorizer probeAuthorizer;

    @Inject
    MessungAuthorizer messungAuthorizer;

    @Override
    public <T> boolean isAuthorized(
        Object data,
        RequestMethod method,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        switch (method) {
        case POST:
        case DELETE:
            TagZuordnung zuordnung = (TagZuordnung) data;
            Tag tag = repository.getByIdPlain(Tag.class, zuordnung.getTagId());
            if (tag == null) {
                return false;
            }
            String mstId = tag.getMstId();
            if (mstId == null) {
                // User tries to assign a global tag
                if (zuordnung.getMessungId() != null) {
                    return messungAuthorizer.isAuthorized(
                        repository.getByIdPlain(
                            Messung.class, zuordnung.getMessungId()),
                        RequestMethod.PUT,
                        userInfo,
                        Messung.class
                    );
                } else if (zuordnung.getProbeId() != null) {
                    return probeAuthorizer.isAuthorized(
                        repository.getByIdPlain(
                            Probe.class, zuordnung.getProbeId()),
                        RequestMethod.PUT,
                        userInfo,
                        Probe.class
                    );
                }
            } else if (userInfo.getMessstellen().contains(mstId)) {
                // Else check if it is the users private tag
                return true;
            }
        default:
            return false;
        }
    }

    @Override
    public <T> boolean isAuthorizedById(
        Object id,
        RequestMethod method,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        TagZuordnung zuordnung = repository.getByIdPlain(
            TagZuordnung.class, id);
        return isAuthorized(zuordnung, method, userInfo, clazz);
    }

    @Override
    public <T> Response filter(
        Response data,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        // Nothing to do
        return data;
    }
}
