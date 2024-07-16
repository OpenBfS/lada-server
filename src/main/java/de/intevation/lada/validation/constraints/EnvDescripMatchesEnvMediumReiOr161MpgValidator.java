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
 * Validates that EnvMedium unambiguously matches the envDescripDisplay
 * in REI and §161 contexts.
 */
public class EnvDescripMatchesEnvMediumReiOr161MpgValidator
    extends EnvDescripMatchesEnvMediumReiOr161Validator<Mpg> {

    @Override
    public boolean isValid(Mpg mpg, ConstraintValidatorContext ctx) {
        if (mpg == null) {
            return true;
        }
        if (doValidation(
            mpg.getRegulationId(),
            mpg.getEnvDescripDisplay(),
            mpg.getEnvMediumId())
        ) {
            return true;
        }
        ctx.disableDefaultConstraintViolation();
        ctx.buildConstraintViolationWithTemplate(this.message)
            .addPropertyNode(Mpg_.ENV_MEDIUM_ID)
            .addConstraintViolation();
        return false;
    }
}
