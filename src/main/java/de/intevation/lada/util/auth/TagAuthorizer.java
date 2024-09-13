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
public class TagAuthorizer extends BaseAuthorizer {

    public TagAuthorizer(Repository repository) {
        super(repository);
    }

    @Override
    public <T> String isAuthorizedReason(
        Object data,
        RequestMethod method,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        Tag tag = (Tag) data;
        if (tag.getNetworkId() != null) {
            // Netzbetreiber tags may only be edited by stamm users
            switch (method) {
            case GET:
            case POST:
                return userInfo.getNetzbetreiber().contains(
                    tag.getNetworkId())
                    ? null : I18N_KEY_FORBIDDEN;
            case PUT:
            case DELETE:
                return userInfo.getFunktionenForNetzbetreiber(
                    tag.getNetworkId()).contains(4)
                    ? null : I18N_KEY_FORBIDDEN;
            default:
                return I18N_KEY_FORBIDDEN;
            }
        } else if (tag.getMeasFacilId() != null) {
            // Tags my only be edited by members of the referenced Messstelle
            return userInfo.getMessstellen().contains(tag.getMeasFacilId())
                ? null : I18N_KEY_FORBIDDEN;
        } else {
            // Global tags (and anything unknown) can not be edited
            return I18N_KEY_FORBIDDEN;
        }
    }
}
