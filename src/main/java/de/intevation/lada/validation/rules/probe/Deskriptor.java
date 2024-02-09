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

import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.master.EnvDescrip;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Violation;
import de.intevation.lada.validation.annotation.ValidationRule;
import de.intevation.lada.validation.rules.Rule;

/**
 * Validation rule for probe.
 * Validates if the probe has a valid deskriptor string.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@ValidationRule("Sample")
public class Deskriptor implements Rule {

    @Inject
    private Repository repository;

    @Override
    public Violation execute(Object object) {
        Sample probe = (Sample) object;
        if (probe.getEnvDescripDisplay() == null) {
            Violation violation = new Violation();
            violation.addWarning("envDescripDisplay", StatusCodes.VALUE_MISSING);
            return violation;
        }
        String[] mediaDesk = probe.getEnvDescripDisplay().split(" ");
        if (mediaDesk.length <= 1) {
            Violation violation = new Violation();
            violation.addWarning("envDescripDisplay", StatusCodes.VALUE_MISSING);
            return violation;
        }
        if (mediaDesk.length >= 1
            && probe.getRegulationId() != null
            && probe.getRegulationId() == 4
            && (mediaDesk[1].equals("00")
            || mediaDesk[2].equals("00"))
        ) {
            Violation violation = new Violation();
            violation.addWarning("envDescripDisplay", StatusCodes.VAL_S1_NOTSET);
            return violation;
        }

        boolean zebs = false;
        Integer parent = null;
        Integer hdParent = null;
        Integer ndParent = null;
        if ("01".equals(mediaDesk[1])) {
            zebs = true;
        }
        for (int i = 1; i < mediaDesk.length; i++) {
            if ("00".equals(mediaDesk[i])) {
                continue;
            }
            if (zebs && i < 5) {
                parent = hdParent;
            } else if (!zebs && i < 3) {
                parent = hdParent;
            } else {
                parent = ndParent;
            }
            QueryBuilder<EnvDescrip> builder =
                repository.queryBuilder(EnvDescrip.class);
            if (parent != null) {
                builder.and("predId", parent);
            }
            builder.and("levVal", mediaDesk[i])
                .and("lev", i - 1);
            List<EnvDescrip> data = repository.filterPlain(builder.getQuery());
            if (data.isEmpty()) {
                Violation violation = new Violation();
                violation.addWarning("envDescripDisplay", StatusCodes.VAL_DESK);
                return violation;
            }
            hdParent = data.get(0).getId();
            if (i == 2) {
                ndParent = data.get(0).getId();
            }
        }
        return null;
    }
}
