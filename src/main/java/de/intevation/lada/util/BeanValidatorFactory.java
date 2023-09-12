/* Copyright (C) 2023 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util;

import java.util.Locale;

import jakarta.enterprise.inject.Produces;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Validation;
import jakarta.ws.rs.core.Context;

import org.hibernate.validator.HibernateValidator;

/**
 * Factory class creating configured validator instances.
 */
public class BeanValidatorFactory {

    @Context HttpServletRequest req;
    /**
     * Create a validator instance.
     *
     * The instance is configured with a default Locale and available locales.
     * @return Validator instance.
     */
    @Produces
    public jakarta.validation.Validator createHibernateValidator() {
        return Validation.byProvider(HibernateValidator.class)
            .configure()
            .defaultLocale(Locale.GERMANY)
            .locales(Locale.GERMANY, Locale.US)
            .buildValidatorFactory()
            .getValidator();
    }
}
