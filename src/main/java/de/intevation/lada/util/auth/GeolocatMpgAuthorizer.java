/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.auth;

import de.intevation.lada.model.BaseModel;
import de.intevation.lada.model.lada.GeolocatMpg;
import de.intevation.lada.model.lada.Mpg;
import de.intevation.lada.model.master.MeasFacil;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;


public class GeolocatMpgAuthorizer extends BaseAuthorizer {

    public GeolocatMpgAuthorizer(Repository repository) {
        super(repository);
    }

    @Override
    public <T> String isAuthorizedReason(
        Object data,
        RequestMethod method,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        GeolocatMpg loc = (GeolocatMpg) data;
        MeasFacil mst = repository.getById(
            MeasFacil.class,
            repository.getById(Mpg.class, loc.getMpgId()).getMeasFacilId());
        if (userInfo.getFunktionenForNetzbetreiber(
                mst.getNetworkId()).contains(4)
        ) {
            return null;
        }
        return I18N_KEY_FORBIDDEN;
    }

    @Override
    public <T> boolean isAuthorizedById(
        Object id,
        RequestMethod method,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        //TODO implement
        return false;
    }

    @Override
    public <T extends BaseModel> void setAuthAttrs(
        BaseModel data,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        // Set readonly flag
        super.setAuthAttrs(data, userInfo, clazz);

        GeolocatMpg loc = (GeolocatMpg) data;
        loc.setOwner(!loc.isReadonly());
    }
}
