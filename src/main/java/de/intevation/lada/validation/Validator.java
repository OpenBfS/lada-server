/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation;

import java.util.Locale;
import java.util.Set;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;

import org.hibernate.validator.HibernateValidator;

import de.intevation.lada.validation.rules.Rule;


/**
 * Interface for object validators.
 *
 * @param <T> Type of objects to be validated by implementing validator
 *
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public abstract class Validator<T> {

    @Inject
    private jakarta.validation.Validator beanValidator;

    /**
     * @param locale Set locale for message localization.
     */
    public void setLocale(Locale locale) {
        this.beanValidator = Validation.byProvider(HibernateValidator.class)
            .configure()
            .defaultLocale(locale)
            .buildValidatorFactory()
            .getValidator();
    }

    /**
     * Validates given object.
     *
     * Implementations should cast object to T and delegate to
     * validate(T, Instance<Rule>)
     *
     * @param object The object to be validated
     * @return A Violation object
     */
    public abstract Violation validate(Object object);

    /**
     * Validate objects of type T with given set of rules.
     *
     * @param object The object to be validated
     * @param rules The rules to apply
     * @return A Violation object
     */
    protected Violation validate(T object, Instance<Rule> rules) {
        Violation violations = new Violation();

        // Bean Validation
        Set<ConstraintViolation<T>> beanViolations =
            beanValidator.validate(object);
        if (!beanViolations.isEmpty()) {
            // Do not expect other rules to work with invalid Beans
            for (ConstraintViolation<T> violation: beanViolations) {
                violations.addError(
                    violation.getPropertyPath().toString(),
                    violation.getMessage());
            }
            return violations;
        }

        for (Rule rule : rules) {
            Violation result = rule.execute(object);
            if (result != null) {
                if (result.hasWarnings()) {
                    violations.addWarnings(result.getWarnings());
                }
                if (result.hasErrors()) {
                    violations.addErrors(result.getErrors());
                }
                if (result.hasNotifications()) {
                    violations.addNotifications(result.getNotifications());
                }
            }
        }
        return violations;
    }
}
