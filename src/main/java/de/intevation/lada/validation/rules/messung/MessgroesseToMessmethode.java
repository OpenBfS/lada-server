/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.rules.messung;

import java.util.List;

import javax.inject.Inject;

import de.intevation.lada.model.land.Messung;
import de.intevation.lada.model.land.Messwert;
import de.intevation.lada.model.stammdaten.Measd;
import de.intevation.lada.model.stammdaten.MmtMeasdView;
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
@ValidationRule("Messung")
public class MessgroesseToMessmethode implements Rule {

    @Inject
    private Repository repository;

    @Override
    public Violation execute(Object object) {
        Messung messung = (Messung) object;

        QueryBuilder<Messwert> builder =
            repository.queryBuilder(Messwert.class)
                .and("messungsId", messung.getId());
        List<Messwert> messwerte = repository.filterPlain(builder.getQuery());

        QueryBuilder<MmtMeasdView> mmtBuilder =
            repository.queryBuilder(MmtMeasdView.class)
                .and("mmtId", messung.getMmtId());
        List<MmtMeasdView> mmtMs =
            repository.filterPlain(mmtBuilder.getQuery());

        Violation violation = new Violation();
        for (Messwert messwert : messwerte) {
            boolean hit = false;
            for (MmtMeasdView mmtM: mmtMs) {
                if (messwert.getMessgroesseId().equals(
                        mmtM.getMeasdId())) {
                    hit = true;
                }
            }
            if (!hit) {
                Measd mg = repository.getByIdPlain(
                    Measd.class, messwert.getMessgroesseId());
                violation.addError(
                    "messgroesse#" + messung.getMmtId()
                    + " " + mg.getName(),
                    StatusCodes.VALUE_NOT_MATCHING);
            }
        }
        if (violation.hasErrors()) {
            return violation;
        }
        return null;
    }
}
