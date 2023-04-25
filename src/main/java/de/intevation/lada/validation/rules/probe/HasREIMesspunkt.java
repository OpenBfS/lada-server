/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
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
 * Validates if the probe has a "REIMesspunkt".
 *
 * @author <a href="mailto:jbuermeyer@bfs.de">Jonas Buermeyer</a>
 */
@ValidationRule("Sample")
public class HasREIMesspunkt implements Rule {

    @Inject
    private Repository repository;

    @Override
    public Violation execute(Object object) {
        Sample probe = (Sample) object;
        Integer id = probe.getId();
        if (probe.getReiAgGrId() != null
            || Integer.valueOf(4).equals(probe.getRegulationId())) {
            QueryBuilder<Geolocat> builder =
                repository.queryBuilder(Geolocat.class);
            builder.and("sampleId", id);
            List<Geolocat> orte = repository.filterPlain(builder.getQuery());
            for (Geolocat ort: orte) {
                if ("R".equals(ort.getTypeRegulation())) {
                    return null;
                }
            }
            Violation violation = new Violation();
            violation.addWarning("REIMesspunkt", StatusCodes.VALUE_MISSING);
            return violation;
       }
       return null;
    }
}
