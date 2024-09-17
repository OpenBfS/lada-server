/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.lada;

public interface BelongsToMpg {

    /**
     * @return the ID of the Mpg the instance belongs to
     */
    Integer getMpgId();

    /**
     * @return true if requesting user is owner of the instance.
     * Default method always returns false.
     */
    default boolean isOwner() {
        return false;
    }

    /**
     * @param owner boolean specifying if requesting user
     * is owner of the instance.
     * Default method just ignores the parameter.
     */
    default void setOwner(boolean owner) { }
}
