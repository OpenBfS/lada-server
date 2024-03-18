/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.constraints;

import de.intevation.lada.util.data.KdaUtil;
import de.intevation.lada.util.data.KdaUtil.KDA;
import de.intevation.lada.util.data.KdaUtil.Result;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public abstract class ValidCoordinatesBaseValidator<T>
        implements ConstraintValidator<ValidCoordinates, T> {

    /**
     * Check if the given coordinate strings are valid.
     * @param x X coordinate
     * @param y y coordinate
     * @param kda Reference system to use
     * @param ctx Validator context
     * @return True if valid, else false
     */
    protected boolean isValid(String x, String y, KDA kda,
            ConstraintValidatorContext ctx) {
        if (x == null || y == null) {
            return true;
        }
        boolean valid = switch (kda) {
            case GK -> validateGkCoordinates(x, y);
            case GS -> validateGsCoordinates(x, y);
            case GD -> validateGdCoordinates(x, y);
            case UTM_WGS84, UTM_ETRS89, UTM_ED50
                -> validateUtmCoordinates(x, y);
            default -> false;
        };
        if (!valid) {
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate(ValidCoordinates.MSG)
                .addPropertyNode("coordXExt")
                .addConstraintViolation();
            ctx.buildConstraintViolationWithTemplate(ValidCoordinates.MSG)
                .addPropertyNode("coordYExt")
                .addConstraintViolation();
        }
        return valid;
    }

    private boolean validateGkCoordinates(String x, String y) {
        return KdaUtil.X_GK.matcher(x).matches()
            && KdaUtil.Y.matcher(y).matches();
    }

    private boolean validateGsCoordinates(String x, String y) {
        KdaUtil kdaUtil = new KdaUtil();
        if (!(KdaUtil.LON_DEC.matcher(x).matches()
                && KdaUtil.LAT_DEC.matcher(y).matches()
                || KdaUtil.LON.matcher(x).matches()
                && KdaUtil.LAT.matcher(y).matches())) {
            return false;
        }
        Result decimal = kdaUtil.arcToDegree(x, y);
        if (decimal == null) {
            return false;
        }
        double dX;
        double dY;
        try {
            dX = Double.parseDouble(decimal.getX());
            dY = Double.parseDouble(decimal.getY());
        } catch (NumberFormatException nfe) {
            return false;
        }
        if (dX < -KdaUtil.MAX_LON || dX > KdaUtil.MAX_LON
            || dY < -KdaUtil.MAX_LAT || dY > KdaUtil.MAX_LAT
        ) {
            return false;
        }
        return true;
    }

    private boolean validateGdCoordinates(String x, String y) {
        try {
            double dX = Double.parseDouble(x), dY = Double.parseDouble(y);
            if (dX < -KdaUtil.MAX_LON || dX > KdaUtil.MAX_LON
                || dY < -KdaUtil.MAX_LAT || dY > KdaUtil.MAX_LAT
            ) {
                return false;
            }
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    private boolean validateUtmCoordinates(String x, String y) {
        return KdaUtil.X_UTM.matcher(x).matches()
                && KdaUtil.Y.matcher(y).matches();
    }
}
