/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.importer.identification;

import java.util.Map;

/**
 * Thrown if an {@link Identifier} is given an object with
 * ambiguous identifying attributes.
 */
public class IdentificationException extends Exception {
    private static final long serialVersionUID = 1L;

    private Map<String, Object> identifyingAttributes;

    public IdentificationException() { };

    public IdentificationException(Map<String, Object> identifyingAttributes) {
        this.identifyingAttributes = identifyingAttributes;
    }

    public Map<String, Object> getIdentifyingAttributes() {
        return identifyingAttributes;
    }
}
