/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.context;

import java.util.Locale;


/**
 * Add locale to thread context.
 */
public class ThreadLocale {

    private ThreadLocale() { }

    private static final ThreadLocal<Locale> LOCALE = new ThreadLocal<>() {
        @Override
        protected Locale initialValue() {
            return Locale.getDefault();
        }
    };

    /**
     * Get locale for current thread.
     * @return Locale for current thread.
     */
    public static Locale get() {
        return LOCALE.get();
    }

    /**
     * Set locale for current thread.
     * @param locale Locale for current thread.
     */
    public static void set(Locale locale) {
        LOCALE.set(locale);
    }
}
