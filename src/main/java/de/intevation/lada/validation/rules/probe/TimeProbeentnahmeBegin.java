/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.rules.probe;

import java.sql.Timestamp;
import java.util.Date;

import de.intevation.lada.model.land.Probe;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Violation;
import de.intevation.lada.validation.annotation.ValidationRule;
import de.intevation.lada.validation.rules.Rule;

/**
 * Validation rule for probe.
 * Validates if the "probeentnahmeBeginn" exists an if "probeentnahmeBeginn is
 * in future or after "probeentnahmeEnde".
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@ValidationRule("Probe")
public class TimeProbeentnahmeBegin implements Rule {

    @Override
    public Violation execute(Object object) {
        Probe probe = (Probe) object;
        Timestamp begin = probe.getProbeentnahmeBeginn();
        Timestamp end = probe.getProbeentnahmeEnde();
        if (begin == null) {
            if (end == null) {
                return null;
            }
            Violation violation = new Violation();
            violation.addWarning(
                "probeentnahmeBeginn", StatusCodes.DATE_BEGIN_AFTER_END);
            return violation;
        }
        if (begin.after(new Date())) {
            Violation violation = new Violation();
            violation.addWarning(
                "probeentnahmeBeginn", StatusCodes.DATE_IN_FUTURE);
            return violation;
        }
        if (end != null && begin.after(end)) {
            Violation violation = new Violation();
            violation.addWarning(
                "probeentnahmeBeginn", StatusCodes.DATE_BEGIN_AFTER_END);
            return violation;
        }
        return null;
    }
}
