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
 * Check if coordinate fields are changed if they should not.
 */
@Target({ TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = { CanChangeCoordinatesValidator.class })
@Documented
public @interface CanChangeCoordinates {

    static final String MSG =
        "{de.intevation.lada.validation.constraints.CanChangeCoordinates.message}";

    String message() default MSG;

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
