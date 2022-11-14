/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.rules.probe;

import java.sql.Timestamp;
import de.intevation.lada.model.land.Probe;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Violation;
import de.intevation.lada.validation.annotation.ValidationRule;
import de.intevation.lada.validation.rules.Rule;

/**
 * Validation rule for probe.
 * Validates if the "probeart" matches date values.
 *
 */
@ValidationRule("Probe")
public class CheckProbeart implements Rule {

    private static final Integer DATENBASIS_161 = 1;
    private static final Integer PROBENART_INDIVIDUAL = 1;

    @Override
    public Violation execute(Object object) {
        Probe probe = (Probe) object;
        Timestamp end = probe.getProbeentnahmeEnde();
        Timestamp begin = probe.getProbeentnahmeBeginn();
        if (probe.getProbenartId() != null
            && !DATENBASIS_161.equals(probe.getDatenbasisId())) {
          if (begin != null && end != null
              && !begin.equals(end)
              && PROBENART_INDIVIDUAL.equals(probe.getProbenartId())) {
            Violation violation = new Violation();
            violation.addWarning("probenartId", StatusCodes.VAL_SINGLE_DATE);
            return violation;
          }
        } else {
            return null;
          }
        return null;
    }
}
