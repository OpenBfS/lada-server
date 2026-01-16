/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.i18n;

import java.util.ResourceBundle;

import de.intevation.lada.context.ThreadLocale;


/**
 * Provides i18n utilities with context locale.
 */
public class I18n {

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
     * @return localized string for language given by context locale
     */
    public String getString(String key) {
        return this.bundle.getString(key);
    }
}
