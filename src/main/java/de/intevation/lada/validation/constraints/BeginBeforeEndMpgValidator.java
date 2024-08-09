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

import de.intevation.lada.model.lada.Mpg;
import de.intevation.lada.model.lada.Mpg_;


/**
 * Validation rule for Mpg.
 * Validates if the subintervall period is meaningful.
 */
public class BeginBeforeEndMpgValidator
    implements ConstraintValidator<BeginBeforeEnd, Mpg> {

    @Override
    public boolean isValid(Mpg mpg, ConstraintValidatorContext ctx) {
        if (mpg == null) {
            return true;
        }

        Integer begin = mpg.getSamplePdStartDate();
        Integer end = mpg.getSamplePdEndDate();

        // Leave null checks up to field-level constraints
        if (begin == null || end == null) {
            return true;
        }

        boolean isValid = true;
        if (begin > end) {
            isValid = false;
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate(BeginBeforeEnd.MSG)
                .addPropertyNode(Mpg_.SAMPLE_PD_START_DATE)
                .addConstraintViolation();
            ctx.buildConstraintViolationWithTemplate(BeginBeforeEnd.MSG)
                .addPropertyNode(Mpg_.SAMPLE_PD_END_DATE)
                .addConstraintViolation();
        }

        return isValid;
    }
}
