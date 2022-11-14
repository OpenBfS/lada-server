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

import de.intevation.lada.model.land.Ortszuordnung;
import de.intevation.lada.model.land.Probe;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.validation.Violation;
import de.intevation.lada.validation.annotation.ValidationRule;
import de.intevation.lada.validation.rules.Rule;

/**
 * Validation rule for probe.
 * Validates if the probe has a "REIMesspunkt".
 *
 * @author <a href="mailto:jbuermeyer@bfs.de">Jonas Buermeyer</a>
 */
@ValidationRule("Probe")
public class HasREIMesspunkt implements Rule {

    @Inject
    private Repository repository;

    @Override
    public Violation execute(Object object) {
        Probe probe = (Probe) object;
        Integer id = probe.getId();
        if (id == null) {
            Violation violation = new Violation();
            violation.addWarning("extPID", StatusCodes.VALUE_MISSING);
            return violation;
        }
        if (probe.getReiAgGrId() != null
            || Integer.valueOf(4).equals(probe.getRegulationId())) {
            QueryBuilder<Ortszuordnung> builder =
                repository.queryBuilder(Ortszuordnung.class);
            builder.and("probeId", id);
            Response response = repository.filter(builder.getQuery());
            @SuppressWarnings("unchecked")
            List<Ortszuordnung> orte = (List<Ortszuordnung>) response.getData();
            for (Ortszuordnung ort: orte) {
                if ("R".equals(ort.getOrtszuordnungTyp())) {
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
