/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.constraints;

import de.intevation.lada.rest.SpatRefSysService.PostData;
import de.intevation.lada.util.data.KdaUtil;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validate the coordinates in the given POST data.
 */
public class ValidCoordinatesPostDataValidator
        extends ValidCoordinatesBaseValidator<PostData>{
    @Override
    public boolean isValid(
        PostData value, ConstraintValidatorContext context) {
        return isValid(value.x(), value.y(), KdaUtil.KDAS.get(value.from()));
    }
}
