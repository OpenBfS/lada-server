/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */

package de.intevation.lada.util.auth;

import de.intevation.lada.model.BaseModel;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.TagLinkMeasm;
import de.intevation.lada.model.master.Tag;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;


/**
 * Authorizer class for TagZuordnung objects.
 *
 */
public class TagLinkMeasmAuthorizer extends BaseAuthorizer {

    MessungAuthorizer messungAuthorizer;

    public TagLinkMeasmAuthorizer(Repository repository) {
        super(repository);
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
            TagLinkMeasm zuordnung = (TagLinkMeasm) data;
            Tag tag = repository.getById(Tag.class, zuordnung.getTagId());

            if (tag.getNetworkId() == null && tag.getMeasFacilId() == null) {
                return messungAuthorizer.isAuthorizedReason(
                    repository.getById(
                        Measm.class, zuordnung.getMeasmId()),
                    RequestMethod.PUT,
                    userInfo,
                    Measm.class
                );
            } else if (tag.getNetworkId() != null) {
                return userInfo.getNetzbetreiber().contains(tag.getNetworkId())
                    ? null : I18N_KEY_FORBIDDEN;
            } else if (tag.getMeasFacilId() != null) {
                return userInfo.getMessstellen().contains(tag.getMeasFacilId())
                    ? null : I18N_KEY_FORBIDDEN;
            }
        default:
            return I18N_KEY_FORBIDDEN;
        }
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
