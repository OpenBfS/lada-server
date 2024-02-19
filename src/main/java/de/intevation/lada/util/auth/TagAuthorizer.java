/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */

package de.intevation.lada.util.auth;

import de.intevation.lada.model.BaseModel;
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
    public <T> boolean isAuthorized(
        Object data,
        RequestMethod method,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        Tag tag = (Tag) data;
        switch (tag.getTagType()) {
            // Netzbetreiber tags may only be edited by stamm users
            case Tag.TAG_TYPE_NETZBETREIBER:
                switch (method) {
                case GET:
                case POST:
                    return userInfo.getNetzbetreiber().contains(
                        tag.getNetworkId());
                case PUT:
                case DELETE:
                    return userInfo.getFunktionenForNetzbetreiber(
                        tag.getNetworkId()).contains(4);
                default:
                    return false;
                }
            // Tags my only be edited by members of the referenced Messstelle
            case Tag.TAG_TYPE_MST:
                return userInfo.getMessstellen().contains(tag.getMeasFacilId());
            // Global tags (and anything unknown) can not be edited
            default:
                return false;
        }
    }

    @Override
    public <T> boolean isAuthorizedById(Object id, RequestMethod method,
        UserInfo userInfo, Class<T> clazz) {
        Tag tag = repository.getByIdPlain(Tag.class, id);
        return isAuthorized(tag, method, userInfo, clazz);
    }

    @Override
    public <T extends BaseModel> void setAuthAttrs(
        BaseModel data,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        if (data instanceof Tag) {
            setAuthData(userInfo, (Tag) data);
        }
    }

    private void setAuthData(UserInfo userInfo, Tag tag) {
        tag.setReadonly(!isAuthorized(tag, RequestMethod.PUT,
            userInfo, Tag.class));
    }
}
