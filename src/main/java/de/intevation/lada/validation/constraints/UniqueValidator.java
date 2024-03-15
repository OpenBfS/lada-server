/* Copyright (C) 2023 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.constraints;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.persistence.Id;
import jakarta.persistence.Query;
import jakarta.persistence.Table;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import de.intevation.lada.model.NamingStrategy;
import de.intevation.lada.util.data.Repository;


/**
 * Check if combination of values for given fields is unique for all
 * entities of the given class.
 */
public class UniqueValidator implements ConstraintValidator<Unique, Object> {

    private static final String EXISTS_QUERY_TEMPLATE =
        "SELECT EXISTS(SELECT 1 FROM %s WHERE %s)";

    private String[] fields;
    private String predicateValue;

    private Map<String, Method> fieldGetters = new HashMap<>();
    private Method predicateGetter;

    private String idField;
    private Method idGetter;

    private String tableName;
    private String whereClause;

    private String message;
    private String propertyNodeName;

    @Override
    public void initialize(Unique constraintAnnotation) {
        this.fields = constraintAnnotation.fields();
        Class<?> clazz = constraintAnnotation.clazz();
        this.predicateValue = constraintAnnotation.predicateValue();

        try {
            // Getter methods for fields given in annotation
            for (String field: this.fields) {
                this.fieldGetters.put(
                    field,
                    new PropertyDescriptor(field, clazz).getReadMethod());
            }
            String predicateField = constraintAnnotation.predicateField();
            if (!predicateField.isEmpty()) {
                this.predicateGetter = new PropertyDescriptor(
                    predicateField, clazz).getReadMethod();
            }

            // ID field of the annotated class
            for (Field classField: clazz.getDeclaredFields()) {
                if (classField.getAnnotation(Id.class) != null) {
                    this.idField = classField.getName();
                    break;
                }
            }
            this.idGetter = new PropertyDescriptor(idField, clazz)
                .getReadMethod();

            // Qualified database table name corresponding to annotated class
            this.tableName = clazz.getAnnotation(Table.class).schema()
                + "." + NamingStrategy.camelToSnake(
                    Introspector.getBeanInfo(clazz).getBeanDescriptor()
                    .getName());

            // WHERE clause for EXISTS_QUERY_TEMPLATE restricting query result
            // to entries distinct from current entity but with the same
            // value combination for fields making up the UNIQUE constraint
            // respectively partial UNIQUE constraint, if predicate is given.
            List<String> whereClauseParams = new ArrayList<>();
            for (int i = 0; i < fields.length; i++) {
                whereClauseParams.add(NamingStrategy.camelToSnake(fields[i])
                    + "=:" + fields[i]);
            }
            whereClauseParams.add(
                NamingStrategy.camelToSnake(idField)
                + " IS DISTINCT FROM :" + idField);
            if (!predicateField.isEmpty()) {
                whereClauseParams.add(
                    NamingStrategy.camelToSnake(predicateField)
                    + "='" + this.predicateValue + "'");
            }
            this.whereClause = String.join(" AND ", whereClauseParams);
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }

        this.message = constraintAnnotation.message();
        String nodeName = constraintAnnotation.propertyNodeName();
        this.propertyNodeName = nodeName.isEmpty() ? this.fields[0] : nodeName;
    }

    @Override
    @Transactional
    public boolean isValid(Object entity, ConstraintValidatorContext ctx) {
        if (entity == null) {
            return true;
        }
        try {
            // For partial constraint, only consider entities matching predicate
            if (this.predicateGetter != null && !this.predicateValue.equals(
                    this.predicateGetter.invoke(entity))
            ) {
                return true;
            }

            // Get instance programmatically because dependency injection
            // is not guaranteed to work in ConstraintValidator implementations
            Query exists = CDI.current().getBeanContainer().createInstance()
                .select(Repository.class).get().entityManager()
                .createNativeQuery(
                    String.format(
                        EXISTS_QUERY_TEMPLATE,
                        this.tableName,
                        this.whereClause),
                    Boolean.class);
            exists.setParameter(this.idField, this.idGetter.invoke(entity));
            for (String field: this.fields) {
                exists.setParameter(
                    field, this.fieldGetters.get(field).invoke(entity));
            }
            boolean isValid = !(Boolean) exists.getSingleResult();
            if (!isValid) {
                ctx.disableDefaultConstraintViolation();
                ctx.buildConstraintViolationWithTemplate(this.message)
                    .addPropertyNode(this.propertyNodeName)
                    .addConstraintViolation();
            }
            return isValid;
        } catch (IllegalAccessException | InvocationTargetException e) {
            return false;
        }
    }
}
