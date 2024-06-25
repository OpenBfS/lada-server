/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.constraints;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import de.intevation.lada.model.master.Site;
import de.intevation.lada.model.master.Site_;


/**
 * Check if site object has either coordinates, adminUnitId or stateId.
 */
public class HasCoordsOrAdminUnitOrStateValidator
    implements ConstraintValidator<HasCoordsOrAdminUnitOrState, Site> {

    @Override
    public boolean isValid(Site value, ConstraintValidatorContext ctx) {
        if (value == null
            || value.getSpatRefSysId() != null
            && value.getCoordXExt() != null
            && value.getCoordYExt() != null
            || value.getAdminUnitId() != null
            || value.getStateId() != null
        ) {
            return true;
        }

        ctx.disableDefaultConstraintViolation();
        ctx.buildConstraintViolationWithTemplate(
            HasCoordsOrAdminUnitOrState.MSG)
            .addPropertyNode("coordinates")
            .addConstraintViolation();
        ctx.buildConstraintViolationWithTemplate(
            HasCoordsOrAdminUnitOrState.MSG)
            .addPropertyNode(Site_.ADMIN_UNIT_ID)
            .addConstraintViolation();
        ctx.buildConstraintViolationWithTemplate(
            HasCoordsOrAdminUnitOrState.MSG)
            .addPropertyNode(Site_.STATE_ID)
            .addConstraintViolation();
        return false;
    }
}
