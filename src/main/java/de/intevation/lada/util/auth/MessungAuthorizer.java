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
import de.intevation.lada.model.master.AuthCoordOfcEnvMediumMp_;
import de.intevation.lada.model.master.MeasFacil;
import de.intevation.lada.model.master.StatusMp;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;


public class MessungAuthorizer extends BaseAuthorizer {

    private ProbeAuthorizer probeAuthorizer;

    public MessungAuthorizer(Repository repository) {
        super(repository);

        this.probeAuthorizer = new ProbeAuthorizer(repository);
    }

    @Override
    public <T> String isAuthorizedReason(
        Object data,
        RequestMethod method,
        UserInfo userInfo
    ) {
        Measm messung = (Measm) data;
        Sample probe =
            repository.getById(
                Sample.class, messung.getSampleId());
        if (method == RequestMethod.PUT
            || method == RequestMethod.DELETE) {
            return !this.isMessungReadOnly(messung.getId())
                && probeAuthorizer.isAuthorized(
                    probe, RequestMethod.POST, userInfo)
                ? null : I18N_KEY_FORBIDDEN;
        }
        if (method == RequestMethod.POST) {
            return probeAuthorizer.isAuthorized(
                probe, RequestMethod.POST, userInfo)
                ? null : I18N_KEY_FORBIDDEN;
        }
        StatusProt status = repository.getById(
            StatusProt.class,
            messung.getStatus()
        );
        StatusMp kombi = repository.getById(
            StatusMp.class,
            status.getStatusMpId()
        );
        return kombi.getStatusVal().getId() > 0
            || probeAuthorizer.isAuthorized(probe, RequestMethod.POST, userInfo)
            ? null : I18N_KEY_FORBIDDEN;
    }

    @Override
    public <T extends BaseModel> void setAuthAttrs(
        BaseModel data,
        UserInfo userInfo
    ) {
        // Set readonly flag
        super.setAuthAttrs(data, userInfo);

        Measm messung = (Measm) data;
        Sample probe = repository.getById(Sample.class, messung.getSampleId());
        MeasFacil mst = repository.getById(
            MeasFacil.class, probe.getMeasFacilId());

        messung.setOwner(userInfo.belongsTo(
                probe.getMeasFacilId(), probe.getApprLabId()));

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

        StatusProt status = repository.getById(
            StatusProt.class,
            messung.getStatus()
        );
        StatusMp kombi = repository.getById(
            StatusMp.class, status.getStatusMpId());
        int stufe = kombi.getStatusLev().getId();
        int wert  = kombi.getStatusVal().getId();

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
            lstFilter.or(
                AuthCoordOfcEnvMediumMp_.measFacilId,
                userInfo.getMessstellen());
            List<AuthCoordOfcEnvMediumMp> lsts =
                repository.filter(lstFilter.getQuery());
            for (int i = 0; i < lsts.size(); i++) {
                if (lsts.get(i).getEnvMediumId()
                        .equals(probe.getEnvMediumId())) {
                    messung.setStatusEditLst(true);
                }
                if (messung.getStatusEditLst()) {
                    messung.setStatusEdit(true);
                }
            }
        }
    }
}
