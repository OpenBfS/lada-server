/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import de.intevation.lada.model.master.StatusMpView;


/**
 * REST service for human readable descriptions of statusMpIds.
 */
@Path(LadaService.PATH_REST + "statusmpview")
public class StatusMpViewService extends LadaIntegerIdEntityService {

    /**
     * Get statusMpIds with human readable descriptions.
     *
     * @return all {@link StatusMpView} objects
     */
    @GET
    public List<StatusMpView> get() {
        return repository.getAll(StatusMpView.class);
    }
}
