/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.rules.probe;

import java.util.List;

import jakarta.inject.Inject;

import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.validation.Violation;
import de.intevation.lada.validation.annotation.ValidationRule;
import de.intevation.lada.validation.rules.Rule;

/**
 * Validation rule for probe.
 * Validates if the probe has a unique "hauptprobennr".
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@ValidationRule("Sample")
public class UniqueHauptprobenNr implements Rule {

    @Inject
    private Repository repository;

    @SuppressWarnings("unchecked")
    @Override
    public Violation execute(Object object) {
        Sample probe = (Sample) object;
        if (probe.getMainSampleId() != null) {
            QueryBuilder<Sample> builder = repository.queryBuilder(Sample.class);
            builder.and("mainSampleId", probe.getMainSampleId());
            builder.and("measFacilId", probe.getMeasFacilId());
            Response response = repository.filter(builder.getQuery());
            if (!((List<Sample>) response.getData()).isEmpty()) {
                Sample found = ((List<Sample>) response.getData()).get(0);
                // The probe found in the db equals the new probe. (Update)
                if (probe.getId() != null
                    && probe.getId().equals(found.getId())
                ) {
                    return null;
                }
                Violation violation = new Violation();
                violation.addError("mainSampleId", StatusCodes.VALUE_AMBIGOUS);
                return violation;
            }
        }
        return null;
    }

}
