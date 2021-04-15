/* Copyright (C) 2021 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.rules.messwert;

import java.util.List;

import javax.inject.Inject;

import de.intevation.lada.model.land.Messwert;
import de.intevation.lada.util.annotation.RepositoryConfig;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.RepositoryType;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Violation;
import de.intevation.lada.validation.annotation.ValidationRule;
import de.intevation.lada.validation.rules.Rule;

/**
 * Validation rule for messwert.
 * Validates if the "messgroesse" is unique for the parent Messung object
 */
@ValidationRule("Messwert")
public class UniqueMessungMessgroesse implements Rule {

    @Inject
    @RepositoryConfig(type = RepositoryType.RO)
    private Repository repository;

    @Override
    public Violation execute(Object object) {
        Messwert messwert = (Messwert) object;
        QueryBuilder<Messwert> messwertQuery = new QueryBuilder<Messwert>(
            repository.entityManager(),
            Messwert.class);
        messwertQuery.and("messungsId", messwert.getMessungsId());
        messwertQuery.and("messgroesseId", messwert.getMessgroesseId());
        List<Messwert> result =
            repository.filterPlain(messwertQuery.getQuery());
        if (!result.isEmpty()
            && !result.get(0).getId().equals(messwert.getId())
        ) {
            Violation violation = new Violation();
            violation.addError("messgroesse", StatusCodes.VALUE_AMBIGOUS);
            return violation;
        }
        return null;
    }
}
