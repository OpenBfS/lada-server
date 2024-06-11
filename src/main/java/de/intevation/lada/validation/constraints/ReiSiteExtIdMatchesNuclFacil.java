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
 * Validates if extId is valid for REI site.
 */
@Target({ TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = { ReiSiteExtIdMatchesNuclFacilValidator.class })
@Documented
public @interface ReiSiteExtIdMatchesNuclFacil {

    String message() default
        "{de.intevation.lada.validation.constraints.ReiSiteExtIdMatchesNuclFacil.message}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
