/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.rules.probe;

import java.sql.Timestamp;

import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Violation;
import de.intevation.lada.validation.annotation.ValidationRule;
import de.intevation.lada.validation.rules.Rule;

/**
 * Validation rule for probe.
 * Validates if the probe has a "probeentnahmeEnde".
 *
 *
 */
@ValidationRule("Sample")
public class HasProbeentnahmeEnde implements Rule {

    @Override
    public Violation execute(Object object) {
        Sample probe = (Sample) object;
        Timestamp ende = probe.getSampleEndDate();
        Timestamp begin = probe.getSampleStartDate();
        if (probe.getRegulationId() != null
            && probe.getSampleMethId() != null
            && ((probe.getRegulationId() == 4
            && probe.getSampleMethId() == 9
            && ende == null)
            || ((probe.getSampleMethId() == 9
                || probe.getSampleMethId() == 3)
            && probe.getRegulationId() != 4
            && (ende == null || ende.before(begin))))
        ) {
            Violation violation = new Violation();
            violation.addWarning(
                "probeentnahmeEnde", StatusCodes.VALUE_MISSING);
            return violation;
        }
        return null;
    }

}
