/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
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
 * Validates if sampleEndDate is given if required.
 */
public class HasEndDateSampleValidator
    implements ConstraintValidator<HasEndDate, Sample> {

    private static final Integer SAMPLE_METH_X = 9;
    private static final Integer SAMPLE_METH_S = 3;
    private static final Integer REG_REI_I = 4;

    private String message;

    @Override
    public void initialize(HasEndDate constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Sample probe, ConstraintValidatorContext ctx) {
        if (probe == null) {
            return true;
        }
        Integer sampleMeth = probe.getSampleMethId();
        if (probe.getSampleEndDate() == null
            && (SAMPLE_METH_X.equals(sampleMeth)
                || !REG_REI_I.equals(probe.getRegulationId())
                && SAMPLE_METH_S.equals(sampleMeth))
        ) {
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(Sample_.SAMPLE_END_DATE)
                .addConstraintViolation();
            return false;
        }
        return true;
    }
}
