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

import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Measm_;
import de.intevation.lada.model.lada.Sample;


/**
 * Validation rule for Measm.
 * Validates that measuring occures after sampling.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class MeasuringAfterSamplingValidator
    implements ConstraintValidator<MeasuringAfterSampling, Measm> {

    private String message;

    @Override
    public void initialize(MeasuringAfterSampling constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Measm messung, ConstraintValidatorContext ctx) {
        if (messung == null) {
            return true;
        }

        Sample probe = messung.getSample();
        Date measmStartDate = messung.getMeasmStartDate();
        if (probe == null || measmStartDate == null) {
            return true;
        }

        Date sampleStartDate = probe.getSampleStartDate();
        Date sampleEndDate = probe.getSampleEndDate();
        if (sampleStartDate != null && sampleStartDate.after(measmStartDate)
            || sampleEndDate != null && sampleEndDate.after(measmStartDate)
        ) {
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate(this.message)
                .addPropertyNode(Measm_.MEASM_START_DATE)
                .addConstraintViolation();
            return false;
        }
        return true;
    }
}
