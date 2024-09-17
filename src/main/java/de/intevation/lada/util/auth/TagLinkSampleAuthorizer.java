/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */

package de.intevation.lada.util.auth;

import de.intevation.lada.model.BaseModel;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.lada.TagLinkSample;
import de.intevation.lada.model.master.Tag;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;


/**
 * Authorizer class for TagZuordnung objects.
 *
 */
public class TagLinkSampleAuthorizer extends BaseAuthorizer {

    ProbeAuthorizer probeAuthorizer;

    public TagLinkSampleAuthorizer(Repository repository) {
        super(repository);
        probeAuthorizer = new ProbeAuthorizer(repository);
    }

    @Override
    public String isAuthorizedReason(
        Object data,
        RequestMethod method,
        UserInfo userInfo
    ) {
        switch (method) {
        case POST:
        case DELETE:
            TagLinkSample zuordnung = (TagLinkSample) data;
            Tag tag = repository.getById(Tag.class, zuordnung.getTagId());

            if (tag.getNetworkId() == null && tag.getMeasFacilId() == null) {
                return probeAuthorizer.isAuthorizedReason(
                    repository.getById(
                        Sample.class, zuordnung.getSampleId()),
                    RequestMethod.PUT,
                    userInfo
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
    public void setAuthAttrs(
        BaseModel data,
        UserInfo userInfo
    ) {
        // Nothing to do
    }
}
