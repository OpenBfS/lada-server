/* Copyright (C) 2026 by Bundesamt fuer Strahlenschutz
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
 * Validates if referenced {@link de.intevation.lada.model.master.MunicDiv}
 * belongs to (indirectly) referenced
 * {@link de.intevation.lada.model.master.AdminUnit}.
 */
@Target({ TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = {
        MunicDivMatchesAdminUnitSiteValidator.class,
        MunicDivMatchesAdminUnitGeolocatValidator.class,
        MunicDivMatchesAdminUnitGeolocatMpgValidator.class })
@Documented
public @interface MunicDivMatchesAdminUnit {

    String message() default
        "{de.intevation.lada.validation.constraints.MunicDivMatchesAdminUnit.message}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
