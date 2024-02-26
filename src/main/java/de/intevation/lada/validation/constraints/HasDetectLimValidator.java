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

import de.intevation.lada.model.lada.MeasVal;


/**
 * Validation rule for MeasVal.
 * Validates if detectLim was set correctly.
 *
 * @author <a href="mailto:mstanko@bfs.de">Michael Stanko</a>
 */
public class HasDetectLimValidator
    implements ConstraintValidator<HasDetectLim, MeasVal> {

    @Override
    public boolean isValid(MeasVal messwert, ConstraintValidatorContext ctx) {
        if (messwert == null) {
            return true;
        }
        String messwertNwg = messwert.getLessThanLOD();
        Double nachweisgrenze = messwert.getDetectLim();
        boolean isValid = true;
        if (messwertNwg != null && nachweisgrenze == null) {
            isValid = false;
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate(HasDetectLim.MSG)
                .addPropertyNode("detectLim")
                .addConstraintViolation();
        }
        return isValid;
    }
}
