/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.constraints;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintValidatorContext;

import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.util.data.Repository;


/**
 * Validation rule for Measm.
 * Validates if the Measm has a measmStartDate.
 */
abstract class HasMeasmStartDate {
    /**
     * Validates if measmStartDate is given.
     * @param messung Measm to be validated
     * @param regulation1 If true, only consider measms that
     * reference a sample with regulationId 1, else only
     * consider measms that do not reference such a sample.
     * @param ctx Context in which the constraint is evaluated
     * @param message Message to be used in case of violation
     * @return false if messung does not pass the constraint
     */
    @Transactional
    boolean isValid(
        Measm messung,
        boolean regulation1,
        ConstraintValidatorContext ctx,
        String message
    ) {
        if (messung != null
            && messung.getSampleId() != null
            && messung.getMeasmStartDate() == null
        ) {
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate(message)
                .addPropertyNode("measmStartDate")
                .addConstraintViolation();

            Sample probe = CDI.current().getBeanContainer()
                .createInstance().select(Repository.class).get()
                .entityManager().find(Sample.class, messung.getSampleId());
            if (probe != null
                && probe.getRegulationId() != null
                && probe.getRegulationId() != 1
            ) {
                return !regulation1;
            }
            return regulation1;
        }
        return true;
    }
}
