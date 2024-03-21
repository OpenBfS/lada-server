/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.constraints;

import de.intevation.lada.model.master.Site;
import de.intevation.lada.util.data.KdaUtil;
import jakarta.validation.ConstraintValidatorContext;

public class ValidCoordinatesSiteValidator
    extends ValidCoordinatesBaseValidator<Site>{

    @Override
    public boolean isValid(Site value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        Integer spatRefSysId = value.getSpatRefSysId();
        return
            spatRefSysId == null
            || isValid(value.getCoordXExt(), value.getCoordYExt(),
                KdaUtil.KDAS.get(spatRefSysId), context);
    }
}
