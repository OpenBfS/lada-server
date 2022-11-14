/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */

package de.intevation.lada.util.auth;

import de.intevation.lada.model.land.Messung;
import de.intevation.lada.model.land.Sample;
import de.intevation.lada.model.land.TagZuordnung;
import de.intevation.lada.model.stammdaten.Tag;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.util.rest.Response;

/**
 * Authorizer class for TagZuordnung objects.
 *
 */
public class TagZuordnungAuthorizer extends BaseAuthorizer {

    ProbeAuthorizer probeAuthorizer;
    MessungAuthorizer messungAuthorizer;

    public TagZuordnungAuthorizer(Repository repository) {
        super(repository);
        probeAuthorizer = new ProbeAuthorizer(repository);
        messungAuthorizer = new MessungAuthorizer(repository);
    }

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

            switch (tag.getTypId()) {
            case Tag.TAG_TYPE_GLOBAL:
                if (zuordnung.getMessungId() != null) {
                    return messungAuthorizer.isAuthorized(
                        repository.getByIdPlain(
                            Messung.class, zuordnung.getMessungId()),
                        RequestMethod.PUT,
                        userInfo,
                        Messung.class
                    );
                }
                if (zuordnung.getProbeId() != null) {
                    return probeAuthorizer.isAuthorized(
                        repository.getByIdPlain(
                            Sample.class, zuordnung.getProbeId()),
                        RequestMethod.PUT,
                        userInfo,
                        Sample.class
                    );
                }
                // Should not happen because either Messung or Sample is assigned
                return false;
            case Tag.TAG_TYPE_NETZBETREIBER:
                return userInfo.getNetzbetreiber().contains(
                    tag.getNetzbetreiberId());
            case Tag.TAG_TYPE_MST:
                return userInfo.getMessstellen().contains(tag.getMstId());
            default:
                throw new IllegalArgumentException("Unknown tag type");
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
