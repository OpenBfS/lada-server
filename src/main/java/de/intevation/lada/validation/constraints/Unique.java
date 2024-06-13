/* Copyright (C) 2023 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.constraints;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;


/**
 * Check if combination of values for given fields is unique for either all
 * entities of the given class or a subset defined by a given predicate.
 */
@Target({ TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = { UniqueValidator.class })
@Documented
@Repeatable(value = Uniques.class)
public @interface Unique {

    static final String MSG =
        "{de.intevation.lada.validation.constraints.Unique.message}";

    String message() default MSG;

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

    /**
     * The annotated entity class.
     */
    Class<?> clazz();

    /**
     * Names of the fields of the annotated entity class, which are expected
     * to form a unique combination, if compared with all other persistent
     * entities of the same class.
     */
    String[] fields();

    /**
     * Optional names of String-typed fields that can be used to restrict
     * checked entities of the given class to those for which those fields
     * values equal the given predicateValues.
     */
    String[] predicateFields() default { };

    /**
     * The values to be used with predicateFields given as literal SQL.
     *
     * Values must be any valid SQL expression that can be used at
     * the right-hand side of the '=' operator to be compared with
     * actual values of the matching predicate field.
     */
    String[] predicateValues() default { };

    /**
     * If an element is set to true, consider null as predicate value
     * for the respective element of predicateFields instead of
     * respective element of predicateValues.
     */
    boolean[] predicateIsNull() default { };

    /**
     * Name of the property node ConstraintViolation will be associated to.
     * Defaults to the first entry of fields.
     */
    String propertyNodeName() default "";
}
