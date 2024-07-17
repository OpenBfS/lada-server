/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.constraints;

import jakarta.validation.ConstraintValidatorContext;

import de.intevation.lada.model.lada.Mpg;
import de.intevation.lada.model.lada.Mpg_;


/**
 * Validates if envMediumId fits the descriptor string.
 */
public class EnvDescripMatchesEnvMediumMpgValidator
    extends EnvDescripMatchesEnvMediumValidator<Mpg> {

    @Override
    public boolean isValid(Mpg mpg, ConstraintValidatorContext ctx) {
        return mpg == null
            || doValidation(
                ctx,
                Mpg_.ENV_MEDIUM_ID,
                mpg.getEnvDescripDisplay(),
                mpg.getEnvMediumId(),
                mpg.getRegulationId());
    }
}
