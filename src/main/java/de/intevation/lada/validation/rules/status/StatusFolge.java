/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.rules.status;

import java.util.List;

import jakarta.inject.Inject;

import de.intevation.lada.model.lada.StatusProt;
import de.intevation.lada.model.master.StatusOrdMp;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Violation;
import de.intevation.lada.validation.annotation.ValidationRule;
import de.intevation.lada.validation.rules.Rule;

/**
 * Validation rule for status.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@ValidationRule("Status")
public class StatusFolge implements Rule {

    @Inject
    private Repository repository;

    @Override
    public Violation execute(Object object) {
        StatusProt status = (StatusProt) object;

        // Get the previous status
        QueryBuilder<StatusProt> lastFilter = repository
            .queryBuilder(StatusProt.class)
            .and("measmId", status.getMeasmId())
            .orderBy("id", false);
        List<StatusProt> protos =
            repository.filter(lastFilter.getQuery(), 1, 1);
        if (protos.isEmpty()) {
            return null;
        }
        StatusProt last = protos.get(0);
        QueryBuilder<StatusOrdMp> folgeFilter = repository
            .queryBuilder(StatusOrdMp.class)
            .and("fromId", last.getStatusMpId())
            .and("toId", status.getStatusMpId());
        List<StatusOrdMp> reihenfolge =
            repository.filter(folgeFilter.getQuery());
        if (reihenfolge.isEmpty()) {
            Violation violation = new Violation();
            violation.addError("status", StatusCodes.VALUE_NOT_MATCHING);
            return violation;
        }

        return null;
    }
}
