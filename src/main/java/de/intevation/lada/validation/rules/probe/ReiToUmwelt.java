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

import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.master.ReiAgGrEnvMediumMp;
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
@ValidationRule("Sample")
public class ReiToUmwelt implements Rule {

    @Inject
    private Repository repository;

    @Override
    public Violation execute(Object object) {
        Sample probe = (Sample) object;
        if (probe.getRegulationId() != null
            && probe.getRegulationId() != 3
            && probe.getRegulationId() != 4
        ) {
            return null;
        }
        if (probe.getEnvMediumId() == null) {
            return null;
        }
        if (probe.getReiAgGrId() == null) {
            return null;
        }
        QueryBuilder<ReiAgGrEnvMediumMp> builder =
            repository.queryBuilder(ReiAgGrEnvMediumMp.class);
        builder.and("reiAgGrId", probe.getReiAgGrId());
        List<ReiAgGrEnvMediumMp> zuord =
            repository.filterPlain(builder.getQuery());
        for (ReiAgGrEnvMediumMp entry : zuord) {
            if (entry.getEnvMediumId().equals(probe.getEnvMediumId())) {
                return null;
            }
        }
        Violation violation = new Violation();
        violation.addWarning("envMediumId", StatusCodes.VAL_UWB_NOT_MATCHING_REI);
        return violation;
    }
}
