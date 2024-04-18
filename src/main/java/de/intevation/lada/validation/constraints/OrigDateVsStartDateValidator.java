/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
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
 * Validation rule for sample.
 * Validates if the sample's origDate and sampleStartDate match.
 */
public class OrigDateVsStartDateValidator
    implements ConstraintValidator<OrigDateVsStartDate, Sample> {

    private String message;

    @Override
    public void initialize(OrigDateVsStartDate constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Sample probe, ConstraintValidatorContext ctx) {
        Date uZeit = probe.getOrigDate();
        Date begin = probe.getSampleStartDate();
        if (uZeit != null && begin != null && uZeit.after(begin)) {
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate(this.message)
                .addPropertyNode("origDate")
                .addConstraintViolation();
            return false;
        }
        return true;
    }

}
