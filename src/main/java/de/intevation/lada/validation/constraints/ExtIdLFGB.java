/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.constraints;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;


/**
 * Validates that on creation an ExtId is only allowd for LFGB.
 */
@Target({ TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = { ExtIdLFGBValidator.class })
@Documented
public @interface ExtIdLFGB {

    String message() default
        "{de.intevation.lada.validation.constraints.ExtIdLFGB.message}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
