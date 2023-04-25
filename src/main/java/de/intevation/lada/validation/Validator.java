/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation;

import jakarta.enterprise.inject.Instance;

import de.intevation.lada.validation.rules.Rule;


/**
 * Interface for object validators.
 *
 * @param <T> Type of objects to be validated by implementing validator
 *
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public interface Validator<T> {

    /**
     * Validates given object.
     *
     * Implementations should cast object to T and delegate to
     * validate(T, Instance<Rule>)
     *
     * @param object The object to be validated
     * @return A Violation object
     */
    Violation validate(Object object);

    /**
     * Default method for validating objects of type T with given set of rules.
     *
     * @param object The object to be validated
     * @param rules The rules to apply
     * @return A Violation object
     */
    default Violation validate(T object, Instance<Rule> rules) {
        Violation violations = new Violation();
        if (object != null) {
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
        }
        return violations;
    }
}
