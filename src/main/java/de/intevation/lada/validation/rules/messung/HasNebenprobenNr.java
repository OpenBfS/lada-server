/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.rules.messung;

import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Violation;
import de.intevation.lada.validation.annotation.ValidationRule;
import de.intevation.lada.validation.rules.Rule;

/**
 * Validation rule for messungen.
 * Validates if the messung has a "nebenprobennummer"
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@ValidationRule("Messung")
public class HasNebenprobenNr implements Rule {

    @Override
    public Violation execute(Object object) {
        Measm messung = (Measm) object;
        if (messung.getMinSampleId() == null
            || messung.getMinSampleId().equals("")) {
            Violation violation = new Violation();
            violation.addNotification(
                "minSampleId", StatusCodes.VALUE_MISSING);
            return violation;
        }
        return null;
    }

}
