/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.constraints;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;


/**
 * Validates if value references a border of administrative unit.
 */
@Target({ FIELD })
@Retention(RUNTIME)
@Constraint(validatedBy = { IsAdminBorderKeyValidator.class })
@Documented
public @interface IsAdminBorderKey {

    String message() default
        "{de.intevation.lada.validation.constraints.IsAdminBorderKey.message}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
