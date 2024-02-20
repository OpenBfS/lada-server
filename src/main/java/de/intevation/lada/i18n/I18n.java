/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.i18n;

import java.util.ResourceBundle;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;


/**
 * Injectable class providing i18n utilities within request context.
 */
public class I18n {

    private ResourceBundle bundle;

    @Inject
    I18n(HttpServletRequest request) {
        this.bundle = ResourceBundle.getBundle("lada", request.getLocale());
    }

    /**
     * Get ResourceBundle for localization in language accepted by request.
     * @return ResourceBundle
     */
    public ResourceBundle getResourceBundle() {
        return this.bundle;
    }

    /**
     * @param key Key to lookup in resource bundle
     * @return localized string for language accepted by request
     */
    public String getString(String key) {
        return this.bundle.getString(key);
    }
}
