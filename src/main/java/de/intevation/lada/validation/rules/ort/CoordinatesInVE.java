/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.rules.ort;

import java.util.List;

import jakarta.inject.Inject;

import org.locationtech.jts.geom.Point;

import de.intevation.lada.model.master.AdminBorderView;
import de.intevation.lada.model.master.Site;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Violation;
import de.intevation.lada.validation.annotation.ValidationRule;
import de.intevation.lada.validation.rules.Rule;

/**
 * Validation rule for ort.
 * Validates if the coordinates are in the specified "Verwaltungseinheit".
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@ValidationRule("Ort")
public class CoordinatesInVE implements Rule {

    @Inject
    private Repository repository;

    @Override
    public Violation execute(Object object) {
        Site ort = (Site) object;
        String gemId = ort.getAdminUnitId();
        Point p = ort.getGeom();

        if (gemId != null && p != null) {
            Violation violation = new Violation();

            final String municIdKey = "municId";
            QueryBuilder<AdminBorderView> vg = repository
                .queryBuilder(AdminBorderView.class)
                .and(municIdKey, gemId);
            List<AdminBorderView> vgs = repository.filterPlain(vg.getQuery());
            if (vgs.isEmpty()) {
                violation.addWarning(
                    municIdKey, StatusCodes.GEO_COORD_UNCHECKED);
                return violation;
            }

            AdminBorderView singlevg = vgs.get(0);
            if (singlevg.getShape().contains(p)) {
                return null;
            }
            if (ort.getIsFuzzy()
                && singlevg.getShape().distance(p) * (3.1415926 / 180) * 6378137
                < 1000
            ) {
                return null;
            }
            violation.addWarning("coordXExt", StatusCodes.GEO_POINT_OUTSIDE);
            violation.addWarning("coordYExt", StatusCodes.GEO_POINT_OUTSIDE);
            return violation;
        }
        return null;
    }
}
