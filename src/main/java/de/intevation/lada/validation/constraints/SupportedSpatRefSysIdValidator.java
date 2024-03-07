/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.constraints;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import de.intevation.lada.util.data.KdaUtil;


/**
 * Check if value is a SpatRefSys ID for which coordinate transformation
 * is supported.
 */
public class SupportedSpatRefSysIdValidator
    implements ConstraintValidator<SupportedSpatRefSysId, Integer> {

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext ctx) {
        return value == null || KdaUtil.KDAS.containsKey(value);
    }
}
