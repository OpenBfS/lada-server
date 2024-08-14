/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.context;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Locale;

import jakarta.enterprise.concurrent.spi.ThreadContextRestorer;
import jakarta.enterprise.concurrent.spi.ThreadContextSnapshot;

import org.jboss.logging.Logger;


/**
 * Add locale to thread context.
 */
public class LocaleContextSnapshot implements ThreadContextSnapshot {

    private static final Logger LOG = Logger.getLogger(
        LocaleContextSnapshot.class);

    private final Locale locale;

    LocaleContextSnapshot(Locale locale) {
        this.locale = locale;
        LOG.debug("Created snapshot with locale " + locale);
    }

    @Override
    public ThreadContextRestorer begin() {
        Locale localeToRestore = ThreadLocale.get();
        LOG.debug("Locale to restore: " + localeToRestore);
        AtomicBoolean restored = new AtomicBoolean();

        ThreadContextRestorer contextRestorer = () -> {
            if (restored.compareAndSet(false, true)) {
                ThreadLocale.set(localeToRestore);
                LOG.debug("Restored locale " + localeToRestore);
            } else {
                throw new IllegalStateException();
            }
        };

        ThreadLocale.set(this.locale);
        LOG.debug("Set locale " + this.locale);

        return contextRestorer;
    }
}
