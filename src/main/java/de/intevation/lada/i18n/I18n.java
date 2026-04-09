/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.i18n;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import de.intevation.lada.context.ThreadLocale;


/**
 * Provides i18n utilities with context locale and constants for defined keys.
 */
public class I18n {

    public static final String KEY_VERSION = "version";
    public static final String KEY_TRUE = "true";
    public static final String KEY_FALSE = "false";
    public static final String KEY_FORBIDDEN = "forbidden";
    public static final String KEY_CANNOT_DELETE = "cannot_delete";
    public static final String KEY_DATASET_CHANGED = "dataset_changed";
    public static final String KEY_OP_NOT_POSSIBLE = "op_not_possible";
    public static final String KEY_NO_VALID_USER = "no_valid_user_found";
    public static final String KEY_NO_VALID_ROLE = "no_valid_role_found";
    public static final String KEY_DOWNLOAD_UNTIL = "download_until";

    private ResourceBundle bundle;

    I18n() {
        this.bundle = ResourceBundle.getBundle("lada", ThreadLocale.get());
    }

    /**
     * Get ResourceBundle for localization in language given by context locale.
     * @return ResourceBundle
     */
    public ResourceBundle getResourceBundle() {
        return this.bundle;
    }

    /**
     * @param key Key to lookup in resource bundle
     * @param arguments object(s) to format in localized string
     * @return localized string for language given by context locale
     */
    public String getString(String key, Object... arguments) {
        MessageFormat format = new MessageFormat(
            this.bundle.getString(key), ThreadLocale.get());
        return format.format(arguments);
    }
}
