/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.auth;

import de.intevation.lada.model.BaseModel;
import de.intevation.lada.model.lada.Mpg;
import de.intevation.lada.model.lada.MpgMmtMp;
import de.intevation.lada.model.master.MeasFacil;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;


public class MessprogrammAuthorizer extends BaseAuthorizer {

    public MessprogrammAuthorizer(Repository repository) {
        super(repository);
    }

    @Override
    public <T> boolean isAuthorized(
        Object data,
        RequestMethod method,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        if (method == RequestMethod.GET) {
            // Allow read access to everybody
            return true;
        }
        Mpg messprogramm;
        if (data instanceof Mpg) {
            messprogramm = (Mpg) data;
        } else if (data instanceof MpgMmtMp) {
            messprogramm = repository.getByIdPlain(
                Mpg.class,
                ((MpgMmtMp) data).getMpgId()
            );
            if (messprogramm == null) {
                return false;
            }
        } else {
            return false;
        }
        String mstId = messprogramm.getMeasFacilId();
        if (mstId != null) {
            MeasFacil mst = repository.getByIdPlain(
                MeasFacil.class, mstId);
            if (userInfo.getFunktionenForNetzbetreiber(
                    mst.getNetworkId()).contains(4)
            ) {
                return true;
            }
        }
        return false;
    }

    @Override
    public <T> boolean isAuthorizedById(
        Object id,
        RequestMethod method,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        Mpg mp =
            repository.getByIdPlain(Mpg.class, id);
        return isAuthorized(mp, method, userInfo, Mpg.class);
    }

    @Override
    public <T extends BaseModel> void setAuthAttrs(
        BaseModel data,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        if (data instanceof Mpg) {
            setAuthData(userInfo, (Mpg) data);
        }
    }

    /**
     * Set authorization attributes.
     *
     * @param userInfo  The user information.
     * @param messprogramm     The Mpg object.
     */
    private void setAuthData(
        UserInfo userInfo,
        Mpg messprogramm
    ) {
        MeasFacil mst =
            repository.getByIdPlain(
                MeasFacil.class, messprogramm.getMeasFacilId());
        messprogramm.setReadonly(
            !userInfo.getFunktionenForNetzbetreiber(
                mst.getNetworkId()).contains(4));
    }
}
