/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.rules.kommentarM;

import java.util.List;

import javax.inject.Inject;

import de.intevation.lada.model.lada.CommMeasm;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Violation;
import de.intevation.lada.validation.annotation.ValidationRule;
import de.intevation.lada.validation.rules.Rule;

/**
 * Validation rule for messung.
 * Validates if the messung has a kommentarM.
 *
 */
@ValidationRule("KommentarM")
public class DuplicateKommentar implements Rule {

    @Inject
    private Repository repository;

    @Override
    public Violation execute(Object object) {
        CommMeasm kommentar = (CommMeasm) object;
        Integer messungID  = kommentar.getMeasmId();

        QueryBuilder<CommMeasm> kommentarBuilder = repository
            .queryBuilder(CommMeasm.class)
            .and("measmId", messungID);
        List<CommMeasm> kommentarExist = repository.filterPlain(
            kommentarBuilder.getQuery());

        // TODO: Should be the job of EXISTS and a WHERE-clause in database
        if (kommentarExist.stream().anyMatch(
                elem -> elem.getText().trim().replace(" ", "").toUpperCase()
                .equals(kommentar.getText().trim().replace(" ", "")
                    .toUpperCase()))
        ) {
            Violation violation = new Violation();
            violation.addError("Kommentar", StatusCodes.VAL_EXISTS);
            return violation;
        }
        return null;
    }
}

