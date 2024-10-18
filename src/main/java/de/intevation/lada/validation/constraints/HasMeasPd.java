/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.constraints;

import jakarta.validation.ConstraintValidatorContext;

import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Measm_;
import de.intevation.lada.model.lada.Sample;


/**
 * Validation rule for Measm.
 * Validates if measPd is given.
 *
 * @author <a href="mailto:jbuermeyer@bfs.de">Jonas Buermeyer</a>
 */
abstract class HasMeasPd {
    /**
     * Validates if measPd is given.
     * @param messung Measm to be validated
     * @param sampleMeth9OrRegulation1 If true, only consider measms that
     * reference a sample with sampleMethId 9 or regulationId 1, else only
     * consider measms that do not reference such a sample.
     * @param ctx Context in which the constraint is evaluated
     * @param message Message to be used in case of violation
     * @return false if messung does not pass the constraint
     */
    boolean isValid(
        Measm messung,
        boolean sampleMeth9OrRegulation1,
        ConstraintValidatorContext ctx,
        String message
    ) {
        if (messung != null
            && messung.getSample() != null
            && messung.getMeasPd() == null
        ) {
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(Measm_.MEAS_PD)
                .addConstraintViolation();

            Sample probe = messung.getSample();
            //Exception for continous samples or Datenbasis = §161
            if (probe.getSampleMethId() != null
                && probe.getSampleMethId() == 9
                || probe.getRegulationId() != null
                && probe.getRegulationId() == 1
            ) {
                return !sampleMeth9OrRegulation1;
            }
            return sampleMeth9OrRegulation1;
        }
        return true;
    }
}
