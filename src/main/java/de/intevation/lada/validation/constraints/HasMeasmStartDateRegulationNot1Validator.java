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
 * Validates if the Measm has a measmStartDate in case it does not belong to
 * a sample for which regulationId equals 1.
 */
public class HasMeasmStartDateRegulationNot1Validator extends HasMeasmStartDate
    implements ConstraintValidator<HasMeasmStartDateRegulationNot1, Measm> {

    private String message;

    @Override
    public void initialize(
        HasMeasmStartDateRegulationNot1 constraintAnnotation
    ) {
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Measm messung, ConstraintValidatorContext ctx) {
        return isValid(messung, false, ctx, this.message);
    }
}
