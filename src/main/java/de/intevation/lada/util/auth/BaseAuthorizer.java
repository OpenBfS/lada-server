/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.auth;

import java.util.List;

import javax.inject.Inject;

import de.intevation.lada.model.land.Messung;
import de.intevation.lada.model.land.Probe;
import de.intevation.lada.model.land.StatusProtokoll;
import de.intevation.lada.model.stammdaten.StatusKombi;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.Response;

public abstract class BaseAuthorizer implements Authorizer {

    /**
     * The Repository used to read from Database.
     */
    @Inject
    protected Repository repository;

    /**
     * Get the authorization of a single probe.
     *
     * @param userInfo  The user information.
     * @param probe     The probe to authorize.
     */
    protected boolean getAuthorization(UserInfo userInfo, Probe probe) {
        return (probe.getMstId() != null
            && userInfo.getMessstellen().contains(probe.getMstId()));
    }

    /**
     * Test whether a probe is readonly.
     *
     * @param probeId   The probe Id.
     * @return True if the probe is readonly.
     */
    public boolean isProbeReadOnly(Integer probeId) {
        QueryBuilder<Messung> builder = repository.queryBuilder(Messung.class);
        builder.and("probeId", probeId);
        Response response = repository.filter(builder.getQuery());
        @SuppressWarnings("unchecked")
        List<Messung> messungen = (List<Messung>) response.getData();
        for (int i = 0; i < messungen.size(); i++) {
            if (messungen.get(i).getStatus() == null) {
                continue;
            }
            StatusProtokoll status =
                repository.getByIdPlain(
                    StatusProtokoll.class,
                    messungen.get(i).getStatus()
                );
            StatusKombi kombi = repository.getByIdPlain(
                StatusKombi.class, status.getStatusKombi());
            if (kombi.getStatusWert().getId() != 0
                && kombi.getStatusWert().getId() != 4
            ) {
                return true;
            }
        }
        return false;
    }

    public boolean isMessungReadOnly(Integer messungsId) {
        Messung messung =
            repository.getByIdPlain(Messung.class, messungsId);
        if (messung.getStatus() == null) {
            return false;
        }
        StatusProtokoll status = repository.getByIdPlain(
            StatusProtokoll.class, messung.getStatus());
        StatusKombi kombi = repository.getByIdPlain(
            StatusKombi.class, status.getStatusKombi());
        return (kombi.getStatusWert().getId() != 0
                && kombi.getStatusWert().getId() != 4);
    }

}
