/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */

package de.intevation.lada.rest;

import java.io.Serializable;

/**
 * DTO to carry the refId from the job.
 */
public final class AsyncJobResponse implements Serializable {
    private final String refId;

    public AsyncJobResponse(String refId) {
        this.refId = refId;
    }

    public String getRefId() {
        return refId;
    }
}
