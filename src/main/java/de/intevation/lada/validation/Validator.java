/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation;

import javax.enterprise.inject.Instance;

import de.intevation.lada.validation.rules.Rule;


/**
 * Interface for object validators.
 *
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public interface Validator {
    Violation validate(Object object);

    static Violation validate(Object object, Instance<Rule> rules) {
        Violation violations = new Violation();
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
