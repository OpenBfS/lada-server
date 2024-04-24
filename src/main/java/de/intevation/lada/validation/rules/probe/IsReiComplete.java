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
 * Validates if the probe has valid REI attributes.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@ValidationRule("Sample")
public class IsReiComplete implements Rule {

    private static final int REG_REI_I = 4;
    private static final int REG_REI_X = 3;

    @Override
    public Violation execute(Object object) {
        Sample probe = (Sample) object;
        if (probe == null || probe.getRegulationId() == null) {
            return null;
        }

        final int regulation = probe.getRegulationId();
        Violation violation = new Violation();
        if (regulation != REG_REI_X && regulation != REG_REI_I) {
            if (probe.getReiAgGrId() != null) {
                violation.addWarning(
                    "reiAgGrId", StatusCodes.VALUE_NOT_MATCHING);
            }
            if (probe.getNuclFacilGrId() != null) {
                violation.addWarning(
                    "nuclFacilGrId", StatusCodes.VALUE_NOT_MATCHING);
            }
            if (violation.hasWarnings()) {
                return violation;
            }
            return null;
        }
        if (probe.getReiAgGrId() == null) {
            violation.addWarning(
                "reiAgGrId", StatusCodes.VALUE_MISSING);
        }
        if (probe.getNuclFacilGrId() == null) {
            violation.addWarning(
                "nuclFacilGrId", StatusCodes.VALUE_MISSING);
        }
        if (violation.hasWarnings()) {
            return violation;
        }
        return null;
    }
}
