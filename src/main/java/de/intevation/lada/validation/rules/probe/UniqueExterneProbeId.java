/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.rules.probe;

import java.util.List;

import javax.inject.Inject;

import de.intevation.lada.model.land.Probe;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Violation;
import de.intevation.lada.validation.annotation.ValidationRule;
import de.intevation.lada.validation.rules.Rule;

/**
 * Validation rule for probe.
 * Validates if the probe has a unique "ext_id".
 *
 */
@ValidationRule("Probe")
public class UniqueExterneProbeId implements Rule {

    @Inject
    private Repository repository;

    @Override
    public Violation execute(Object object) {
        Probe probe = (Probe) object;
        QueryBuilder<Probe> builder = repository.queryBuilder(Probe.class);
        builder.and("sampleExtId", probe.getSampleExtId());
        List<Probe> existing =
            repository.filterPlain(builder.getQuery());
        if (!existing.isEmpty()) {
            Probe found = existing.get(0);
            // The probe found in the db equals the new probe. (Update)
            if (probe.getId() != null && probe.getId().equals(found.getId())) {
                return null;
            }
            Violation violation = new Violation();
            violation.addError("sampleExtId", StatusCodes.VALUE_AMBIGOUS);
            return violation;
        }
        return null;
    }
}
