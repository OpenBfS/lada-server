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
import de.intevation.lada.model.stammdaten.ReiProgpunktGrpUmwZuord;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Violation;
import de.intevation.lada.validation.annotation.ValidationRule;
import de.intevation.lada.validation.rules.Rule;

/**
 * Validation rule for probe.
 * Validates if the umwelt id fits the deskriptor string.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@ValidationRule("Probe")
public class ReiToUmwelt implements Rule {

    @Inject
    private Repository repository;

    @Override
    public Violation execute(Object object) {
        Probe probe = (Probe) object;
        if (probe.getDatenbasisId() != null
            && probe.getDatenbasisId() != 3
            && probe.getDatenbasisId() != 4
        ) {
            return null;
        }
        if (probe.getUmwId() == null) {
            return null;
        }
        if (probe.getReiProgpunktGrpId() == null) {
            return null;
        }
        QueryBuilder<ReiProgpunktGrpUmwZuord> builder =
            repository.queryBuilder(ReiProgpunktGrpUmwZuord.class);
        builder.and("reiProgpunktGrpId", probe.getReiProgpunktGrpId());
        List<ReiProgpunktGrpUmwZuord> zuord =
            repository.filterPlain(builder.getQuery());
        for (ReiProgpunktGrpUmwZuord entry : zuord) {
            if (entry.getUmwId().equals(probe.getUmwId())) {
                return null;
            }
        }
        Violation violation = new Violation();
        violation.addWarning("umwId", StatusCodes.VALUE_NOT_MATCHING);
        return violation;
    }
}
