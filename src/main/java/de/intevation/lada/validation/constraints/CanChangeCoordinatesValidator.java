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

import de.intevation.lada.model.master.Site;
import de.intevation.lada.util.data.Repository;


/**
 * Check if coordinate fields are changed if they should not.
 */
public class CanChangeCoordinatesValidator
    implements ConstraintValidator<CanChangeCoordinates, Site> {

    @Override
    @Transactional
    public boolean isValid(Site value, ConstraintValidatorContext ctx) {
        if (value == null || value.getId() == null) {
            return true;
        }
        // Get instance programmatically because dependency injection is not
        // guaranteed to work in ConstraintValidator implementations
        Site dbSite = CDI.current().getBeanContainer().createInstance()
            .select(Repository.class).get().entityManager()
            .find(Site.class, value.getId());

        if (dbSite == null || dbSite.getPlausibleReferenceCount() == 0) {
            return true;
        }

        boolean isValid = true;
        if (!dbSite.getCoordXExt().equals(value.getCoordXExt())) {
            isValid = false;
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate(CanChangeCoordinates.MSG)
                .addPropertyNode("coordXExt")
                .addConstraintViolation();
        }
        if (!dbSite.getCoordYExt().equals(value.getCoordYExt())) {
            isValid = false;
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate(CanChangeCoordinates.MSG)
                .addPropertyNode("coordYExt")
                .addConstraintViolation();
        }
        return isValid;
    }
}
