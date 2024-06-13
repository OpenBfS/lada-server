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
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@ValidationRule("Sample")
public class HasEntnahmeOrt implements Rule {

    private static final int REG_REI_I = 4;
    private static final int REG_REI_X = 3;
    private static final String TYPE_REG_E = "E";
    private static final String TYPE_REG_R = "R";

    @Inject
    private Repository repository;

    @Override
    public Violation execute(Object object) {
        Sample probe = (Sample) object;
        Integer id = probe.getId();
        if (id == null) {
            Violation violation = new Violation();
            violation.addWarning("geolocats", StatusCodes.VALUE_MISSING);
            return violation;
        }

        final int regulation = probe.getRegulationId();
        final List<String> expectedTypeRegs =
            probe.getReiAgGrId() != null
            || regulation == REG_REI_X
            || regulation == REG_REI_I
            ? List.of(TYPE_REG_R)
            : List.of(TYPE_REG_E, TYPE_REG_R);
        QueryBuilder<Geolocat> builder = repository
            .queryBuilder(Geolocat.class)
            .and("sampleId", id)
            .andIn("typeRegulation", expectedTypeRegs);
        if (repository.filter(builder.getQuery()).isEmpty()) {
            Violation violation = new Violation();
            violation.addWarning("geolocats", StatusCodes.VALUE_MISSING);
            return violation;
        }
        return null;
    }
}
