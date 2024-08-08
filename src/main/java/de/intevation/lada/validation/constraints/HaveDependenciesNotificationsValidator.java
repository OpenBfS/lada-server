/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.constraints;

import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import de.intevation.lada.model.lada.StatusProt;


/**
 * Validation rule for status.
 * A StatusProt is considered invalid for certain statusMpIds, if validation
 * of any related Sample, Measm, MeasVal or Site returns notificactions.
 */
public class HaveDependenciesNotificationsValidator
    extends ValidDependenciesValidator
    implements ConstraintValidator<HaveDependenciesNotifications, StatusProt> {

    private String message;

    @Override
    public void initialize(HaveDependenciesNotifications constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Transactional
    @Override
    public boolean isValid(
        StatusProt status, ConstraintValidatorContext ctx
    ) {
        return doValidation(status, ctx, this.message, true);
    }
}
