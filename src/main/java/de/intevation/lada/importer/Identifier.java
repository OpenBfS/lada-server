/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.importer;


/**
 * Interface for object identifier.
 */
public interface Identifier<T> {

    /**
     * Find and identify the object.
     * @param object the object.
     * @return Enum idicating the identification type.
     */
    Identified find(T object);

    /**
     * Get the object identified in "find", if any.
     * @return the found object
     */
    T getExisting();
}
