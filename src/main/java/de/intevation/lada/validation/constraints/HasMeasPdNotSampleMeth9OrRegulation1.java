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
 * Validates if measPd is given for measms that reference a sample with
 * neither sampleMethId 9 nor regulationId 1.
 */
@Target({ TYPE })
@Retention(RUNTIME)
@Constraint(
    validatedBy = { HasMeasPdNotSampleMeth9OrRegulation1Validator.class })
@Documented
public @interface HasMeasPdNotSampleMeth9OrRegulation1 {

    String message() default
        "{de.intevation.lada.validation.constraints.HasMeasPdNotSampleMeth9OrRegulation1.message}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
