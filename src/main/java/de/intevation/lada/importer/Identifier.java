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
     * Get persistent entity identified by attributes of given object.
     * @param object object carrying identifying attributes
     * @return the found object or null
     * @throws IdentificationException in case of ambiguous identifying attributes
     */
    T getExisting(T object) throws IdentificationException;

    public class IdentificationException extends Exception { }
}
