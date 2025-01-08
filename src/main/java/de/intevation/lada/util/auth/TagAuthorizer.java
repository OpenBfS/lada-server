/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */

package de.intevation.lada.util.auth;

import de.intevation.lada.model.master.Tag;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;


/**
 * Authorizer class for tags.
 *
 * @author Alexander Woestmann <awoestmann@intevation.de>
 */
class TagAuthorizer extends Authorizer<Tag> {

    TagAuthorizer(
        UserInfo userInfo,
        Repository repository
    ) {
        super(userInfo, repository);
    }

    @Override
    void authorize(
        Tag tag,
        RequestMethod method
    ) throws AuthorizationException {
        if (tag.getNetworkId() != null) {
            // Netzbetreiber tags may only be edited by stamm users
            switch (method) {
            case GET:
            case POST:
                if (userInfo.getNetzbetreiber().contains(tag.getNetworkId())) {
                    return;
                }
                throw new AuthorizationException(I18N_KEY_FORBIDDEN);
            case PUT:
            case DELETE:
                if (userInfo.getFunktionenForNetzbetreiber(
                    tag.getNetworkId()).contains(4)
                ) {
                    return;
                }
                throw new AuthorizationException(I18N_KEY_FORBIDDEN);
            default:
                throw new AuthorizationException(I18N_KEY_FORBIDDEN);
            }
        } else if (tag.getMeasFacilId() != null
            // Tags my only be edited by members of the referenced Messstelle
            && userInfo.getMessstellen().contains(tag.getMeasFacilId())
        ) {
            return;
        }
        // Global tags (and anything unknown) can not be edited
        throw new AuthorizationException(I18N_KEY_FORBIDDEN);
    }
}
