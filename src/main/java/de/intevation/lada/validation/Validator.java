/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation;

import jakarta.enterprise.inject.Instance;

import de.intevation.lada.model.BaseModel;
import de.intevation.lada.validation.rules.Rule;


/**
 * Interface for object validators.
 *
 * @param <T> Type of objects to be validated by implementing validator
 *
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public interface Validator<T extends BaseModel> {

    /**
     * Validates given object.
     *
     * Implementations should cast object to T and delegate to
     * validate(T, Instance<Rule>)
     *
     * @param object The object to be validated
     */
    void validate(Object object);

    /**
     * Default method for validating objects of type T with given set of rules.
     *
     * @param object The object to be validated
     * @param rules The rules to apply
     */
    default void validate(T object, Instance<Rule> rules) {
        if (object != null) {
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
        }
    }
}
