/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.rules.status;

import javax.inject.Inject;

import org.jboss.logging.Logger;

import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.lada.StatusProt;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Violation;
import de.intevation.lada.validation.annotation.ValidationRule;
import de.intevation.lada.validation.rules.Rule;

/**
 * Validation rule for status.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@ValidationRule("Status")
public class IsReiComplete implements Rule {

    @Inject Logger logger;

    @Inject
    private Repository repository;

    @Override
    public Violation execute(Object object) {
        StatusProt status = (StatusProt) object;

        Measm messung =
            repository.getByIdPlain(
                Measm.class, status.getMeasmId());
        Sample probe =
            repository.getByIdPlain(Sample.class, messung.getSampleId());
        if (!Integer.valueOf(3).equals(probe.getRegulationId())
            && !Integer.valueOf(4).equals(probe.getRegulationId())) {
            return null;
        }
        Violation violation = new Violation();
        if (probe.getReiAgGrId() == null) {
            violation.addError("reiProgpunktGrpId", StatusCodes.VALUE_MISSING);
        }
        if (probe.getNuclFacilGrId() == null) {
            violation.addError("ktaGruppeId", StatusCodes.VALUE_MISSING);
        }
        if (violation.hasErrors()) {
            return violation;
        }
        return null;
    }
}
