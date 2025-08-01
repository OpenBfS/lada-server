/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.importer;


/**
 * Container for result of importing a file.
 */
public abstract class Report {

    /**
     * Name of tag that was generated for the import.
     */
    private String tag;

    /**
     * Indicates whether contents of the file could be imported without errors.
     */
    public abstract boolean isSuccess();

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
