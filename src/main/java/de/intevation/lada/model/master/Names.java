/* Copyright (C) 2021 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.master;

/**
 * Provide constants holding names for objects in this package.
 */
public class Names {

    /**
     * The name to be used to schema-qualify database object names
     * represented by classes in this package.
     */
    public static final String SCHEMA_NAME = "master";

    public static final String QUERY_UPDATE_SITE_IMG = "updateSiteImg";

    public static final String QUERY_UPDATE_SITE_MAP = "updateSiteMap";

    public static final String QUERY_GET_MEASD_FOR_MMT = "getMeasdForMmt";

    public static final String QUERY_GET_SAMPLE_SPECIF_FOR_ENV_MEDIUM =
        "getSampleSpecifForEnvMedium";

    private Names() { };
}
