/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.constraints;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Validate the coordinates contained in the given object.
 */
@Target({ TYPE, PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = {
    ValidCoordinatesSiteValidator.class,
    ValidCoordinatesPostDataValidator.class })
@Documented
public @interface ValidCoordinates {
    static final String MSG =
        "{de.intevation.lada.validation.constraints.ValidCoordinates.message}";

    String message() default MSG;

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
