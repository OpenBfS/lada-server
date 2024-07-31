/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.constraints;

import jakarta.validation.ConstraintValidatorContext;

import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.lada.Sample_;


/**
 * Validates that EnvMedium unambiguously matches the envDescripDisplay
 * in REI and §161 contexts.
 */
public class EnvDescripMatchesEnvMediumReiOr161SampleValidator
    extends EnvDescripMatchesEnvMediumReiOr161Validator<Sample> {

    @Override
    public boolean isValid(Sample sample, ConstraintValidatorContext ctx) {
        if (sample == null) {
            return true;
        }
        if (doValidation(
            sample.getRegulationId(),
            sample.getEnvDescripDisplay(),
            sample.getEnvMediumId())
        ) {
            return true;
        }
        ctx.disableDefaultConstraintViolation();
        ctx.buildConstraintViolationWithTemplate(this.message)
            .addPropertyNode(Sample_.ENV_MEDIUM_ID)
            .addConstraintViolation();
        return false;
    }
}
