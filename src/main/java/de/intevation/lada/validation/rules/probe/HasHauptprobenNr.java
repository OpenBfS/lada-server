/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.rules.probe;

import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Violation;
import de.intevation.lada.validation.annotation.ValidationRule;
import de.intevation.lada.validation.rules.Rule;

/**
 * Validation rule for probe.
 * Validates if the probe has a "hauptprobennr".
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@ValidationRule("Sample")
public class HasHauptprobenNr implements Rule {

    @Override
    public Violation execute(Object object) {
        Sample probe = (Sample) object;
        if (probe.getMainSampleId() == null
            || probe.getMainSampleId().equals("")
        ) {
            Violation violation = new Violation();
            violation.addNotification(
                "mainSampleId", StatusCodes.VALUE_MISSING);
            return violation;
        }
        return null;
    }
}
