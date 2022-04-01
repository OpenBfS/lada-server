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

import de.intevation.lada.model.land.KommentarM;
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
public class duplicateKommentar implements Rule {

    @Inject
    private Repository repository;

    @Override
    public Violation execute(Object object) {
        KommentarM kommentar = (KommentarM) object;
        Integer messungID  = kommentar.getMessungsId();

        QueryBuilder<KommentarM> KommentarBuilder =
            repository.queryBuilder(KommentarM.class);
            KommentarBuilder.and("messungsId", messungID);
        List<KommentarM> KommentarExist = (List<KommentarM>) repository.filterPlain(KommentarBuilder.getQuery());

        if (KommentarExist.stream().anyMatch(elem -> elem.getText().trim().replace(" ","").toUpperCase().equals(kommentar.getText().trim().replace(" ", "").toUpperCase())==true)) {
            Violation violation = new Violation();
         violation.addError("Kommentar", StatusCodes.VAL_EXISTS);
         return violation;
        } else {
            return null;
        }

    }
}
