/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.rules.probe;

import java.util.Date;

import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Violation;
import de.intevation.lada.validation.annotation.ValidationRule;
import de.intevation.lada.validation.rules.Rule;

/**
 * Validation rule for probe.
 * Validates if the probe has a "probeentnahmeEnde".
 */
@ValidationRule("Sample")
public class HasProbeentnahmeEnde implements Rule {

    @Override
    public Violation execute(Object object) {
        Sample probe = (Sample) object;
        Integer regulation = probe.getRegulationId();
        Integer sampleMeth = probe.getSampleMethId();
        Date ende = probe.getSampleEndDate();
        Date begin = probe.getSampleStartDate();
        if (regulation != null && sampleMeth != null
            && (regulation == 4 && sampleMeth == 9 && ende == null
                || regulation != 4
                && (sampleMeth == 9 || sampleMeth == 3)
                && (ende == null || begin != null && ende.before(begin)))
        ) {
            Violation violation = new Violation();
            violation.addWarning(
                "sampleEndDate", StatusCodes.VALUE_MISSING);
            return violation;
        }
        return null;
    }
}
