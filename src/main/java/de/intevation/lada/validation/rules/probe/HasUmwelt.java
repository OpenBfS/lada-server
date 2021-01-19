/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.rules.probe;

import de.intevation.lada.model.land.Probe;
import de.intevation.lada.validation.Violation;
import de.intevation.lada.validation.annotation.ValidationRule;
import de.intevation.lada.validation.rules.Rule;

/**
 * Validation rule for probe.
 * Validates if the probe has a "umwelt bereich".
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@ValidationRule("Probe")
public class HasUmwelt implements Rule {

    @Override
    public Violation execute(Object object) {
        Probe probe = (Probe) object;
        if (probe.getUmwId() == null
            || probe.getUmwId().equals("")
        ) {
            Violation violation = new Violation();
            violation.addWarning("umwId", 631);
            return violation;
        }
        return null;
    }
}
