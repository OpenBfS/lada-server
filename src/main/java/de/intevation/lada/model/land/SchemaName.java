/* Copyright (C) 2021 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.land;

/**
 * Provide a constant holding the name to be used to schema-qualify
 * database object names represented by classes in this package.
 *
 */
public class SchemaName {
    public static final String NAME = "lada";
    public static final String LEGACY_NAME = "land";

    private SchemaName() { };
}
