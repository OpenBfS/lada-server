/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.auth;

import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.StatusProt;
import de.intevation.lada.model.master.StatusMp;
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
     * Check whether a Measm instance should be read-only due to its status.
     *
     * @param messungsId The ID of the Measm instance
     * @return true if instance should be read-only
     */
    protected boolean isMessungReadOnly(Integer messungsId) {
        Measm messung =
            repository.getById(Measm.class, messungsId);
        if (messung.getStatus() == null) {
            return false;
        }
        StatusProt status = repository.getById(
            StatusProt.class, messung.getStatus());
        StatusMp kombi = repository.getById(
            StatusMp.class, status.getStatusMpId());
        return (kombi.getStatusVal().getId() != 0
                && kombi.getStatusVal().getId() != 4);
    }

}
