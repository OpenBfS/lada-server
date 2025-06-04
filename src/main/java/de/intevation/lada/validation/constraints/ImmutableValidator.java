/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.constraints;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.persistence.Id;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import de.intevation.lada.util.data.Repository;


/**
 * Check if values for given fields either belong to a new instance
 * or are equal to the value of the persistent entity.
 */
public class ImmutableValidator
    implements ConstraintValidator<Immutable, Object> {

    private Class<?> clazz;

    private Map<String, Method> fieldGetters = new HashMap<>();

    private Method idGetter;

    private String message;

    @Override
    public void initialize(Immutable constraintAnnotation) {
        this.clazz = constraintAnnotation.clazz();

        try {
            // Getter methods for fields given in annotation
            for (String field: constraintAnnotation.fields()) {
                this.fieldGetters.put(
                    field,
                    new PropertyDescriptor(field, clazz).getReadMethod());
            }

            // ID field of the annotated class
            for (Field classField: clazz.getDeclaredFields()) {
                if (classField.getAnnotation(Id.class) != null) {
                    this.idGetter = new PropertyDescriptor(
                        classField.getName(), clazz).getReadMethod();
                    break;
                }
            }
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }

        this.message = constraintAnnotation.message();
    }

    @Override
    @Transactional
    public boolean isValid(Object entity, ConstraintValidatorContext ctx) {
        if (entity == null) {
            return true;
        }
        try {
            // New instance: no ID given or no persistent entity with ID exists
            Object id = this.idGetter.invoke(entity);
            if (id == null) {
                return true;
            }
            Object persistent = CDI.current().getBeanContainer()
                .createInstance().select(Repository.class).get().entityManager()
                .find(this.clazz, id);
            if (persistent == null) {
                return true;
            }

            // Persistent entity exists: Compare field values
            boolean isValid = true;
            for (String field: this.fieldGetters.keySet()) {
                Method getter = this.fieldGetters.get(field);
                if (!Objects.equals(
                        getter.invoke(persistent),
                        getter.invoke(entity))
                ) {
                    isValid = false;
                    ctx.disableDefaultConstraintViolation();
                    ctx.buildConstraintViolationWithTemplate(this.message)
                        .addPropertyNode(field)
                        .addConstraintViolation();
                }
            }
            return isValid;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
