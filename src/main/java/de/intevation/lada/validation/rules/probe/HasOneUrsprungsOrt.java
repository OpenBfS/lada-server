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

import de.intevation.lada.model.lada.Geolocat;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Violation;
import de.intevation.lada.validation.annotation.ValidationRule;
import de.intevation.lada.validation.rules.Rule;

/**
 * Validation rule for probe.
 * Validates if the probe has a "entnahmeort".
 *
 */
@ValidationRule("Sample")
public class HasOneUrsprungsOrt implements Rule {

    private static final int REG_REI_I = 4;
    private static final int REG_REI_X = 3;

    @Inject
    private Repository repository;

    @Override
    public Violation execute(Object object) {
        Sample probe = (Sample) object;
        Integer id = probe.getId();

        final int regulation = probe.getRegulationId();
        if (id == null
            || probe.getReiAgGrId() != null
            || regulation == REG_REI_X
            || regulation == REG_REI_I
        ) {
            return null;
        }

        QueryBuilder<Geolocat> builder = repository
            .queryBuilder(Geolocat.class)
            .and("sampleId", id)
            .andIn("typeRegulation", List.of("U", "R"));
        List<Geolocat> orte = repository.filter(builder.getQuery());
        if (orte.size() > 1) {
            Violation violation = new Violation();
            violation.addWarning("geolocats", StatusCodes.ORT_SINGLE_UORT);
            return violation;
        }
        return null;
    }
}
