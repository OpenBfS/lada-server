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
 * Validates if measPd is given for measms that reference a sample with
 * neither sampleMethId 9 nor regulationId 1.
 */
public class HasMeasPdNotSampleMeth9OrRegulation1Validator extends HasMeasPd
    implements ConstraintValidator<HasMeasPdNotSampleMeth9OrRegulation1, Measm> {

    private String message;

    @Override
    public void initialize(
        HasMeasPdNotSampleMeth9OrRegulation1 constraintAnnotation
    ) {
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Measm measm, ConstraintValidatorContext ctx) {
        return isValid(measm, false, ctx, this.message);
    }
}
