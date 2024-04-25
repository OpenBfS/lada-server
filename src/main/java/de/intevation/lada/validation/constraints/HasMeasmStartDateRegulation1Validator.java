/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.constraints;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import de.intevation.lada.model.lada.Measm;


/**
 * Validation rule for Measm.
 * Validates if the Measm has a measmStartDate in case it belongs to
 * a sample for which regulationId equals 1.
 */
public class HasMeasmStartDateRegulation1Validator extends HasMeasmStartDate
    implements ConstraintValidator<HasMeasmStartDateRegulation1, Measm> {

    private String message;

    @Override
    public void initialize(HasMeasmStartDateRegulation1 constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Measm messung, ConstraintValidatorContext ctx) {
        return isValid(messung, true, ctx, this.message);
    }
}
