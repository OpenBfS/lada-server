/* Copyright (C) 2026 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.constraints;

import de.intevation.lada.model.master.MunicDiv;
import de.intevation.lada.util.data.Repository;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


/**
 * Validates if referenced {@link MunicDiv}
 * belongs to referenced {@link de.intevation.lada.model.master.AdminUnit}.
 */
abstract class MunicDivMatchesAdminUnitValidator<T>
    implements ConstraintValidator<MunicDivMatchesAdminUnit, T> {

    private String message;

    @Override
    public void initialize(MunicDivMatchesAdminUnit constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    protected boolean doValidate(
        String adminUnitId,
        Integer municDivId,
        ConstraintValidatorContext context,
        String propertyNode
    ) {
        if (adminUnitId == null || municDivId == null) {
            return true;
        }
        MunicDiv municDiv = CDI.current().getBeanContainer()
            .createInstance().select(Repository.class).get().entityManager()
            .find(MunicDiv.class, municDivId);
        if (municDiv == null
            || municDiv.getAdminUnitId().equals(adminUnitId)
        ) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(this.message)
            .addPropertyNode(propertyNode)
            .addConstraintViolation();
        return false;
    }
}
