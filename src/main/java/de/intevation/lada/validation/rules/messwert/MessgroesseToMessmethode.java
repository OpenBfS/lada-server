/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.rules.messwert;

import java.util.List;

import jakarta.inject.Inject;

import de.intevation.lada.model.lada.MeasVal;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.master.Measd;
import de.intevation.lada.model.master.MmtMeasdView;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Violation;
import de.intevation.lada.validation.annotation.ValidationRule;
import de.intevation.lada.validation.rules.Rule;

/**
 * Validation rule for messungen.
 * Validates if the "messgroesse" fits the "messmethode".
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@ValidationRule("Messwert")
public class MessgroesseToMessmethode implements Rule {

    @Inject
    private Repository repository;

    @Override
    public Violation execute(Object object) {
        MeasVal messwert = (MeasVal) object;
        Measm messung = repository.getByIdPlain(
                Measm.class, messwert.getMeasmId());

        QueryBuilder<MmtMeasdView> mmtBuilder =
            repository.queryBuilder(MmtMeasdView.class)
                .and("mmtId", messung.getMmtId());
        List<MmtMeasdView> mmtMs =
            repository.filterPlain(mmtBuilder.getQuery());

        Violation violation = new Violation();
        boolean hit = false;
        for (MmtMeasdView mmtM: mmtMs) {
            if (messwert.getMeasdId().equals(
                    mmtM.getMeasdId())) {
                hit = true;
            }
        }
        if (!hit) {
            Measd mg = repository.getByIdPlain(
                Measd.class, messwert.getMeasdId());
            violation.addWarning(
                "measdId#" + mg.getName(),
                StatusCodes.VAL_MESSGROESSE_NOT_MATCHING_MMT);
        }
        if (violation.hasWarnings()) {
            return violation;
        }
        return null;
    }
}
