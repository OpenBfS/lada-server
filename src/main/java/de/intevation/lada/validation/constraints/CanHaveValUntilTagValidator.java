/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.constraints;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import de.intevation.lada.model.master.Tag;
import de.intevation.lada.model.master.Tag_;


/**
 * Checks if the validated entity can have validity set.
 */
public class CanHaveValUntilTagValidator
    implements ConstraintValidator<CanHaveValUntil, Tag> {

    private String message;

    @Override
    public void initialize(CanHaveValUntil constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Tag tag, ConstraintValidatorContext ctx) {
        if (tag == null
            || tag.getValUntil() == null
            // Only generated and meas_facil tags can have validity:
            || tag.getMeasFacilId() != null
            || tag.getIsAutoTag()
        ) {
            return true;
        }

        ctx.disableDefaultConstraintViolation();
        ctx.buildConstraintViolationWithTemplate(this.message)
            .addPropertyNode(Tag_.VAL_UNTIL)
            .addConstraintViolation();
        return false;
    }
}
