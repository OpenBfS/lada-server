/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
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
 * Validation rule for Measm.
 * Validates if the Measm has a measmStartDate in case it belongs to
 * a sample for which regulationId equals 1.
 */
@Target({ TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = { HasMeasmStartDateRegulation1Validator.class })
@Documented
public @interface HasMeasmStartDateRegulation1 {

    String message() default
        "{de.intevation.lada.validation.constraints.HasMeasmStartDateRegulation1.message}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
