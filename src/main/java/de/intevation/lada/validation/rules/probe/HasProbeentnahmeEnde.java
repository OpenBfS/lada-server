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
 * Validates if the probe has a "probeentnahmeEnde".
 */
@ValidationRule("Sample")
public class HasProbeentnahmeEnde implements Rule {

    private static final Integer SAMPLE_METH_X = 9;
    private static final Integer SAMPLE_METH_S = 3;
    private static final Integer REG_REI_I = 4;

    @Override
    public Violation execute(Object object) {
        Sample probe = (Sample) object;
        Integer sampleMeth = probe.getSampleMethId();
        if (probe.getSampleEndDate() == null
            && (SAMPLE_METH_X.equals(sampleMeth)
                || !REG_REI_I.equals(probe.getRegulationId())
                && SAMPLE_METH_S.equals(sampleMeth))
        ) {
            Violation violation = new Violation();
            violation.addWarning(
                "sampleEndDate", StatusCodes.VALUE_MISSING);
            return violation;
        }
        return null;
    }
}
