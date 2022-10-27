/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.rules.probe;

import java.sql.Timestamp;

import de.intevation.lada.model.land.Sample;
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
public class CheckUrsprungszeit implements Rule {

    @Override
    public Violation execute(Object object) {
        Sample probe = (Sample) object;
        Timestamp uZeit = probe.getOrigDate();
        Timestamp begin = probe.getSampleStartDate();
        if (uZeit != null && begin != null && uZeit.after(begin)
        ) {
            Violation violation = new Violation();
            violation.addWarning(
                "ursprungszeit", StatusCodes.URSPR_Date_BEFORE_BEGIN);
            return violation;
        }
        return null;
    }

}
