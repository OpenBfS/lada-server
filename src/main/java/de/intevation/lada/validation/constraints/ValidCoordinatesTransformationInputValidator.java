/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.constraints;

import de.intevation.lada.util.data.KdaUtil.TransformationInput;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validate the coordinates in the given transformation input.
 */
public class ValidCoordinatesTransformationInputValidator
        extends ValidCoordinatesBaseValidator<TransformationInput> {
    @Override
    public boolean isValid(
        TransformationInput value, ConstraintValidatorContext context) {
        return isValid(value.x(), value.y(), value.spatRefSys());
    }
}
