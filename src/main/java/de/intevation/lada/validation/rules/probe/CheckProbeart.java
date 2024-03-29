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
 * Validates if the "probeart" matches date values.
 *
 */
@ValidationRule("Sample")
public class CheckProbeart implements Rule {

    private static final Integer DATENBASIS_161 = 1;
    private static final Integer PROBENART_INDIVIDUAL = 1;

    @Override
    public Violation execute(Object object) {
        Sample probe = (Sample) object;
        Date end = probe.getSampleEndDate();
        Date begin = probe.getSampleStartDate();
        if (probe.getSampleMethId() != null
            && !DATENBASIS_161.equals(probe.getRegulationId())) {
          if (begin != null && end != null
              && !begin.equals(end)
              && PROBENART_INDIVIDUAL.equals(probe.getSampleMethId())) {
            Violation violation = new Violation();
            violation.addWarning("sampleMethId", StatusCodes.VAL_SINGLE_DATE);
            return violation;
          }
        } else {
            return null;
          }
        return null;
    }
}
