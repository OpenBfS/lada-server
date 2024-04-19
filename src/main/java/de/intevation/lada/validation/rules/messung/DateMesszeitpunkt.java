/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.rules.messung;

import java.util.Date;

import jakarta.inject.Inject;

import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Violation;
import de.intevation.lada.validation.annotation.ValidationRule;
import de.intevation.lada.validation.rules.Rule;

/**
 * Validation rule for messungen.
 * Validates if the "messzeitpunkt" is before or after the
 * "probeentnahmebeginn"
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@ValidationRule("Messung")
public class DateMesszeitpunkt implements Rule {

    @Inject
    private Repository repository;

    @Override
    public Violation execute(Object object) {
        Measm messung = (Measm) object;

        Sample probe = repository.getById(
            Sample.class, messung.getSampleId());

        Date measmStartDate = messung.getMeasmStartDate();
        Date sampleStartDate = probe.getSampleStartDate();
        Date sampleEndDate = probe.getSampleEndDate();

        if (measmStartDate != null && (
                sampleStartDate != null && sampleStartDate.after(measmStartDate)
                || sampleEndDate != null && sampleEndDate.after(measmStartDate))
        ) {
            Violation violation = new Violation();
            violation.addWarning(
                "measmStartDate",
                StatusCodes.VALUE_NOT_MATCHING);
            return violation;
        }
        return null;
    }
}
