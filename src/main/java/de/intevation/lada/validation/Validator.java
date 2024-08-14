/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation;

import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;

import org.hibernate.validator.HibernateValidator;

import de.intevation.lada.context.ThreadLocale;
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

    /**
     * Create Validator instance interpolating messages with locale
     * provided by ThreadLocale.
     *
     * Attention should be paid to create instances after ThreadLocale
     * has been set if a locale other than default should be used.
     * This might not be the case e.g. for dependency injection.
     */
    public Validator() {
        this.beanValidator = Validation.byProvider(HibernateValidator.class)
            .configure()
            .defaultLocale(ThreadLocale.get())
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
