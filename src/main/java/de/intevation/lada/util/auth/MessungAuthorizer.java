/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.auth;

import java.util.List;

import de.intevation.lada.model.BaseModel;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.lada.StatusProt;
import de.intevation.lada.model.master.AuthCoordOfcEnvMediumMp;
import de.intevation.lada.model.master.MeasFacil;
import de.intevation.lada.model.master.StatusMp;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;


public class MessungAuthorizer extends BaseAuthorizer {

    public MessungAuthorizer(Repository repository) {
        super(repository);
    }

    @Override
    public <T> String isAuthorizedReason(
        Object data,
        RequestMethod method,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        Measm messung = (Measm) data;
        Sample probe =
            repository.getByIdPlain(
                Sample.class, messung.getSampleId());
        if (method == RequestMethod.PUT
            || method == RequestMethod.DELETE) {
            return !this.isMessungReadOnly(messung.getId())
                && getAuthorization(userInfo, probe)
                ? null : I18N_KEY_FORBIDDEN;
        }
        if (method == RequestMethod.POST) {
            return getAuthorization(userInfo, probe)
                ? null : I18N_KEY_FORBIDDEN;
        }
        StatusProt status = repository.getByIdPlain(
            StatusProt.class,
            messung.getStatus()
        );
        StatusMp kombi = repository.getByIdPlain(
            StatusMp.class,
            status.getStatusMpId()
        );
        return kombi.getStatusVal().getId() > 0
            || getAuthorization(userInfo, probe)
            ? null : I18N_KEY_FORBIDDEN;
    }

    @Override
    public <T> boolean isAuthorizedById(
        Object id,
        RequestMethod method,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        Measm messung;
        messung = repository.getByIdPlain(Measm.class, id);
        return isAuthorized(messung, method, userInfo, clazz);
    }

    @Override
    public <T extends BaseModel> void setAuthAttrs(
        BaseModel data,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        if (data instanceof Measm) {
            setAuthData(userInfo, (Measm) data);
        }
    }

    /**
     * Authorize a single messung object.
     *
     * @param userInfo  The user information.
     * @param messung     The messung object.
     */
    private void setAuthData(
        UserInfo userInfo,
        Measm messung
    ) {
        Sample probe = repository.getByIdPlain(
            Sample.class, messung.getSampleId());
        MeasFacil mst =
            repository.getByIdPlain(
                MeasFacil.class, probe.getMeasFacilId());

        if (userInfo.belongsTo(probe.getMeasFacilId(), probe.getApprLabId())) {
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
            return;
        }

        StatusProt status = repository.getByIdPlain(
            StatusProt.class,
            messung.getStatus()
        );
        StatusMp kombi = repository.getByIdPlain(
            StatusMp.class, status.getStatusMpId());
        int stufe = kombi.getStatusLev().getId();
        int wert  = kombi.getStatusVal().getId();

        messung.setReadonly(wert != 0 && wert != 4);
        if ((stufe == 1 && wert == 00) || wert == 4) {
            stufe = 0;
        }

        // Has the user the right to edit status for the 'Messstelle'?
        if (userInfo.getFunktionenForMst(probe.getMeasFacilId()).contains(1)
            && (stufe == 0 || stufe == 1)
        ) {
            messung.setStatusEditMst(true);
            messung.setStatusEdit(true);
        }

        // Has the user the right to edit status for the 'Netzbetreiber'?
        if (userInfo.getFunktionenForNetzbetreiber(
                mst.getNetworkId()).contains(2)
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
            QueryBuilder<AuthCoordOfcEnvMediumMp> lstFilter =
                repository.queryBuilder(AuthCoordOfcEnvMediumMp.class);
            lstFilter.or("measFacilId", userInfo.getMessstellen());
            List<AuthCoordOfcEnvMediumMp> lsts =
                repository.filterPlain(lstFilter.getQuery());
            for (int i = 0; i < lsts.size(); i++) {
                if (lsts.get(i).getEnvMediumId().equals(probe.getEnvMediumId())) {
                    messung.setStatusEditLst(true);
                }
                if (messung.getStatusEditLst()) {
                    messung.setStatusEdit(true);
                }
            }
        }
    }
}
