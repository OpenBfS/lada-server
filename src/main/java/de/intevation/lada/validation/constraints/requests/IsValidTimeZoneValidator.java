/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.constraints.requests;

import java.util.Set;
import java.util.TimeZone;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IsValidTimeZoneValidator
        implements ConstraintValidator<IsValidTimeZone, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || Set.of(TimeZone.getAvailableIDs()).contains(value);
    }
}
