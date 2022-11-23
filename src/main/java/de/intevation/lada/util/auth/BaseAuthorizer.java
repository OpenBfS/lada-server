/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.auth;

import java.util.List;

import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.lada.StatusProt;
import de.intevation.lada.model.master.StatusMp;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.Response;

public abstract class BaseAuthorizer implements Authorizer {

    protected Repository repository;

    /**
     * Call this in implementations extending this abstract class.
     */
    public BaseAuthorizer(Repository repository) {
        this.repository = repository;
    }

    /**
     * Get the authorization of a single probe.
     *
     * @param userInfo  The user information.
     * @param probe     The probe to authorize.
     */
    protected boolean getAuthorization(UserInfo userInfo, Sample probe) {
        return (probe.getMeasFacilId() != null
            && userInfo.getMessstellen().contains(probe.getMeasFacilId()));
    }

    /**
     * Test whether a probe is readonly.
     *
     * @param probeId   The probe Id.
     * @return True if the probe is readonly.
     */
    public boolean isProbeReadOnly(Integer probeId) {
        QueryBuilder<Measm> builder = repository.queryBuilder(Measm.class);
        builder.and("sampleId", probeId);
        Response response = repository.filter(builder.getQuery());
        @SuppressWarnings("unchecked")
        List<Measm> messungen = (List<Measm>) response.getData();
        for (int i = 0; i < messungen.size(); i++) {
            if (messungen.get(i).getStatus() == null) {
                continue;
            }
            StatusProt status =
                repository.getByIdPlain(
                    StatusProt.class,
                    messungen.get(i).getStatus()
                );
            StatusMp kombi = repository.getByIdPlain(
                StatusMp.class, status.getStatusComb());
            if (kombi.getStatusVal().getId() != 0
                && kombi.getStatusVal().getId() != 4
            ) {
                return true;
            }
        }
        return false;
    }

    public boolean isMessungReadOnly(Integer messungsId) {
        Measm messung =
            repository.getByIdPlain(Measm.class, messungsId);
        if (messung.getStatus() == null) {
            return false;
        }
        StatusProt status = repository.getByIdPlain(
            StatusProt.class, messung.getStatus());
        StatusMp kombi = repository.getByIdPlain(
            StatusMp.class, status.getStatusComb());
        return (kombi.getStatusVal().getId() != 0
                && kombi.getStatusVal().getId() != 4);
    }

}
