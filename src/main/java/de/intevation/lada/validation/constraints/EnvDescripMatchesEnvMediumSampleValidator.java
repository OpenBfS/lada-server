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
 * Validates if envMediumId fits the descriptor string.
 */
public class EnvDescripMatchesEnvMediumSampleValidator
    extends EnvDescripMatchesEnvMediumValidator<Sample> {

    @Override
    public boolean isValid(Sample sample, ConstraintValidatorContext ctx) {
        return sample == null
            || doValidation(
                ctx,
                Sample_.ENV_MEDIUM_ID,
                sample.getEnvDescripDisplay(),
                sample.getEnvMediumId(),
                sample.getRegulationId());
    }
}
