/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.context;

import java.util.Locale;
import java.util.Map;

import jakarta.enterprise.concurrent.spi.ThreadContextProvider;
import jakarta.enterprise.concurrent.spi.ThreadContextSnapshot;


/**
 * Add locale to thread context.
 */
public class LocaleContextProvider implements ThreadContextProvider {

    public String getThreadContextType() {
        return "Locale";
    }

    @Override
    public ThreadContextSnapshot currentContext(Map<String, String> props) {
        return new LocaleContextSnapshot(ThreadLocale.get());
    }

    @Override
    public ThreadContextSnapshot clearedContext(Map<String, String> props) {
        return new LocaleContextSnapshot(Locale.getDefault());
    }
}
