/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.rules.messung;

import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;

import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Violation;
import de.intevation.lada.validation.annotation.ValidationRule;
import de.intevation.lada.validation.rules.Rule;

/**
 * Validation rule for messungen.
 * Validates if the nebenprobennr is unique for a probe.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@ValidationRule("Messung")
public class UniqueNebenprobenNr implements Rule {

    @Inject
    private Repository repository;

    @SuppressWarnings("unchecked")
    @Override
    public Violation execute(Object object) {
        Measm messung = (Measm) object;
        if (messung.getMinSampleId() != null) {
            QueryBuilder<Measm> builder = repository.queryBuilder(Measm.class)
                .and("minSampleId", messung.getMinSampleId())
                .and("sampleId", messung.getSampleId());
            try {
                Measm found = repository.getSinglePlain(builder.getQuery());
                // The messung found in the db equals the new messung. (Update)
                if (messung.getId() != null
                    && messung.getId().equals(found.getId())) {
                    return null;
                }
                Violation violation = new Violation();
                violation.addError("minSampleId", StatusCodes.VALUE_AMBIGOUS);
                return violation;
            } catch (NoResultException nre) {
                return null;
            }
        }
        return null;
    }
}
