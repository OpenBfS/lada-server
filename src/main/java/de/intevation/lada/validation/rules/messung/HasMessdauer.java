/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.rules.messung;

import javax.inject.Inject;

import de.intevation.lada.validation.Violation;
import de.intevation.lada.validation.annotation.ValidationRule;
import de.intevation.lada.validation.rules.Rule;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
/**
 * Validation rule for messungen.
 * Validates if the messung has a "nebenprobennummer"
 *
 * @author <a href="mailto:jbuermeyer@bfs.de">Jonas Buermeyer</a>
 */
@ValidationRule("Messung")
public class HasMessdauer implements Rule {

    @Inject
    private Repository repository;

    @Override
    public Violation execute(Object object) {
        Measm messung = (Measm) object;
        Sample probe =
            repository.getByIdPlain(Sample.class, messung.getSampleId());

        if (messung.getMeasPd() == null) {
            //Exception for continous samples or Datenbasis = §161
            if (probe.getSampleMethId() != null
                && probe.getSampleMethId() == 9
                || probe.getRegulationId() != null
                && probe.getRegulationId() == 1) {
                Violation violation = new Violation();
                violation.addNotification(
                    "measPd", StatusCodes.VALUE_MISSING);
                return violation;

            } else {
                Violation violation = new Violation();
                violation.addWarning(
                    "measPd#" + messung.getMinSampleId(),
                    StatusCodes.VALUE_MISSING);
                return violation;
            }
        }
        return null;
    }
}
