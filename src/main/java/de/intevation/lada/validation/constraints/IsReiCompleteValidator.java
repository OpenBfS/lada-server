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

import de.intevation.lada.model.lada.Sample;


/**
 * Validation rule for sample.
 * Validates if the sample has valid REI attributes.
 */
public class IsReiCompleteValidator
    implements ConstraintValidator<IsReiComplete, Sample> {

    private static final int REG_REI_I = 4;
    private static final int REG_REI_X = 3;

    private String message;

    @Override
    public void initialize(IsReiComplete constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Sample probe, ConstraintValidatorContext ctx) {
        if (probe == null || probe.getRegulationId() == null) {
            return true;
        }

        final int regulation = probe.getRegulationId();
        boolean isValid = true;
        if (regulation == REG_REI_X || regulation == REG_REI_I) {
            if (probe.getReiAgGrId() == null) {
                ctx.disableDefaultConstraintViolation();
                ctx.buildConstraintViolationWithTemplate(this.message)
                    .addPropertyNode("reiAgGrId")
                    .addConstraintViolation();
                isValid = false;
            }
            if (probe.getNuclFacilGrId() == null) {
                ctx.disableDefaultConstraintViolation();
                ctx.buildConstraintViolationWithTemplate(this.message)
                    .addPropertyNode("nuclFacilGrId")
                    .addConstraintViolation();
                isValid = false;
            }
        }
        return isValid;
    }
}
