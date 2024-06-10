/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.rules.ort;

import jakarta.inject.Inject;

import de.intevation.lada.model.master.NuclFacilGrMp;
import de.intevation.lada.model.master.NuclFacilGrMp_;
import de.intevation.lada.model.master.Site;
import de.intevation.lada.model.master.Site_;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Violation;
import de.intevation.lada.validation.annotation.ValidationRule;
import de.intevation.lada.validation.rules.Rule;


@ValidationRule("Ort")
public class ValidREIMesspunkt implements Rule {

    private static final int NUCL_FACIL_EXT_ID_LENGTH = 4;

    @Inject
    private Repository repository;

    @Override
    public Violation execute(Object object) {
        Site ort = (Site) object;

        if (ort == null
            || !Site.SiteClassId.REI.equals(ort.getSiteClassId())
            || ort.getNuclFacilGrId() == null
            // Leave validation of extId up to other rule
            || ort.getExtId() == null
            || ort.getExtId().length() < NUCL_FACIL_EXT_ID_LENGTH
        ) {
            return null;
        }

        // First 4 characters of extId should match a nuclear facility
        // in given group
        QueryBuilder<NuclFacilGrMp> builder = repository
            .queryBuilder(NuclFacilGrMp.class)
            .and(NuclFacilGrMp_.NUCL_FACIL_EXT_ID,
                ort.getExtId().substring(0, NUCL_FACIL_EXT_ID_LENGTH))
            .and(NuclFacilGrMp_.NUCL_FACIL_GR_ID,
                ort.getNuclFacilGrId());
        if (repository.filter(builder.getQuery()).isEmpty()) {
            Violation violation = new Violation();
            violation.addWarning(
                Site_.EXT_ID,
                StatusCodes.VALUE_NOT_MATCHING);
            violation.addWarning(
                Site_.NUCL_FACIL_GR_ID,
                StatusCodes.VALUE_NOT_MATCHING);
            return violation;
        }
        return null;
    }
}
