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
import de.intevation.lada.model.lada.Measm_;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.lada.StatusProt;
import de.intevation.lada.model.master.StatusMp;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;


public abstract class BaseAuthorizer implements Authorizer {

    protected static final String I18N_KEY_FORBIDDEN = "forbidden";
    protected static final String I18N_KEY_CANNOTDELETE = "cannot_delete";

    protected Repository repository;

    /**
     * Call this in implementations extending this abstract class.
     */
    public BaseAuthorizer(Repository repository) {
        this.repository = repository;
    }

    /**
     * Get the authorization of a single sample.
     *
     * @param userInfo  The user information.
     * @param sample    The sample to authorize.
     * @return true if user's measFacils contain measFacil of the sample
     */
    protected boolean getAuthorization(UserInfo userInfo, Sample sample) {
        return userInfo.getMessstellen().contains(sample.getMeasFacilId());
    }

    /**
     * Test whether a probe is readonly.
     *
     * @param probeId   The probe Id.
     * @return True if the probe is readonly.
     */
    @Override
    public boolean isProbeReadOnly(Integer probeId) {
        QueryBuilder<Measm> builder = repository
            .queryBuilder(Measm.class)
            .and(Measm_.sampleId, probeId);
        List<Measm> messungen = repository.filter(builder.getQuery());
        for (Measm measm: messungen) {
            StatusMp kombi = repository.getById(
                StatusMp.class, measm.getStatusProt().getStatusMpId());
            if (kombi.getStatusVal().getId() != 0
                && kombi.getStatusVal().getId() != 4
            ) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isMessungReadOnly(Integer messungsId) {
        Measm messung =
            repository.getById(Measm.class, messungsId);
        StatusProt status = messung.getStatusProt();
        if (status == null) {
            return false;
        }
        StatusMp kombi = repository.getById(
            StatusMp.class, status.getStatusMpId());
        return (kombi.getStatusVal().getId() != 0
                && kombi.getStatusVal().getId() != 4);
    }

}
