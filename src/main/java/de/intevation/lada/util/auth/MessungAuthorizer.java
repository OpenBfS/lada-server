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

import de.intevation.lada.model.land.Messung;
import de.intevation.lada.model.land.Probe;
import de.intevation.lada.model.land.StatusProtokoll;
import de.intevation.lada.model.stammdaten.AuthLstUmw;
import de.intevation.lada.model.stammdaten.MessStelle;
import de.intevation.lada.model.stammdaten.StatusKombi;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.util.rest.Response;

public class MessungAuthorizer extends BaseAuthorizer {

    @Override
    public <T> boolean isAuthorized(
        Object data,
        RequestMethod method,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        Messung messung = (Messung) data;
        if (messung == null) {
            return false;
        }
        Probe probe =
            repository.getByIdPlain(
                Probe.class, messung.getProbeId());
        if (method == RequestMethod.PUT
            || method == RequestMethod.DELETE) {
            return !this.isMessungReadOnly(messung.getId())
                && getAuthorization(userInfo, probe);
        }
        if (method == RequestMethod.POST) {
            return getAuthorization(userInfo, probe);
        }
        StatusProtokoll status = repository.getByIdPlain(
            StatusProtokoll.class,
            messung.getStatus()
        );
        StatusKombi kombi = repository.getByIdPlain(
            StatusKombi.class,
            status.getStatusKombi()
        );
        return kombi.getStatusWert().getId() > 0
            || getAuthorization(userInfo, probe);
    }

    @Override
    public <T> boolean isAuthorizedById(
        Object id,
        RequestMethod method,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        Messung messung;
        messung = repository.getByIdPlain(Messung.class, id);
        return isAuthorized(messung, method, userInfo, clazz);
    }


    @SuppressWarnings("unchecked")
    @Override
    public <T> Response filter(
        Response data,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        if (data.getData() instanceof List<?>) {
            List<Messung> messungen = new ArrayList<Messung>();
            for (Messung messung :(List<Messung>) data.getData()) {
                messungen.add(setAuthData(userInfo, messung));
            }
            data.setData(messungen);
        } else if (data.getData() instanceof Messung) {
            Messung messung = (Messung) data.getData();
            data.setData(setAuthData(userInfo, messung));
        }
        return data;
    }

    /**
     * Authorize a single messung object.
     *
     * @param userInfo  The user information.
     * @param messung     The messung object.
     * @return The messung.
     */
    private Messung setAuthData(
        UserInfo userInfo,
        Messung messung
    ) {
        Probe probe =
            (Probe) repository.getById(
                Probe.class, messung.getProbeId()).getData();
        MessStelle mst =
            repository.getByIdPlain(
                MessStelle.class, probe.getMstId());

        if (userInfo.belongsTo(probe.getMstId(), probe.getLaborMstId())) {
            messung.setOwner(true);
            messung.setReadonly(false);
        } else {
            messung.setOwner(false);
            messung.setReadonly(true);
        }

        messung.setStatusEdit(false);
        messung.setStatusEditMst(false);
        messung.setStatusEditLand(false);
        messung.setStatusEditLst(false);

        if (!userInfo.getFunktionen().contains(1)
            && !userInfo.getFunktionen().contains(2)
            && !userInfo.getFunktionen().contains(3)
        ) {
            return messung;
        }

        StatusProtokoll status = repository.getByIdPlain(
            StatusProtokoll.class,
            messung.getStatus()
        );
        StatusKombi kombi = repository.getByIdPlain(
            StatusKombi.class, status.getStatusKombi());
        int stufe = kombi.getStatusStufe().getId();
        int wert  = kombi.getStatusWert().getId();

        messung.setReadonly(wert != 0 && wert != 4);
        if ((stufe == 1 && wert == 00) || wert == 4) {
            stufe = 0;
        }

        // Has the user the right to edit status for the 'Messstelle'?
        if (userInfo.getFunktionenForMst(probe.getMstId()).contains(1)
            && (stufe == 0 || stufe == 1)
        ) {
            messung.setStatusEditMst(true);
            messung.setStatusEdit(true);
        }

        // Has the user the right to edit status for the 'Netzbetreiber'?
        if (userInfo.getFunktionenForNetzbetreiber(
                mst.getNetzbetreiberId()).contains(2)
            && (stufe == 0 && messung.getStatusEditMst()
            || stufe == 1
            || stufe == 2)
        ) {
            messung.setStatusEditLand(true);
            messung.setStatusEdit(true);
        }

        /* Does the user belong to an appropriate 'Leitstelle' to
           edit status? */
        if (userInfo.getFunktionen().contains(3)
            && (stufe == 0
                && messung.getStatusEditMst()
                && messung.getStatusEditLand()
                || stufe == 1
                && messung.getStatusEditLand()
                || stufe == 2
                || stufe == 3)
        ) {
            QueryBuilder<AuthLstUmw> lstFilter =
                repository.queryBuilder(AuthLstUmw.class);
            lstFilter.or("mstId", userInfo.getMessstellen());
            List<AuthLstUmw> lsts =
                repository.filterPlain(lstFilter.getQuery());
            for (int i = 0; i < lsts.size(); i++) {
                if (lsts.get(i).getUmwId().equals(probe.getUmwId())) {
                    messung.setStatusEditLst(true);
                }
                if (messung.getStatusEditLst()) {
                    messung.setStatusEdit(true);
                }
            }
        }

        return messung;
    }

}
