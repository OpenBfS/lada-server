/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.rules.messung;

import javax.inject.Inject;

import de.intevation.lada.model.land.Messung;
import de.intevation.lada.model.land.Probe;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Violation;
import de.intevation.lada.validation.annotation.ValidationRule;
import de.intevation.lada.validation.rules.Rule;
import de.intevation.lada.util.data.Repository;
/**
 * Validation rule for messung.
 * Validates if the messung has a "messbeginn".
 *
 *
 */
@ValidationRule("Messung")
public class HasMessbeginn implements Rule {

    @Inject
    private Repository repository;

    @Override
    public Violation execute(Object object) {
        Messung messung = (Messung) object;
        Probe probe =
            repository.getByIdPlain(Probe.class, messung.getProbeId());
        if (messung.getMesszeitpunkt() == null
            && (
                probe.getRegulationId() != null
                && probe.getRegulationId() != 1
            )) {
            Violation violation = new Violation();
            violation.addWarning("messzeitpunkt", StatusCodes.VALUE_MISSING);
            return violation;
        } else if (messung.getMesszeitpunkt() == null) {
            Violation violation = new Violation();
            violation.addNotification(
                "messzeitpunkt", StatusCodes.VALUE_MISSING);
            return violation;
        } else {
            return null;
        }
    }

}
