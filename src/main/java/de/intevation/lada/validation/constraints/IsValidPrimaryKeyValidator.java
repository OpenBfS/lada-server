/* Copyright (C) 2023 by Bundesamt fuer Strahlenschutz
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

import de.intevation.lada.util.data.Repository;


/**
 * Check if value is a valid primary key for an entity of the given class.
 */
public class IsValidPrimaryKeyValidator
    implements ConstraintValidator<IsValidPrimaryKey, Object> {

    private Class<?> clazz;

    @Override
    public void initialize(IsValidPrimaryKey constraintAnnotation) {
        this.clazz = constraintAnnotation.clazz();
    }

    @Override
    @Transactional
    public boolean isValid(Object value, ConstraintValidatorContext ctx) {
        if (value == null
            // Get instance programmatically because dependency injection is not
            // guaranteed to work in ConstraintValidator implementations
            || CDI.current().getBeanContainer().createInstance()
                .select(Repository.class).get()
                .getByIdPlain(clazz, value) != null
        ) {
            return true;
        }
        return false;
    }
}
