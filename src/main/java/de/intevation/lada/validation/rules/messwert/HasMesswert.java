/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.rules.messwert;

import de.intevation.lada.model.lada.MeasVal;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Violation;
import de.intevation.lada.validation.annotation.ValidationRule;
import de.intevation.lada.validation.rules.Rule;

/**
 * Validation rule for messwert.
 * Validates if the "messwert" was set correctly.
 *
 * @author <a href="mailto:mstanko@bfs.de">Michael Stanko</a>
 */
@ValidationRule("Messwert")
public class HasMesswert implements Rule {

    @Override
    public Violation execute(Object object) {
        MeasVal messwert = (MeasVal) object;
        String messwertNwg = messwert.getLessThanLOD();
        Double wert = messwert.getMeasVal();
        Violation violation = new Violation();
        if (messwertNwg == null && wert == null) {
            violation.addWarning("messwert", StatusCodes.VALUE_MISSING);
            return violation;
        } else if (messwertNwg != null && wert != null) {
            violation.addError("messwert", StatusCodes.VAL_MEASURE);
            return violation;
        }
        return null;    }
}
