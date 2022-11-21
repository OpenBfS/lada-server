/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.auth;

import java.util.ArrayList;
import java.util.List;

import de.intevation.lada.model.land.Mpg;
import de.intevation.lada.model.land.MessprogrammMmt;
import de.intevation.lada.model.master.MeasFacil;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.util.rest.Response;

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
        } else if (data instanceof MessprogrammMmt) {
            messprogramm = repository.getByIdPlain(
                Mpg.class,
                ((MessprogrammMmt) data).getMpgId()
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
    @SuppressWarnings("unchecked")
    public <T> Response filter(
        Response data,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        if (data.getData() instanceof List<?>
            && !clazz.isAssignableFrom(MessprogrammMmt.class)
        ) {
            List<Mpg> messprogramme = new ArrayList<Mpg>();
            for (Mpg messprogramm
                : (List<Mpg>) data.getData()) {
                messprogramme.add(setAuthData(userInfo, messprogramm));
            }
            data.setData(messprogramme);
        } else if (data.getData() instanceof Mpg) {
            Mpg messprogramm = (Mpg) data.getData();
            data.setData(setAuthData(userInfo, messprogramm));
        }
        return data;
    }

    /**
     * Set authorization data for the current probe object.
     *
     * @param userInfo  The user information.
     * @param probe     The probe object.
     * @return The probe.
     */
    private Mpg setAuthData(
        UserInfo userInfo,
        Mpg messprogramm
    ) {
        MeasFacil mst =
            repository.getByIdPlain(
                MeasFacil.class, messprogramm.getMeasFacilId());
        if (userInfo.getFunktionenForNetzbetreiber(
                mst.getNetworkId()).contains(4)
        ) {
            messprogramm.setReadonly(false);
            return messprogramm;
        } else {
            messprogramm.setReadonly(true);
        }
        return messprogramm;
    }
}
