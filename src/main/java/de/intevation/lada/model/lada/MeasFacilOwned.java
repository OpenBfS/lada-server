/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.lada;

import de.intevation.lada.util.auth.Authorizable;

/**
 * Represents entities owned by a MeasFacil.
 */
public interface MeasFacilOwned extends Authorizable {

    /**
     * Get ID of associated MeasFacil.
     */
    public String getMeasFacilId();
}
