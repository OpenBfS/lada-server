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
import jakarta.validation.groups.Default;

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
     * @param groups Restrict validation to given groups. Defaults to
     * Default, Warnings and Notifications and only a subset of these
     * should be given.
     * @return The validated objects
     */
    public <T extends BaseModel> List<T> validate(
        List<T> objects, Class... groups
    ) {
        for (T object: objects) {
            validate(object, groups);
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
     * @param groups Restrict validation to given groups. Defaults to
     * Default, Warnings and Notifications and only a subset of these
     * should be given.
     * @return The validated object
     */
    public <T extends BaseModel> T validate(T object, Class... groups) {
        final Class[] defaultGroups =  {
            Default.class, Warnings.class, Notifications.class };
        for (Class group: groups.length == 0 ? defaultGroups : groups) {
            Set<ConstraintViolation<T>> beanViolations =
                beanValidator.validate(object, group);
            if (group.equals(Default.class)) {
                for (ConstraintViolation<T> violation: beanViolations) {
                    object.addError(
                        violation.getPropertyPath().toString(),
                        violation.getMessage());
                }
            } else if (group.equals(Warnings.class)) {
                for (ConstraintViolation<T> violation: beanViolations) {
                    object.addWarning(
                        violation.getPropertyPath().toString(),
                        violation.getMessage());
                }
            } else if (group.equals(Notifications.class)) {
                for (ConstraintViolation<T> violation: beanViolations) {
                    object.addNotification(
                        violation.getPropertyPath().toString(),
                        violation.getMessage());
                }
            }
        }

        return object;
    }
}
