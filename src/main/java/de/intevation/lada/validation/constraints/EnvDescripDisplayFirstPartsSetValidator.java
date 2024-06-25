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
import de.intevation.lada.model.lada.Sample_;


/**
 * Validation rule for sample.
 * Validates if the first parts of envDescripDisplay are set correctly.
 */
public class EnvDescripDisplayFirstPartsSetValidator
    implements ConstraintValidator<EnvDescripDisplayFirstPartsSet, Sample> {

    private static final String UNSET = "00";

    private String message;

    @Override
    public void initialize(
        EnvDescripDisplayFirstPartsSet constraintAnnotation
    ) {
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Sample probe, ConstraintValidatorContext ctx) {
        if (probe == null || probe.getEnvDescripDisplay() == null) {
            return true;
        }

        String[] mediaDesk = probe.getEnvDescripDisplay().split(" ");
        // leave it up to Pattern constraint to ensure a valid mediaDesk string.
        // Just avoid IndexOutOfBoundsException here
        if (mediaDesk.length < 3) {
            return true;
        }

        if (probe.getRegulationId() != null
            && probe.getRegulationId() == 4
            && (mediaDesk[1].equals(UNSET) || mediaDesk[2].equals(UNSET))
        ) {
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate(this.message)
                .addPropertyNode(Sample_.ENV_DESCRIP_DISPLAY)
                .addConstraintViolation();
            return false;
        }
        return true;
    }

}
