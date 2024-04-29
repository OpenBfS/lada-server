/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.constraints;

import java.util.List;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.locationtech.jts.geom.Point;

import de.intevation.lada.model.master.AdminBorderView;
import de.intevation.lada.model.master.Site;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;


/**
 * Validation rule for Site.
 * Validates if the coordinates are in the specified administrative unit.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class CoordinatesInAdminBorderValidator
    implements ConstraintValidator<CoordinatesInAdminBorder, Site> {

    private String message;

    @Override
    public void initialize(CoordinatesInAdminBorder constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Transactional
    @Override
    public boolean isValid(Site ort, ConstraintValidatorContext ctx) {
        if (ort == null) {
            return true;
        }

        String gemId = ort.getAdminUnitId();
        Point p = ort.getGeom();
        if (gemId != null && p != null) {
            Repository repository = CDI.current().getBeanContainer()
                .createInstance().select(Repository.class).get();
            QueryBuilder<AdminBorderView> vg = repository
                .queryBuilder(AdminBorderView.class)
                .and("municId", gemId);
            List<AdminBorderView> vgs = repository.filter(vg.getQuery());
            if (vgs.isEmpty()) {
                return true;
            }

            AdminBorderView singlevg = vgs.get(0);
            if (singlevg.getShape().contains(p)) {
                return true;
            }
            if (ort.getIsFuzzy()
                && singlevg.getShape().distance(p) * (3.1415926 / 180) * 6378137
                < 1000
            ) {
                return true;
            }
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate(message)
                .addPropertyNode("coordXExt")
                .addConstraintViolation();
            ctx.buildConstraintViolationWithTemplate(message)
                .addPropertyNode("coordYExt")
                .addConstraintViolation();
            return false;
        }
        return true;
    }
}
