/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.constraints;

import java.util.Date;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import de.intevation.lada.model.lada.Sample;


/**
 * Validation rule for Sample.
 * Validates if the sampling period is meaningful.
 */
public class BeginBeforeEndSampleValidator
    implements ConstraintValidator<BeginBeforeEnd, Sample> {

    private String message;

    @Override
    public void initialize(BeginBeforeEnd constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Sample sample, ConstraintValidatorContext ctx) {
        if (sample == null) {
            return true;
        }

        Date begin = sample.getSampleStartDate();
        Date end = sample.getSampleEndDate();

        // Leave null checks up to field-level constraints
        if (begin == null || end == null) {
            return true;
        }

        if (begin.after(end)) {
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate(this.message)
                .addPropertyNode("sampleStartDate")
                .addConstraintViolation();
            ctx.buildConstraintViolationWithTemplate(this.message)
                .addPropertyNode("sampleEndDate")
                .addConstraintViolation();
            return false;
        }
        return true;
    }
}
