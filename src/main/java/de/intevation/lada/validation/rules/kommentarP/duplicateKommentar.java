/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.rules.kommentarP;

import java.util.List;

import javax.inject.Inject;

import de.intevation.lada.model.land.KommentarP;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.validation.Violation;
import de.intevation.lada.validation.annotation.ValidationRule;
import de.intevation.lada.validation.rules.Rule;

/**
 * Validation rule for probe.
 * Validates if the probe has a "entnahmeort".
 *
 */
@ValidationRule("KommentarP")
public class duplicateKommentar implements Rule {

    @Inject
    private Repository repository;

    @Override
    public Violation execute(Object object) {
        KommentarP kommentar = (KommentarP) object;
        Integer probeID  = kommentar.getProbeId();

        QueryBuilder<KommentarP> KommentarBuilder =
            repository.queryBuilder(KommentarP.class);
            KommentarBuilder.and("probeId", probeID);
        Response responseKommentar =
            repository.filter(KommentarBuilder.getQuery());
        List<KommentarP> KommentarExist = (List<KommentarP>) responseKommentar.getData();

        if (KommentarExist.stream().anyMatch(elem -> elem.getText().trim().replace(" ","").toUpperCase().equals(kommentar.getText().trim().replace(" ", "").toUpperCase())==true)) {
            Violation violation = new Violation();
         violation.addError("Kommentar", StatusCodes.VAL_EXISTS);
         return violation;
        } else {
            return null;
        }

    }
}

