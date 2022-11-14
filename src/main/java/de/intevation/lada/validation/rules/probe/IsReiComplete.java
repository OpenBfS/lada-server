/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.rules.probe;

import de.intevation.lada.model.land.Probe;
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
@ValidationRule("Probe")
public class IsReiComplete implements Rule {

    @Override
    public Violation execute(Object object) {
        Probe probe = (Probe) object;
        Violation violation = new Violation();
        if (probe.getRegulationId() == null) {
            return null;
        }
        if (probe.getRegulationId() != 3
            && probe.getRegulationId() != 4
        ) {
            if (probe.getReiAgGrId() != null) {
                violation.addError(
                    "reiProgpunktGrpId", StatusCodes.VALUE_NOT_MATCHING);
            }
            if (probe.getNuclFacilGrId() != null) {
                violation.addError(
                    "ktaGruppeId", StatusCodes.VALUE_NOT_MATCHING);
            }
            if (violation.hasErrors()) {
                return violation;
            }
            return null;
        }
        if (probe.getReiAgGrId() == null) {
            violation.addWarning(
                "reiProgpunktGrpId", StatusCodes.VALUE_MISSING);
        }
        if (probe.getNuclFacilGrId() == null) {
            violation.addWarning(
                "ktaGruppeId", StatusCodes.VALUE_MISSING);
        }
        if (violation.hasWarnings()) {
            return violation;
        }
        return null;
    }
}
