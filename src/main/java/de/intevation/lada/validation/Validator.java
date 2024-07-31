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

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;

import org.hibernate.validator.HibernateValidator;

import de.intevation.lada.model.BaseModel;
import de.intevation.lada.validation.groups.Notifications;
import de.intevation.lada.validation.groups.Warnings;


/**
 * Object validator.
 *
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class Validator {

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
     * Validates objects in given list.
     *
     * @param <T> Type of objects to be validated
     * @param objects The objects to be validated
     * @return The validated objects
     */
    public <T extends BaseModel> List<T> validate(List<T> objects) {
        for (T object: objects) {
            validate(object);
        }
        return objects;
    }

    /**
     * Validate object of type T with Bean Validation constraints.
     *
     * Validation messages are attached to the passed object.
     *
     * @param <T> Type of object to be validated
     * @param object The object to be validated
     * @return The validated object
     */
    public <T extends BaseModel> T validate(T object) {
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

        return object;
    }
}
