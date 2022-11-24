/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.rules.messwert;

import java.util.List;

import javax.inject.Inject;

import de.intevation.lada.model.land.Messung;
import de.intevation.lada.model.land.Messwert;
import de.intevation.lada.model.stammdaten.Messgroesse;
import de.intevation.lada.model.stammdaten.MmtMessgroesse;
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
        Messwert messwert = (Messwert) object;
        Messung messung = repository.getByIdPlain(Messung.class, messwert.getMessungsId());

        QueryBuilder<MmtMessgroesse> mmtBuilder =
            repository.queryBuilder(MmtMessgroesse.class)
                .and("mmtId", messung.getMmtId());
        List<MmtMessgroesse> mmtMs =
            repository.filterPlain(mmtBuilder.getQuery());

        Violation violation = new Violation();
        boolean hit = false;
        for (MmtMessgroesse mmtM: mmtMs) {
            if (messwert.getMessgroesseId().equals(
                    mmtM.getMessgroesseId())) {
                hit = true;
            }
        }
        if (!hit) {
            Messgroesse mg = repository.getByIdPlain(
                Messgroesse.class, messwert.getMessgroesseId());
            violation.addWarning(
                "messgroesseId#" + mg.getMessgroesse(),
                StatusCodes.VAL_MESSGROESSE_NOT_MATCHING_MMT);
        }
        if (violation.hasWarnings()) {
            return violation;
        }
        return null;
    }
}
