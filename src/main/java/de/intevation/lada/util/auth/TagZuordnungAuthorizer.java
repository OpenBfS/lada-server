/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */

package de.intevation.lada.util.auth;

import de.intevation.lada.model.BaseModel;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.lada.TagLink;
import de.intevation.lada.model.master.Tag;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;


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
    public <T> String isAuthorizedReason(
        Object data,
        RequestMethod method,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        switch (method) {
        case POST:
        case DELETE:
            TagLink zuordnung = (TagLink) data;
            Tag tag = repository.getById(Tag.class, zuordnung.getTagId());

            switch (tag.getTagType()) {
            case Tag.TAG_TYPE_GLOBAL:
                if (zuordnung.getMeasmId() != null) {
                    return messungAuthorizer.isAuthorizedReason(
                        repository.getById(
                            Measm.class, zuordnung.getMeasmId()),
                        RequestMethod.PUT,
                        userInfo,
                        Measm.class
                    );
                }
                if (zuordnung.getSampleId() != null) {
                    return probeAuthorizer.isAuthorizedReason(
                        repository.getById(
                            Sample.class, zuordnung.getSampleId()),
                        RequestMethod.PUT,
                        userInfo,
                        Sample.class
                    );
                }
                // Should not happen because either Messung or Sample is assigned
                return I18N_KEY_FORBIDDEN;
            case Tag.TAG_TYPE_NETZBETREIBER:
                return userInfo.getNetzbetreiber().contains(tag.getNetworkId())
                    ? null : I18N_KEY_FORBIDDEN;
            case Tag.TAG_TYPE_MST:
                return userInfo.getMessstellen().contains(tag.getMeasFacilId())
                    ? null : I18N_KEY_FORBIDDEN;
            default:
                throw new IllegalArgumentException("Unknown tag type");
            }
        default:
            return I18N_KEY_FORBIDDEN;
        }
    }

    @Override
    public <T> boolean isAuthorizedById(
        Object id,
        RequestMethod method,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        TagLink zuordnung = repository.getById(
            TagLink.class, id);
        return isAuthorized(zuordnung, method, userInfo, clazz);
    }

    @Override
    public <T extends BaseModel> void setAuthAttrs(
        BaseModel data,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        // Nothing to do
    }
}
