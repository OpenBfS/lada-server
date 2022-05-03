/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */

package de.intevation.lada.util.auth;

import java.util.ArrayList;
import java.util.List;

import de.intevation.lada.model.stammdaten.Tag;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.util.rest.Response;

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
        switch (tag.getTypId()) {
            //Netzbetreiber tags may only be edited by stamm users
            case Tag.TAG_TYPE_NETZBETREIBER:
                String netzbetreiber = tag.getNetzbetreiberId() != null
                    ? tag.getNetzbetreiberId()
                    : tag.getNetzbetreiber().getId();
                return userInfo.getFunktionenForNetzbetreiber(
                    netzbetreiber).contains(4);
            // Tags my only be edited by members of the referenced Messstelle
            case Tag.TAG_TYPE_MST:
                return userInfo.getMessstellen().contains(tag.getMstId());
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

    @SuppressWarnings("unchecked")
    @Override
    public <T> Response filter(
        Response data,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        if (data.getData() instanceof List<?>) {
            List<Tag> tags = new ArrayList<Tag>();
            for (Tag tag: (List<Tag>) data.getData()) {
                tags.add(setAuthData(userInfo, tag));
            }
        } else if (data.getData() instanceof Tag) {
            Tag tag = (Tag) data.getData();
            data.setData(setAuthData(userInfo, tag));
        }
        return data;
    }

    private Tag setAuthData(UserInfo userInfo, Tag tag) {
        tag.setReadonly(!isAuthorized(tag, RequestMethod.POST,
            userInfo, Tag.class));
        return tag;
    }
}
