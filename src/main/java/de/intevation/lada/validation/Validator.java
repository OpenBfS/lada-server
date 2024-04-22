/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;

import org.hibernate.validator.HibernateValidator;

import de.intevation.lada.model.BaseModel;
import de.intevation.lada.validation.rules.Rule;
import de.intevation.lada.validation.groups.Notifications;
import de.intevation.lada.validation.groups.Warnings;


/**
 * Abstract base class for object validators.
 *
 * @param <T> Type of objects to be validated by implementing validator
 *
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public abstract class Validator<T extends BaseModel> {

    private jakarta.validation.Validator beanValidator;

    @Inject
    Validator(HttpServletRequest request) {
        Locale locale = request != null
            ? request.getLocale()
            : Locale.getDefault();
        this.setLocale(locale);
    }

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
     * @return The validated object
     */
    public abstract T validate(Object object);

    /**
     * Validates objects in given list.
     *
     * @param objects The objects to be validated
     * @return The validated objects
     */
    public List<T> validate(List<T> objects) {
        for (Object object: objects) {
            validate(object);
        }
        return objects;
    }

    /**
     * Validate objects of type T with Bean Validation constraints and
     * given set of rules.
     *
     * Validation messages are attached to the passed object.
     *
     * @param object The object to be validated
     * @param rules The rules to apply
     * @return The validated object
     */
    protected T validate(T object, Instance<Rule> rules) {
        // Bean Validation
        Set<ConstraintViolation<T>> beanViolations =
            beanValidator.validate(object);
        if (!beanViolations.isEmpty()) {
            for (ConstraintViolation<T> violation: beanViolations) {
                object.addError(
                    violation.getPropertyPath().toString(),
                    violation.getMessage());
            }
            // Do not expect other rules to work with invalid Beans
            return object;
        }

        Set<ConstraintViolation<T>> beanViolationWarnings =
            beanValidator.validate(object, Warnings.class);
        for (ConstraintViolation<T> violation: beanViolationWarnings) {
            object.addWarning(
                violation.getPropertyPath().toString(),
                violation.getMessage());
        }

        Set<ConstraintViolation<T>> beanViolationNotifications =
            beanValidator.validate(object, Notifications.class);
        for (ConstraintViolation<T> violation: beanViolationNotifications) {
            object.addNotification(
                violation.getPropertyPath().toString(),
                violation.getMessage());
        }

        for (Rule rule : rules) {
            Violation result = rule.execute(object);
            if (result != null) {
                if (result.hasWarnings()) {
                    object.addWarnings(result.getWarnings());
                }
                if (result.hasErrors()) {
                    object.addErrors(result.getErrors());
                }
                if (result.hasNotifications()) {
                    object.addNotifications(result.getNotifications());
                }
            }
        }
        return object;
    }
}
