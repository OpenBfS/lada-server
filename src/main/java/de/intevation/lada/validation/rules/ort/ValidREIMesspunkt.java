/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.rules.ort;

import jakarta.inject.Inject;

import de.intevation.lada.model.master.NuclFacil;
import de.intevation.lada.model.master.NuclFacilGrMp;
import de.intevation.lada.model.master.Site;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Violation;
import de.intevation.lada.validation.annotation.ValidationRule;
import de.intevation.lada.validation.rules.Rule;


@ValidationRule("Ort")
public class ValidREIMesspunkt implements Rule {

    private static final Integer SITE_CLASS_REI = 3;

    @Inject
    private Repository repository;

    @Override
    public Violation execute(Object object) {
        Site ort = (Site) object;

        if (ort == null
            || !SITE_CLASS_REI.equals(ort.getSiteClassId())
            || ort.getNuclFacilGrId() == null
            // Leave validation of extId up to other rule
            || ort.getExtId() == null || ort.getExtId().length() < 4
        ) {
            return null;
        }

        final String nuclFacilGrIdKey = "nuclFacilGrId", extIdKey = "extId";
        Violation violation = new Violation();

        //Compare first 4 characters of Ort ID to stored KTAs
        String ktaId = ort.getExtId().substring(0, 4);
        NuclFacil kta = repository.entityManager().find(NuclFacil.class, ktaId);
        if (kta == null) {
            violation.addWarning(extIdKey, StatusCodes.ORT_ANLAGE_MISSING);
            return violation;
        }

        QueryBuilder<NuclFacilGrMp> builder = repository
            .queryBuilder(NuclFacilGrMp.class)
            .and(nuclFacilGrIdKey, ort.getNuclFacilGrId());
        for (NuclFacilGrMp ktaGrMp : repository.filter(builder.getQuery())) {
            if (!ktaId.equals(ktaGrMp.getNuclFacilExtId())) {
                violation.addWarning(
                    "nuclFacilGrId", StatusCodes.VALUE_NOT_MATCHING);
            } else {
                break;
            }
        }
        if (violation.hasWarnings()) {
            return violation;
        }
        return null;
    }
}
