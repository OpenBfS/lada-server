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
 * Validates if the sampleMeth matches date values.
 */
public class DatesVsSampleMethValidator
    implements ConstraintValidator<DatesVsSampleMeth, Sample> {

    private static final Integer DATENBASIS_161 = 1;
    private static final Integer PROBENART_INDIVIDUAL = 1;

    private String message;

    @Override
    public void initialize(DatesVsSampleMeth constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Sample probe, ConstraintValidatorContext ctx) {
        Date end = probe.getSampleEndDate();
        Date begin = probe.getSampleStartDate();
        if (probe.getSampleMethId() != null
            && !DATENBASIS_161.equals(probe.getRegulationId())
            && begin != null && end != null && !begin.equals(end)
            && PROBENART_INDIVIDUAL.equals(probe.getSampleMethId())
        ) {
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate(this.message)
                .addPropertyNode("sampleMethId")
                .addConstraintViolation();
            return false;
        }
        return true;
    }
}
