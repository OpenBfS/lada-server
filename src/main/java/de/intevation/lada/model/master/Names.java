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

    public static final String QUERY_MEASD_NAMES = "getMeasdNames";
    public static final String QUERY_PARAM_MEASD_NAMES = "ids";

    public static final String QUERY_INSERT_USER_NAME = "insertUserName";
    public static final String QUERY_LADA_USER_ID = "getLadaUserId";
    public static final String QUERY_PARAM_USER_NAME = "name";

    private Names() { };
}
