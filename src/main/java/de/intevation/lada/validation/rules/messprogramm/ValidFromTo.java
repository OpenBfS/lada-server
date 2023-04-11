/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.rules.messprogramm;

import de.intevation.lada.model.lada.Mpg;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Violation;
import de.intevation.lada.validation.annotation.ValidationRule;
import de.intevation.lada.validation.rules.Rule;

/**
 * Validation rule for Messprogramm.
 * Validates if the validity period is meaningful.
 */
@ValidationRule("Messprogramm")
public class ValidFromTo implements Rule {

    private static final int DOY_MIN = 1;

    // Leap years should be handled in generation of Sample objects
    private static final int DOY_MAX = 365;

    @Override
    public Violation execute(Object object) {
        Mpg messprogramm = (Mpg) object;
        Violation violation = new Violation();

        if (messprogramm.getValidStartDate() < DOY_MIN
                || messprogramm.getValidStartDate() > DOY_MAX) {
                violation.addError(
                    "validStartDate", StatusCodes.VALUE_OUTSIDE_RANGE);
            }

        if (messprogramm.getValidEndDate() < DOY_MIN
                || messprogramm.getValidEndDate() > DOY_MAX) {
                violation.addError(
                    "validEndDate", StatusCodes.VALUE_OUTSIDE_RANGE);
            }

        return violation.hasErrors()
            ? violation
            : null;
    }
}
