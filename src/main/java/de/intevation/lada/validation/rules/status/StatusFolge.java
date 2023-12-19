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

import org.jboss.logging.Logger;

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

    @Inject Logger logger;

    @Inject
    private Repository repository;

    @Override
    public Violation execute(Object object) {
        StatusProt status = (StatusProt) object;

        // Get the previous status
        QueryBuilder<StatusProt> lastFilter =
            repository.queryBuilder(StatusProt.class);

        lastFilter.and("measmId", status.getMeasmId());
        lastFilter.orderBy("id", true);
        List<StatusProt> protos =
            repository.filterPlain(lastFilter.getQuery());
        if (protos.isEmpty()) {
            return null;
        }
        StatusProt last = protos.get(protos.size() - 1);
        QueryBuilder<StatusOrdMp> folgeFilter =
            repository.queryBuilder(StatusOrdMp.class);
        folgeFilter.and("fromId", last.getStatusMpId());
        folgeFilter.and("toId", status.getStatusMpId());
        List<StatusOrdMp> reihenfolge =
            repository.filterPlain(folgeFilter.getQuery());
        if (reihenfolge.isEmpty()) {
            Violation violation = new Violation();
            violation.addError("status", StatusCodes.VALUE_NOT_MATCHING);
            return violation;
        }

        return null;
    }
}
