/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.constraints;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import de.intevation.lada.model.master.AdminBorderView;
import de.intevation.lada.model.master.AdminBorderView_;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;


/**
 * Validates if value references a border of administrative unit.
 */
public class IsAdminBorderKeyValidator
    implements ConstraintValidator<IsAdminBorderKey, String> {

    @Transactional
    @Override
    public boolean isValid(String val, ConstraintValidatorContext ctx) {
        if (val == null) {
            return true;
        }
        Repository repository = CDI.current().getBeanContainer()
            .createInstance().select(Repository.class).get();
        QueryBuilder<AdminBorderView> vg = repository
            .queryBuilder(AdminBorderView.class)
            .and(AdminBorderView_.municId, val);
        return !repository.filter(vg.getQuery()).isEmpty();
    }
}
