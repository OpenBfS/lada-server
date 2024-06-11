/* Copyright (C) 2023 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.constraints;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import de.intevation.lada.util.data.Repository;


/**
 * Check if value is a valid primary key for an entity of the given class.
 */
public class IsValidPrimaryKeyListValidator
    implements ConstraintValidator<IsValidPrimaryKeyList, Object> {

    private Class<?> clazz;

    @Override
    public void initialize(IsValidPrimaryKeyList constraintAnnotation) {
        this.clazz = constraintAnnotation.clazz();
    }

    @Override
    @Transactional
    public boolean isValid(Object value, ConstraintValidatorContext ctx) {
        if (value == null) {
            return true;
        }
        if (!(value instanceof List)) {
            return false;
        }
        @SuppressWarnings("unchecked")
        List<Object> valueList = (List<Object>) value;
        // Get instance programmatically because dependency injection is not
        // guaranteed to work in ConstraintValidator implementations
        EntityManager em = CDI.current().getBeanContainer().createInstance()
            .select(Repository.class).get().entityManager();
        AtomicBoolean valid = new AtomicBoolean(true);
        valueList.forEach(key -> {
            boolean found = em.find(clazz, key) != null;
            if (valid.get() && !found) {
                valid.set(false);
            }
        });
        return valid.get();
    }
}
