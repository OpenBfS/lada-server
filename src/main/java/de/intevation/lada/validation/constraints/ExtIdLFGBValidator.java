/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */

package de.intevation.lada.validation.constraints;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.lada.Sample_;


/**
 * Validation rule for Sample.
 * Validates that on creation an ExtId is only allowd for LFGB
 */
public class ExtIdLFGBValidator
    implements ConstraintValidator<ExtIdLFGB, Sample> {

    private static final int REG_LFGB = 8;

    private String message;

    @Override
    public void initialize(ExtIdLFGB constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Sample sample, ConstraintValidatorContext ctx) {
        if (sample == null) {
            return true;
        }
        Integer regulationId = sample.getRegulationId();

        boolean isIrrelevantRegId = regulationId == null || regulationId == REG_LFGB;

        if (isIrrelevantRegId) {
            return true;
        }

        boolean hasExtId = sample.getExtId() != null;

        if (hasExtId){
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(Sample_.EXT_ID)
                .addConstraintViolation();
            return false;
        }
        return true;
   }
}
