/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.lada;

public interface BelongsToMeasm {

    /**
     * @return the ID of the Measm the instance belongs to
     */
    Integer getMeasmId();

    /**
     * @return true if requesting user is owner of the instance
     */
    boolean isOwner();

    /**
     * @param owner boolean specifying if requesting user
     * is owner of the instance
     */
    void setOwner(boolean owner);
}
