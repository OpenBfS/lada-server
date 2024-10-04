/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */

package de.intevation.lada.util.auth;

import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.TagLinkMeasm;
import de.intevation.lada.model.master.Tag;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;


/**
 * Authorizer class for TagZuordnung objects.
 *
 */
class TagLinkMeasmAuthorizer extends Authorizer<TagLinkMeasm> {

    Authorizer<Measm> messungAuthorizer;

    TagLinkMeasmAuthorizer(
        UserInfo userInfo,
        Repository repository
    ) {
        super(userInfo, repository);

        this.messungAuthorizer = new MessungAuthorizer(
            this.userInfo, this.repository);
    }

    @Override
    @SuppressWarnings("fallthrough")
    void authorize(
        TagLinkMeasm zuordnung,
        RequestMethod method
    ) throws AuthorizationException {
        switch (method) {
        case POST:
        case DELETE:
            Tag tag = repository.getById(Tag.class, zuordnung.getTagId());

            if (tag.getNetworkId() == null && tag.getMeasFacilId() == null) {
                messungAuthorizer.authorize(
                    repository.getById(
                        Measm.class, zuordnung.getMeasmId()),
                    RequestMethod.PUT
                );
                return;
            } else if (tag.getNetworkId() != null) {
                if (userInfo.getNetzbetreiber().contains(tag.getNetworkId())) {
                    return;
                }
            } else if (tag.getMeasFacilId() != null) {
                if (userInfo.getMessstellen().contains(tag.getMeasFacilId())) {
                    return;
                }
            }
        default:
            throw new AuthorizationException(I18N_KEY_FORBIDDEN);
        }
    }
}
