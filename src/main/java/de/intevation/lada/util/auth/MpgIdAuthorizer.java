/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.auth;

import de.intevation.lada.model.BaseModel;
import de.intevation.lada.model.lada.BelongsToMpg;
import de.intevation.lada.model.lada.Mpg;
import de.intevation.lada.model.master.MeasFacil;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;


public class MpgIdAuthorizer extends BaseAuthorizer {

    public MpgIdAuthorizer(Repository repository) {
        super(repository);
    }

    @Override
    public String isAuthorizedReason(
        Object data,
        RequestMethod method,
        UserInfo userInfo
    ) {
        BelongsToMpg object = (BelongsToMpg) data;
        MeasFacil mst = repository.getById(
            MeasFacil.class,
            repository.getById(Mpg.class, object.getMpgId()).getMeasFacilId());
        if (userInfo.getFunktionenForNetzbetreiber(
                mst.getNetworkId()).contains(4)
        ) {
            return null;
        }
        return I18N_KEY_FORBIDDEN;
    }

    @Override
    public void setAuthAttrs(
        BaseModel data,
        UserInfo userInfo
    ) {
        // Set readonly flag
        super.setAuthAttrs(data, userInfo);

        BelongsToMpg object = (BelongsToMpg) data;
        object.setOwner(!object.isReadonly());
    }
}
