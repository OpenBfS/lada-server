/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.rules.probe;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import de.intevation.lada.model.land.Ortszuordnung;
import de.intevation.lada.model.land.Sample;
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
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@ValidationRule("Sample")
public class HasEntnahmeOrt implements Rule {

    @Inject
    private Repository repository;

    @Override
    public Violation execute(Object object) {
        Sample probe = (Sample) object;
        Integer id = probe.getId();
        if (id == null) {
            Violation violation = new Violation();
            violation.addWarning("entnahmeOrt", StatusCodes.VALUE_MISSING);
            return violation;
        }
        if (probe.getReiAgGrId() != null
            || Integer.valueOf(3).equals(probe.getRegulationId())
            || Integer.valueOf(4).equals(probe.getRegulationId())) {
                return null;
        }
        List<String> zuordTypeFilter = Arrays.asList("E", "R");
        QueryBuilder<Ortszuordnung> builder =
            repository.queryBuilder(Ortszuordnung.class);
        builder.and("sampleId", id);
        builder.andIn("typeRegulation", zuordTypeFilter);
        Response response = repository.filter(builder.getQuery());
        @SuppressWarnings("unchecked")
        List<Ortszuordnung> orte = (List<Ortszuordnung>) response.getData();

        if (orte.size()>1) {
            Violation violation = new Violation();
            violation.addWarning("entnahmeOrt", StatusCodes.VALUE_AMBIGOUS);
            return violation;
        }

        for (Ortszuordnung ort: orte) {
            if (("E".equals(ort.getTypeRegulation())
                    || "R".equals(ort.getTypeRegulation())
                    && probe.getRegulationId() != 4)
                    || "R".equals(ort.getTypeRegulation())
                && probe.getRegulationId() == 4
            ) {
                return null;
            }
        }
        Violation violation = new Violation();
        violation.addWarning("entnahmeOrt", StatusCodes.VALUE_MISSING);
        return violation;
    }

}
