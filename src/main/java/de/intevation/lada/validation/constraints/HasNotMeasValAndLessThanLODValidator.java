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

import de.intevation.lada.model.lada.MeasVal;


/**
 * Validation rule for MeasVal.
 * Validates that measVal and lessThanLOD are not given at the same time.
 */
public class HasNotMeasValAndLessThanLODValidator
    implements ConstraintValidator<HasNotMeasValAndLessThanLOD, MeasVal> {

    private String message;

    @Override
    public void initialize(HasNotMeasValAndLessThanLOD constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(MeasVal val, ConstraintValidatorContext ctx) {
        if (val != null
            && val.getMeasVal() != null
            && val.getLessThanLOD() != null
        ) {
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate(this.message)
                .addPropertyNode("measVal")
                .addConstraintViolation();
            return false;
        }
        return true;
    }

}
