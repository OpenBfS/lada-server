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
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.util.rest.Response;

/**
 * Authorizer class for tags.
 *
 * @author Alexander Woestmann <awoestmann@intevation.de>
 */
public class TagAuthorizer extends BaseAuthorizer {

    @Override
    public <T> boolean isAuthorized(Object data, RequestMethod method,
        UserInfo userInfo, Class<T> clazz) {
        Tag tag = (Tag) data;
        int typ = tag.getTyp().getId();
        switch (typ) {
            //Global tags can not be edited
            case 1: return false;
            //Netzbetreiber tags may only be edited by stamm users
            case 2:
                List<Integer> funktionen = userInfo.getFunktionenForNetzbetreiber(tag.getNetzbetreiber().getId());
                for (int i = 0; i < funktionen.size(); i++) {
                    if (funktionen.get(i).intValue() == 4) {
                        return true;
                    }
                }
                break;
            //Messstelle tags my only be edited by members of the respective messstelle
            case 3:
                for (int i = 0; i < userInfo.getMessstellen().size(); i++) {
                    if (userInfo.getMessstellen().contains(tag.getMstId())) {
                        return true;
                    }
                }
                break;
            default: return false;
        }
        return false;
    }

    @Override
    public <T> boolean isAuthorizedById(Object id, RequestMethod method,
        UserInfo userInfo, Class<T> clazz) {
        Tag tag = repository.getByIdPlain(Tag.class, id);
        return isAuthorized(tag, method, userInfo, clazz);
    }

    @Override
    public <T> Response filter(Response data, UserInfo userInfo,
        Class<T> clazz) {
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
        tag.setReadonly(isAuthorized(tag, RequestMethod.POST,
            userInfo, Tag.class));
        return tag;
    }
}
